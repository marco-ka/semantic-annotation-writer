package at.ac.uibk.marco_kainzner.bachelors_thesis;

import at.ac.uibk.marco_kainzner.bachelors_thesis.tsv.Document;
import at.ac.uibk.marco_kainzner.bachelors_thesis.tsv.TsvParser;
import edu.stanford.nlp.util.Pair;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationComparer {
    private static final Path groundTruthPath = Path.of("resources","tsv");
    private static final Path annotationPath = Path.of("out","annotations");

    public static void main(String[] args) throws IOException {
        var expected = TsvParser.readDocuments(groundTruthPath).collect(Collectors.toList());

        var matches = new ArrayList<Pair<Annotation, Annotation>>();
        var misses = new ArrayList<Annotation>();

        var actual = new ArrayList<Document>();

        for (var expectedDoc : expected) {
            var path = Path.of(AnnotationComparer.annotationPath.toString(), expectedDoc.documentId + ".json");
            var actualDoc = Document.readJson(path);
            actual.add(actualDoc);

            var results = compare(expectedDoc, actualDoc);
            matches.addAll(results.first);
            misses.addAll(results.second);
        }

        var actualAnnotations = actual.stream().flatMap(x -> x.annotations.stream()).collect(Collectors.toList());
        var additional = actual.stream().flatMap(x -> x.annotations.stream()).collect(Collectors.toList());

        var actualMatches = matches.stream().map(x -> x.second).collect(Collectors.toList());
        additional.removeAll(actualMatches);

        System.out.println();
        System.out.println("...");
        System.out.println();
        System.out.println("DONE with all documents");
        System.out.println();
        System.out.println("...");
        System.out.println();
        System.out.println("Actual annotations: " + actualAnnotations.size());
        System.out.println();
        System.out.println("Total matches: " + matches.size());
        System.out.println("Total misses: " + misses.size());
        System.out.println("Total additional: " + additional.size());

        var matchesByConcept = matches.stream().collect(Collectors.groupingBy(x -> x.first.annotation.label));
        var missesByConcept = misses.stream().collect(Collectors.groupingBy(x -> x.annotation.label));
        var additionalByConcept = additional.stream().collect(Collectors.groupingBy(x -> x.annotation.label));
        var actualByConcept = actualAnnotations.stream().collect(Collectors.groupingBy(x -> x.annotation.label));

        var concepts = new HashSet<String>();
        concepts.addAll(matchesByConcept.keySet());
        concepts.addAll(missesByConcept.keySet());
        concepts.addAll(actualByConcept.keySet());

        for (var concept : concepts) {
            System.out.println();
            System.out.println("---");
            System.out.println("CONCEPT: " + concept.toUpperCase());
            System.out.println();
            var conceptMatches = matchesByConcept.containsKey(concept) ? matchesByConcept.get(concept).size() : 0;
            System.out.println("Matches: " + conceptMatches);
            var conceptMisses = missesByConcept.containsKey(concept) ? missesByConcept.get(concept).size() : 0;
            System.out.println("Misses: " + conceptMisses);
            var conceptAdditional = additionalByConcept.containsKey(concept) ? additionalByConcept.get(concept).size() : 0;
            System.out.println("Additional: " + conceptAdditional);

            var conceptActual = actualByConcept.containsKey(concept) ? actualByConcept.get(concept).size() : 0;
            System.out.println("(" + conceptActual + " actual annotations)");
        }
    }

    public static Pair<List<Pair<Annotation, Annotation>>, List<Annotation>> compare(Document expected, Document actual) {
        System.out.println();
        System.out.println("BEGIN DOCUMENT " + expected.documentId);

        var concepts = expected.annotations.stream().collect(Collectors.groupingBy(x -> x.annotation.label)).keySet();

        var results = concepts.stream().map(concept -> compareConcept(concept, expected, actual)).collect(Collectors.toList());

        var matched = results.stream().flatMap(x -> x.first.stream()).collect(Collectors.toList());
        var missed = results.stream().flatMap(x -> x.second.stream()).collect(Collectors.toList());

        return new Pair<>(matched, missed);
    }

    public static Pair<List<Pair<Annotation, Annotation>>, List<Annotation>> compareConcept(String concept, Document expectedDoc, Document actualDoc) {
        var verbose = false;

        System.out.println();
        System.out.println("---");
        System.out.println("Concept " + concept.toUpperCase());

        var expected = concept(concept, expectedDoc);
        var actual = concept(concept, actualDoc);

        var missed = new ArrayList<Annotation>();
        var matched = new ArrayList<Pair<Annotation, Annotation>>();

        if (!expectedDoc.documentId.equals(actualDoc.documentId))
            throw new RuntimeException("Documents don't match: " + expectedDoc.documentId + " - " + actualDoc.documentId);

        for (var expectedAnnotation : expected) {
            var matches = actual.stream().filter(x -> x.matches(expectedAnnotation)).collect(Collectors.toList());

            if (matches.isEmpty()) {
                missed.add(expectedAnnotation);
            } else if (matches.size() == 1) {
                matched.add(new Pair<>(expectedAnnotation, matches.get(0)));
            } else {
                if (verbose) {
                    System.out.println("  Multiple matches: for annotation: " + expectedAnnotation);
                    matches.forEach(x -> System.out.println("  - " + x));
                    System.out.println("  -> Saving first match");
                }
                matched.add(new Pair<>(expectedAnnotation, matches.get(0)));
            }
        }

        System.out.println();
        System.out.println("Expected " + concept + " annotations: " + expected.size());
        System.out.println("Actual " + concept + " annotations: " + actual.size());

        System.out.println();
        System.out.println("Misses: " + missed.size());
        System.out.println("Matches: " + matched.size());

        if (verbose) {
            System.out.println();
            matched.forEach(x -> System.out.println(x.first + " == " + x.second));
        }

        return new Pair<>(matched, missed);
    }

    public static List<Annotation> concept(String concept, Document doc) {
        return doc.annotations.stream()
                .filter(x -> x.annotation.label.toLowerCase().equals(concept.toLowerCase()))
                .collect(Collectors.toList());
    }
}
