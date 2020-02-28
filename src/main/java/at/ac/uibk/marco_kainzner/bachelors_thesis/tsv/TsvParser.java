package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jooq.lambda.tuple.Tuple.tuple;

public class TsvParser {
    public static void main(String[] args) throws IOException {
        var inputDir = Path.of("resources", "tsv");
        var outputDir = Path.of("out", "tsv-json");

        var docs = readDocuments(inputDir);
    }

    public static void convertDocuments(Path inputDir, Path outputDir) throws IOException {
        var documents = readDocuments(inputDir).collect(Collectors.toList());
        for (var doc: documents) {
            doc.saveToDir(outputDir);
        }
    }

    public static Stream<Document> readDocuments(Path dir) throws IOException {
        return Files.list(dir)
            .filter(Files::isRegularFile)
            .map(TsvParser::readDocument);
    }

    public static Document readDocument(Path file) {
        List<String> tsv;
        try {
            tsv = Files.readAllLines(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        var pennTreeExtension = ".mrg";
        var documentId = FilenameUtils.removeExtension(file.getFileName().toString()) + pennTreeExtension;

        var sentences = segmentIntoSentences(tsv).map(x -> parseSentence(documentId, x));
        var annotations = sentences.flatMap(Sentence::GetAnnotations).collect(Collectors.toList());

        return new Document(documentId, annotations);
    }

    private static Sentence parseSentence(String documentId, List<String> sentenceLines) {
        var text = sentenceLines.get(0).replace("#Text=", "");

        var annotationLines = sentenceLines
                .stream()
                .filter(Predicate.not(String::isEmpty))
                .filter(Predicate.not(line -> line.startsWith("#")))
                .map(line -> line.split("\t"))
                .filter(arr -> arr.length > 3)
                .collect(Collectors.toList());

        return new Sentence(documentId, text, mergeAnnotationLines(text, annotationLines));
    }

    private static ArrayList<InceptionAnnotation> mergeAnnotationLines(String sentenceText, List<String[]> lines) {
        var labels = lines.stream()
                .flatMap(arr -> Arrays.stream(arr[3].split("\\|")))
                .distinct()
                .filter(x -> !x.equals("_"))
                .collect(Collectors.toList());

        var sentenceId = Integer.parseInt(lines.get(0)[0].split("-")[0]);
        var sentenceStart = Integer.parseInt(lines.get(0)[1].split("-")[0]);

        var annotations = new ArrayList<InceptionAnnotation>();
        labels.forEach(label -> {
            var members = lines.stream().filter(x -> x[3].contains(label)).collect(Collectors.toList());

            if (label.contains("[") && label.contains("]")) {
                // Chain: aggregate tokens
                var beginsAndEnds = members.stream().map(member -> member[1]).collect(Collectors.toList());
                var begin = beginsAndEnds.stream().map(x -> Integer.parseInt(x.split("-")[0])).min(Integer::compareTo).get();
                var end = beginsAndEnds.stream().map(x -> Integer.parseInt(x.split("-")[1])).max(Integer::compareTo).get();

                var tokens = members.stream().map(x -> tuple(x[0], x[2])).collect(Collectors.toList());

                var annotation = new InceptionAnnotation(sentenceStart, sentenceId, sentenceText, label, tokens, begin, end);
                annotations.add(annotation);
            }
            else {
                // No chain: Each line is a single annotation
                members.forEach(x -> {
                    var beginAndEnd = x[1].split("-");
                    var begin = Integer.parseInt(beginAndEnd[0]);
                    var end = Integer.parseInt(beginAndEnd[1]);

                    var tokens = List.of(tuple(x[0], x[2]));

                    var annotation = new InceptionAnnotation(sentenceStart, sentenceId, sentenceText, label, tokens, begin, end);
                    annotations.add(annotation);
                });
            }
        });

        return annotations;
    }

    private static Stream<List<String>> segmentIntoSentences(List<String> tsv) {
        List<List<String>> sentences = new ArrayList<>();
        List<String> sentence = new ArrayList<>();

        for (String line : tsv) {
            if (line.startsWith("#Text=")) {
                sentences.add(sentence);
                sentence = new ArrayList<>();
            };

            if (!line.isEmpty() && (!line.startsWith("#") || line.startsWith("#Text"))) {
                sentence.add(line);
            }
        }

        return sentences.stream().filter(x -> x.size() > 1);
    }
}
