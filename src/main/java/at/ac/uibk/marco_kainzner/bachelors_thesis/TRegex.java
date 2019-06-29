package at.ac.uibk.marco_kainzner.bachelors_thesis;

import jdk.jshell.spi.ExecutionControl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TRegex {
    private static final List<String> PENN_TAGS = Arrays.asList("CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNS", "NNP", "NNPS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBZ", "VBP", "VBD", "VBN", "VBG", "WDT", "WP", "WP$", "WRB", "NP", "PP", "VP", "ADVP", "ADJP", "SBAR", "PRT", "INTJ");

    static String ruleFromMarkers(String beforeEachRule, Set<String> markers, String afterEachRule) {
        var sanitizedMarkers = preprocess(markers).stream()
                .collect(Collectors.partitioningBy(x -> x.contains(" ")));

        var multiWordMarkers = sanitizedMarkers.get(true);
        var singleWordMarkers = sanitizedMarkers.get(false);

        String multiWordRules = any(multiWordMarkers.stream().map(lemma -> ruleFromMultipleWords(beforeEachRule, lemma, afterEachRule)));
        String singleWordRules = beforeEachRule + any(singleWordMarkers.stream()) + afterEachRule;

        if (singleWordMarkers.isEmpty()){
            return multiWordRules;
        }
        return singleWordRules + "|" + multiWordRules;
    }

    static Set<String> subRules(Set<String> markers) {
        // TODO: return set of merged words
        return markers.stream().map(TRegex::mergeWords).collect(Collectors.toSet());
    }

    static Set<String> preprocess(Set<String> markers) {
        return markers.stream()
                // Remove lemmas that look like Penn Tags
                .filter(lemma -> !PENN_TAGS.contains(lemma))
                // even escaped dots ("/.") and commas break TRegex rules
                .map(lemma -> lemma.replace(".", ""))
                .map(lemma -> lemma.replace(",", ""))
                // even escaped slashes ("\/") break TRegex
                .map(lemma -> lemma.replace("/", ""))
                // Remove lemmas that consist only of digits. Such lemmas seem to lead to an "illegal octal escape sequence" exception
                .filter(lemma -> !lemma.replaceAll("\\d", "").isEmpty())
                .map(TRegex::regexEscape)
                .collect(Collectors.toSet());
    }

    static String any(Stream<String> rules) {
        return rules.collect(Collectors.joining("|"));
    }

    private static String mergeWords(String words) {
        var wordList = Arrays.asList(words.split(" "));
        return mergeWords(wordList);
    }

    static String mergeWords(List<String> wordsFixedList) {
        Collections.reverse(wordsFixedList);
        var words = new ArrayList<>(wordsFixedList);

        // Use last word as initial state. This approach avoids empty parentheses at the end of the rule.
        var fst = words.get(0);
        words.remove(0);

        return words.stream().reduce(fst, (suffix, word) -> immediatelyPrecedes(word, suffix));
    }

    private static String immediatelyPrecedes(String fst, String snd) {
        return "(" + fst + " . " + snd + ")";
    }

    // If marker consists of two words (eg. marker = "word1 word2", merge into tregex rule like this: `((__ < word1) $ (__ < word2))`
    // e.g. `NP < "this article"` becomes `NP < ((__ < this) $ (__ < article))`
    private static String ruleFromMultipleWords(String beforeEachRule, String lemma, String afterEachRule) {
        if (lemma.contains(" ")) {
            return beforeEachRule + mergeWords(lemma) + afterEachRule;
        }
        return lemma;
    }

    private static String regexEscape(String str) {
        return str.replaceAll("[.\\\\+*?\\[\\^\\]$(){}0123456789=!<>|:\\-]", "\\\\$0");    }
}
