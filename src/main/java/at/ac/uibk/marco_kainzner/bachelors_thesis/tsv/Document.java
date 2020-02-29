package at.ac.uibk.marco_kainzner.bachelors_thesis.tsv;

import at.ac.uibk.marco_kainzner.bachelors_thesis.Annotation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static at.ac.uibk.marco_kainzner.bachelors_thesis.AnnotationComparer.removeOverlapping;

public class Document {
    public final String documentId;
    public final List<Annotation> annotations;

    public Document(String documentId, List<Annotation> annotations) {
        this.documentId = documentId;
        this.annotations = annotations;
    }

    public void saveToDir(Path dir) throws IOException {
        var gson = new GsonBuilder().setPrettyPrinting().create();

        var file = Path.of(dir.toString(), documentId + ".json");
        var writer = new FileWriter(file.toString());

        var json = gson.toJson(annotations);
        writer.write(json);
        writer.close();
    }

    public static Document readJson(Path file) throws FileNotFoundException {
        var gson = new Gson();

        var documentId = FilenameUtils.removeExtension(file.getFileName().toString());

        var listOfAnnotations = new TypeToken<ArrayList<Annotation>>() {}.getType();

        var reader = new FileReader(file.toFile());
        List<Annotation> annotations = gson.fromJson(reader, listOfAnnotations);

        var uniqueAnnotations = annotations.stream().distinct().collect(Collectors.toList());
        var uniqueAnnotations2 = removeOverlapping(uniqueAnnotations);

        return new Document(documentId, uniqueAnnotations2);
    }
}
