package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static at.ac.uibk.marco_kainzner.bachelors_thesis.TRegex.any;
import static at.ac.uibk.marco_kainzner.bachelors_thesis.TRegex.ruleFromMarkers;

public class Rules {
    private static final String V_PART = "(VP <, VBG)";
    private static final String VP_INF = "(VP < (TO $ (__ << VB)))";
    private static final String S_REL = "SBAR <<, (WDT < who|which|whom|that|where|why|when)";

    public static void main(String[] args) throws IOException, JWNLException {
        getAndSaveAll();
    }

    static void getAndSaveAll() throws IOException, JWNLException {
        getAll().forEach(Rules::save);
    }

    /// Returns a list of pairs where first elements are names of the rules and seconds elements are the rule strings
    static List<Rule> getAll() throws IOException, JWNLException {
        var rules = new ArrayList<Rule>();

        rules.add(actor());
        rules.add(artifact());
        rules.add(condition());
        rules.add(exception());
        rules.add(location());
        rules.add(modality());
        rules.add(reason());
        rules.add(situation());
        rules.add(sanction());
        rules.add(time());
        rules.add(violation());

        return rules;
    }

    static Rule exception() {
        var markers = Markers.exception();

        var ruleSrel = createSrelRule(markers);
        var ruleVPart = createVPartRule(markers);
        var ruleVPinf = createVPinfRule(markers);
        var ruleSsub = ruleFromMarkers("(SBAR << (", markers,"))");
        var rulePP = ruleFromMarkers("(PP << (", markers,"))");

        var ruleStr =  any(Stream.of(ruleSrel, ruleVPart, ruleVPinf, ruleSsub, rulePP));
        return new Rule("exception", ruleStr, null);
    }

    static Rule actor() throws IOException {
        var markers = Markers.actor();
        var ruleStr = "NP < (__" + ruleFromMarkers(" < ", markers,"") + ")";

        return new Rule("actor", ruleStr, null);
    }

    static Rule artifact() throws JWNLException, IOException {
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

        var ruleStr = or(ruleNP, ruleNothingElseMatches);
        return new Rule("artifact", ruleStr, null);
    }

    static Rule condition() {
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

        var ruleStr = any(Stream.of(ruleSrel, ruleVPinfAndNoBadMarkers, ruleVpartAndNoBadMarkers ,rulePP, ruleSsub));
        return new Rule("condition", ruleStr, null);
    }

    static Rule location() {
        var markers = Markers.location();
        var ruleStr = ruleFromMarkers("(NP < (", markers, "))");

        return new Rule("location", ruleStr, null);
    }

    static Rule modality() {
        var markers = Markers.modality();
        var ruleStr = ruleFromMarkers("(VN < (", markers,"))");

        return new Rule("modality", ruleStr, null);
    }

    static Rule reason() {
        var markers = Markers.reason();

        // TODO: Test SBAR and VPart extensively
        var ruleSrel = createSrelRule(markers);
        var rulePP = ruleFromMarkers("(PP < (", markers,"))");
        var ruleSsub = ruleFromMarkers("(SBAR << (", markers, "))"); // Suspicious match
        var ruleVPart = createVPartRule(markers);
        var ruleVPinf = createVPinfRule(markers);

        var ruleStr = any(Stream.of(ruleSrel, rulePP, ruleVPinf, ruleSsub, ruleVPart));
        return new Rule("reason", ruleSrel, null);
    }

    static Rule sanction() {
        var markers = Markers.sanction();
        var ruleStr = ruleFromMarkers("(NP < (", markers, "))");

        return new Rule("sanction", ruleStr, null);
    }

    static Rule situation() {
        var markers = Markers.situation();
        var ruleStr = ruleFromMarkers("(NP < (", markers, "))");

        return new Rule("situation", ruleStr, null);
    }

    static Rule time() throws JWNLException {
        var markers = Markers.time();

        String ruleNP = ruleFromMarkers("(NP < (", markers, "))");
        // TODO: This rule has not generated any matches yet. Investigate!
        String rulePP = ruleFromMarkers("(PP < (P < (", markers, ")) $ NP)");

        var ruleStr = or(ruleNP, rulePP);

        return new Rule("time", ruleStr, null);
    }

    static Rule violation() {
        var markers = Markers.violation();
        var ruleStr = ruleFromMarkers("(NP < (", markers, "))");

        return new Rule("violation", ruleStr, null);
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

    // Save constituency part of a rule
    private static void save(Rule rule) {
        File file = new File("resources/rules/" + rule.name + ".txt");
        try {
            file.createNewFile();

            System.out.println("---");
            System.out.println(rule.name);
            System.out.println(rule);
            String ruleWithNewlines = rule.constituencyRule.replace("|(", "\n|(");

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(ruleWithNewlines);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
