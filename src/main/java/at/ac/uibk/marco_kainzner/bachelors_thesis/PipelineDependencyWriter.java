package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordDependencyConverter;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import net.sf.extjwnl.JWNLException;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class PipelineDependencyWriter extends JCasConsumer_ImplBase {
    @Override
    // TODO: Param: Target location
    public void process(JCas jCas) {
        try {
            var metaData = DocumentMetaData.get(jCas);
            var docName = metaData.getDocumentId();

            var rules = Rules.getAll();
            var dir = "C:/Users/Marco/Documents/Projects/dkpro-pipeline/resources/";
            var path = Paths.get(dir, docName + "_matches.txt");
            writeAllText(path.toString(), "");

            var sentenceIndex = 0;
            for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
                sentenceIndex++;

//                log("Starting sentence: '" + sentence.getCoveredText() + "'");
//                log("Rule: '" + rules.get(0).getFirst() + "'");
//                log("---< RULE>---");
//                log(rules.get(0).getSecond());
//                log("---</RULE>---");

                log("Matches for constituency rule: \n---");
                var matchesConstituency = getMatches(sentence, rules.get(0).getSecond())
                        .stream()
//                        .peek(System.out::println)
                        .collect(Collectors.toList());

                log("Matches for dependency rule: \n---");
                var matches = matchesConstituency.stream()
                        .filter(pennTreeNode -> hasDependency(pennTreeNode, ".*"))
//                        .peek(System.out::println)
                        .collect(Collectors.toList());

//                log(matchesConstituency.size() + " constituency matches");
//                log(matches.size() + " final matches");
//                log("---\nDone with sentence");

                for (PennTreeNode match : matches) {
                    var txt = "sentence_" + sentenceIndex + ": " + match.toString();
                    appendLine(path.toString(), txt);
                }
            }

            log("All sentences done");

        } catch (IOException | JWNLException e) {
            log("Failed to get rules");
            e.printStackTrace();
        }
    }

    private static List<PennTreeNode> getMatches(Sentence sentence, String constituencyRule) {
        return getMatches(sentence, constituencyRule, null);
    }

    private static List<PennTreeNode> getMatches(Sentence sentence, String constituencyRule, String dependencyTypeRegex) {
        var constituents = JCasUtil.selectCovered(Constituent.class, sentence)
                .stream()
                .map(PennTreeUtils::convertPennTree)
                .filter(tree -> matches(tree, constituencyRule))
                .collect(Collectors.toList());

        if (dependencyTypeRegex != null) {
            return constituents.stream()
                    .filter(tree -> hasDependency(tree, ".*subj|.*obj"))
                    .collect(Collectors.toList());
        }

        return constituents;
    }

    private static boolean hasDependency(PennTreeNode constituencyTree, String dependencyTypeRegex) {
        var dependencies = toDependencyTree(constituencyTree)
            .filter(d -> d.getDependencyType().matches(dependencyTypeRegex))
//            .peek(d -> System.out.println("Dependency matches regex ('" + dependencyTypeRegex + "'): " + dependencyStr(d)))
            .collect(Collectors.toList());
        return !dependencies.isEmpty();
    }

    private static Stream<Dependency> toDependencyTree(PennTreeNode node) {
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
            System.out.println("Failed to convert constituency tree to dependency tree");
            e.printStackTrace();
            return null;
        }
    }

    private static String dependencyStr(Dependency d) {
        return d.getDependencyType() + ": '" + d.getGovernor().getText() + "' -> '" + d.getDependent().getText() + "'";
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

    private static void appendLine(String path, String text) throws IOException {
        File file = new File(path);
        FileWriter fw = new FileWriter(path, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(text);
        bw.newLine();
        bw.close();
    }

    private static void writeAllText(String path, String text) throws IOException {
        File file = new File(path);
        file.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(text);
        writer.close();
    }
}
