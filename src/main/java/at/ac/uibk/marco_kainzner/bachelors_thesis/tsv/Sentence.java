package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

import at.ac.uibk.marco_kainzner.bachelors_thesis.Annotation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sentence {
    public final String documentId;
    public String text;
    public List<InceptionAnnotation> annotations;

    public Sentence(String documentId, String text, List<InceptionAnnotation> annotations) {
        this.documentId = documentId;
        this.text = text;
        this.annotations = annotations;
    }

    public Stream<Annotation> GetAnnotations() {
        return annotations.stream().map(x -> x.toAnnotation(documentId));
    }

    @Override
    public String toString() {
        var annotations = this.annotations.stream()
                .map(InceptionAnnotation::toString)
                .collect(Collectors.joining("\n"));
        return "Text: " + text + "\n" + annotations;
    }
}
