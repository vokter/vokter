package argus.stopper;

import argus.parser.Parser;
import argus.parser.ParserResult;
import argus.parser.SimpleParser;
import argus.util.Constants;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module that checks if the received textual state of a term corresponds
 * to a stopword loaded from a local file in the format used in
 * http://snowball.tartarus.org
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class SnowballStopwords implements Stopwords {

    private static final Logger logger = LoggerFactory.getLogger(SnowballStopwords.class);

    private Set<MutableString> stopwords;

    @Override
    public void load(String language) {
        try (InputStream is = new FileInputStream(new File(Constants.STOPWORDS_DIR, language));
             Parser parser = new SimpleParser()) {

            Set<MutableString> stopwordsAux = new HashSet<>();
            List<String> lines = IOUtils.readLines(is);
            lines.forEach(line -> {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) {
                    int indexOfPipe = line.indexOf('|');

                    MutableString stopwordLine;
                    if (indexOfPipe == -1) {
                        // there are no pipes in this line
                        // -> add the whole line as a stopword
                        stopwordLine = new MutableString(line);

                    } else if (indexOfPipe > 0) {
                        // there is a pipe in this line and it's not the first char
                        // -> add everything from index 0 to the pipe's index
                        String word = line.substring(0, indexOfPipe).trim();
                        stopwordLine = new MutableString(word);
                    } else {
                        return;
                    }

                    Set<MutableString> stopwordsAtLine = parser
                            .parse(stopwordLine)
                            .parallelStream()
                            .map(sw -> sw.text)
                            .collect(Collectors.toSet());
                    stopwordsAux.addAll(stopwordsAtLine);
                }
            });

            this.stopwords = ImmutableSet.copyOf(stopwordsAux);

        } catch (IOException e) {
            logger.error("There was a problem loading the stopword file.", e);
            this.stopwords = Collections.emptySet();
        }
    }

    @Override
    public void destroy() {
        this.stopwords = null;
    }

    @Override
    public boolean isStopword(MutableString termText) {
        return stopwords.contains(termText);
    }
}
