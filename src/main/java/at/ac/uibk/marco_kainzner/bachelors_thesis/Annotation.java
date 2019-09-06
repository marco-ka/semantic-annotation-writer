package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.io.Serializable;

public class Annotation implements Serializable {
    public final String documentId;
    public final String sentenceId;
    public final String label;
    public final int begin;
    public final int end;

    public Annotation(String documentId, String sentenceId, String label, int begin, int end) {
        this.documentId = documentId;
        this.sentenceId = sentenceId;
        this.label = label;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String toString() {
        return label + ": " + sentenceId + " from " + begin + " to " + end;
    }
}
