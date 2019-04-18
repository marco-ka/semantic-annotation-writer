package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.*;
import edu.stanford.nlp.trees.PennTreeReader;

//import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PennTree {
    public static void main(String[] args) throws IOException {
        String file = args[0];
        Tree tree = read(file);
        String rulePath = "C:\\Users\\Marco\\Documents\\Projects\\dkpro-pipeline\\src\\main\\resources\\longRule.txt";
        matchRuleFromFile(tree, rulePath);
//        printLocations(tree);
    }

    public static void matchRuleFromFile(Tree tree, String ruleFile) throws IOException {
        String rule = readFile(ruleFile);
        printMatches(tree, rule);
    }

    private static String readFile(String path) throws IOException {
        BufferedReader buf = new BufferedReader(new FileReader(path));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while(line != null){
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        return sb.toString();
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

            System.out.println(subTree.label());
        }
    }

    public static Tree read(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        PennTreeReader treeReader = new PennTreeReader(reader);

        return treeReader.readTree();
    }
}
