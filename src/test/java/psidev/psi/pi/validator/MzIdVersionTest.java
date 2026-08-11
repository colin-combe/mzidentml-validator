package psidev.psi.pi.validator;

import org.junit.After;
import org.junit.Test;
import psidev.psi.pi.validator.MzIdentMLValidator.MzIdVersion;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the mapping from the version attribute of a .mzid file to {@link MzIdVersion},
 * and the version comparison used to decide which checks apply to a file.
 */
public class MzIdVersionTest {

    /**
     * Resets the global version state, which is a public static field shared by all validators.
     */
    @After
    public void tearDown() {
        MzIdentMLValidator.currentFileVersion = null;
    }

    /**
     * Invokes the private instance method mapping a version string to a {@link MzIdVersion}.
     *
     * @param version the value of the version attribute of the MzIdentML element
     * @return the matching MzIdVersion, or null if the version is not supported
     * @throws Exception if the method cannot be invoked
     */
    private static MzIdVersion parse(String version) throws Exception {
        Method method = MzIdentMLValidator.class.getDeclaredMethod("getMzIdentMLVersion", String.class);
        method.setAccessible(true);
        return (MzIdVersion) method.invoke(null, version);
    }

    @Test
    public void recognisesEverySupportedVersion() throws Exception {
        assertEquals(MzIdVersion._1_1, parse("1.1"));
        assertEquals(MzIdVersion._1_1, parse("1.1.0"));
        // 1.1.1 shares the 1.1 rule files; validation.properties aliases it to the 1.1.0 ones.
        assertEquals(MzIdVersion._1_1, parse("1.1.1"));
        assertEquals(MzIdVersion._1_2, parse("1.2"));
        assertEquals(MzIdVersion._1_2, parse("1.2.0"));
        assertEquals(MzIdVersion._1_3, parse("1.3"));
        assertEquals(MzIdVersion._1_3, parse("1.3.0"));
    }

    @Test
    public void returnsNullForAnUnsupportedVersion() throws Exception {
        assertNull(parse("1.0.0"));
        assertNull(parse("2.0.0"));
    }

    @Test
    public void versionsAreDeclaredInAscendingOrder() {
        assertTrue(MzIdVersion._1_1.ordinal() < MzIdVersion._1_2.ordinal());
        assertTrue(MzIdVersion._1_2.ordinal() < MzIdVersion._1_3.ordinal());
    }

    @Test
    public void newerFilesInheritTheChecksOfOlderVersions() {
        MzIdentMLValidator.currentFileVersion = MzIdVersion._1_3;
        assertTrue(MzIdentMLValidator.isVersionAtLeast(MzIdVersion._1_2));

        MzIdentMLValidator.currentFileVersion = MzIdVersion._1_2;
        assertTrue(MzIdentMLValidator.isVersionAtLeast(MzIdVersion._1_2));

        MzIdentMLValidator.currentFileVersion = MzIdVersion._1_1;
        assertFalse(MzIdentMLValidator.isVersionAtLeast(MzIdVersion._1_2));
    }

    /**
     * An unsupported version leaves currentFileVersion null, so the comparison has to tolerate it.
     */
    @Test
    public void toleratesAnUnknownVersion() {
        MzIdentMLValidator.currentFileVersion = null;
        assertFalse(MzIdentMLValidator.isVersionAtLeast(MzIdVersion._1_2));
    }
}
