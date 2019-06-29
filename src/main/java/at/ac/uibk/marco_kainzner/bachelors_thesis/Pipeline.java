package at.ac.uibk.marco_kainzner.bachelors_thesis;

import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreebankCombinedWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.io.conll.ConllUWriter;
import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

public class Pipeline {

    public static void main(String[] args)
            throws UIMAException, IOException {

        String outputDir = "resources/fffs/penn-trees";

        // http://pdf2md.morethan.io/
        CollectionReader reader = createReader(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "resources/fffs/text/active",
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_PATTERNS, new String[]{"[+]*.txt"});

        AnalysisEngineDescription seg = createEngineDescription(StanfordSegmenter.class);

        AnalysisEngineDescription pos = createEngineDescription(StanfordPosTagger.class);

        AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

        AnalysisEngineDescription berkeleyParser = createEngineDescription(BerkeleyParser.class,
                BerkeleyParser.PARAM_WRITE_PENN_TREE, true);

        AnalysisEngineDescription maltParser = createEngineDescription(MaltParser.class);

        AnalysisEngineDescription pennWriter = createEngineDescription(PennTreebankCombinedWriter.class,
                PennTreebankCombinedWriter.PARAM_TARGET_LOCATION, outputDir,
                PennTreebankCombinedWriter.PARAM_OVERWRITE, true);

        AnalysisEngineDescription conllWriter = createEngineDescription(ConllUWriter.class,
                ConllUWriter.PARAM_TARGET_LOCATION, outputDir,
                ConllUWriter.PARAM_OVERWRITE, true);

//        SimplePipeline.runPipeline(reader, seg, pos, ner, maltParser, berkeleyParser, conllWriter, pennWriter);
        SimplePipeline.runPipeline(reader, seg, pos, ner, berkeleyParser, pennWriter);
    }
}
