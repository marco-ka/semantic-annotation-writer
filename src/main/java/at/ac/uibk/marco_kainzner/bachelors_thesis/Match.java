package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.stanford.nlp.trees.Tree;

public class Match {
    public final String label;
    public final String documentId;
    public final String sentenceId;
    public final Tree sentenceTree;
    public final Tree matchTree;

    private final int sentenceNumber;

    public Match(String label, String documentId, int sentenceNumber, Tree sentenceTree, Tree matchTree) {
        this.label = label;
        this.sentenceTree = sentenceTree;
        this.matchTree = matchTree;

        this.documentId = documentId;
        this.sentenceNumber = sentenceNumber;
        this.sentenceId = documentId + "-" + sentenceNumber;
    }

    @Override
    public String toString() {
        return label + ": " + documentId + "-" + sentenceNumber + " " + matchTree.pennString();
    }
}
