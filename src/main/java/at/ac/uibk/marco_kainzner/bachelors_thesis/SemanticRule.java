package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.util.ArrayList;
import java.util.List;

public class SemanticRule {
    public final String name;
    public final String constituencyRule;
    // Only needed for "actor" rule
    public final String dependencyRuleOrNull;
    // List of annotations names to remove from matched strings. Only needed for "action" rule
    public final List<ConstituentRemovalRule> constituentRemovalRules;

    public SemanticRule(String name, String constituencyRule) {
        this(name, constituencyRule, null, new ArrayList<>());
    }

    public SemanticRule(String name, String constituencyRule, String dependencyRuleOrNull) {
        this(name, constituencyRule, dependencyRuleOrNull, new ArrayList<>());
    }

    public SemanticRule(String name, String constituencyRule, List<ConstituentRemovalRule> constituentRemovalRules) {
        this(name, constituencyRule, null, constituentRemovalRules);
    }

    public SemanticRule(String name, String constituencyRule, String dependencyRuleOrNull, List<ConstituentRemovalRule> constituentRemovalRules) {
        this.name = name;
        this.constituencyRule = constituencyRule;
        this.dependencyRuleOrNull = dependencyRuleOrNull;
        this.constituentRemovalRules = constituentRemovalRules;
    }
}
