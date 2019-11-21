package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

import java.util.List;
import java.util.stream.Collectors;

public class Sentence {
    public String Text;
    public List<InceptionAnnotation> Annotations;

    public Sentence(String text, List<InceptionAnnotation> annotations) {
        Text = text;
        Annotations = annotations;
    }

    @Override
    public String toString() {
        var annotations = Annotations.stream()
                .map(InceptionAnnotation::toString)
                .collect(Collectors.joining("\n"));
        return "Text: " + Text + "\n" + annotations;
    }
}
