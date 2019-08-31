package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonParseException;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import net.sf.extjwnl.JWNLException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

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
                log("--- ----------------- ---");
                log("--- Rule : " + rule.name);
                var matches = getMatches(jCas, rule);
                log("--- Done with rule : " + rule.name);
                var matchesStr = String.join("", matches);
                write(jCas, rule.name, matchesStr);
            }
            log("--- Done with document " + documentId);

        } catch (IOException | JWNLException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void write(JCas jCas, String annotation, String str) throws IOException {
        var fileSuffix = "-" + annotation + ".txt";
        var outputStream = new OutputStreamWriter(getOutputStream(jCas, fileSuffix));
        outputStream.write(str);
        outputStream.close();
    }


    private List<String> getMatches(JCas jCas, SemanticRule rule) {
        String documentId = DocumentMetaData.get(jCas).getDocumentId();
        var roots = JCasUtil.select(jCas, ROOT.class);

        var matchStrings = new ArrayList<String>();
        var sentenceNum = 1;

        for (var root: roots) {
            var rootTreeNode = PennTreeUtils.convertPennTree(root);
            var rootTree = PennTree.toTree(rootTreeNode);
            System.out.println(rootTree.toString());
<
            var visitor = getMatchTreeVisitor(documentId, rule.constituencyRule, rootTreeNode);
            List<MyTreeFromFile> matches = visitor.getMatches();
            Map<MyTreeFromFile, List<Tree>> matchedParts = visitor.getMatchedParts();

            var numMatches = 0;

            for (var match : matches) {
                var sentenceId = match.getFilename() + "-" + sentenceNum;
                List<Tree> matchedPartsInSentence = matchedParts.get(match);
                numMatches += matchedPartsInSentence.size();

                System.out.println(sentenceId + ": " + matchedPartsInSentence.size() + " matches");

                var sentenceTree = match.getTree();
                for (Tree matchedPart : matchedParts.get(match)) {
                    if (hasDependency(matchedPart, rule.dependencyRuleOrNull)) {
                        Tree withConstituentsRemoved = removeConstituents(matchedPart, rule.constituentRemovalRules);

                        System.out.println("Parent:");
                        sentenceTree.pennPrint();
                        System.out.println();
                        matchedPart.pennPrint();

//                        System.out.println("Span: " + matchedPart.getSpan());
                        var left = sentenceTree.leftCharEdge(matchedPart);
                        var right = sentenceTree.rightCharEdge(matchedPart);
                        System.out.println("Span: " + left + " - " + right);

                        matchStrings.add(sentenceId + " " + withConstituentsRemoved.pennString());
                    }
                }
            }

            sentenceNum++;
        }

        return matchStrings;
    }

    private static TRegexGUITreeVisitor getMatchTreeVisitor(String fileName, String patternString, PennTreeNode treeNode) {
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

    private static boolean hasDependency(Tree constituencyTree, String dependencyTypeRegex) {
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
