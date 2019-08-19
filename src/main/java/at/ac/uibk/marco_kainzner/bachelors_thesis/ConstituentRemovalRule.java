package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.util.List;

public class ConstituentRemovalRule {
    public final String ruleName;
    public final String constituencyRule;
    public final List<String> namesToRemove;

    private ConstituentRemovalRule(String ruleName, String constituencyRule, List<String> namesToRemove) {
        this.ruleName = ruleName;
        this.constituencyRule = constituencyRule;
        this.namesToRemove = namesToRemove;
    }

    public ConstituentRemovalRule(SemanticRule rule, List<String> namesToRemove) {
        this(rule.name, rule.constituencyRule, namesToRemove);
    }
}
