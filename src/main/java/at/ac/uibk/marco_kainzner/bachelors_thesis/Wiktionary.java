package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech;
import de.tudarmstadt.ukp.jwktl.api.filter.WiktionaryEntryFilter;
import de.tudarmstadt.ukp.jwktl.api.filter.WiktionarySenseFilter;
import de.tudarmstadt.ukp.jwktl.api.util.Language;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Wiktionary {
    private final static String DUMP_PATH = "resources\\wiktionary\\enwiktionary-20190320-pages-articles.xml.bz2";
    private final static String DB_DIR = "resources\\wiktionary\\berkeley-db";

    private final static String[] ACTOR_IDENTIFIERS = { " person ", " organization ", " body " };
    private final static String[] SITUATION_IDENTIFIERS = { " act ", " acting ", " action ", " actions " };

    public static void main(String[] args) throws IOException {
        saveMarkers("situation", PartOfSpeech.NOUN, SITUATION_IDENTIFIERS);
        saveMarkers("artifact", PartOfSpeech.NOUN, SITUATION_IDENTIFIERS);
    }

    public static void parse(String pathIn, String pathOut) {
        File dumpFile = new File(pathIn);
        File outputDirectory = new File(pathOut);

        JWKTL.parseWiktionaryDump(dumpFile, outputDirectory, true);
    }

    private static void saveMarkers(String markerName, PartOfSpeech pos, String[] identifiers) throws IOException {
        System.out.println(new Date());
        File wiktionaryDirectory = new File(DB_DIR);
        IWiktionaryEdition wkt = JWKTL.openEdition(wiktionaryDirectory);

        WiktionarySenseFilter filter = new WiktionarySenseFilter();
//        WiktionaryEntryFilter filter = new WiktionaryEntryFilter();
        filter.setAllowedWordLanguages(Language.ENGLISH);
        filter.setAllowedPartsOfSpeech(pos);

        System.out.println(new Date());

//        Stream<IWiktionaryEntry> entries = StreamSupport.stream(wkt.getAllEntries(filter).spliterator(), false);
        Stream<IWiktionarySense> senses = StreamSupport.stream(wkt.getAllSenses(filter).spliterator(), false);

        Set<String> words = new HashSet<>();
        senses.forEach(sense -> {
            if (containsAny(sense.getGloss().getPlainText(), identifiers)) {
                words.add(sense.getEntry().getWord());
            }
        });

//        List<String> words = entries
//            .limit(50000)
//            .filter(entry -> {
//                boolean containsAct = false;
//                for (IWiktionarySense sense : entry.getSenses()) {
//                    String definition = sense.getGloss().getPlainText();
//                    if (containsAny(definition, identifiers)) {
//                        containsAct = true;
//                        break;
//                    }
//                }
//                return containsAct;
//            })
//            .map(IWiktionaryEntry::getWord)
//            .collect(Collectors.toList());

        System.out.println("English nouns containing markers for '" + markerName + "': " + words.size());

        Writer w = new FileWriter("out/wiktionary-" + markerName + ".txt");

        words.forEach(word -> {
            try {
                w.write(word);
                w.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        w.close();

        System.out.println(new Date());

        wkt.close();
    }

    private static boolean containsAny(String haystack, String[] needles) {
        return Arrays.stream(needles).parallel().anyMatch(haystack::contains);
    }
}
