package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.*;
import edu.stanford.nlp.util.Pair;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TRegexMatch {
    public static void main(String[] args) throws IOException {
        var rule = Rules.reason();
        var pattern = TregexPattern.compile(rule.constituencyRule);
        var path = "./resources/fffs/penn-trees/fffs_200_statements.txt.mrg";

        var matches = findLabelledMatches(path, pattern);
        matches.forEach(System.out::println);
    }

    private static List<String> findLabelledMatches(String path, TregexPattern pattern) throws IOException {
        var trees = readTrees(path);
        var sentences = addSentenceLabels(path, trees);

        return  sentences.stream()
                .flatMap(sent -> findAllMatches(sent.second, pattern).map(match -> sent.first + ":\n" + match.pennString()))
                .collect(Collectors.toList());
    }

    private static Stream<Tree> findAllMatches(Tree tree, TregexPattern pattern) {
        var matcher = pattern.matcher(tree);

        List<Tree> matches = new ArrayList<>();
        while (matcher.findNextMatchingNode()) {
            matches.add(matcher.getMatch());
        }
        return matches.stream();
    }

    private static List<Tree> readTrees(String path) throws IOException {
        var treeReader = new PennTreeReader(new FileReader(path));

        List<Tree> sentences = new ArrayList<>();
        Tree tree;
        while ((tree = treeReader.readTree()) != null) {
            sentences.add(tree);
        }

        return sentences;
    }

    private static List<Tree> readTrees(Reader reader) throws IOException {
        var treeReader = new PennTreeReader(reader);

        List<Tree> sentences = new ArrayList<>();
        Tree tree;
        while ((tree = treeReader.readTree()) != null) {
            sentences.add(tree);
        }

        return sentences;
    }

    private static List<Pair<String, Tree>> addSentenceLabels(String path, List<Tree> sentences) {
        var fileName = Paths.get(path).getFileName().toString();

        AtomicInteger i = new AtomicInteger();
        var numberedSentences =
                sentences.stream().map(sent -> {
                    i.getAndIncrement();
                    var sentenceName = fileName + "-" + i;
                    return new Pair<>(sentenceName, sent);
                });

        return numberedSentences.collect(Collectors.toList());
    }

}
