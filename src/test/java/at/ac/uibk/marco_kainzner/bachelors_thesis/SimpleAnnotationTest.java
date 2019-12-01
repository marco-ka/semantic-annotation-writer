package at.ac.uibk.marco_kainzner.bachelors_thesis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static at.ac.uibk.marco_kainzner.bachelors_thesis.SimpleAnnotation.Match;

class SimpleAnnotationTest {

    private final String someText = "Hello, John.";
    private final String otherText = "Hello, Juan.";

    @Test
    void compareTo() {
        var annotation = new SimpleAnnotation(someText, "Label", 0, 2);

        var similar = new SimpleAnnotation(someText, "Label", 1, 2);
        var different = new SimpleAnnotation(someText, "DifferentLabel", 1, 2);

        var withOtherText = new SimpleAnnotation(otherText, "Label", 0, 2);

        assertEquals(Match.PERFECT, annotation.compareTo(annotation));

        assertEquals(Match.PARTIAL, annotation.compareTo(similar));
        assertEquals(Match.PARTIAL, similar.compareTo(annotation));

        assertEquals(Match.NONE, annotation.compareTo(different));
        assertThrows(IllegalArgumentException.class, () -> withOtherText.compareTo(annotation));
    }

    @Test
    void overlapsWithSelf() {
        var annotation = new SimpleAnnotation(someText, "Name", 7, 11);

        assertTrue(annotation.overlapsWith(annotation));
    }

    @Test
    void overlapsWith() {
        var nameAnnotation = new SimpleAnnotation(someText, "X", 7, 11);
        var partialAnnotation = new SimpleAnnotation(someText, "Y", 8, 10);

        assertTrue(nameAnnotation.overlapsWith(partialAnnotation));
        assertTrue(partialAnnotation.overlapsWith(nameAnnotation));
    }

    @Test
    void doesNotOverlap() {
        var some = new SimpleAnnotation(someText, "Name", 7, 8);
        var other = new SimpleAnnotation(someText, "Name", 9, 10);

        assertFalse(some.overlapsWith(other));
        assertFalse(other.overlapsWith(some));
    }
}
