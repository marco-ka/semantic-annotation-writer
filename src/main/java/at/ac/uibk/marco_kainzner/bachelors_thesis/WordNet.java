package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.list.PointerTargetTreeNodeList.*;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordNet {
    private final static String PROPERTIES_FILE = "resources\\wordnet\\extjwnl_properties.xml";

    private static final List<String> PENN_TAGS = Arrays.asList("CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNS", "NNP", "NNPS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBZ", "VBP", "VBD", "VBN", "VBG", "WDT", "WP", "WP$", "WRB", "NP", "PP", "VP", "ADVP", "ADJP", "SBAR", "PRT", "INTJ");

    public static void main(String[] args) throws JWNLException, FileNotFoundException {
        Dictionary dict = Dictionary.getInstance(new FileInputStream(PROPERTIES_FILE));

//        System.out.println(ruleFromMultipleWords("the municipal authorities"));
        ruleFromSynset(dict, "person%1:03:00::");
//        Stream<String> hyponyms = findAllHyponyms(syn);

//        ruleFromSynset(dict, "body%1:14:00::");
//        ruleFromSynset(dict, "organisation%1:14:00::");
    }

    private static String ruleFromSynset(Dictionary dict, String senseKey) throws JWNLException {
        Synset syn = dict.getWordBySenseKey(senseKey).getSynset();
        Set<String> lemmas = findAllHyponyms(syn);
        String tregexRule = createTregexRule(lemmas.stream(), "NP", PENN_TAGS);

        System.out.println("LEMMA:      " + syn.getWords().get(0).getLemma());
        System.out.println("GLOSS:      " + syn.getGloss());
        System.out.println("HYPONYMS:   " + lemmas);
        System.out.println("#HYPONYMS:  " + lemmas.size());
        System.out.println("TREGEX:     " + tregexRule);
        System.out.println("---");

        return tregexRule;
    }

    private static String createTregexRule(Stream<String> lemmas, String rootNode, Collection<String> stopWords) {
        Stream<String> sanitizedLemmas = lemmas
                .filter(lemma -> !stopWords.contains(lemma))
                .map(lemma -> lemma.replace(".", "")) // even escaped dots ("/.") break TRegex rules
                .map(lemma -> regexEscape(lemma));

        Set<String> singleWordLemmas = new TreeSet<>();
        Set<String> multiWordLemmas = new TreeSet<>();

        sanitizedLemmas.forEach(lemma -> {
            if (lemma.contains(" "))
                multiWordLemmas.add(lemma);
            else
                singleWordLemmas.add(lemma);
        });

        String oneWordRule = withinNode(rootNode, withinNode("__", any(singleWordLemmas.stream())));
        String multiWordRules = any(multiWordLemmas.stream().map(lemma -> ruleFromMultipleWords(rootNode, lemma)));

        return oneWordRule + "|" + multiWordRules;
    }

    private static Set<String> findAllHyponyms(Synset syn) throws JWNLException {
        Set<String> lemmas = new TreeSet<>();
        Operation addLemmasToList = pointerTargetTreeNode -> {

            List<Word> words = pointerTargetTreeNode.getPointerTarget().getSynset().getWords();
            words.stream()
                .map(word -> word.getLemma())
                .forEach(lemma -> lemmas.add(lemma));

            return pointerTargetTreeNode;
        };

        PointerTargetTree hyponyms = PointerUtils.getHyponymTree(syn);
        hyponyms.getAllMatches(addLemmasToList);

        return lemmas;
    }

    private static String any(Stream<String> rules) {
        return rules.collect(Collectors.joining("|"));
    }

    // If lemma consists of two words (eg. lemma = "word1 word2", merge into tregex rule like this: `((__ < word1) $ (__ < word2))`
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
        return str.replaceAll("[.\\\\+*?\\[\\^\\]$(){}=!<>|:\\-]", "\\\\$0");    }
}
