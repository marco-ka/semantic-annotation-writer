package at.ac.uibk.marco_kainzner.bachelors_thesis;

public class Rule {
    final String name;
    final String constituencyRule;
    final String dependencyRuleOrNull;

    public Rule(String name, String constituencyRule, String dependencyRule) {
        this.name = name;
        this.constituencyRule = constituencyRule;
        this.dependencyRuleOrNull = dependencyRule;
    }
}
