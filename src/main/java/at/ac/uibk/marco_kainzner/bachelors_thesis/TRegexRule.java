package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TRegexRule {

    static String matchMarkers(String parentNode, Set<String> markers, Collection<String> stopWords) {
        Stream<String> sanitizedMarkers = markers.stream()
                .filter(lemma -> !stopWords.contains(lemma))
                .map(lemma -> lemma.replace(".", "")) // even escaped dots ("/.") break TRegexRule rules
                .map(lemma -> lemma.replace("/", "")) // even escaped slashes ("\/") break TRegex
                .map(TRegexRule::regexEscape);

        Set<String> singleWordMarkers = new TreeSet<>();
        Set<String> multiWordMarkers = new TreeSet<>();

        sanitizedMarkers.forEach(lemma -> {
            if (lemma.contains(" "))
                multiWordMarkers.add(lemma);
            else
                singleWordMarkers.add(lemma);
        });

        String oneWordRules = withinNode(parentNode, withinNode("__", any(singleWordMarkers.stream())));
        String multiWordRules = any(multiWordMarkers.stream().map(lemma -> ruleFromMultipleWords(parentNode, lemma)));

        return oneWordRules + "|" + multiWordRules;
    }

    private static String any(Stream<String> rules) {
        return rules.collect(Collectors.joining("|"));
    }

    // If marker consists of two words (eg. marker = "word1 word2", merge into tregex rule like this: `((__ < word1) $ (__ < word2))`
    // e.g. `NP < "this article"` becomes `NP < ((__ < this) $ (__ < article))`
    private static String ruleFromMultipleWords(String rootNode, String lemma) {
        if (lemma.contains(" ")) {
            Stream<String> words = Arrays.stream(lemma.split(" "));
            List<String> wordList = words.map(word -> "(__ < " + word + ")").collect(Collectors.toList());

            String rule = String.join(" $ ", wordList);

            return withinNode(rootNode, rule);
        }
        return lemma;
    }

    private static String withinNode(String parentNode, String rule) {
        return "("+ parentNode + " < (" + rule + "))";
    }

    private static String regexEscape(String str) {
        return str.replaceAll("[.\\\\+*?\\[\\^\\]$(){}0123456789=!<>|:\\-]", "\\\\$0");    }
}
