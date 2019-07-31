package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static at.ac.uibk.marco_kainzner.bachelors_thesis.TregexRuleGenerator.any;
import static at.ac.uibk.marco_kainzner.bachelors_thesis.TregexRuleGenerator.ruleFromMarkers;

public class SemanticRuleGenerator {
    private static final String V_PART = "(VP <, VBG)";
    private static final String VP_INF = "(VP < (TO $ (__ << VB)))";
    private static final String S_REL = "SBAR <<, (WDT < who|which|whom|that|where|why|when)";

    public static void main(String[] args) throws IOException, JWNLException {
        getAndSaveAll();
    }

    static void getAndSaveAll() throws IOException, JWNLException {
        getAll().forEach(SemanticRuleGenerator::save);
    }

    /// Returns a list of pairs where first elements are names of the rules and seconds elements are the rule strings
    static List<SemanticRule> getAll() throws IOException, JWNLException {
        var rules = new ArrayList<SemanticRule>();

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

    static SemanticRule exception() {
        var markers = MarkerGenerator.exception();

        var ruleSrel = createSrelRule(markers);
        var ruleVPart = createVPartRule(markers);
        var ruleVPinf = createVPinfRule(markers);
        var ruleSsub = ruleFromMarkers("(SBAR << (", markers,"))");
        var rulePP = ruleFromMarkers("(PP << (", markers,"))");

        var ruleStr =  any(Stream.of(ruleSrel, ruleVPart, ruleVPinf, ruleSsub, rulePP));
        return new SemanticRule("exception", ruleStr, null);
    }

    static SemanticRule actor() throws IOException {
        var markers = MarkerGenerator.actor();
        var ruleStr = "NP < (__" + ruleFromMarkers(" < ", markers,"") + ")";

        return new SemanticRule("actor", ruleStr, null);
    }

    static SemanticRule artifact() throws JWNLException, IOException {
        var markers = MarkerGenerator.artifact();
        var ruleNP = ruleFromMarkers("(NP < (", markers, "))");

        Set<String> disallowedMarkers = new TreeSet<>();
        disallowedMarkers.addAll(MarkerGenerator.violation());
        disallowedMarkers.addAll(MarkerGenerator.time());
        disallowedMarkers.addAll(MarkerGenerator.situation());
        disallowedMarkers.addAll(MarkerGenerator.sanction());
        disallowedMarkers.addAll(MarkerGenerator.location());
        disallowedMarkers.addAll(MarkerGenerator.actor());

        var ruleNothingElseMatches = "NP " + TregexRuleGenerator.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "");

        var ruleStr = or(ruleNP, ruleNothingElseMatches);
        return new SemanticRule("artifact", ruleStr, null);
    }

    static SemanticRule condition() {
        var markers = MarkerGenerator.condition();

        Set<String> disallowedMarkers = new TreeSet<>();
        disallowedMarkers.addAll(MarkerGenerator.exception());
        disallowedMarkers.addAll(MarkerGenerator.reason());

        var ruleSrel = createSrelRule(markers);

        var VPinfAdjusted = "__ < " + VP_INF;
        var ruleVPinfAndNoBadMarkers = "(NP < (" + VPinfAdjusted + " " + TregexRuleGenerator.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "") + "))";;
        var ruleVpartAndNoBadMarkers = "(NP < (" + V_PART + " " + TregexRuleGenerator.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "") + "))";;
        var rulePP = ruleFromMarkers("(PP << (", markers,"))");
        var ruleSsub = ruleFromMarkers("(SBAR < (", markers,"))");

        var ruleStr = any(Stream.of(ruleSrel, ruleVPinfAndNoBadMarkers, ruleVpartAndNoBadMarkers ,rulePP, ruleSsub));
        return new SemanticRule("condition", ruleStr, null);
    }

    static SemanticRule location() {
        var markers = MarkerGenerator.location();
        var ruleStr = ruleFromMarkers("(NP < (", markers, "))");

        return new SemanticRule("location", ruleStr, null);
    }

    static SemanticRule modality() {
        var markers = MarkerGenerator.modality();
        var ruleStr = ruleFromMarkers("(VN < (", markers,"))");

        return new SemanticRule("modality", ruleStr, null);
    }

    static SemanticRule reason() {
        var markers = MarkerGenerator.reason();

        // TODO: Test SBAR and VPart extensively
        var ruleSrel = createSrelRule(markers);
        var rulePP = ruleFromMarkers("(PP < (", markers,"))");
        var ruleSsub = ruleFromMarkers("(SBAR << (", markers, "))"); // Suspicious match
        var ruleVPart = createVPartRule(markers);
        var ruleVPinf = createVPinfRule(markers);

        var ruleStr = any(Stream.of(ruleSrel, rulePP, ruleVPinf, ruleSsub, ruleVPart));
        return new SemanticRule("reason", ruleSrel, null);
    }

    static SemanticRule sanction() {
        var markers = MarkerGenerator.sanction();
        var ruleStr = ruleFromMarkers("(NP < (", markers, "))");

        return new SemanticRule("sanction", ruleStr, null);
    }

    static SemanticRule situation() {
        var markers = MarkerGenerator.situation();
        var ruleStr = ruleFromMarkers("(NP < (", markers, "))");

        return new SemanticRule("situation", ruleStr, null);
    }

    static SemanticRule time() throws JWNLException {
        var markers = MarkerGenerator.time();

        String ruleNP = ruleFromMarkers("(NP < (", markers, "))");
        // TODO: This rule has not generated any matches yet. Investigate!
        String rulePP = ruleFromMarkers("(PP < (P < (", markers, ")) $ NP)");

        var ruleStr = or(ruleNP, rulePP);

        return new SemanticRule("time", ruleStr, null);
    }

    static SemanticRule violation() {
        var markers = MarkerGenerator.violation();
        var ruleStr = ruleFromMarkers("(NP < (", markers, "))");

        return new SemanticRule("violation", ruleStr, null);
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
        var markersWithoutTO = MarkerGenerator.removeTO(markers);
        var VPinfExtended = "(__ << " + VP_INF + ")";

        return ruleFromMarkers("((" + VPinfExtended + " $ (__ < ", markersWithoutTO, ")) > __) >> NP");
    }

    // Save constituency part of a rule
    private static void save(SemanticRule rule) {
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
