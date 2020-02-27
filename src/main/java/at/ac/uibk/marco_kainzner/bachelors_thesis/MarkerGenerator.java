package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.Synset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkerGenerator {
    private static final String resourceDir = "resources";

    static Set<String> actor() throws IOException {
        Set<String> markers = new TreeSet<>();

        markers.addAll(WordNet.getAllHyponyms(WordNet.getSynset("body%1:14:00::")));
        markers.addAll(WordNet.getAllHyponyms(WordNet.getSynset("organisation%1:14:00::")));
        markers.addAll(WordNet.getPersonsWithoutNames());

        var path = Paths.get(resourceDir, "markers-auto/actor.txt");
        toFile(path, markers);

        return markers;
    }

    static Set<String> artifact() {
        Synset syn = WordNet.getSynset("artifact%1:03:00::");

        Set<String> markers = WordNet.getAllHyponyms(syn);
        markers.addAll(MarkerGenerator.fromFile(Paths.get(resourceDir, "markers-manual", "artifact.txt")));

        return markers;
    }

    static Set<String> condition() {
        return manual("condition");
    }

    static Set<String> location() {
        return manual("location_fibo");
    }

    static Set<String> modality() {
        return manual("modality");
    }

    static Set<String> reason() {
        return manual("reason");
    }

    public static Set<String> sanction() {
        return manual("sanction_paper");
    }

    static Set<String> situation() {
        return manual("situation_wiktionary");
    }

    static Set<String> exception() {
        return manual("exception");
    }

    static Set<String> time() throws JWNLException, IOException {
        Synset synUnitOfTime = WordNet.getSynset("unit_of_time%1:28:00::");
        Synset synPeriod = WordNet.getSynset("time_period%1:28:00::");
        Synset synTemporarily = WordNet.getSynset("temporarily%4:02:00::");

        Set<String> unitsOfTime = WordNet.getAllHyponyms(synUnitOfTime);
        Set<String> temporarily = WordNet.getWords(synTemporarily);
        Set<String> temporary = WordNet.getAdjectivesAndAntonyms(synTemporarily);
        Set<String> periodHyponyms = WordNet.getAllHyponyms(synPeriod);

        Set<String> markers = MarkerGenerator.manual("time");
        markers.addAll(unitsOfTime);
        markers.addAll(temporarily);
        markers.addAll(temporary);
        markers.addAll(periodHyponyms);

        toFile(Path.of( resourceDir, "markers-auto", "time_wordnet.txt"), markers);

        return markers;
    }

    static Set<String> violation() {
        return manual("violation_paper");
    }

    private static Set<String> manual(String fileName) {
        var path = Paths.get(resourceDir, "markers-manual", fileName + ".txt");
        return fromFile(path);
    }

    private static void toFile(Path path, Set<String> markers) throws IOException {
        Files.write(path, markers, Charset.defaultCharset());
    }

    private static Set<String> fromFile(Path path) {
        try {
            return FileUtils.readLines(new File(path.toUri()), Charset.defaultCharset())
                    .stream()
                    .map(String::trim)
                    .flatMap(marker -> Stream.of(marker, StringUtils.capitalize(marker))) // Transform first letter to uppercase
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
            return new TreeSet<>();
        }
    }

    static Set<String> removeTO(Set<String> markers) {
        return markers.stream()
                .map(marker -> marker.replace(" to", ""))
                .map(String::trim)
                .filter(marker -> !marker.isEmpty())
                .collect(Collectors.toSet());
    }
}
