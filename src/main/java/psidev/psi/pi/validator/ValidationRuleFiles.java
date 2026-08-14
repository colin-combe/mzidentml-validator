package psidev.psi.pi.validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import psidev.psi.pi.validator.MzIdentMLValidator.MzIdVersion;

/**
 * Resolves the configuration files a validation run needs (cv-mapping and object rule files, rule
 * filters, ontology configurations) from validation.properties.<br>
 * Deliberately free of any GUI dependency, so that command line runs select their rules from the
 * version of the file being validated in exactly the way the GUI does.
 *
 * @author Gerhard Mayer, MPC, Ruhr-University of Bochum
 */
public final class ValidationRuleFiles {

    private static final Logger LOGGER = LogManager.getLogger(ValidationRuleFiles.class);

    /**
     * The kind of validation a rule file belongs to. The infix matches the keys of
     * validation.properties, e.g. mapping.rule.file.<b>semantic</b>.validation.1.3.0.
     */
    public enum ValidationKind {
        SEMANTIC("semantic"),
        MIAPE("miape");

        private final String propertyInfix;

        ValidationKind(String propertyInfix) {
            this.propertyInfix = propertyInfix;
        }

        public String getPropertyInfix() {
            return this.propertyInfix;
        }
    }

    /**
     * Constants.
     */
    public static final String STR_MAPPING = "mapping";
    public static final String STR_OBJECT  = "object";

    /** Configuration files found in this folder override the ones bundled with the application. */
    public static final String STR_RESOURCE_FOLDER =
        System.getProperty("user.dir") + System.getProperty("file.separator") + "resources" + System.getProperty("file.separator");

    private static final String STR_VALIDATION_PROPERTIES = "validation.properties";

    /** Rule file version used when the .mzid declares a version the validator does not know. */
    private static final String STR_LATEST_VERSION = "1.3.0";

    /**
     * Constructor: not to be instantiated.
     */
    private ValidationRuleFiles() {
    }

    /**
     * Reads a property from the .properties file.
     * @param propertyName the property name
     * @return the property value
     */
    public static String getProperty(String propertyName) {
        PropertyFile propFile = new PropertyFile();
        Properties properties = propFile.loadProperties(ValidationRuleFiles.STR_RESOURCE_FOLDER + ValidationRuleFiles.STR_VALIDATION_PROPERTIES);

        return properties.getProperty(propertyName);
    }

    /**
     * Gets the suffix identifying a .mzid version in the keys of validation.properties.
     *
     * @param mzIdVersion the {@link MzIdVersion} of the file to validate
     * @return the property key suffix, or null if the version is not supported
     */
    private static String getPropertyKeySuffix(MzIdVersion mzIdVersion) {
        if (mzIdVersion == null) {
            return null;
        }
        switch (mzIdVersion) {
            case _1_1:
                return "1.1.0";
            case _1_2:
                return "1.2.0";
            case _1_3:
                return "1.3.0";
            default:
                return null;
        }
    }

    /**
     * Gets the cv-mapping or object rule file matching the version of the file being validated.
     *
     * @param mzIdVersion the {@link MzIdVersion} of the file to validate: 1.1, 1.2 or 1.3.
     * @param ruleKind {@link #STR_MAPPING} or {@link #STR_OBJECT}
     * @param validationKind semantic or MIAPE validation
     * @return InputStream for the rule file
     * @throws IOException in case of problems reading the rule file
     */
    public static InputStream getRuleFileInputStream(MzIdVersion mzIdVersion, String ruleKind, ValidationKind validationKind) throws IOException {
        String versionSuffix = ValidationRuleFiles.getPropertyKeySuffix(mzIdVersion);
        if (versionSuffix == null) {
            // Fall back to the newest rule files, keeping the MIAPE/semantic choice the caller made.
            ValidationRuleFiles.LOGGER.error("Unsupported .mzid version: " + mzIdVersion
                + ", falling back to the " + ValidationRuleFiles.STR_LATEST_VERSION + " rule files.");
            versionSuffix = ValidationRuleFiles.STR_LATEST_VERSION;
        }
        else {
            ValidationRuleFiles.LOGGER.debug(".mzid version: " + versionSuffix);
        }

        final String propertyName = ruleKind + ".rule.file." + validationKind.getPropertyInfix() + ".validation." + versionSuffix;

        return ValidationRuleFiles.openConfigFile(ValidationRuleFiles.getProperty(propertyName), ruleKind + " rule file");
    }

    /**
     * Gets the rule filter file for the given kind of validation.
     *
     * @param validationKind semantic or MIAPE validation
     * @return InputStream for the rule filter file
     * @throws IOException in case of problems reading the rule filter file
     */
    public static InputStream getRuleFilterInputStream(ValidationKind validationKind) throws IOException {
        final String propertyName = (validationKind == ValidationKind.MIAPE) ? "miape.filter.rule.file" : "semantic.filter.rule.file";

        return ValidationRuleFiles.openConfigFile(ValidationRuleFiles.getProperty(propertyName), "rule filter");
    }

    /**
     * Gets the ontologies configuration file.
     *
     * @param ontologyPropertyName can take the values: ols.ontologies.file or local.ontologies.file
     * @return InputStream for the ontologies file
     * @throws IOException in case of problems reading the ontologies file
     */
    public static InputStream getOntologiesInputStream(String ontologyPropertyName) throws IOException {
        return ValidationRuleFiles.openConfigFile(ValidationRuleFiles.getProperty(ontologyPropertyName), "ontologies config");
    }

    /**
     * Opens a configuration file: a remote URL if the property holds one, else the copy in the
     * resources folder of the launch directory, else the copy bundled with the application.
     *
     * @param fileName the file name/URL read from validation.properties
     * @param description what the file is, for logging
     * @return InputStream for the configuration file
     * @throws IOException in case of problems reading the configuration file
     */
    private static InputStream openConfigFile(String fileName, String description) throws IOException {
        if (fileName == null) {
            throw new IOException("No " + description + " configured.");
        }

        try {
            return new URL(fileName).openStream();
        }
        catch (IOException exc) {
            // not a URL (or not reachable): fall back to a local copy
        }

        final File file = new File(ValidationRuleFiles.STR_RESOURCE_FOLDER + fileName);
        if (file.exists()) {
            return Files.newInputStream(file.toPath());
        }

        ValidationRuleFiles.LOGGER.debug(description + " does not exist: " + file.getPath() + ", using the bundled " + fileName);
        final InputStream bundled = ValidationRuleFiles.getResourceAsStream(fileName);
        if (bundled == null) {
            throw new IOException("Could not find the " + description + ": " + fileName);
        }

        return bundled;
    }

    /**
     * Gets a resource bundled with the application.
     * @param resourceName the name of the resource
     * @return InputStream for the resource, or null if there is no such resource
     */
    private static InputStream getResourceAsStream(String resourceName) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            final InputStream is = contextClassLoader.getResourceAsStream(resourceName);
            if (is != null) {
                return is;
            }
        }

        return ValidationRuleFiles.class.getClassLoader().getResourceAsStream(resourceName);
    }
}
