package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

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
        var dir = Path.of("resources", "tsv");

        var files = Files.list(dir);
        var file = files.findFirst().get();

        var tsv = Files.readAllLines(file);
        var sentences = segmentIntoSentences(tsv).map(TsvParser::parseSentence);

        sentences.forEach(x -> System.out.println(x + "\n"));
    }

    public static Sentence parseSentence(List<String> sentenceLines) {
        var text = sentenceLines.get(0).replace("#Text=", "");
        var annotationLines = sentenceLines
                .stream()
                .filter(Predicate.not(String::isEmpty))
                .filter(Predicate.not(line -> line.startsWith("#")))
                .map(line -> line.split("\t"))
                .filter(arr -> arr.length > 3)
                .collect(Collectors.toList());

        return new Sentence(text, mergeAnnotationLines(annotationLines));
    }

    private static ArrayList<InceptionAnnotation> mergeAnnotationLines(List<String[]> lines) {
        var labels = lines.stream()
                .flatMap(arr -> Arrays.stream(arr[3].split("\\|")))
                .distinct()
                .filter(x -> !x.equals("_"))
                .collect(Collectors.toList());

        var annotations = new ArrayList<InceptionAnnotation>();
        labels.forEach(label -> {
            var members = lines.stream().filter(x -> x[3].contains(label)).collect(Collectors.toList());

            if (label.contains("[") && label.contains("]")) {
                // Chain: aggregate tokens
                var beginsAndEnds = members.stream().map(member -> member[1]).collect(Collectors.toList());
                var begin = beginsAndEnds.stream().map(x -> Integer.parseInt(x.split("-")[0])).min(Integer::compareTo).get();
                var end = beginsAndEnds.stream().map(x -> Integer.parseInt(x.split("-")[1])).max(Integer::compareTo).get();

                var tokens = members.stream().map(x -> tuple(x[0], x[2])).collect(Collectors.toList());

                var annotation = new InceptionAnnotation(label, tokens, begin, end);
                annotations.add(annotation);
            }
            else {
                // No chain: Each line is a single annotation
                members.forEach(x -> {
                    var beginAndEnd = x[1].split("-");
                    var begin = Integer.parseInt(beginAndEnd[0]);
                    var end = Integer.parseInt(beginAndEnd[1]);

                    var tokens = List.of(tuple(x[0], x[2]));

                    var annotation = new InceptionAnnotation(label, tokens, begin, end);
                    annotations.add(annotation);
                });
            }
        });

//        annotations.forEach(System.out::println);
        return annotations;
    }

    public static Stream<List<String>> segmentIntoSentences(List<String> tsv) {
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
