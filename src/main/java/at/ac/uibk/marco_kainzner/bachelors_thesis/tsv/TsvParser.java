package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

import com.google.gson.GsonBuilder;

import java.io.FileWriter;
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

        Files.list(inputDir)
            .filter(Files::isRegularFile)
            .forEach(tsvFile -> toJson(tsvFile, outputDir));
    }

    public static void toJson(Path tsvFile, Path outputDir) {
        System.out.println("Parsing " + tsvFile.toString());

        List<Sentence> sentences = null;
        try {
            sentences = parseFile(tsvFile);
        } catch (IOException e) {
            System.out.println("Failed to parse file " + tsvFile.toString());
            e.printStackTrace();
        }

        var outputFile = Path.of(outputDir.toString(), tsvFile.getFileName().toString());

        try {
            exportJson(outputFile, sentences);
        } catch (IOException e) {
            System.out.println("Failed to export json " + outputFile.toString());
            e.printStackTrace();
        }
    }

    public static void exportJson(Path file, List<Sentence> sentences) throws IOException {
        var gson = new GsonBuilder().setPrettyPrinting().create();

        var writer = new FileWriter(file.toString() + ".json");
        var annotations = sentences.stream().flatMap(Sentence::GetAnnotations);

        var json = gson.toJson(annotations.collect(Collectors.toList()));
        writer.write(json);
        writer.close();
    }

    public static Stream<List<Sentence>> parseFolder(Path path) throws IOException {
        var files = Files.list(path);
        return files.map(file -> {
            try {
                return TsvParser.parseFile(file);
            } catch (IOException e) {
                System.out.println("Failed to parse sentence");
                e.printStackTrace();
            }
            return null;
        });
    }

    public static List<Sentence> parseFile(Path path) throws IOException {
        var tsv = Files.readAllLines(path);

        var documentId = path.getFileName().toString();

        List<Sentence> sentences = new ArrayList<>();
        segmentIntoSentences(tsv).forEach(x -> sentences.add(parseSentence(documentId, x)));

        return sentences;
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

//        annotations.forEach(System.out::println);
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
