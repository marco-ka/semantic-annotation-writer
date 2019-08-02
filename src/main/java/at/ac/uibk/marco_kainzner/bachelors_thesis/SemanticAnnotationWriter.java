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
import net.sf.extjwnl.JWNLException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemanticAnnotationWriter extends JCasFileWriter_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            var rules = SemanticRuleGenerator.getAllRules();
            for (SemanticRule rule : rules) {
                getAndWriteMatches(jCas, rule);
            }

        } catch (IOException | JWNLException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void getAndWriteMatches(JCas jCas, SemanticRule rule) throws IOException {
        var documentId = DocumentMetaData.get(jCas).getDocumentId();

        var sentences = JCasUtil.select(jCas, Sentence.class);
        var matches = sentences.stream()
                .flatMap(sentence -> getMatches(sentence, rule.constituencyRule, rule.dependencyRuleOrNull))
                .collect(Collectors.toList());

        var matchesStr = matchesToString(documentId, matches);
        write(jCas, matchesStr);
    }

    private void write(JCas jCas, String str) throws IOException {
        var outputStream = new OutputStreamWriter(getOutputStream(jCas, ".txt"));
        outputStream.write(str);
        outputStream.close();
    }

    private static String matchesToString(String documentId, List<PennTreeNode> matches) {
        var sb = new StringBuilder();
        var lineId = new AtomicInteger(1);

        matches.forEach(match -> {
            sb.append(documentId);
            sb.append("-");
            sb.append(lineId.get());
            sb.append(" ");

            sb.append(match);
            sb.append(System.lineSeparator());

            lineId.getAndIncrement();
        });

        return sb.toString();
    }

    private static Stream<PennTreeNode> getMatches(Sentence sentence, String constituencyRule, String dependencyTypeRegex) {
        var constituents = JCasUtil.selectCovered(Constituent.class, sentence)
                .stream()
                .map(PennTreeUtils::convertPennTree)
                .filter(tree -> matches(tree, constituencyRule));

        if (dependencyTypeRegex != null) {
            return constituents.filter(tree -> hasDependency(tree, dependencyTypeRegex));
        }

        return constituents;
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
}
