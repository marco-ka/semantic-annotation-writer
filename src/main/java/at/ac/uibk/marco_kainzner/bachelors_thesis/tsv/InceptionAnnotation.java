package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

import at.ac.uibk.marco_kainzner.bachelors_thesis.Annotation;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;
import java.util.stream.Collectors;

public class InceptionAnnotation {
    public final String sentenceText;
    public final String label;
    public final List<Tuple2<String, String>> coveredTokens;
    public final int begin;
    public final int end;

    public InceptionAnnotation(String sentenceText, String label, List<Tuple2<String, String>> coveredTokens, int begin, int end) {
        this.sentenceText = sentenceText;
        this.label = label;
        this.coveredTokens = coveredTokens;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String toString() {
        return begin + "-" + end + ": " + label + ": " + coveredTokens;
    }

    public Annotation toAnnotation() {
        return new Annotation("inception-annotation", getSentenceId(), "_", getCoveredText(), label, begin, end);
    }

    private String getCoveredText() {
        return coveredTokens.stream().map(x -> x.v2).collect(Collectors.joining());
    }

    private int getSentenceId() {
        var anyAnnotationId = coveredTokens.get(0).v1;
        var sentenceId = anyAnnotationId.split("-")[0];
        return Integer.parseInt(sentenceId);
    }

}
