package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

import at.ac.uibk.marco_kainzner.bachelors_thesis.Annotation;
import at.ac.uibk.marco_kainzner.bachelors_thesis.SimpleAnnotation;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;
import java.util.stream.Collectors;

public class InceptionAnnotation {
    public final int sentenceStart;
    public final int sentenceId;

    public final String sentenceText;
    public final String label;
    public final List<Tuple2<String, String>> coveredTokens;
    public final int begin;
    public final int end;

    public InceptionAnnotation(int sentenceStart, int sentenceId, String sentenceText, String label, List<Tuple2<String, String>> coveredTokens, int begin, int end) {
        this.sentenceStart = sentenceStart;
        this.sentenceId = sentenceId;
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

    public SimpleAnnotation toSimpleAnnotation() {
        return new SimpleAnnotation(sentenceText, label, begin, end);
    }

    public Annotation toAnnotation(String documentId) {
        var relativeBegin = begin - sentenceStart;
        var relativeEnd = end - sentenceStart;

        return new Annotation(documentId, sentenceId, sentenceText, normalizedLabel(), relativeBegin, relativeEnd);
    }

    private String normalizedLabel() {
        var begin = label.lastIndexOf('[');
        var end = label.lastIndexOf(']');
        if (begin == -1 || end == -1 || end <= begin)
            return label;

        return label.substring(0, begin);
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
