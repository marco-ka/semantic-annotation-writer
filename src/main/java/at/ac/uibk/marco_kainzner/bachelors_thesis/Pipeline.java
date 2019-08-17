package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.conll.ConllUWriter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreebankCombinedReader;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreebankCombinedWriter;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

public class Pipeline {

    public static void main(String[] args) throws UIMAException, IOException {

        var outputDir = "resources/fffs/penn-trees";

        var textReader = createReader(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "resources/fffs/text/active",
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, new String[]{"[+]*.txt"});

        var pennReader = createReader(PennTreebankCombinedReader.class,
                PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, "resources/fffs/penn-trees/fffs_200_statements.txt.mrg");

        var segmenter = createEngineDescription(StanfordSegmenter.class);
        var posTagger = createEngineDescription(StanfordPosTagger.class);
        var ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

        var berkeleyParser = createEngineDescription(BerkeleyParser.class,
                BerkeleyParser.PARAM_WRITE_PENN_TREE, true);

        var pennWriter = createEngineDescription(PennTreebankCombinedWriter.class,
                PennTreebankCombinedWriter.PARAM_TARGET_LOCATION, outputDir,
                PennTreebankCombinedWriter.PARAM_OVERWRITE, true);

        var conllWriter = createEngineDescription(ConllUWriter.class,
                ConllUWriter.PARAM_TARGET_LOCATION, outputDir,
                ConllUWriter.PARAM_OVERWRITE, true);

        var annotationWriter = createEngineDescription(SemanticAnnotationWriter.class,
                SemanticAnnotationWriter.PARAM_TARGET_LOCATION, "out/matches",
                SemanticAnnotationWriter.PARAM_OVERWRITE, true);

//        SimplePipeline.runPipeline(textReader, segmenter, posTagger, ner, berkeleyParser, annotationWriter);
        SimplePipeline.runPipeline(pennReader, annotationWriter);
    }
}
