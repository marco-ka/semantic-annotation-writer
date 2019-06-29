package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.uibk.marco_kainzner.bachelors_thesis.TRegex.*;

public class Rules {
    private static String VP_INF = "(VP < (TO $ (__ << VB)))";

    public static void main(String[] args) throws IOException, JWNLException {
        // Find dangerous characters with this regex: [^()<>|\w $\\\-\ä\ü\ö']
        createAndSaveAll();
    }

    private static void createAndSaveAll() throws IOException, JWNLException {
        save("actor", actor());
        save("artifact", artifact());
        save("condition", condition());
        save("exception", exception());
        save("location", location());
        save("modality", modality());
        save("reason", reason());
        save("situation", situation());
        save("sanction", sanction());
        save("time", time());
        save("violation", violation());
    }

    private static String exception() {
        var markers = Markers.exception();

        var rulePP = ruleFromMarkers("(PP << (", markers,"))");
        var ruleVPart = ruleFromMarkers("(NP < (VP <1 VBG) << (", markers,"))"); // TODO: Only 2 matches in 2013_10
        var ruleSsub = ruleFromMarkers("(SBAR << (", markers,"))");

        // TODO: Check alternative rules for VPinf:
        // Alternative example with better braces but probably not quite right yet: `(that . intend) . (__ << (VP < (TO $ (__ << VB))))`
        // Proven rule: `(does . (not . (need . (VP < (TO $ (__ << VB))))))` (2 matches in fffs_2009_03)
        // Almost there: (__ < (does . (not . (need)))) $ (VP << (TO $ (__ << VB)))

        var markersWithoutTO = Markers.removeTO(markers);

        var ruleVPinf1 = any(TRegex.preprocess(markersWithoutTO).stream() // This rule is probably wrong. "does not need to" should not be a marker
                .map(marker -> new ArrayList<>(Arrays.asList(marker.split(" "))))
                .peek(marker -> marker.add(VP_INF))
                .map(TRegex::mergeWords));

        System.out.println(ruleVPinf1);

        return any(Stream.of(ruleVPinf1, rulePP, ruleSsub, ruleVPart));
    }

    private static String actor() throws IOException {
        var markers = Markers.actor();
        return ruleFromMarkers("(NP < (", markers,"))");
    }

    private static String artifact() throws JWNLException, IOException {
        var markers = Markers.artifact();
        var ruleNP = ruleFromMarkers("(NP < (", markers, "))");

        Set<String> disallowedMarkers = new TreeSet<>();
        disallowedMarkers.addAll(Markers.violation());
        disallowedMarkers.addAll(Markers.time());
        disallowedMarkers.addAll(Markers.situation());
        disallowedMarkers.addAll(Markers.sanction());
        disallowedMarkers.addAll(Markers.location());
        disallowedMarkers.addAll(Markers.actor());

        var ruleNothingElseMatches = "NP " + TRegex.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "");
        System.out.println(ruleNothingElseMatches);

        return or(ruleNP, ruleNothingElseMatches);
    }

    private static String condition() {
        var markers = Markers.condition();

        var rulePP = ruleFromMarkers("(PP << (", markers,"))");
        var ruleSsub = ruleFromMarkers("(SBAR < (", markers,"))"); // TODO: SBAR == Ssub?

        return or(rulePP, ruleSsub);
    }

    private static String location() {
        var markers = Markers.location();
        return ruleFromMarkers("(NP < (", markers, "))");
    }

    private static String modality() {
        var markers = Markers.modality();
        return ruleFromMarkers("(VN < (", markers,"))");
    }

    private static String reason() {
        var markers = Markers.reason();

        // (in . (order . (TO . VB)))
        // var VP_INF = "(VP < (TO . VB))";

        var VPinfExtended = "(__ << " + VP_INF + ")";

        // var exampleInOrderTo = "(__ < ((IN < in) $ (NN < order) $ (__ << (VP < (TO $ (__ << VB))))))";
        var markersWithoutTO = markers.stream()
                .map(marker -> marker.replace(" to", ""))
                .map(String::trim)
                .filter(marker -> !marker.isEmpty())
                .collect(Collectors.toSet());

        markers.forEach(marker -> System.out.println("Marker: " + marker));

        // TODO: Test SBAR and VPart extensively
        var rulePP    = ruleFromMarkers("(PP < (", markers,"))");
        var ruleSsub  = ruleFromMarkers("(SBAR << (", markers, "))"); // Suspicious match
        var ruleVPart = ruleFromMarkers("(NP < (VP <1 VBG) << (", markers, "))"); // No match
        var ruleVPinf = ruleFromMarkers("(__ < (", markersWithoutTO, " $ " + VPinfExtended + "))"); // Modified to make less strict

        return any(Stream.of(rulePP, ruleSsub, ruleVPart, ruleVPinf));
    }

    private static String sanction() {
        var markers = Markers.sanction();
        return ruleFromMarkers("(NP < (", markers, "))");
    }

    private static String situation() {
        var markers = Markers.situation();
        return ruleFromMarkers("(NP < (", markers, "))");
    }

    private static String time() throws JWNLException {
        var markers = Markers.time();

        String ruleNP = ruleFromMarkers("(NP < (", markers, "))");
        // TODO: This rule has not generated any matches yet. Investigate!
        String rulePP = ruleFromMarkers("(PP < (P < (", markers, ")) $ NP)");

        return or(ruleNP, rulePP);
    }

    private static String violation() {
        var markers = Markers.violation();
        return ruleFromMarkers("(NP < (", markers, "))");
    }

    private static String or(String rule1, String rule2) {
        return rule1 + "|" + rule2;
    }

    private static String and(String rule1, String rule2) {
        return rule1 + "&" + rule2;
    }

    private static void save(String name, String rule) throws IOException {
        File file = new File("resources/rules/" + name + ".txt");
        file.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(rule);
        writer.close();
    }
}
