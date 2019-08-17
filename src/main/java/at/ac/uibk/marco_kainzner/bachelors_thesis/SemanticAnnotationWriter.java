package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
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
import java.util.ArrayList;
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
                var matches = getMatchesInDocument(jCas, rule);
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

    private List<String> getMatchesInDocument(JCas jCas, SemanticRule rule) {
        String documentId = DocumentMetaData.get(jCas).getDocumentId();
        var roots = JCasUtil.select(jCas, ROOT.class);

        var matchStrings = new ArrayList<String>();
        var sentence = 1;

        for (var root: roots) {
            var visitor = getMatchTreeVisitor(documentId, rule.constituencyRule, PennTreeUtils.convertPennTree(root));
            var matchedParts = visitor.getMatchedParts();
            for (var match : visitor.getMatches()) {
                for (var matchedPart : matchedParts.get(match)) {
                    var matchStr = match.getFilename() + "-" + sentence + " " + matchedPart.pennString();
                    if (hasDependency(matchedPart, rule.dependencyRuleOrNull)) {
                        matchStrings.add(matchStr);
                    } else {
                        System.out.println("Match but no dependency: " + matchStr);
                    }
                }
            }
            sentence++;
        }

        return matchStrings;
    }

    private void write(JCas jCas, String annotation, String str) throws IOException {
        var fileSuffix = "-" + annotation + ".txt";
        var outputStream = new OutputStreamWriter(getOutputStream(jCas, fileSuffix));
        outputStream.write(str);
        outputStream.close();
    }

    private static boolean hasDependency(Tree constituencyTree, String dependencyTypeRegex) {
        if (dependencyTypeRegex == null) {
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
