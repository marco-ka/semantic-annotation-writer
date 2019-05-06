package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
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

        String timeRule = createTimeRule(dict);
        String actorRule = createActorRules(dict);

        System.out.println("Time rule:  " + timeRule);
        System.out.println("Actor rule: " + actorRule);

        // Match characters that might break tregex: [^()<>|\w $]
        // String artifactRule = createRuleFromHyponyms(dict, "artifact%1:03:00::", "NP");
    }

    private static String createActorRules(Dictionary dict) {
        List<String> senseKeys = Arrays.asList("person%1:03:00::", "body%1:14:00::", "organisation%1:14:00::");

        Set<String> markers = new TreeSet<>();
        senseKeys.forEach(key -> markers.addAll(getAllHyponyms(getSynset(dict, key))));

        return TRegexRule.matchMarkers("NP", markers, PENN_TAGS);
    }

    private static String createTimeRule(Dictionary dict) throws JWNLException {
        Synset temporarily = dict.getWordBySenseKey("temporarily%4:02:00::").getSynset();
        Synset period = dict.getWordBySenseKey("period%1:28:00::").getSynset();

        List<String> plainMarkers = Arrays.asList("before", "after", "date");
        Set<String> temporary = getAdjectivesAndAntonyms(temporarily);
        Set<String> periodHyponyms = getAllHyponyms(period);

        Set<String> markers = new TreeSet<>(plainMarkers);
        markers.addAll(temporary);
        markers.addAll(periodHyponyms);

        return TRegexRule.matchMarkers("NP", markers, PENN_TAGS);
    }

    private static String createRuleFromHyponyms(Dictionary dict, String senseKey, String parentNode) throws JWNLException {
        Synset syn = dict.getWordBySenseKey(senseKey).getSynset();
        Set<String> markers = getAllHyponyms(syn);
        String tregexRule = TRegexRule.matchMarkers(parentNode, markers, PENN_TAGS);

        System.out.println("" + syn.getWords().get(0).getLemma());
        System.out.println("" + syn.getGloss());
        System.out.println("HYPONYMS:   \n" + markers);
        System.out.println("#HYPONYMS:  " + markers.size());
        System.out.println("TREGEX:     \n" + tregexRule);
        System.out.println("---");

        return tregexRule;
    }

    //    private static Set<String> findAllLinkedWords(Synset syn) throws JWNLException {
//        Set<String> lemmas = new TreeSet<>();
//
//        PointerTargetNodeList alsoSees = PointerUtils.getAlsoSees(syn);
//        PointerTargetNodeList antonyms = PointerUtils.getAntonyms(syn);
////        PointerTargetNodeList antonyms = PointerUtils.getIndirectAntonyms(syn);
//        PointerTargetNodeList holonyms = PointerUtils.getHolonyms(syn);
//        PointerTargetNodeList related = PointerUtils.getAttributes(syn);
//        PointerTargetNodeList entailments = PointerUtils.getEntailments(syn);
//        PointerTargetNodeList causes = PointerUtils.getCauses(syn);
//        PointerTargetNodeList coordinateTerms = PointerUtils.getCoordinateTerms(syn);
//        PointerTargetNodeList partMeronyms = PointerUtils.getPartMeronyms(syn);
//        PointerTargetNodeList pertainyms = PointerUtils.getPertainyms(syn);
//        PointerTargetNodeList synonyms = PointerUtils.getAdjectivesAndAntonyms(syn);
//
//        PointerUtils.

//        Stream<String> synonymsX = synonyms.stream().flatMap(pt -> pt.getSynset().synsetToWord().stream().map(Word::getLemma));
//    }

    private static Set<String> getAdjectivesAndAntonyms(Synset adverb) throws JWNLException {
        Set<Synset> adjectives = getPertainyms(adverb);
        Set<Synset> antonyms = getAntonyms(adverb);
        Set<Synset> antonymAdjectives = antonyms.stream().flatMap(antonym -> getPertainyms(antonym).stream()).collect(Collectors.toSet());

        Set<Synset> attributes = adjectives.stream().flatMap(syn -> getRelatedSynsets(syn, PointerType.ATTRIBUTE)).collect(Collectors.toSet());
        Set<Synset> antonymAttributes = antonymAdjectives.stream().flatMap(syn -> getRelatedSynsets(syn, PointerType.ATTRIBUTE)).collect(Collectors.toSet());

        Set<String> words = synsetToWord(adverb).collect(Collectors.toCollection(TreeSet::new));
        words.addAll(synsetsToWords(adjectives));
        words.addAll(synsetsToWords(antonyms));
        words.addAll(synsetsToWords(antonymAdjectives));
        words.addAll(synsetsToWords(attributes));
        words.addAll(synsetsToWords(antonymAttributes));

        return words;
    }

    private static Stream<Synset> getRelatedSynsets(Synset syn, PointerType ptType) {
        PointerTargetNodeList pointerTargets = null;
        try {
            pointerTargets = new PointerTargetNodeList(syn.getTargets(ptType), ptType);
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return pointerTargets.stream().map(PointerTargetNode::getSynset);
    }

    private static Set<Synset> getPertainyms(Synset syn) {
        try {
            return targetNodesToSynsets(PointerUtils.getPertainyms(syn));
        } catch (JWNLException e) {
            e.printStackTrace();
            return new TreeSet<>();
        }
    }

    private static Set<Synset> getAntonyms(Synset syn) {
        try {
            return targetNodesToSynsets(PointerUtils.getAntonyms(syn));
        } catch(JWNLException e) {
            e.printStackTrace();
            return new TreeSet<>();
        }
    }

    private static Set<Synset> targetNodesToSynsets(PointerTargetNodeList pts) {
        return pts.stream().map(PointerTargetNode::getSynset).collect(Collectors.toSet());
    }

    private static Set<String> getAllHyponyms(Synset syn) {
        Set<String> lemmas = new TreeSet<>();
        Operation addLemmasToList = pointerTargetTreeNode -> {

            List<Word> words = pointerTargetTreeNode.getPointerTarget().getSynset().getWords();
            words.stream()
                .map(Word::getLemma)
                .forEach(lemmas::add);

            return pointerTargetTreeNode;
        };

        PointerTargetTree hyponyms = null;
        try {
            hyponyms = PointerUtils.getHyponymTree(syn);
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        hyponyms.getAllMatches(addLemmasToList);

        return lemmas;
    }

    private static Synset getSynset(Dictionary dict, String senseKey) {
        try {
            return dict.getWordBySenseKey(senseKey).getSynset();
        } catch (JWNLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Set<String> synsetsToWords(Set<Synset> syns) {
        return syns.stream()
                .flatMap(WordNet::synsetToWord)
                .collect(Collectors.toSet());
    }

    private static Stream<String> synsetToWord(Synset syn) {
        return syn.getWords().stream().map(Word::getLemma);
    }
}
