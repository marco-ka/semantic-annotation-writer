package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TRegexRule {

    static String createFromMarkers(String beforeEachRule, Set<String> markers, String afterEachRule, Collection<String> stopWords) {
        Stream<String> sanitizedMarkers = markers.stream()
                .filter(lemma -> !stopWords.contains(lemma))
                // even escaped dots ("/.") and commas break TRegexRule rules
                .map(lemma -> lemma.replace(".", ""))
                .map(lemma -> lemma.replace(",", ""))
                // even escaped slashes ("\/") break TRegex
                .map(lemma -> lemma.replace("/", ""))
                // Remove lemmas that consist only of digits. Such lemmas seem to lead to an "illegal octal escape sequence" exception
                .filter(lemma -> !lemma.replaceAll("\\d", "").isEmpty())
                .map(TRegexRule::regexEscape);

        Set<String> singleWordMarkers = new TreeSet<>();
        Set<String> multiWordMarkers = new TreeSet<>();

        sanitizedMarkers.forEach(lemma -> {
            if (lemma.contains(" "))
                multiWordMarkers.add(lemma);
            else
                singleWordMarkers.add(lemma);
        });

        String oneWordRules = beforeEachRule + withinNode("__", any(singleWordMarkers.stream())) + afterEachRule;
        String multiWordRules = any(multiWordMarkers.stream().map(lemma -> ruleFromMultipleWords(beforeEachRule, lemma, afterEachRule)));

        return oneWordRules + "|" + multiWordRules;
    }

    private static String any(Stream<String> rules) {
        return rules.collect(Collectors.joining("|"));
    }

    // If marker consists of two words (eg. marker = "word1 word2", merge into tregex rule like this: `((__ < word1) $ (__ < word2))`
    // e.g. `NP < "this article"` becomes `NP < ((__ < this) $ (__ < article))`
    private static String ruleFromMultipleWords(String beforeEachRule, String lemma, String afterEachRule) {
        if (lemma.contains(" ")) {
            Stream<String> words = Arrays.stream(lemma.split(" "));
            List<String> wordList = words.map(word -> "(__ < " + word + ")").collect(Collectors.toList());

            String rule = String.join(" $ ", wordList);

            return beforeEachRule + rule + afterEachRule;
        }
        return lemma;
    }

    private static String withinNode(String parentNode, String rule) {
        return "("+ parentNode + " < (" + rule + "))";
    }

    private static String regexEscape(String str) {
        return str.replaceAll("[.\\\\+*?\\[\\^\\]$(){}0123456789=!<>|:\\-]", "\\\\$0");    }
}
