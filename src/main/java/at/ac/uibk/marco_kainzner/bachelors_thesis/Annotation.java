package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.io.Serializable;

public class Annotation implements Serializable {
    public final String label;
    public final String documentId;
    public final String sentenceText;
    public final String matchText;
    public final int sentenceId;
    public final int begin;
    public final int end;

    public Annotation(String documentId, int sentenceId, String sentenceText, String matchText, String label, int begin, int end) {
        this.documentId = documentId;
        this.sentenceId = sentenceId;
        this.sentenceText = sentenceText;
        this.matchText = matchText;
        this.label = label;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String toString() {
        return label + ": " + sentenceId + " from " + begin + " to " + end;
    }

    public String compareTo(Annotation other) {
        if (!sentenceText.equals(other.sentenceText)) {
            throw new IllegalArgumentException("Sentences don't match");
        }

        if (begin == other.begin && end == other.end) {
            return "perfect match";
        }

        if (this.overlapsWith(other)) {
            return "partial match";
        }

        else return "no match";
    }

    public boolean overlapsWith(Annotation other) {
        var isBeginIncluded = begin >= other.begin && begin <= other.end;
        var isEndIncluded = end >= other.begin && end <= other.end;

        return isBeginIncluded || isEndIncluded;
    }

}
