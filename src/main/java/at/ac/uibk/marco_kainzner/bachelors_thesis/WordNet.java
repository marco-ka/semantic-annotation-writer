package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.list.PointerTargetTreeNodeList.*;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class WordNet {
    private final static String PROPERTIES_FILE = "resources\\wordnet\\extjwnl_properties.xml";

    private static final List<String> PENN_TAGS = Arrays.asList("CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNS", "NNP", "NNPS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBZ", "VBP", "VBD", "VBN", "VBG", "WDT", "WP", "WP$", "WRB", "NP", "PP", "VP", "ADVP", "ADJP", "SBAR", "PRT", "INTJ");

    public static void main(String[] args) throws JWNLException, FileNotFoundException {
        Dictionary dict = Dictionary.getInstance(new FileInputStream(PROPERTIES_FILE));

        String actorRule1 = ruleFromSynset(dict, "person%1:03:00::", "NP");
        String actorRule2 = ruleFromSynset(dict, "body%1:14:00::", "NP");
        String actorRule3 = ruleFromSynset(dict, "organisation%1:14:00::", "NP");
    }

    private static String ruleFromSynset(Dictionary dict, String senseKey, String parentNode) throws JWNLException {
        Synset syn = dict.getWordBySenseKey(senseKey).getSynset();
        Set<String> markers = findAllHyponyms(syn);
        String tregexRule = TRegexRule.matchMarkers(parentNode, markers, PENN_TAGS);

        System.out.println("" + syn.getWords().get(0).getLemma());
        System.out.println("" + syn.getGloss());
        System.out.println("HYPONYMS:   " + markers);
        System.out.println("#HYPONYMS:  " + markers.size());
        System.out.println("TREGEX:     " + tregexRule);
        System.out.println("---");

        return tregexRule;
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

}
