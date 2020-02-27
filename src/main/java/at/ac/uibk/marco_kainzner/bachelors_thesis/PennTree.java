package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalStructure;
import org.dkpro.core.io.penntree.PennTreeNode;
import org.dkpro.core.io.penntree.PennTreeUtils;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class PennTree {

    static List<TypedDependency> toDependencyTree(Tree t) {
        UniversalEnglishGrammaticalStructure gs = new UniversalEnglishGrammaticalStructure(t);
        return new ArrayList<>(gs.typedDependencies());
    }

    static List<TypedDependency> toDependencyTree(PennTreeNode t) {
        return toDependencyTree(toTree(t));
    }

    public static Tree toTree(PennTreeNode treeNode) {
        var treeStr = PennTreeUtils.toPennTree(treeNode);
        return Tree.valueOf(treeStr);
    }

    static PennTreeNode ofTree(Tree tree) {
        return PennTreeUtils.parsePennTree(tree.pennString());
    }
}
