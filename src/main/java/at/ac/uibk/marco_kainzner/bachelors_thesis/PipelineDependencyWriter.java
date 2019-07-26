package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordDependencyConverter;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class PipelineDependencyWriter extends JCasConsumer_ImplBase {
    @Override
    public void process(JCas jCas) {
        var rule = Rules.condition();

        for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
            log(sentence.getCoveredText());

            log("Matches: ");
            var constituentTrees = JCasUtil.selectCovered(Constituent.class, sentence)
                    .stream()
                    .map(PennTreeUtils::convertPennTree)
                    .filter(tree -> matches(tree, rule))
                    .peek(System.out::println)
                    .collect(Collectors.toList());

            log("Dependencies: ");
            constituentTrees
                    .stream()
                    .flatMap(PipelineDependencyWriter::toDependency)
                    .forEach(PipelineDependencyWriter::printDependency);
            log("Done");

//            System.out.println("Subject Dependencies: ");
//            var dependencies = JCasUtil.selectCovered(jCas, Dependency.class, sentence);
//            dependencies.stream()
//                .filter(dep -> dep.getDependencyType().contains("subj"))
//                .forEach(dep -> System.out.println(dep.getDependencyType() + ": " + dep.getDependent().getText() + " <- " + dep.getGovernor().getText()));
        }
    }

    private static Stream<Dependency> toDependency(PennTreeNode node) {
        try {
            // Taken from DKPro: StanfordDependencyConverterTest.java
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
            e.printStackTrace();
            return null;
        }
    }

    private static void printDependency(Dependency d) {
        System.out.println(d.getDependencyType() + ":\t" + d.getGovernor().getText() + " -> " + d.getDependent().getText());
    }

    private static boolean matches(PennTreeNode treeNode, String rule) {
        var treeStr = PennTreeUtils.toPennTree(treeNode);
        var tree = Tree.valueOf(treeStr);

        var pattern = TregexPattern.compile(rule);
        var matcher = pattern.matcher(tree);

        return matcher.matches();
    }

    private static void log(String msg) {
        var timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp + ": " + msg);
    }
}
