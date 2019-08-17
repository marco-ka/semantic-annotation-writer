package at.ac.uibk.marco_kainzner.bachelors_thesis;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeVisitor;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.Generics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// import edu.stanford.nlp.trees.tregex.gui.InputPanel.TRegexGUITreeVisitor;
// From package edu.stanford.nlp.trees.tregex.gui
// The only change is: using class MyTreeFromFile instead of TreeFromFile
public class TRegexGUITreeVisitor implements TreeVisitor {

    private int totalMatches; // = 0;
    private final TregexPattern p;
    private final List<MyTreeFromFile> matchedTrees;
    private final Map<MyTreeFromFile,List<Tree>> matchedParts;
    private String filename = "";

    TRegexGUITreeVisitor(TregexPattern p) {
        this.p = p;
        matchedTrees = new ArrayList<>();
        matchedParts = Generics.newHashMap();
    }

    public Map<MyTreeFromFile,List<Tree>> getMatchedParts() {
        return matchedParts;
    }

    public void visitTree(Tree t) {
        int numMatches = 0;
        TregexMatcher match = p.matcher(t);
        List<Tree> matchedPartList = null; // initialize lazily, since usually most trees don't match!
        while (match.find()) {
            Tree curMatch = match.getMatch();
            //System.out.println("Found match is: " + curMatch);
            if (matchedPartList == null) matchedPartList = new ArrayList<>();
            matchedPartList.add(curMatch);
            numMatches++;
        } // end while match.find()
        if(numMatches > 0) {
            MyTreeFromFile tff = new MyTreeFromFile(t, filename);
            matchedTrees.add(tff);
            matchedParts.put(tff,matchedPartList);
            totalMatches += numMatches;
        }
    } // end visitTree

    /**
     * Method for returning the number of matches found in the last tree
     * visited by this tree visitor.
     * @return number of matches found in previous tree
     */
    public int numUniqueMatches() {
        return totalMatches;
    }

    public List<MyTreeFromFile> getMatches() {
        return matchedTrees;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String curFilename) {
        this.filename = curFilename.intern();
    }

    public TregexPattern getPattern() {
        return p;
    }

} // end class TRegexTreeVisitor
