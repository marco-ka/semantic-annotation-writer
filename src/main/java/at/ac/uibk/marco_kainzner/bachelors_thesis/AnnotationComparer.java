package at.ac.uibk.marco_kainzner.bachelors_thesis;

import at.ac.uibk.marco_kainzner.bachelors_thesis.tsv.Document;
import at.ac.uibk.marco_kainzner.bachelors_thesis.tsv.TsvParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class AnnotationComparer {
    private static final Path groundTruthPath = Path.of("resources","tsv");
    private static final Path annotationPath = Path.of("out","annotations");

    public static void main(String[] args) throws IOException {
        var expected = TsvParser.readDocuments(groundTruthPath).collect(Collectors.toList());

        for (var doc : expected) {
            System.out.println("Trying to read doc " + doc.documentId + " ...");
            var path = Path.of(annotationPath.toString(), doc.documentId + ".json");

            var actual = Document.readJson(path);
            System.out.println("Success: " + actual.documentId + ";" + actual.annotations.size());
        }
    }
}
