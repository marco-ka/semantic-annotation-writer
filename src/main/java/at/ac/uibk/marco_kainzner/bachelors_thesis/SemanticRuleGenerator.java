package at.ac.uibk.marco_kainzner.bachelors_thesis;

import net.sf.extjwnl.JWNLException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static at.ac.uibk.marco_kainzner.bachelors_thesis.TregexRuleGenerator.ruleFromMarkers;

public class SemanticRuleGenerator {
    private static final String V_PART = "(VP <, VBG)";
    private static final String VP_INF = "(VP < (TO $ (__ << VB)))";
    private static final String S_REL = "SBAR <<, (WDT < who|which|whom|that|where|why|when)";

    public static final Map<String, String> conceptMap = Map.of(
            // Actor handled by separate replacement
            "Constraint:Condition:Violation", "Violation",
            "Constraint:Exception", "Exception",
            "Constraint:Condition", "Condition",
            "Situation:Result", "Situation",
            "Situation:Result:Sanction", "Sanction"
    );

    public static void main(String[] args) throws JWNLException, IOException {
        getAndSaveAll();
    }

    private static void getAndSaveAll() throws JWNLException, IOException {
        getAllRules().forEach(SemanticRuleGenerator::save);
    }

    static List<SemanticRule> getAllRules() throws JWNLException, IOException {
        var rules = new ArrayList<SemanticRule>();

        rules.add(artifact());
        rules.addAll(actorRaw());
//        rules.addAll(actor());
        rules.add(condition());
        rules.add(exception());
        rules.add(location());
        rules.add(modality());
//        rules.add(reason());
        rules.add(situation());
        rules.add(sanction());
        rules.add(time());
        rules.add(violation());
        rules.add(action());

        return rules;
    }

    private static List<SemanticRule> actorRaw() throws IOException {
        var markers = MarkerGenerator.actor();
        var tregexNP = "NP < (__" + TregexRuleGenerator.ruleFromMarkers(" < ", markers,"") + ")"; // Can there ever be a match?
        var tregexPP = "PP < S $ (NP < (__" + TregexRuleGenerator.ruleFromMarkers(" < ", markers,"") + "))"; // Changed P to S

        var ruleNP = new SemanticRule("Actor-NP", tregexNP);
        var rulePP = new SemanticRule("Actor-PP", tregexPP);

        return List.of(ruleNP, rulePP);
    }

    private static SemanticRule action() {
        var constituencyRule = "VP";

        List<ConstituentRemovalRule> toRemove = List.of(
            new ConstituentRemovalRule(modality(), List.of("modality")),
            new ConstituentRemovalRule(condition(), List.of("condition_1", "condition_2", "condition_3", "condition_4", "condition_5")),
            new ConstituentRemovalRule(exception(), List.of("exception_1", "exception_2", "exception_3", "exception_4", "exception_5")),
            new ConstituentRemovalRule(reason(), List.of("reason_1", "reason_2", "reason_3", "reason_4", "reason_5"))
        );

        return new SemanticRule("Action", constituencyRule, toRemove);
    }

    private static List<SemanticRule> actor() throws IOException {
        var markers = MarkerGenerator.actor();
        var tregexNP = "NP < (__" + TregexRuleGenerator.ruleFromMarkers(" < ", markers,"") + ")"; // Can there ever be a match?
        var tregexPP = "PP < S $ (NP < (__" + TregexRuleGenerator.ruleFromMarkers(" < ", markers,"") + "))"; // Changed P to S

        var ruleNPSubj = new SemanticRule("Actor-np-subj", tregexNP, ".*subj");
        var ruleNPObj = new SemanticRule("Actor-np-obj", tregexNP, ".*obj");
        var rulePPObj = new SemanticRule("Actor-pp-obj", tregexPP, ".*obj");

        return List.of(ruleNPSubj, ruleNPObj, rulePPObj);
    }

