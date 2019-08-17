package at.ac.uibk.marco_kainzner.bachelors_thesis;

import java.util.List;

public class ConstituentRemovalRule {
    public final String ruleName;
    public final String constituencyRule;
    public final List<String> namesToRemove;

    public ConstituentRemovalRule(String ruleName, String constituencyRule, List<String> namesToRemove) {
        this.ruleName = ruleName;
        this.constituencyRule = constituencyRule;
        this.namesToRemove = namesToRemove;
    }
}
