package argus.util;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A loader class that parses a stopword file.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class StopwordFileLoader {

    private static final Logger logger = LoggerFactory.getLogger(StopwordFileLoader.class);

    public Set<MutableString> load(InputStream stopwordsFileStream) {
        Set<MutableString> stopwords = new HashSet<>();
        try {
            List<String> lines = IOUtils.readLines(stopwordsFileStream);
            lines.forEach(line -> {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) {
                    int indexOfPipe = line.indexOf('|');

                    if (indexOfPipe == -1) {
                        // there are no pipes in this line
                        // -> add the whole line as a stopword
                        stopwords.add(new MutableString(line));

                    } else if (indexOfPipe > 0) {
                        // there is a pipe in this line and it's not the first char
                        // -> add everything from index 0 to the pipe's index
                        String word = line.substring(0, indexOfPipe).trim();
                        stopwords.add(new MutableString(word));
                    }
                }
            });
        } catch (IOException e) {
            logger.error("There was a problem detecting the currently implemented Reader classes.", e);
        }
        return ImmutableSet.copyOf(stopwords);
    }
}
