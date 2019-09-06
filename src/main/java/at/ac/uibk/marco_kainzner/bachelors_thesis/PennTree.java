package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordDependencyConverter;
import edu.stanford.nlp.trees.Tree;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.stream.Stream;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class PennTree {

    static Stream<Dependency> toDependencyTree(PennTreeNode node) {
        try {
            // from DKPro: StanfordDependencyConverterTest.java
            JCas jcas = JCasFactory.createJCas();

            StringBuilder sb = new StringBuilder();
            PennTreeToJCasConverter converter = new PennTreeToJCasConverter(null, null);
            converter.setCreatePosTags(true);
            converter.convertPennTree(jcas, sb, node);

            jcas.setDocumentText(sb.toString());
            jcas.setDocumentLanguage("en");
            new Sentence(jcas, 0, jcas.getDocumentText().length()).addToIndexes();

            AnalysisEngineDescription annotator = null;

            annotator = createEngineDescription(StanfordDependencyConverter.class);
            runPipeline(jcas, annotator);

            return JCasUtil.select(jcas, Dependency.class).stream();
        } catch (UIMAException e) {
            System.out.println("Failed to convert constituency tree to dependency tree");
            e.printStackTrace();
            return null;
        }
    }

    public static Tree toTree(PennTreeNode treeNode) {
        var treeStr = PennTreeUtils.toPennTree(treeNode);
        return Tree.valueOf(treeStr);
    }

    static PennTreeNode ofTree(Tree tree) {
        return PennTreeUtils.parsePennTree(tree.pennString());
    }
}
