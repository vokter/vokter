package argus.stopper;

import argus.tokenizer.Tokenizer;
import argus.util.Constants;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class SnowballStopwordsLoader implements StopwordsLoader {

    private static final Logger logger = LoggerFactory.getLogger(SnowballStopwordsLoader.class);

    private Set<MutableString> stopwords;


    @Override
    public void load(String language) {
        try {
            this.stopwords = loadSnowballFile(new FileInputStream(
                    new File(Constants.STOPWORDS_DIR, language)
            ));
            System.out.println(stopwords.toString());
        } catch (FileNotFoundException e) {
            this.stopwords = Collections.emptySet();
        }
    }


    @Override
    public void destroy() {
        this.stopwords.clear();
        this.stopwords = null;
    }


    @Override
    public boolean isStopword(MutableString termText) {
        return stopwords.contains(termText);
    }


    private static Set<MutableString> loadSnowballFile(InputStream stopwordsFileStream) {
        Set<MutableString> stopwords = new HashSet<>();
        try {
            List<String> lines = IOUtils.readLines(stopwordsFileStream);
            lines.forEach(line -> {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) {
                    int indexOfPipe = line.indexOf('|');

                    MutableString stopword;
                    if (indexOfPipe == -1) {
                        // there are no pipes in this line
                        // -> add the whole line as a stopword
                        stopword = new MutableString(line);

                    } else if (indexOfPipe > 0) {
                        // there is a pipe in this line and it's not the first char
                        // -> add everything from index 0 to the pipe's index
                        String word = line.substring(0, indexOfPipe).trim();
                        stopword = new MutableString(word);
                    } else {
                        return;
                    }

                    List<Tokenizer.Result> results = new Tokenizer().tokenize(stopword);
                    results.parallelStream().map(sw -> sw.text).forEach(stopwords::add);
                }
            });
        } catch (IOException e) {
            logger.error("There was a problem loading the stopword file.", e);
        }
        return ImmutableSet.copyOf(stopwords);
    }
}
