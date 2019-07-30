package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.list.PointerTargetTreeNodeList.*;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordNet {
    private final static String PROPERTIES_FILE = "resources\\wordnet\\extjwnl_properties.xml";
    private static Dictionary dict = null;

    static {
        try {
            dict = Dictionary.getInstance(new FileInputStream(PROPERTIES_FILE));
        } catch (JWNLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static Set<String> getAllHyponyms(Synset syn) {
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

    static Synset getSynset(String senseKey) {
        try {
            return dict.getWordBySenseKey(senseKey).getSynset();
        } catch (JWNLException e) {
            e.printStackTrace();
            return null;
        }
    }

    static Set<String> getPersonsWithoutNames() {
        var senseKey = "person%1:03:00::";
        var persons = getAllHyponyms(getSynset(senseKey));
        var alphabetLower = "abcdefghijklmnopqrstuvwxyz";

        return persons.stream()
                // Keep only persons that start with a lowercase character.
                // Drop everything that starts with digits, uppercase chars (names), special chars etc.
                .filter(person -> alphabetLower.indexOf(person.charAt(0)) != -1)
                .collect(Collectors.toSet());
    }

    static Set<String> getAdjectivesAndAntonyms(Synset adverb) throws JWNLException {
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

    static Set<String> getWords(Synset syn) {
        return synsetToWord(syn).collect(Collectors.toSet());
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

    private static Set<String> synsetsToWords(Set<Synset> syns) {
        return syns.stream()
                .flatMap(WordNet::synsetToWord)
                .collect(Collectors.toSet());
    }

    private static Stream<String> synsetToWord(Synset syn) {
        return syn.getWords().stream().map(Word::getLemma);
    }
}
