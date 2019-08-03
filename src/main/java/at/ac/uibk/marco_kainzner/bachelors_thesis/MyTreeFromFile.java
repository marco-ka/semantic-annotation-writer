package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.gui.TreeFromFile;

public class MyTreeFromFile extends TreeFromFile {
    private Tree tree;
    private String treeString;

    public MyTreeFromFile(Tree t, String filename) {
        super(t, filename);
        this.tree = t;
        this.treeString = t.pennString();
    }

    @Override
    public Tree getTree() {
        return Tree.valueOf(treeString);
    }

    public String getString() {
        return this.treeString;
    }
}
