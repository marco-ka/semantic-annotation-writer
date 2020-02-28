package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.util.Objects;

public class SimpleAnnotation {
    public final String containingText;
    public final String label;
    public final int begin;
    public final int end;

    public enum Match {
        PERFECT,
        PARTIAL,
        NONE
    }

    public SimpleAnnotation(String containingText, String label, int begin, int end) {
        this.containingText = containingText;
        this.label = label;
        this.begin = begin;
        this.end = end;

        validate();
    }

    public Match compareTo(SimpleAnnotation other) {
        if (!containingText.equals(other.containingText)) {
            throw new IllegalArgumentException("Sentences don't match");
        }

        if (label.equals(other.label)) {
            if (begin == other.begin && end == other.end) {
                return Match.PERFECT;
            }

            if (overlapsWith(other)) {
                return Match.PARTIAL;
            }
        }

        return Match.NONE;
    }

    public boolean overlapsWith(SimpleAnnotation other) {
        return this.containsBeginOrEnd(other) || other.containsBeginOrEnd(this);
    }

    public boolean contains(SimpleAnnotation other) {
        return begin <= other.begin && end >= other.end;
    }

    public boolean containsBeginOrEnd(SimpleAnnotation other) {
        return containsIndex(other.begin) || containsIndex(other.end);
    }

    private boolean containsIndex(int index) {
        return index >= begin && index <= end;
    }

    private void validate() {
        if (label.isEmpty())
            throw new IllegalArgumentException("label is empty");

        if (begin < 0)
            throw new IllegalArgumentException("begin is" + begin);

        if (begin >= end)
            throw new IllegalArgumentException("begin (" + begin + ") >= end (" + end + ")");

        if (end > containingText.length()) {
            System.out.println(containingText + "\n from " + begin + " to " + end);
            throw new IllegalArgumentException("end (" + end + ") >= length of text (" + containingText.length() + ")");
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleAnnotation that = (SimpleAnnotation) o;
        return begin == that.begin &&
                end == that.end &&
                containingText.equals(that.containingText) &&
                label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containingText, label, begin, end);
    }

    public String getSpanningText() {
        return containingText.substring(begin, end);
    }
}
