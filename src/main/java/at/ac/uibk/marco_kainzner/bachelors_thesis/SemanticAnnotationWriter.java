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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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

    private static TRegexGUITreeVisitor getMatchTreeVisitor(String fileName, String patternString, PennTreeNode treeNode) {
        var pattern = TregexPattern.compile(patternString);
        var visitor = new TRegexGUITreeVisitor(pattern);
        var tree = PennTree.toTree(treeNode);

        visitor.setFilename(fileName);
        visitor.visitTree(tree);

        return visitor;
    }

    private List<String> getMatches(JCas jCas, SemanticRule rule) {
        String documentId = DocumentMetaData.get(jCas).getDocumentId();
        var roots = JCasUtil.select(jCas, ROOT.class);

        var matchStrings = new ArrayList<String>();
        var sentence = 1;


        for (var root: roots) {
            var visitor = getMatchTreeVisitor(documentId, rule.constituencyRule, PennTreeUtils.convertPennTree(root));
            var matchedParts = visitor.getMatchedParts();
            for (var match : visitor.getMatches()) {
                for (Tree matchedPart : matchedParts.get(match)) {
                    var sentenceId = match.getFilename() + "-" + sentence;
                    if (hasDependency(matchedPart, rule.dependencyRuleOrNull)) {
                        Tree withConstituentsRemoved = removeConstituents(matchedPart, rule.constituentRemovalRules);
                        matchStrings.add(sentence  + " " + withConstituentsRemoved.pennString());
                    } else {
//                        System.out.println("Match but no dependency: " + sentenceId + " " + matchedPart.pennString());
                    }
                }
            }
            sentence++;
        }

        return matchStrings;
    }

    private Tree removeConstituents(Tree node, List<ConstituentRemovalRule> rules) {
        var modifiedNode = node;
        for (ConstituentRemovalRule rule: rules) {
            System.out.println("Removing constituent");
            modifiedNode = removeConstituents(modifiedNode, rule);
        }
        return modifiedNode;
    }

    private Tree removeConstituents(Tree node, ConstituentRemovalRule rule) {
        var namesToPrune = String.join(" ", rule.namesToRemove);
        var operation = "prune " + namesToPrune;

        TregexPattern matchPattern = TregexPattern.compile(rule.constituencyRule);
        List<TsurgeonPattern> ps = new ArrayList<>();
        TsurgeonPattern p = Tsurgeon.parseOperation(operation);
        ps.add(p);

        System.out.println("Removing " + rule.ruleName + " from ");
        node.pennPrint();
        System.out.println("Result");

        Collection<Tree> result = Tsurgeon.processPatternOnTrees(matchPattern, Tsurgeon.collectOperations(ps), node);
        result.forEach(res -> {
            if (res == null) System.out.println("Result is null");
            else System.out.println(res.pennString());
        });
        System.out.println("---");
        System.out.println();

        return new ArrayList<>(result).get(0);
    }

    private void write(JCas jCas, String annotation, String str) throws IOException {
        var fileSuffix = "-" + annotation + ".txt";
        var outputStream = new OutputStreamWriter(getOutputStream(jCas, fileSuffix));
        outputStream.write(str);
        outputStream.close();
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
