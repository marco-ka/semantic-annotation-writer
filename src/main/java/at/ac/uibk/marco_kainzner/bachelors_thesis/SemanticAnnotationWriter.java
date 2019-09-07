package at.ac.uibk.marco_kainzner.bachelors_thesis;

import com.google.gson.Gson;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import net.sf.extjwnl.JWNLException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SemanticAnnotationWriter extends JCasFileWriter_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String documentId = DocumentMetaData.get(jCas).getDocumentId();
        try {
            List<SemanticRule> rules = SemanticRuleGenerator.getAllRules();
            for (SemanticRule rule : rules) {
                var annos = getAnnotations(jCas, rule);
                write(jCas, rule.name, annos);
            }
        } catch (IOException | JWNLException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void write(JCas jCas, String ruleName, List<Annotation> annotations) throws IOException {
        Gson gson = new Gson();

        var fileSuffix = "-" + ruleName + ".txt";
        var outputStream = new OutputStreamWriter(getOutputStream(jCas, fileSuffix));

        for (var anno: annotations) {
            outputStream.write(gson.toJson(anno));
        }

        outputStream.close();
    }

    private List<Annotation> getAnnotations(JCas jCas, SemanticRule rule) {
        return getMatches(jCas, rule).stream()
                .map(SemanticAnnotationWriter::getAnnotation)
                .filter(annotation -> annotation != null)
                .collect(Collectors.toList());
    }

    private static Annotation getAnnotation(Match match) {
        String sentenceWords = TreeUtils.tree2Words(match.sentenceTree);
        String matchWords = TreeUtils.tree2Words(match.matchTree);

        var begin = sentenceWords.indexOf(matchWords);
        if (begin == -1) {
            System.out.println("Cannot generate annotation from match: " + match);
            return null;
        }
        var end = begin + matchWords.length();

        return new Annotation(match.documentId, match.sentenceId, match.label, begin, end);
    }

    private List<Match> getMatches(JCas jCas, SemanticRule rule) {
        String documentId = DocumentMetaData.get(jCas).getDocumentId();
        var roots = JCasUtil.select(jCas, ROOT.class);
        var matchesForRule = new ArrayList<Match>();

        var sentenceNum = 1;
        for (var sentence: roots) {
            var sentenceTreeNode = PennTreeUtils.convertPennTree(sentence);

            var visitor = getMatchTreeVisitor(documentId, rule.constituencyRule, sentenceTreeNode);
            List<MyTreeFromFile> matchesInSentence = visitor.getMatches();
            Map<MyTreeFromFile, List<Tree>> matchedParts = visitor.getMatchedParts();

            for (var match : matchesInSentence) {
                Tree sentenceTree = match.getTree();
                for (Tree matchedPart : matchedParts.get(match)) {
                    if (hasDependency(matchedPart, rule.dependencyRuleOrNull)) {
                        Tree withConstituentsRemoved = removeConstituents(matchedPart, rule.constituentRemovalRules);
                        matchesForRule.add(new Match(rule.name, documentId, sentenceNum, sentenceTree, withConstituentsRemoved));
                    }
                }
            }
            sentenceNum++;
        }

        return matchesForRule;
    }

    public static TRegexGUITreeVisitor getMatchTreeVisitor(String fileName, String patternString, PennTreeNode treeNode) {
        var pattern = TregexPattern.compile(patternString);
        var visitor = new TRegexGUITreeVisitor(pattern);
        var tree = PennTree.toTree(treeNode);

        visitor.setFilename(fileName);
        visitor.visitTree(tree);

        return visitor;
    }

    private Tree removeConstituents(Tree node, List<ConstituentRemovalRule> rules) {
        var modifiedNode = node;
        for (ConstituentRemovalRule rule: rules) {
            modifiedNode = removeConstituents(modifiedNode, rule);
        }
        return modifiedNode;
    }

    private Tree removeConstituents(Tree node, ConstituentRemovalRule rule) {
        TregexPattern matchPattern = TregexPattern.compile(rule.constituencyRule);
        Tree modifiedNode = node;

        int noNodesMatchCount = 0;
        for (String nameToRemove : rule.namesToRemove) {
            String readableSurgery = "delete " + nameToRemove;
            TsurgeonPattern surgery = Tsurgeon.parseOperation(readableSurgery);
            try {
                modifiedNode = Tsurgeon.processPattern(matchPattern, surgery, node);
            } catch (NullPointerException e) {
//                System.out.println("null node fetched by Tsurgeon operation (either no node labeled this, or the labeled node didn't match anything)");
                noNodesMatchCount++;
            }
        }

        if (noNodesMatchCount > 0)
            System.out.println("Rule '" + rule.ruleName + "': " + noNodesMatchCount + " times no match");

        return modifiedNode;
    }

    public static boolean hasDependency(Tree constituencyTree, String dependencyTypeRegex) {
        if (dependencyTypeRegex == null || dependencyTypeRegex.isEmpty()) {
            return true;
        }

        var pennTree = PennTree.ofTree(constituencyTree);
        var dependencyTree = PennTree.toDependencyTree(pennTree);

        var dependencies = dependencyTree
            .filter(d -> d.getDependencyType().matches(dependencyTypeRegex))
            .peek(d -> System.out.println("Dependency matches regex ('" + dependencyTypeRegex + "'): " + dependencyStr(d)))
            .collect(Collectors.toList());

        return !dependencies.isEmpty();
    }

    private static String dependencyStr(Dependency d) {
        return d.getDependencyType() + ": '" + d.getGovernor().getText() + "' -> '" + d.getDependent().getText() + "'";
    }

    private static void log(String msg) {
        System.out.println(new Date() + ": " + msg);
    }
}
