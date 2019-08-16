package at.ac.uibk.marco_kainzner.bachelors_thesis;

public class SemanticRule {
    final String name;
    final String constituencyRule;
    final String dependencyRuleOrNull;
    final String constituencyRuleExclusion;

    public SemanticRule(String name, String constituencyRule) {
        this(name, constituencyRule, null);
    }

    public SemanticRule(String name, String constituencyRule, String dependencyRuleOrNull) {
        this(name, constituencyRule, dependencyRuleOrNull, null);
    }

    public SemanticRule(String name, String constituencyRule, String dependencyRule, String constituencyRuleExclusion) {
        this.name = name;
        this.constituencyRule = constituencyRule;
        this.dependencyRuleOrNull = dependencyRule;
        this.constituencyRuleExclusion = constituencyRuleExclusion;
    }
}
