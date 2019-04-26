package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech;
import de.tudarmstadt.ukp.jwktl.api.filter.WiktionaryEntryFilter;
import de.tudarmstadt.ukp.jwktl.api.util.Language;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Wiktionary {
    final static String DUMP_PATH = "resources\\wiktionary\\enwiktionary-20190320-pages-articles.xml.bz2";
    final static String DB_DIR = "resources\\wiktionary\\berkeley-db";

    final static String[] ACTOR_IDENTIFIERS = { " person ", " organization ", " body " };
    final static String[] SITUATION_IDENTIFIERS = { " act ", " action " };

    public static void main(String[] args) throws IOException {
        query();
    }

    public static void parse(String pathIn, String pathOut) {
        File dumpFile = new File(pathIn);
        File outputDirectory = new File(pathOut);

        JWKTL.parseWiktionaryDump(dumpFile, outputDirectory, true);
    }

    public static void query() throws IOException {
        System.out.println(new Date());
        File wiktionaryDirectory = new File(DB_DIR);
        IWiktionaryEdition wkt = JWKTL.openEdition(wiktionaryDirectory);

        WiktionaryEntryFilter filter = new WiktionaryEntryFilter();
        filter.setAllowedWordLanguages(Language.ENGLISH);
        filter.setAllowedPartsOfSpeech(PartOfSpeech.NOUN);

        System.out.println(new Date());


        Stream<IWiktionaryEntry> entries = StreamSupport.stream(wkt.getAllEntries(filter).spliterator(), false);

        List<String> words = entries
//            .limit(10000)
            .filter(entry -> {
                boolean containsAct = false;
                for (IWiktionarySense sense : entry.getSenses()) {
                    String definition = sense.getGloss().getPlainText();
                    if (containsAny(definition, ACTOR_IDENTIFIERS)) {

//                        System.out.println(entry.getWord());
//                        System.out.println(definition);
//                        System.out.println();
                        containsAct = true;
                        break;
                    }
                }
                return containsAct;
            })
            .map(entry -> entry.getWord())
            .collect(Collectors.toList());

        System.out.println("English nouns containing actor markers: " + words.size());

        Writer w = new FileWriter("out/wiktionary.txt");

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
