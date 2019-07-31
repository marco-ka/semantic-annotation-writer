package at.ac.uibk.marco_kainzner.bachelors_thesis;

public class SemanticRule {
    final String name;
    final String constituencyRule;
    final String dependencyRuleOrNull;

    public SemanticRule(String name, String constituencyRule, String dependencyRule) {
        this.name = name;
        this.constituencyRule = constituencyRule;
        this.dependencyRuleOrNull = dependencyRule;
    }
}
