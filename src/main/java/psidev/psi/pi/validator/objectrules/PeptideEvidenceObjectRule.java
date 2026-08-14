package psidev.psi.pi.validator.objectrules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.Context;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorMessage;
import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.DBSequence;
import uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence;

/**
 * Check if the start and end attributes of PeptideEvidence are set unless it's a de novo search.
 * 
 * @author Gerhard
 * 
 */
public class PeptideEvidenceObjectRule extends AObjectRule<PeptideEvidence> {

    /**
     * Constants.
     */
    private static final Context PEV_CONTEXT    = new Context(MzIdentMLElement.PeptideEvidence.getXpath());
    private final String startEndAttrMissingMsg = " must have correct start and end attributes set (since it's not a de novo search).";
    private final String startEndAttrWrongMsg   = " has wrong start and end attributes set (start must be >= 1 and end >=start, but < length of the protein sequence.";
    public static HashSet<String> peptideRefSet = new HashSet<>();
    public static HashSet<String> dbSeqRefSet   = new HashSet<>();
    public static HashMap<String, String> peptideRef2PeptideEvidenceIDMap = new HashMap<>();
    
    /**
     * Constructors.
     */
    public PeptideEvidenceObjectRule() {
        this(null);
    }

    /**
     * Constructor.
     * @param ontologyManager the ontology manager
     */
    public PeptideEvidenceObjectRule(OntologyManager ontologyManager) {
        super(ontologyManager);
    }

    /**
     * Checks, if the object is a PeptideEvidence.
     * 
     * @param obj   the object to check
     * @return true, if obj is a PeptideEvidence
     */
    @Override
    public boolean canCheck(Object obj) {
        return (obj instanceof PeptideEvidence);
    }

    /**
     * Checks, if the required triplet of terms is present in case of peptide-level scoring.
     * 
     * @param pev the PeptideEvidence element
     * @return collection of messages
     */
    @Override
    public Collection<ValidatorMessage> check(PeptideEvidence pev) {
        List<ValidatorMessage> messages = new ArrayList<>();

        if (!SearchTypeObjectRule.bIsDeNovoSearch) {
            // Both attributes are optional in the schema but, as its documentation says, have to
            // be provided unless this is a de novo search. An absent one is reported as missing
            // rather than as a wrong value: reading it used to throw, and the rule then carried
            // on with 0 and reported the same element a second time as having wrong values.
            final Integer start = pev.getStart();
            final Integer end = pev.getEnd();

            if (start == null || end == null) {
                this.addMissingMessageToCollection(pev, messages);
            }
            // end == start is a peptide of a single residue, which is legal.
            else if (start < 1 || end < start) {
                this.addWrongMessageToCollection(pev, messages);
            }
            else {
                DBSequence dbSequence = pev.getDBSequence();    // TODO: Why is dbSequence here null ?

                if (dbSequence == null || dbSequence.getSeq() == null) {
                    this.LOGGER.info("No sequence to check the end position against");
                }
                else if (end > dbSequence.getSeq().length()) {
                    this.addWrongMessageToCollection(pev, messages);
                }
            }
        }
        
        PeptideEvidenceObjectRule.peptideRefSet.add(pev.getPeptideRef());
        PeptideEvidenceObjectRule.dbSeqRefSet.add(pev.getDBSequenceRef());
        PeptideEvidenceObjectRule.peptideRef2PeptideEvidenceIDMap.put(pev.getPeptideRef(), pev.getId());

        return messages;
    }
    
    /**
     * Adds a missing start/end attribute message to the messages collection.
     */
    private void addMissingMessageToCollection(PeptideEvidence pev, List<ValidatorMessage> messages) {
        messages.add(new ValidatorMessage("The PeptideEvidence (id='"
        + pev.getId() + "') element at " + PeptideEvidenceObjectRule.PEV_CONTEXT.getContext()
        + this.startEndAttrMissingMsg, MessageLevel.ERROR, PeptideEvidenceObjectRule.PEV_CONTEXT, this));
    }
    
    /**
     * Adds a wrong start/end attribute message to the messages collection.
     */
    private void addWrongMessageToCollection(PeptideEvidence pev, List<ValidatorMessage> messages) {
        messages.add(new ValidatorMessage("The PeptideEvidence (id='"
        + pev.getId() + "') element at " + PeptideEvidenceObjectRule.PEV_CONTEXT.getContext()
        + this.startEndAttrWrongMsg, MessageLevel.ERROR, PeptideEvidenceObjectRule.PEV_CONTEXT, this));
    }
    
    /**
     * Gets the tips how to fix the error.
     * 
     * @return collection of tips
     */
    @Override
    public Collection<String> getHowToFixTips() {
        List<String> ret = new ArrayList<>();

        ret.add("<PeptideEvidence> elements must have correct start and end attributes set, unless it's a de novo search.");
        
        return ret;
    }
}
