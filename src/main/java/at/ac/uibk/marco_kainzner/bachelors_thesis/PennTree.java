package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.*;
import edu.stanford.nlp.trees.PennTreeReader;

import java.io.*;

public class PennTree {
    public static void main(String[] args) throws IOException {
        String file = args[0];
        Tree tree = read(file);
        printLocations(tree);
    }

    public static void printLocations(Tree tree) {
        String rule = "NP < (NN < territory|municipality) | NP < (NNS < roads|agglomerations)";
        printMatches(tree, rule);
    }

    private static void printMatches(Tree tree, String tregexPattern) {
        TregexPattern p = TregexPattern.compile(tregexPattern);
        TregexMatcher m = p.matcher(tree);

        while (m.find()) {
            Tree subTree = m.getMatch();
            System.out.println(subTree.toString());
        }
    }

    public static Tree read(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        PennTreeReader treeReader = new PennTreeReader(reader);

        return treeReader.readTree();
    }
}
