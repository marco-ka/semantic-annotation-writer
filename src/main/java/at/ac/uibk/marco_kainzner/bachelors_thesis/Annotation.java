package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.io.Serializable;

public class Annotation implements Serializable {
    public final String documentId;
    public final int sentenceId;
    public final SimpleAnnotation annotation;
    public final String coveredText;

    public Annotation(String documentId, int sentenceId, String containingText, String label, int begin, int end) {
        this.documentId = documentId;
        this.sentenceId = sentenceId;
        this.annotation = new SimpleAnnotation(containingText, label, begin, end);
        coveredText = containingText.substring(begin, end);
    }

    @Override
    public String toString() {
        return annotation.label + ": " + sentenceId + " from " + annotation.begin + " to " + annotation.end;
    }
}
