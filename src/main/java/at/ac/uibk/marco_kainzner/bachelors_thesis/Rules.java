package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.berkeley.nlp.util.Pair;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static at.ac.uibk.marco_kainzner.bachelors_thesis.TRegex.*;

public class Rules {
    private static final String V_PART = "(VP <, VBG)";
    private static final String VP_INF = "(VP < (TO $ (__ << VB)))";
    private static final String S_REL = "SBAR <<, (WDT < who|which|whom|that|where|why|when)";

    public static void main(String[] args) throws IOException, JWNLException {
        createAndSaveAll();
    }

    static void createAndSaveAll() throws IOException, JWNLException {
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

    /// Returns a list of pairs where first elements are names of the rules and seconds elements are the rule strings
    static List<Pair<String, String>> getAll() throws IOException, JWNLException {
        var rules = new ArrayList<Pair<String, String>>();

        rules.add(new Pair<>("actor", actor()));
        rules.add(new Pair<>("artifact", artifact()));
        rules.add(new Pair<>("condition", condition()));
        rules.add(new Pair<>("exception", exception()));
        rules.add(new Pair<>("location", location()));
        rules.add(new Pair<>("modality", modality()));
        rules.add(new Pair<>("reason", reason()));
        rules.add(new Pair<>("situation", situation()));
        rules.add(new Pair<>("sanction", sanction()));
        rules.add(new Pair<>("time", time()));
        rules.add(new Pair<>("violation", violation()));

        return rules;
    }

    static String exception() {
        var markers = Markers.exception();

        var ruleSrel = createSrelRule(markers);
        var ruleVPart = createVPartRule(markers);
        var ruleVPinf = createVPinfRule(markers);
        var ruleSsub = ruleFromMarkers("(SBAR << (", markers,"))");
        var rulePP = ruleFromMarkers("(PP << (", markers,"))");

       return any(Stream.of(ruleSrel, ruleVPart, ruleVPinf, ruleSsub, rulePP));
    }

    static String actor() throws IOException {
        var markers = Markers.actor();
        return "NP < (__" + ruleFromMarkers(" < ", markers,"") + ")";
    }

    static String artifact() throws JWNLException, IOException {
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

        return or(ruleNP, ruleNothingElseMatches);
    }

    static String condition() {
        var markers = Markers.condition();

        Set<String> disallowedMarkers = new TreeSet<>();
        disallowedMarkers.addAll(Markers.exception());
        disallowedMarkers.addAll(Markers.reason());

        var ruleSrel = createSrelRule(markers);

        var VPinfAdjusted = "__ < " + VP_INF;
        var ruleVPinfAndNoBadMarkers = "(NP < (" + VPinfAdjusted + " " + TRegex.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "") + "))";;
        var ruleVpartAndNoBadMarkers = "(NP < (" + V_PART + " " + TRegex.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "") + "))";;
        var rulePP = ruleFromMarkers("(PP << (", markers,"))");
        var ruleSsub = ruleFromMarkers("(SBAR < (", markers,"))");

        return any(Stream.of(ruleSrel, ruleVPinfAndNoBadMarkers, ruleVpartAndNoBadMarkers ,rulePP, ruleSsub));
    }

    static String location() {
        var markers = Markers.location();
        return ruleFromMarkers("(NP < (", markers, "))");
    }

    static String modality() {
        var markers = Markers.modality();
        return ruleFromMarkers("(VN < (", markers,"))");
    }

    static String reason() {
        var markers = Markers.reason();

        // TODO: Test SBAR and VPart extensively
        var ruleSrel = createSrelRule(markers);
        var rulePP = ruleFromMarkers("(PP < (", markers,"))");
        var ruleSsub = ruleFromMarkers("(SBAR << (", markers, "))"); // Suspicious match
        var ruleVPart = createVPartRule(markers);
        var ruleVPinf = createVPinfRule(markers);

        return any(Stream.of(ruleSrel, rulePP, ruleVPinf, ruleSsub, ruleVPart));
    }

    static String sanction() {
        var markers = Markers.sanction();
        return ruleFromMarkers("(NP < (", markers, "))");
    }

    static String situation() {
        var markers = Markers.situation();
        return ruleFromMarkers("(NP < (", markers, "))");
    }

    static String time() throws JWNLException {
        var markers = Markers.time();

        String ruleNP = ruleFromMarkers("(NP < (", markers, "))");
        // TODO: This rule has not generated any matches yet. Investigate!
        String rulePP = ruleFromMarkers("(PP < (P < (", markers, ")) $ NP)");

        return or(ruleNP, rulePP);
    }

    static String violation() {
        var markers = Markers.violation();
        return ruleFromMarkers("(NP < (", markers, "))");
    }

    private static String or(String rule1, String rule2) {
        return rule1 + "|" + rule2;
    }

    private static String and(String rule1, String rule2) {
        return rule1 + "&" + rule2;
    }

    private static String createSrelRule(Set<String> markers) {
        return "(" + S_REL + "(" + ruleFromMarkers("<< ", markers, "") + "))";
    }

    private static String createVPartRule(Set<String> markers) {
        return ruleFromMarkers("NP < (" + V_PART + " << (", markers, "))");
    }

    private static String createVPinfRule(Set<String> markers) {
        // See https://trello.com/c/DwCEANSr/52-needs-adjustment-reason-rule
        var markersWithoutTO = Markers.removeTO(markers);
        var VPinfExtended = "(__ << " + VP_INF + ")";

        return ruleFromMarkers("((" + VPinfExtended + " $ (__ < ", markersWithoutTO, ")) > __) >> NP");
    }

    private static void save(String name, String rule) throws IOException {
        File file = new File("resources/rules/" + name + ".txt");
        file.createNewFile();

        System.out.println("---");
        System.out.println(name);
        System.out.println(rule);
        String ruleWithNewlines = rule.replace("|(", "\n|(");

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(ruleWithNewlines);
        writer.close();
    }
}
