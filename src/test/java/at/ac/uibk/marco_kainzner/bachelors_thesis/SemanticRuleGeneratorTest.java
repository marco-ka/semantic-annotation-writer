package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.stanford.nlp.trees.tregex.TregexPattern;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SemanticRuleGeneratorTest {

    @Test
    void createSrelRule() {
        // Given
        var markers = Set.of("marker", "another marker");

        // When
        var actual = SemanticRuleGenerator.createSrelRule(markers);

        // Then
        var expected = "(SBAR <<, (WDT < who|which|whom|that|where|why|when)((<< (marker))|(<< (another . marker))))";
        assertEquals(expected, actual);
        assertDoesNotThrow(() -> TregexPattern.compile(actual));
    }

    @Test
    void createEmptySrelRule() {
        // Given
        var markers = new TreeSet<String>();

        // When
        var actual = SemanticRuleGenerator.createSrelRule(markers);

        // Then
        var expected = "(SBAR <<, (WDT < who|which|whom|that|where|why|when)())";
        assertEquals(expected, actual);
//        assertDoesNotThrow(() -> TregexPattern.compile(actual));
    }

    @Test
    void createSrelRuleNamed() {
        // Given
        var markers = Set.of("marker", "another marker");

        // When
        var actual = SemanticRuleGenerator.createSrelRule(markers, "srel_name");

        // Then
        var expected = "(SBAR=srel_name <<, (WDT < who|which|whom|that|where|why|when)((<< (marker))|(<< (another . marker))))";
        assertEquals(expected, actual);
        assertDoesNotThrow(() -> TregexPattern.compile(actual));
    }

    @Test
    void createVPartRule() {
        // Given
        var markers = Set.of("a marker");

        // When
        var actual = SemanticRuleGenerator.createVPartRule(markers);

        // Then
        var expected = "(NP < ((VP <, VBG) << ((a . marker))))";
        assertEquals(expected, actual);
        assertDoesNotThrow(() -> TregexPattern.compile(actual));
    }

    @Test
    void createVPartRuleNamed() {
        // Given
        var markers = Set.of("a marker");

        // When
        var actual = SemanticRuleGenerator.createVPartRule(markers, "vpart_name");

        // Then
        var expected = "(NP < ((VP=vpart_name <, VBG) << ((a . marker))))";
        assertEquals(expected, actual);
        assertDoesNotThrow(() -> TregexPattern.compile(actual));
    }

    @Test
    void createVPinfRuleNamed() {
        // Given
        var markers = Set.of("in order");

        // When
        var actual = SemanticRuleGenerator.createVPinfRule(markers, "reason_x");

        // Then
        var expected = "((__ << (VP=reason_x < (TO $ (__ << VB))))((($ (__ < (in . order))) > __) >> NP))";
        assertEquals(expected, actual);
        assertDoesNotThrow(() -> TregexPattern.compile(actual));
    }

    @Test
    void createVPinfRuleNamedSingleMarkers() {
        // Given
        var markers = Set.of("due", "because");

        // When
        var actual = SemanticRuleGenerator.createVPinfRule(markers, "reason_x");

        // Then
        var expected = "((__ << (VP=reason_x < (TO $ (__ << VB))))((($ (__ < (due|because))) > __) >> NP))";
        assertEquals(expected, actual);
        assertDoesNotThrow(() -> TregexPattern.compile(actual));
    }
}
