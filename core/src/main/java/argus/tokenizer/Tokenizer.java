package argus.tokenizer;

import argus.stemmer.Stemmer;
import argus.stopper.StopwordsLoader;
import it.unimi.dsi.lang.MutableString;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizer of contents, which separates, stops and stems words separated by
 * the specified character in the constructor. By default, the tokenizer
 * performs word separation by whitespaces.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class Tokenizer {

    private char separator;
    private StopwordsLoader stopwordsLoader;
    private Stemmer stemmer;
    private boolean ignoreCase = false;

    public Tokenizer() {
        this(' ');
    }

    public Tokenizer(char separator) {
        this.separator = separator;
    }

    public void enableStopwords(final StopwordsLoader stopwordsLoader) {
        this.stopwordsLoader = stopwordsLoader;
    }

    public void enableStemming(final Stemmer stemmer) {
        this.stemmer = stemmer;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public List<Result> tokenize(MutableString text) {
        List<Result> retrievedTokens = new ArrayList<>();


        boolean loop = true;
        int startIndex = 0, count = 0, endIndex;
        while (loop) {
            endIndex = text.indexOf(separator, startIndex);

            if (endIndex < 0) {
                // is the last word, add it as a token and stop the loop
                endIndex = text.length();
                loop = false;
            }

            if (startIndex == endIndex) {
                // is empty or the first character in the text is a space, so skip it
                startIndex++;
                continue;
            }

            final MutableString termText = text.substring(startIndex, endIndex);

            // clean trailing spaces
            termText.trim();

            if (ignoreCase) {
                termText.toLowerCase();
            }

            // if after trimming the string is empty, then there
            // is no valuable token to collect
            if (termText.isEmpty()) {
                startIndex = endIndex + 1;
                continue;
            }

            // checks if the text is a stopword
            // if true, do not stem it nor add it to the Result list
            boolean isStopword = false;
            if (stopwordsLoader != null) {
                MutableString textToTest = termText;
                if (!ignoreCase) {
                    textToTest = textToTest.copy().toLowerCase();
                }
                isStopword = stopwordsLoader.isStopword(textToTest);
            }
            if (!isStopword) {

                // stems the term text
                if (stemmer != null) {
                    stemmer.stem(termText);
                }

                retrievedTokens.add(new Result(count, startIndex, endIndex, termText));
            }

            count++;
            startIndex = endIndex + 1;
        }

        return retrievedTokens;
    }


    /**
     * Represents a tokenization result, providing access to a token's phrase
     * position, start position, end position and text.
     */
    public static class Result {

        public final int count;
        public final int start;
        public final int end;
        public final MutableString text;

        private Result(final int count,
                       final int start,
                       final int end,
                       final MutableString text) {
            this.count = count;
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }
}