    private static SemanticRule artifact() throws JWNLException, IOException {
        var markers = MarkerGenerator.artifact();
        var ruleNP = TregexRuleGenerator.ruleFromMarkers("(NP < (__ <", markers, "))");

        Set<String> disallowedMarkers = new TreeSet<>();
        disallowedMarkers.addAll(MarkerGenerator.violation());
        disallowedMarkers.addAll(MarkerGenerator.time());
        disallowedMarkers.addAll(MarkerGenerator.situation());
        disallowedMarkers.addAll(MarkerGenerator.sanction());
        disallowedMarkers.addAll(MarkerGenerator.location());
        disallowedMarkers.addAll(MarkerGenerator.actor());

        var ruleNothingElseMatches = "NP " + TregexRuleGenerator.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "");

        var ruleStr = or(ruleNP, ruleNothingElseMatches);
        return new SemanticRule("Artifact", ruleStr);
    }

    private static SemanticRule condition() {
        var markers = MarkerGenerator.condition();

        Set<String> disallowedMarkers = new TreeSet<>();
        disallowedMarkers.addAll(MarkerGenerator.exception());
        disallowedMarkers.addAll(MarkerGenerator.reason());

        var ruleSrel = createSrelRule(markers, "condition_1");

        var VPinfAdjusted = "__ < " + VP_INF;
        // TODO: Adjust name: VPinf instead of NP
        var ruleVPinfAndNoBadMarkers = "(NP=condition_2 < (" + VPinfAdjusted + " " + TregexRuleGenerator.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "") + "))";;
        var ruleVpartAndNoBadMarkers = "(NP=condition_3 < (" + V_PART + " " + TregexRuleGenerator.ruleFromMarkers("(!<< ", disallowedMarkers, ")", "|", "") + "))";
        var rulePP = "(PP=condition_4 " + TregexRuleGenerator.ruleFromMarkers("<< (", markers,")") + ")";
        var ruleSsub = "(SBAR=condition_5 " + TregexRuleGenerator.ruleFromMarkers("< (__ < ", markers,")") + ")";

