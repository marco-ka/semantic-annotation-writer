package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.list.PointerTargetTreeNodeList.*;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class WordNet {
    private final static String PROPERTIES_FILE = "C:\\Users\\Marco\\Documents\\Projects\\dkpro-pipeline\\src\\main\\resources\\extjwnl_properties.xml";

    public static void main(String[] args) throws JWNLException, FileNotFoundException {
        Dictionary dict = Dictionary.getInstance(new FileInputStream(PROPERTIES_FILE));

        POS pos = POS.NOUN;
        String word = "person";
        Synset syn = dict.getIndexWord(pos, word).getSenses().get(0);

        List<String> lemmas = findAllHyponyms(syn);

        System.out.println(lemmas);
        System.out.println(lemmas.size());
    }

    private static List<String> findAllHyponyms(Synset syn) throws JWNLException {
        List<String> lemmas = new ArrayList<>();
        Operation addLemmasToList = pointerTargetTreeNode -> {

            List<Word> words = pointerTargetTreeNode.getPointerTarget().getSynset().getWords();
            words.forEach(word -> lemmas.add(word.getLemma()));

            return pointerTargetTreeNode;
        };

        PointerTargetTree hyponyms = PointerUtils.getHyponymTree(syn);
        hyponyms.getAllMatches(addLemmasToList);

        return lemmas;
    }
}
