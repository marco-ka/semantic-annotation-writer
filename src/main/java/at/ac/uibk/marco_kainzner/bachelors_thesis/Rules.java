package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.uibk.marco_kainzner.bachelors_thesis.TRegex.*;

public class Rules {
    private static String anyTag = "__";

    public static void main(String[] args) throws IOException, JWNLException {
        // Find dangerous characters with this regex:
        // [^()<>|\w $\\\-\ä\ü\ö']

        createAndSaveAll();
    }

    private static void createAndSaveAll() throws IOException, JWNLException {
//        save("actor", actor());
//        save("situation", situation());
//        save("condition", condition());
//        save("modality", modality());
        save("reason", reason());
//        save("time", time());
//        save("location", location());
//        save("artifact", artifact());
    }

    private static String actor() {
        var markers = Markers.actor();
        return ruleFromMarkers("(NP < (", anyTag, markers,"))");
    }

    private static String artifact() {
        var markers = Markers.artifact();
        return ruleFromMarkers("(NP < (", anyTag, markers, "))");
    }

    private static String condition() {
        var markers = Markers.condition();

        var rule1 = ruleFromMarkers("(PP << (", anyTag, markers,"))");
        var rule2 = ruleFromMarkers("(SBAR < (", anyTag, markers,"))"); // TODO: SBAR == Ssub?

        return or(rule1, rule2);
    }

    private static String location() {
        var markers = Markers.location();
        return ruleFromMarkers("(NP < (", anyTag, markers, "))");
    }

    private static String modality() {
        var markers = Markers.modality();
        return ruleFromMarkers("(VN < (", anyTag, markers,"))");
    }

    private static String reason() {
        var markers = Markers.reason();

        // (in . (order . (TO . VB)))
        // var VPinf = "(VP < (TO . VB))";

        var VPinf = "(VP < (TO $ (__ << VB)))";
        var VPinfExtended = "(__ << " + VPinf + ")";

        // var exampleInOrderTo = "(__ < ((IN < in) $ (NN < order) $ (__ << (VP < (TO $ (__ << VB))))))";
        var markersWithoutTO = markers.stream()
                .map(marker -> marker.replace(" to", ""))
                .map(String::trim)
                .filter(marker -> !marker.isEmpty())
                .collect(Collectors.toSet());

        markers.forEach(marker -> System.out.println("Marker: " + marker));

        // TODO: Test SBAR and VPart extensively
        var rulePP    = ruleFromMarkers("(PP < (", anyTag, markers,"))");
        var ruleSBAR  = ruleFromMarkers("(SBAR << (", anyTag, markers, "))"); // Suspicious match
        var ruleVPart = ruleFromMarkers("(NP < (VP <1 VBG) << (", anyTag, markers, "))"); // No match
        var ruleVPinf = ruleFromMarkers("(__ < (", anyTag, markersWithoutTO, " $ " + VPinfExtended + "))"); // Modified to make less strict

        return any(Stream.of(rulePP, ruleSBAR, ruleVPart, ruleVPinf));
    }

    private static String situation() {
        var markers = Markers.situation();
        return ruleFromMarkers("(NP < (", anyTag, markers, "))");
    }

    private static String time() throws JWNLException {
        var markers = Markers.time();

        String rule1 = ruleFromMarkers("(NP < (", anyTag, markers, "))");
        // TODO: This rule has not generated any matches yet. Investigate!
        String rule2 = ruleFromMarkers("(PP < (P < (", anyTag, markers, ")) $ NP)");

        return or(rule1, rule2);
    }

    private static String or(String rule1, String rule2) {
        return rule1 + "|" + rule2;
    }

    private static void save(String name, String rule) throws IOException {
        File file = new File("resources/rules/" + name + ".txt");
        file.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(rule);
        writer.close();
    }
}
