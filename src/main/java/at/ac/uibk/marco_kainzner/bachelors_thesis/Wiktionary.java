package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech;
import de.tudarmstadt.ukp.jwktl.api.filter.WiktionarySenseFilter;
import de.tudarmstadt.ukp.jwktl.api.util.Language;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Wiktionary {
    private final static String DUMP_PATH = "resources\\db\\wiktionary\\enwiktionary-20190320-pages-articles.xml.bz2";
    private final static String DB_DIR = "resources\\db\\wiktionary\\berkeley-db";

    private final static String[] ACTOR_IDENTIFIERS = { " person ", " organization ", " body " };
    private final static String[] SITUATION_IDENTIFIERS = { " act ", " acts ", " acting ", " action ", " actions " };

    public static void main(String[] args) throws IOException {
        saveMarkers("situation", PartOfSpeech.NOUN, SITUATION_IDENTIFIERS);
    }

    public static void parse(String pathIn, String pathOut) {
        File dumpFile = new File(pathIn);
        File outputDirectory = new File(pathOut);

        JWKTL.parseWiktionaryDump(dumpFile, outputDirectory, true);
    }

    private static void saveMarkers(String markerName, PartOfSpeech pos, String[] identifiers) throws IOException {
        log("Wiktionary: Looking for '" +  markerName + "' markers ...");
        File wiktionaryDirectory = new File(DB_DIR);
        IWiktionaryEdition wkt = JWKTL.openEdition(wiktionaryDirectory);

        WiktionarySenseFilter filter = new WiktionarySenseFilter();
        filter.setAllowedWordLanguages(Language.ENGLISH);
        filter.setAllowedPartsOfSpeech(pos);

        System.out.println(new Date());

        Stream<IWiktionarySense> senses = StreamSupport.stream(wkt.getAllSenses(filter).spliterator(), false);

        Set<String> words = new HashSet<>();
        senses.forEach(sense -> {
            if (containsAny(sense.getGloss().getPlainText(), identifiers)) {
                words.add(sense.getEntry().getWord());
            }
        });

        log("Wiktionary: Entries containing markers for '" + markerName + "': " + words.size());
        log("Wiktionary: Writing to file ...");

        Writer w = new FileWriter("out/wiktionary-markers_" + markerName + ".txt");

        words.forEach(word -> {
            try {
                w.write(word);
                w.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        w.close();
        log("Wiktionary: Done writing");

        wkt.close();
        log("Wiktionary: Done");
    }

    private static void log(String msg) {
        System.out.println(new Date() + ": " + msg);
    }

    private static boolean containsAny(String haystack, String[] needles) {
        return Arrays.stream(needles).parallel().anyMatch(haystack::contains);
    }
}
