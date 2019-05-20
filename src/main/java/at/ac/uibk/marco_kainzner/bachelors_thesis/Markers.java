package at.ac.uibk.marco_kainzner.bachelors_thesis;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class Markers {
    static Set<String> fromFile(String path) {
        try {
            List<String> markers = FileUtils.readLines(new File(path), Charset.defaultCharset());
            return new TreeSet<>(markers);
        } catch (IOException e) {
            e.printStackTrace();
            return new TreeSet<>();
        }
    }
}
