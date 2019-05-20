package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;

import java.io.*;

public class Rules {
    public static void main(String[] args) throws IOException, JWNLException {
        // Find dangerous characters with this regex:
        // [^()<>|\w $\\\-\ä\ü\ö']

        createAndSaveAll();
    }

    private static void createAndSaveAll() throws IOException, JWNLException {
        save("actor", actor());
        save("situation", situation());
        save("condition", condition());
        save("modality", modality());
        save("reason", reason());
        save("time", time());
        save("location", location());
        save("artifact", artifact());
    }

    private static String actor() {
        var markers = Markers.actor();
        return TRegex.ruleFromMarkers("(NP < (", markers,"))");
    }

    private static String artifact() {
        var markers = Markers.artifact();
        return TRegex.ruleFromMarkers("(NP < (", markers, "))");
    }

    private static String condition() {
        var markers = Markers.condition();

        var rule1 = TRegex.ruleFromMarkers("(PP << (", markers,"))");
        var rule2 = TRegex.ruleFromMarkers("(SBAR < (", markers,"))"); // TODO: SBAR == Ssub?
        var rule3 = TRegex.ruleFromMarkers("(NP < (VP < (TO $  VB < (", markers,"))))");

        return rule3;
    }

    private static String location() {
        var markers = Markers.location();
        return TRegex.ruleFromMarkers("(NP < (", markers, "))");
    }

    private static String modality() {
        var markers = Markers.modality();
        return TRegex.ruleFromMarkers("(VN < (", markers,"))");
    }

    private static String reason() {
        var markers = Markers.reason();
        return TRegex.ruleFromMarkers("(PP < (", markers,"))");
    }

    private static String situation() {
        var markers = Markers.situation();
        return TRegex.ruleFromMarkers("(NP < (", markers, "))");
    }

    private static String time() throws JWNLException {
        var markers = Markers.time();

        String rule1 = TRegex.ruleFromMarkers("(NP < (", markers, "))");
        // TODO: This rule has not generated any matches yet. Investigate!
        String rule2 = TRegex.ruleFromMarkers("(PP < (P < (", markers, ")) $ NP)");

        return rule1;
    }

    private static void save(String name, String rule) throws IOException {
        File file = new File("resources/rules/" + name + ".txt");
        file.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(rule);
        writer.close();
    }
}
