package at.ac.uibk.marco_kainzner.bachelors_thesis;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.berkeleyparser.BerkeleyParser;
import org.dkpro.core.io.penntree.PennTreebankCombinedReader;
import org.dkpro.core.io.penntree.PennTreebankCombinedWriter;
import org.dkpro.core.io.text.StringReader;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import org.dkpro.core.stanfordnlp.StanfordPosTagger;
import org.dkpro.core.stanfordnlp.StanfordSegmenter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

public class Pipeline {

    public static void main(String[] args) throws UIMAException, IOException {
        var documentId = "fffs_200_statements.txt";
        var fileContent = Files.readString(Path.of("C:/Users/Marco/Documents/Projects/semantic-legal-metadata-annotation/resources/fffs/text/active/fffs_200_statements.txt"));
        var annotationsFile = Path.of("out/annotations.json");
        run(documentId, fileContent, annotationsFile);
    }

    public static void run(String documentId, String casDocumentText, Path annotationOutputFile) throws UIMAException, IOException {
        var pennTreeOutputDir = "out/penn-trees";
        var pennTreeFile = "out/penn-trees/fffs_200_statements.txt.mrg";

        var textReader = createReader(
                TextReader.class, TextReader.PARAM_SOURCE_LOCATION, "C:/Users/Marco/Documents/Projects/semantic-legal-metadata-annotation/resources/fffs/text/active",
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, new String[]{"[+]*.txt"});

        var stringReader = createReader(
                StringReader.class,
                StringReader.PARAM_DOCUMENT_ID, documentId,
                StringReader.PARAM_DOCUMENT_TEXT, casDocumentText,
                StringReader.PARAM_LANGUAGE, "en");

        var pennReader = createReader(PennTreebankCombinedReader.class,
                PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, pennTreeFile);

        var segmenter = createEngineDescription(StanfordSegmenter.class);
        var posTagger = createEngineDescription(StanfordPosTagger.class);
        var ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

        var berkeleyParser = createEngineDescription(BerkeleyParser.class,
                BerkeleyParser.PARAM_WRITE_PENN_TREE, true);

        var pennWriter = createEngineDescription(PennTreebankCombinedWriter.class,
                PennTreebankCombinedWriter.PARAM_TARGET_LOCATION, pennTreeOutputDir,
                PennTreebankCombinedWriter.PARAM_OVERWRITE, true);

        var annotationWriter = createEngineDescription(SemanticAnnotationWriter.class,
                SemanticAnnotationWriter.PARAM_TARGET_LOCATION, annotationOutputFile.toAbsolutePath().toString(),
                SemanticAnnotationWriter.PARAM_OVERWRITE, true);

        SimplePipeline.runPipeline(stringReader, segmenter, posTagger, ner, berkeleyParser, pennWriter);
        SimplePipeline.runPipeline(pennReader, annotationWriter);
    }
}
