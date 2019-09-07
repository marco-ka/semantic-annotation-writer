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
        var fileContent = Files.readString(Path.of("C:/Users/Marco/Documents/Projects/semantic-legal-metadata-annotation/resources/fffs/text/active/fffs_10_statements.txt"));
        var annotationOutputDir = "out/annotations";
        run(documentId, fileContent, annotationOutputDir);
    }

    public static void run(String documentId, String casDocumentText, String annotationOutputDir) throws UIMAException, IOException {
//        var textReader = createReader(
//                TextReader.class,
//                                TextReader.PARAM_SOURCE_LOCATION, "C:/Users/Marco/Documents/Projects/semantic-legal-metadata-annotation/resources/fffs/text/active",
//                TextReader.PARAM_LANGUAGE, "en",
//                TextReader.PARAM_PATTERNS, new String[]{"[+]*.txt"});

        var stringReader = createReader(
                StringReader.class,
                StringReader.PARAM_DOCUMENT_ID, documentId,
                StringReader.PARAM_DOCUMENT_TEXT, casDocumentText,
                StringReader.PARAM_LANGUAGE, "en");

        var pennLocation = "C:/Users/Marco/Documents/Projects/semantic-legal-metadata-annotation/resources/fffs/penn-trees/fffs_200_statements.txt.mrg";
        var pennReader = createReader(PennTreebankCombinedReader.class,
                PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, pennLocation);

        var segmenter = createEngineDescription(StanfordSegmenter.class);
        var posTagger = createEngineDescription(StanfordPosTagger.class);
        var ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

        var berkeleyParser = createEngineDescription(BerkeleyParser.class,
                BerkeleyParser.PARAM_WRITE_PENN_TREE, true);

        var annotationWriter = createEngineDescription(SemanticAnnotationWriter.class,
                SemanticAnnotationWriter.PARAM_TARGET_LOCATION, annotationOutputDir,
                SemanticAnnotationWriter.PARAM_OVERWRITE, true);

//        SimplePipeline.runPipeline(textReader, segmenter, posTagger, ner, berkeleyParser, annotationWriter);
//        SimplePipeline.runPipeline(pennReader, annotationWriter);

        SimplePipeline.runPipeline(stringReader, segmenter, posTagger, ner, berkeleyParser, annotationWriter);
    }
}
