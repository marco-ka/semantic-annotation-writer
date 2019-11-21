package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

import org.jooq.lambda.tuple.Tuple2;

import java.util.List;

public class InceptionAnnotation {
    public InceptionAnnotation(String label, List<Tuple2<String, String>> coveredTokens, int begin, int end) {
        this.label = label;
        this.coveredTokens = coveredTokens;
        this.begin = begin;
        this.end = end;
    }

    public String label;
    public List<Tuple2<String, String>> coveredTokens;
    public int begin;
    public int end;

    @Override
    public String toString() {
        return begin + "-" + end + ": " + label + ": " + coveredTokens;
    }
}
