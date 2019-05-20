package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Rules {
    public static void main(String[] args) throws IOException, JWNLException {
        // Find dangerous characters with this regex:
        // [^()<>|\w $\\\-\ä\ü\ö']

        var actorRule = createActorRule();
        saveRule("actor", actorRule);

        var situationRule = createSituationRule();
        saveRule("situation", situationRule);

        var conditionRule = createConditionRule();
        saveRule("condition", conditionRule);

        var modalityRule = createModalityRule();
        saveRule("modality", modalityRule);

        var reasonRule = createReasonRule();
        saveRule("reason", reasonRule);

        var timeRule = createTimeRule();
        saveRule("time", timeRule);

        var locationRule = createLocationRule();
        saveRule("location", locationRule);

        var artifactRule = createArtifactRule();
        saveRule("artifact", artifactRule);
    }

    private static String createActorRule() {
        Set<String> markers = new TreeSet<>();
        markers.addAll(WordNet.getAllHyponyms(WordNet.getSynset("body%1:14:00::")));
        markers.addAll(WordNet.getAllHyponyms(WordNet.getSynset("organisation%1:14:00::")));
        markers.addAll(WordNet.getPersonsWithoutNames());

        return TRegex.createRuleFromMarkers("(NP < (", markers,"))");
    }

    private static String createSituationRule() {
        var markers = Markers.fromFile("resources/markers-manual/wiktionary-situation.txt");
        return TRegex.createRuleFromMarkers("(NP < (", markers, "))");
    }

    private static String createConditionRule() {
        var markers = Markers.fromFile("resources/markers-manual/condition.txt");
        var rule1 = TRegex.createRuleFromMarkers("(PP << (", markers,"))");
        var rule2 = TRegex.createRuleFromMarkers("(SBAR < (", markers,"))"); // TODO: SBAR == Ssub?
        var rule3 = TRegex.createRuleFromMarkers("(NP < (VP < (TO $  VB < (", markers,"))))"); //

        return rule3;
    }

    private static String createModalityRule() {
        var markers = Markers.fromFile("resources/markers-manual/modality.txt");

        return TRegex.createRuleFromMarkers("(VN < (", markers,"))");
    }

    private static String createReasonRule() {
        var markers = Markers.fromFile("resources/markers-manual/reason.txt");

        return TRegex.createRuleFromMarkers("(PP < (", markers,"))");
    }


    private static String createTimeRule() throws JWNLException, IOException {
        Synset temporarily = WordNet.getSynset("temporarily%4:02:00::");
        Synset period = WordNet.getSynset("time_period%1:28:00::");

        List<String> manualMarkers = FileUtils.readLines(new File("resources/markers-manual/time.txt"), Charset.defaultCharset());

        Set<String> temporary = WordNet.getAdjectivesAndAntonyms(temporarily);
        Set<String> periodHyponyms = WordNet.getAllHyponyms(period);

        Set<String> markers = new TreeSet<>(manualMarkers);
        markers.addAll(temporary);
        // markers.addAll(periodHyponyms);

        String rule1 = TRegex.createRuleFromMarkers("(NP < (", markers, "))");

        // TODO: This rule has not generated any matches yet. Investigate!
        String rule2 = TRegex.createRuleFromMarkers("(PP < (P < (", markers, ")) $ NP)");

        return rule1;
    }

    private static String createLocationRule() {
        Set<String> markers = Markers.fromFile("resources/markers-manual/location.txt");
        return TRegex.createRuleFromMarkers("(NP < (", markers, "))");
    }

    private static String createArtifactRule() throws JWNLException {
        Synset syn = WordNet.getSynset("artifact%1:03:00::");
        Set<String> markers = WordNet.getAllHyponyms(syn);
        markers.addAll(Markers.fromFile("resources/markers-manual/artifact.txt"));

        return TRegex.createRuleFromMarkers("(NP < (", markers, "))");
    }

    private static void saveRule(String name, String rule) throws IOException {
        File file = new File("resources/rules/auto_" + name + ".txt");
        file.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(rule);
        writer.close();
    }
}
