package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.conll.ConllUWriter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreebankCombinedReader;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreebankCombinedWriter;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

public class Pipeline {

    public static void main(String[] args) throws UIMAException, IOException {
        var documentId = "fffs_200_statements.txt";

        var pennTreeOutputDir = "resources/fffs/penn-trees";
        var matchOutputDir = "out/matches";

        var fileContent = Files.readString(Path.of("C:/Users/Marco/Documents/Projects/dkpro-pipeline/resources/fffs/text/active/fffs_200_statements.txt"));

        var textReader = createReader(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "resources/fffs/text/active",
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, new String[]{"[+]*.txt"});

        var stringReader = createReader(
                StringReader.class,
                StringReader.PARAM_DOCUMENT_ID, documentId,
                StringReader.PARAM_DOCUMENT_TEXT, fileContent,
                StringReader.PARAM_LANGUAGE, "en");

        var pennReader = createReader(PennTreebankCombinedReader.class,
                PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, "resources/fffs/penn-trees/fffs_200_statements.txt.mrg");

        var segmenter = createEngineDescription(StanfordSegmenter.class);
        var posTagger = createEngineDescription(StanfordPosTagger.class);
        var ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

        var berkeleyParser = createEngineDescription(BerkeleyParser.class,
                BerkeleyParser.PARAM_WRITE_PENN_TREE, true);

        var pennWriter = createEngineDescription(PennTreebankCombinedWriter.class,
                PennTreebankCombinedWriter.PARAM_TARGET_LOCATION, pennTreeOutputDir,
                PennTreebankCombinedWriter.PARAM_OVERWRITE, true);

        var conllWriter = createEngineDescription(ConllUWriter.class,
                ConllUWriter.PARAM_TARGET_LOCATION, pennTreeOutputDir,
                ConllUWriter.PARAM_OVERWRITE, true);

        var annotationWriter = createEngineDescription(SemanticAnnotationWriter.class,
                SemanticAnnotationWriter.PARAM_TARGET_LOCATION, matchOutputDir,
                SemanticAnnotationWriter.PARAM_OVERWRITE, true);

//        SimplePipeline.runPipeline(textReader, segmenter, posTagger, ner, berkeleyParser, annotationWriter);
//        SimplePipeline.runPipeline(stringReader, segmenter, posTagger, ner, berkeleyParser, annotationWriter);
        SimplePipeline.runPipeline(pennReader, annotationWriter);
    }
}
