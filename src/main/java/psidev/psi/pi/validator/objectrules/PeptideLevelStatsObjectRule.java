package psidev.psi.pi.validator.objectrules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.Context;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorMessage;
import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;

/**
 * Check if triplet of terms is present under SpectrumIdentificationItem in case of peptide-level scoring.
 * 
 * @author Gerhard
 * 
 */
public class PeptideLevelStatsObjectRule extends AObjectRule<SpectrumIdentificationItem> {

    /**
     * Constants.
     */
    private static final Context SII_CONTEXT = new Context(MzIdentMLElement.SpectrumIdentificationItem.getXpath());
    private final String tripletMsg = " triplet of terms MS:1002520 (peptide group ID), MS:1002500 (peptide passes threshold) and a child of MS:1002358 (search engine specific score for distinct peptides) ";
    /** mzIdentML 1.3.0: peptide-level scoring of a cross-linked peptide pair uses the pair-level terms. */
    private final String tripletMsgXL = " triplet of terms MS:1002520 (peptide group ID), MS:1003339 (peptide-pair passes threshold) and a child of MS:1002664 (interaction score derived from crosslinking), or their non cross-linking equivalents MS:1002500 and a child of MS:1002358, ";
    private static final String XL_PEPTIDE_PAIR_PASSES_THRESHOLD = "MS:1003339";
    private boolean firstTerm = false;
    private boolean secondTerm = false;
    private boolean thirdTerm = false;

    /**
     * Members.
     */
    private HashMap<String, String> map_1001143 = null; // maps accession to term name
    private HashMap<String, String> map_1002358 = null; // maps accession to term name
    private HashMap<String, String> map_1002664 = null; // maps accession to term name
    
    /**
     * Constructors.
     */
    public PeptideLevelStatsObjectRule() {
        this(null);
    }

    /**
     * Constructor.
     * @param ontologyManager the ontology manager
     */
    public PeptideLevelStatsObjectRule(OntologyManager ontologyManager) {
        super(ontologyManager);
        
        this.map_1001143 = this.getTermChildren("MS:1001143", "MS");
        this.map_1002358 = this.getTermChildren("MS:1002358", "MS");
        this.map_1002664 = this.getTermChildren("MS:1002664", "MS");
    }

    /**
     * Checks, if the object is a SpectrumIdentificationItem.
     * 
     * @param obj   the object to check
     * @return true, if obj is a SpectrumIdentificationItem
     */
    @Override
    public boolean canCheck(Object obj) {
        return (obj instanceof SpectrumIdentificationItem);
    }

    /**
     * Checks, if the required triplet of terms is present in case of peptide-level scoring.
     * 
     * @param sii the SpectrumIdentificationItem element
     * @return collection of messages
     */
    @Override
    public Collection<ValidatorMessage> check(SpectrumIdentificationItem sii) {
        List<ValidatorMessage> messages = new ArrayList<>();

        if (AdditionalSearchParamsObjectRule.bIsPeptideLevelScoring) {
            String acc;
            for (CvParam cv: sii.getCvParam()) {
                if (cv != null) {
                    acc = cv.getAccession();
                    if (this.isASearchEnginePeptideScore(acc) || this.isASearchEnginePSMScore(acc)
                        || this.isAXLInteractionScore(acc)) {
                        this.firstTerm = true;
                    }
                    switch (acc) {
                        case "MS:1002500":  // peptide passes threshold
                            this.secondTerm = true;
                            break;
                        case "MS:1002520":  // peptide group ID
                            this.thirdTerm = true;
                            break;
                    }
                    // A cross-linked peptide pair passes the threshold as a pair (mzIdentML 1.3.0).
                    if (AdditionalSearchParamsObjectRule.bIsCrossLinkingSearch
                        && PeptideLevelStatsObjectRule.XL_PEPTIDE_PAIR_PASSES_THRESHOLD.equals(acc)) {
                        this.secondTerm = true;
                    }
                }
            }
            if (!(this.firstTerm && this.secondTerm && this.thirdTerm)) {
                this.addMessageToCollection(sii, messages);
            }
        }

        return messages;
    }
    
    /**
     * Checks, if a term is a child of MS:1002358.
     * @return true, if the accession belongs to a CV term, which is a child of MS:1002358 ("search engine specific score for distinct peptides")
     */
    private boolean isASearchEnginePeptideScore(String acc) {
        return this.map_1002358.containsKey(acc);
    }

    /**
     * Gets the wording of the required triplet for the kind of search being validated.
     * @return the triplet description
     */
    private String getTripletMsg() {
        return AdditionalSearchParamsObjectRule.bIsCrossLinkingSearch ? this.tripletMsgXL : this.tripletMsg;
    }

    /**
     * Checks, if a term is a child of MS:1002664, e.g. MS:1003338 ("peptide-pair sequence-level
     * global FDR"). Cross-linking scores the peptide pair rather than the single peptide, so such
     * a score stands in for a child of MS:1002358 in a cross-linking file (mzIdentML 1.3.0).
     * @return true, if the accession belongs to a CV term, which is a child of MS:1002664 ("interaction score derived from crosslinking")
     */
    private boolean isAXLInteractionScore(String acc) {
        return AdditionalSearchParamsObjectRule.bIsCrossLinkingSearch && this.map_1002664 != null && this.map_1002664.containsKey(acc);
    }

    /**
     * Checks, if a term is a child of MS:1001143.
     * @return true, if the accession belongs to a CV term, which is a child of MS:1001143 ("search engine specific score for PSMs")
     */
    private boolean isASearchEnginePSMScore(String acc) {
        return this.map_1001143.containsKey(acc);
    }

    /**
     * Adds a message to the messages collection.
     */
    private void addMessageToCollection(SpectrumIdentificationItem sii, List<ValidatorMessage> messages) {
        messages.add(new ValidatorMessage("The SpectrumIdentificationItem (id='"
        + sii.getId() + "') element at " + PeptideLevelStatsObjectRule.SII_CONTEXT.getContext()
        + " doesn't contain the " + this.getTripletMsg() + "required in case of peptide-level scoring", MessageLevel.ERROR, PeptideLevelStatsObjectRule.SII_CONTEXT, this));
    }
    
    /**
     * Gets the tips how to fix the error.
     * 
     * @return collection of tips
     */
    @Override
    public Collection<String> getHowToFixTips() {
        List<String> ret = new ArrayList<>();

        ret.add("Add the" + this.getTripletMsg() + "to each SpectrumIdentificationItem.");
        
        return ret;
    }
}
