package at.ac.uibk.marco_kainzner.bachelors_thesis;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.berkeleyparser.BerkeleyParser;
import org.dkpro.core.io.penntree.PennTreebankCombinedReader;
import org.dkpro.core.io.penntree.PennTreebankCombinedWriter;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import org.dkpro.core.stanfordnlp.StanfordPosTagger;
import org.dkpro.core.stanfordnlp.StanfordSegmenter;

public class Pipeline {

    public static void main(String[] args) throws UIMAException, IOException {
        var input = Path.of("resources/fffs/text");
        var annotationsFile = Path.of("out/annotations.json");

        run(input, annotationsFile);
    }

    public static void run(Path inputDir, Path outputFile) throws UIMAException, IOException {
        var pennTreeDir = Path.of("out/penn-trees");
        writePennTrees(inputDir, pennTreeDir);
        writeAnnotations(pennTreeDir, outputFile);
    }

    public static void writePennTrees(Path inputDir, Path outputDir) throws UIMAException, IOException {
        FileUtils.deleteDirectory(outputDir.toFile());

        var textReader = createReader(
                TextReader.class, TextReader.PARAM_SOURCE_LOCATION, inputDir.toString(),
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, new String[]{"[+]*.txt"});

        var segmenter = createEngineDescription(StanfordSegmenter.class);
        var posTagger = createEngineDescription(StanfordPosTagger.class);
        var ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

        var berkeleyParser = createEngineDescription(BerkeleyParser.class,
                BerkeleyParser.PARAM_WRITE_PENN_TREE, true);

        var pennWriter = createEngineDescription(PennTreebankCombinedWriter.class,
                PennTreebankCombinedWriter.PARAM_TARGET_LOCATION, outputDir.toString(),
                PennTreebankCombinedWriter.PARAM_OVERWRITE, true);

        System.out.println(new Date() + " Writing Penn Trees ...");
        SimplePipeline.runPipeline(textReader, segmenter, posTagger, ner, berkeleyParser, pennWriter);
        System.out.println(new Date() + " Done");
    }

    public static void writeAnnotations(Path inputDir, Path outputFile) throws UIMAException, IOException {
        var pennReader = createReader(PennTreebankCombinedReader.class,
                PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, inputDir.toString() + "/*");

        var annotationWriter = createEngineDescription(SemanticAnnotationWriter.class,
                SemanticAnnotationWriter.PARAM_TARGET_LOCATION, outputFile.toString(),
                SemanticAnnotationWriter.PARAM_OVERWRITE, true);

        System.out.println(new Date() + " Writing Annotations ...");
        SimplePipeline.runPipeline(pennReader, annotationWriter);
        System.out.println(new Date() + " Done");
    }
}
