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

    public boolean matches(Annotation other) {
        if (!documentId.equals(other.documentId)) {
            System.out.println("Document ids do not match: " + documentId + " != " + other.documentId);
            return false;
        }

        if (sentenceId != other.sentenceId) {
//            System.out.println("Sentence ids do not match: " + sentenceId + " != " + other.sentenceId);
            return false;
        }

        if (!annotation.label.equals(other.annotation.label)) {
//            System.out.println("Labels do not match: " + annotation.label + " != " + other.annotation.label);
            return false;
        }

        if (!overlaps(other)) {
//            System.out.println("'" + toString() + "' does not overlap '" + other.toString() + "'");
            return false;
        }

        return true;
    }

    private boolean overlaps(Annotation other) {
        var containsOther = contains(other.annotation.begin) || contains(other.annotation.end);
        var containedInOther = other.contains(annotation.begin) || other.contains(annotation.end);

        return containsOther || containedInOther;
    }

    private boolean contains(int position) {
        return (annotation.begin <= position) && (position <= annotation.end);
    }

    @Override
    public String toString() {
        return annotation.label + ": " + sentenceId + " from " + annotation.begin + " to " + annotation.end + " (" + coveredText + ")";
    }
}
