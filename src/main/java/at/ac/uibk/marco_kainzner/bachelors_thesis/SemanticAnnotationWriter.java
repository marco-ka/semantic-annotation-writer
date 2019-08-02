package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.Pair;
import net.sf.extjwnl.JWNLException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemanticAnnotationWriter extends JCasFileWriter_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String documentId = DocumentMetaData.get(jCas).getDocumentId();
        try {
            log("--- ----------------- ---");
            log("--- Starting document " + documentId);
            List<SemanticRule> rules = SemanticRuleGenerator.getAllRules();
            for (SemanticRule rule : rules) {
                log("---");
                log("--- Rule : " + rule.name);
                var matches = getMatchesInDocument(jCas, rule);
                log("--- Done with rule : " + rule.name);
                String matchesStr = matchesToString(matches);
                log("--- Done with toString");

                write(jCas, rule.name, matchesStr);
                log("--- Done with writing");
                log("--- ----------------- ---");
            }
            log("--- Done with document " + documentId);

        } catch (IOException | JWNLException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private List<Pair<String, PennTreeNode>> getMatchesInDocument(JCas jCas, SemanticRule rule) {
        String documentId = DocumentMetaData.get(jCas).getDocumentId();

        var sentences = JCasUtil.select(jCas, Sentence.class);
        var sentenceNr = 1;

        List<Pair<String, PennTreeNode>> matches = new ArrayList<>();
        for (Sentence sentence : sentences) {
            var label = documentId + "-" + sentenceNr + " ";

            for (PennTreeNode match : getMatchesInSentence(sentence, rule.constituencyRule, rule.dependencyRuleOrNull)) {
                matches.add(new Pair<>(label, match));
            }

            sentenceNr++;
        }
        return matches;
    }

    private void write(JCas jCas, String annotation, String str) throws IOException {
        var fileSuffix = "-" + annotation + ".txt";
        var outputStream = new OutputStreamWriter(getOutputStream(jCas, fileSuffix));
        outputStream.write(str);
        outputStream.close();
    }

    private static String matchesToString(List<Pair<String, PennTreeNode>> matches) {
        var sb = new StringBuilder();

        matches.forEach(match -> {
            sb.append(match.first);
            sb.append(match.second);
            sb.append(System.lineSeparator());
        });

        return sb.toString();
    }

    private static List<PennTreeNode> getMatchesInSentence(Sentence sentence, String constituencyRule, String dependencyTypeRegex) {
        var constituents = JCasUtil.selectCovered(Constituent.class, sentence)
                .stream()
                .map(PennTreeUtils::convertPennTree)
                .filter(tree -> matches(tree, constituencyRule));

        if (dependencyTypeRegex != null) {
            return constituents
                    .filter(tree -> hasDependency(tree, dependencyTypeRegex))
                    .collect(Collectors.toList());
        }

        return constituents.collect(Collectors.toList());
    }

    private static boolean hasDependency(PennTreeNode constituencyTree, String dependencyTypeRegex) {
        var dependencies = PennTree.toDependencyTree(constituencyTree)
            .filter(d -> d.getDependencyType().matches(dependencyTypeRegex))
            .peek(d -> System.out.println("Dependency matches regex ('" + dependencyTypeRegex + "'): " + dependencyStr(d)))
            .collect(Collectors.toList());
        return !dependencies.isEmpty();
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
        System.out.println(new Date() + ": " + msg);
    }
}
