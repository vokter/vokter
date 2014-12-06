package argus.parser;

import argus.stemmer.Stemmer;
import argus.stopper.Stopwords;
import it.unimi.dsi.lang.MutableString;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple tokenizer implementation, which separates, stops and stems words
 * separated by the specified character in the constructor. By default, the
 * tokenizer performs word separation by whitespaces.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class SimpleParser implements Parser {

    private final char separator;

    public SimpleParser() {
        this(' ');
    }

    public SimpleParser(char separator) {
        this.separator = separator;
    }

    @Override
    public List<ParserResult> parse(final MutableString text,
                                    final Stopwords stopwords,
                                    final Stemmer stemmer,
                                    final boolean ignoreCase) {
        List<ParserResult> retrievedTokens = new ArrayList<>();

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
            // if true, do not stem it nor add it to the ParserResult list
            boolean isStopword = false;
            if (stopwords != null) {
                MutableString textToTest = termText;
                if (!ignoreCase) {
                    textToTest = textToTest.copy().toLowerCase();
                }
                isStopword = stopwords.isStopword(textToTest);
            }
            if (!isStopword) {

                // stems the term text
                if (stemmer != null) {
                    stemmer.stem(termText);
                }

                retrievedTokens.add(new ParserResult(count, startIndex, endIndex, termText));
            }

            count++;
            startIndex = endIndex + 1;
        }

        return retrievedTokens;
    }

    @Override
    public void close() {
    }
}