//        var rules = List.of(
//                new SemanticRule("condition-Srel", ruleSrel),
//                new SemanticRule("condition-PP", rulePP),
//                new SemanticRule("condition-VPinf_no-other-markers", ruleVPinfAndNoBadMarkers),
//                new SemanticRule("condition-VPart_no-other-markers", ruleVpartAndNoBadMarkers),
//                new SemanticRule("condition-Ssub", ruleSsub)
//        );
//        return rules;

        var ruleStr = TregexRuleGenerator.any(Stream.of(ruleSrel, ruleVPinfAndNoBadMarkers, ruleVpartAndNoBadMarkers ,rulePP, ruleSsub));
        return new SemanticRule("Condition", ruleStr);
    }

    private static SemanticRule exception() {
        var markers = MarkerGenerator.exception();

        var ruleSrel = createSrelRule(markers, "exception_1");
        var ruleVPart = createVPartRule(markers, "exception_2");
        var ruleVPinf = createVPinfRule(markers, "exception_3");
        var ruleSsub = TregexRuleGenerator.ruleFromMarkers("(SBAR << (", markers,"))"); // TODO: Add name to SBAR: SBAR=exception_4
        var rulePP = TregexRuleGenerator.ruleFromMarkers("(PP << (", markers,"))"); // TODO: Add name to PP: PP=exception_5

        var ruleStr =  TregexRuleGenerator.any(Stream.of(ruleSrel, ruleVPart, ruleVPinf, ruleSsub, rulePP));
        return new SemanticRule("Exception", ruleStr);
    }

    private static SemanticRule location() {
        var markers = MarkerGenerator.location();
        var ruleStr = TregexRuleGenerator.ruleFromMarkers("(NP < (__ < ", markers, "))");

        return new SemanticRule("Location", ruleStr);
    }

    public static SemanticRule modality() {
        return new SemanticRule("Modality", "MD=modality");
    }

    private static SemanticRule reason() {
        var markers = MarkerGenerator.reason();

        // TODO: Test SBAR and VPart extensively
        var ruleSrel = createSrelRule(markers, "reason_1");
        var rulePP = "(PP=reason_2 " + TregexRuleGenerator.ruleFromMarkers("< (__ < ", markers,")") + ")";
        var ruleSsub = "(SBAR=reason_3 " + TregexRuleGenerator.ruleFromMarkers("<< (", markers, ")") + ")";
        var ruleVPart = createVPartRule(markers, "reason_4");
        var ruleVPinf = createVPinfRule(markers, "reason_5");

        var ruleStr = TregexRuleGenerator.any(Stream.of(ruleSrel, rulePP, ruleVPinf, ruleSsub, ruleVPart));
        return new SemanticRule("Reason", ruleStr);
    }

    private static SemanticRule sanction() {
        var markers = MarkerGenerator.sanction();
        var ruleStr = TregexRuleGenerator.ruleFromMarkers("(NP < (__ < ", markers, "))");

        return new SemanticRule("Sanction", ruleStr);
    }

    private static SemanticRule situation() {
        var markers = MarkerGenerator.situation();
        var ruleStr = TregexRuleGenerator.ruleFromMarkers("(NP < (__ < ", markers, "))");

        return new SemanticRule("Situation", ruleStr);
    }

    private static SemanticRule time() throws JWNLException, IOException {
        var markers = MarkerGenerator.time();

        String ruleNP = TregexRuleGenerator.ruleFromMarkers("(NP < (__ < ", markers, "))");
        // TODO: This rule has not generated any matches yet. Investigate!
        String rulePP = TregexRuleGenerator.ruleFromMarkers("(PP < (P < (__ < ", markers, ")) $ NP)");

        var ruleStr = or(ruleNP, rulePP);

        return new SemanticRule("Time", ruleStr);
    }

    private static SemanticRule violation() {
        var markers = MarkerGenerator.violation();
        var ruleStr = TregexRuleGenerator.ruleFromMarkers("(NP < (__ < ", markers, "))");

        return new SemanticRule("Violation", ruleStr);
    }

    private static String or(String rule1, String rule2) {
        return rule1 + "|" + rule2;
    }

    private static String and(String rule1, String rule2) {
        return rule1 + "&" + rule2;
    }

    static String createSrelRule(Set<String> markers) {
        return createSrelRule(markers, "");
    }

    static String createSrelRule(Set<String> markers, String targetNodeName) {
        String srelNamed;
        if (targetNodeName.isEmpty()) {
            srelNamed = S_REL;
        } else {
            srelNamed = S_REL.replace("SBAR", "SBAR=" + targetNodeName);
        }

        return "(" + srelNamed + "(" + TregexRuleGenerator.ruleFromMarkers("<< ", markers, "") + "))";
    }

    static String createVPartRule(Set<String> markers, String targetNodeName) {
        // NP < (VPart( << marker ))
        String vpartNamed;
        if (targetNodeName.isEmpty()) {
            vpartNamed = V_PART;
        } else {
            vpartNamed = V_PART.replace("VP", "VP=" + targetNodeName);
        }
        return "(NP < (" + vpartNamed + TregexRuleGenerator.ruleFromMarkers("<< (", markers, ")") + "))";
    }

    static String createVPartRule(Set<String> markers) {
        return createVPartRule(markers, "");
    }

    // TODO: Fix annotation target
    static String createVPinfRule(Set<String> markers, String targetNodeName) {
        // NP << (P < (marker) $ VPinf)
        String vpinfExtended = "(__ << " + VP_INF + ")";
        String vpinfNamed;

        if (targetNodeName.isEmpty()) {
            vpinfNamed = vpinfExtended;
        } else {
            vpinfNamed = vpinfExtended.replace("VP", "VP=" + targetNodeName);
        }

        var markersWithoutTO = MarkerGenerator.removeTO(markers);
        return "(" + vpinfNamed + TregexRuleGenerator.ruleFromMarkers("(($ (__ < ", markersWithoutTO, ")) > __) >> NP") + ")";
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
