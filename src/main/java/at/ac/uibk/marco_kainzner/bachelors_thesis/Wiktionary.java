package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEdition;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech;
import de.tudarmstadt.ukp.jwktl.api.filter.WiktionaryEntryFilter;
import de.tudarmstadt.ukp.jwktl.api.util.Language;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Wiktionary {
    final static String DUMP_PATH = "C:\\Users\\Marco\\Documents\\Projects\\dkpro-pipeline\\src\\main\\resources\\wiktionary\\enwiktionary-20190320-pages-articles.xml.bz2";
    final static String DB_DIR = "C:\\Users\\Marco\\Documents\\Projects\\dkpro-pipeline\\src\\main\\resources\\wiktionary\\berkeley-db";

    final static String[] ACTOR_IDENTIFIERS = { " person ", " organization ", " body " };
    final static String[] SITUATION_IDENTIFIERS = { " act ", " action " };

    public static void main(String[] args) {
        query();
    }

    public static void parse(String pathIn, String pathOut) {
        File dumpFile = new File(pathIn);
        File outputDirectory = new File(pathOut);

        JWKTL.parseWiktionaryDump(dumpFile, outputDirectory, true);
    }

    public static void query() {
        System.out.println(new Date());
        File wiktionaryDirectory = new File(DB_DIR);
        IWiktionaryEdition wkt = JWKTL.openEdition(wiktionaryDirectory);

        WiktionaryEntryFilter filter = new WiktionaryEntryFilter();
        filter.setAllowedWordLanguages(Language.ENGLISH);
        filter.setAllowedPartsOfSpeech(PartOfSpeech.NOUN);

        System.out.println(new Date());

        Stream<IWiktionaryEntry> entries = StreamSupport.stream(wkt.getAllEntries(filter).spliterator(), false);
        entries = entries
            .limit(2000)
            .filter(entry -> {
                boolean containsAct = false;
                for (IWiktionarySense sense : entry.getSenses()) {
                    String definition = sense.getGloss().getPlainText();
                    if (containsAny(definition, ACTOR_IDENTIFIERS)) {
                        System.out.println(entry.getWord());
                        System.out.println(definition);
                        System.out.println();
                        containsAct = true;
                        break;
                    }
                }
                return containsAct;
            });
        System.out.println("English nouns containing \" act\": " + entries.count());

        System.out.println(new Date());

        wkt.close();
    }

    private static boolean containsAny(String haystack, String[] needles) {
        return Arrays.stream(needles).parallel().anyMatch(haystack::contains);
    }
}
