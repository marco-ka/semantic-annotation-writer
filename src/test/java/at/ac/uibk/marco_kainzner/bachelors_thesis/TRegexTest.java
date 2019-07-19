package at.ac.uibk.marco_kainzner.bachelors_thesis;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TRegexTest {

    private static final Set<String> shortMarkers = Stream.of("some", "short", "markers").collect(Collectors.toSet());
    private static final Set<String> longMarkers = Stream.of("some longer", "markers with multiple words").collect(Collectors.toSet());

    @Test
    void ruleFromMarkers() {
        var markers = new TreeSet<String>();
        markers.addAll(shortMarkers);
        markers.addAll(longMarkers);

        var expected = "(SBAR <<, (some|short|markers))|(SBAR <<, (some . longer))|(SBAR <<, (markers . (with . (multiple . words))))";
        var actual = TRegex.ruleFromMarkers("SBAR <<, ", markers, "");

        assertEquals(expected, actual);
    }

    @Test
    void ruleFromShortMarkers() {
        var expected = "(NP < (some|short|markers))";
        var actual = TRegex.ruleFromMarkers("NP < ", shortMarkers, "");

        assertEquals(expected, actual);
    }

    @Test
    void ruleFromLongMarkers() {
        var expected = "(VP << (some . longer))|(VP << (markers . (with . (multiple . words))))";
        var actual = TRegex.ruleFromMarkers("VP << ", longMarkers, "");

        assertEquals(expected, actual);
    }

    @Test
    void preprocess() {
        // Given
        var unprocessed = Stream.of("12345", "9 Starts With Digit", "/Word 1", ".Word 2", ",Word 3").collect(Collectors.toSet());

        // When
        var actual = TRegex.preprocess(unprocessed);

        // Then
        var expected = Stream.of("Word \\1", "Word \\2", "Word \\3").collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void mergeWords() {
        // Given
        var words = Arrays.asList("one", "two", "three", "four");

        // When
        var actual = TRegex.mergeWords(words);

        // Then
        var expected = "(one . (two . (three . four)))";
        assertEquals(expected, actual);
    }
}