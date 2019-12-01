package at.ac.uibk.marco_kainzner.bachelors_thesis;

import at.ac.uibk.marco_kainzner.bachelors_thesis.tsv.InceptionAnnotation;
import at.ac.uibk.marco_kainzner.bachelors_thesis.tsv.TsvParser;

import java.io.IOException;
import java.nio.file.Path;

public class AnnotationComparer {
    private static final Path groundTruthPath = Path.of("resources","tsv");

    public static void main(String[] args) throws IOException {
        var groundTruth = TsvParser.parseFolder(groundTruthPath);
        var firstFile = groundTruth.findFirst().get();
        var expected = firstFile.map(doc -> doc.Annotations.stream().map(InceptionAnnotation::toAnnotation));

    }
}
