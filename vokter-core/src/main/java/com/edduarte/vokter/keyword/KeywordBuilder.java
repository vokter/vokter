/*
 * Copyright 2015 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.vokter.keyword;

import com.edduarte.vokter.parser.Parser;
import com.edduarte.vokter.parser.ParserPool;
import com.google.common.base.Stopwatch;
import com.optimaize.langdetect.LanguageDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class that loads an input text and processes this into a
 * {@link Keyword} structure.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public final class KeywordBuilder {

    private static final Logger logger = LoggerFactory.getLogger(KeywordBuilder.class);

    private final String keywordInput;

    /**
     * The language detector that will assure that the right Stopword filter
     * and Stemmer are used for the input content.
     */
    private LanguageDetector langDetector;

    /**
     * Flag that sets usage of stopword filtering.
     */
    private boolean isStoppingEnabled;

    /**
     * Flag that sets usage of a porter stemmer.
     */
    private boolean isStemmingEnabled;

    /**
     * Flag that sets matching of equal occurrences with different casing.
     */
    private boolean ignoreCase;


    private KeywordBuilder(final String keywordInput) {
        this.keywordInput = keywordInput;
        this.isStoppingEnabled = false;
        this.isStemmingEnabled = false;
        this.ignoreCase = false;
    }


    public static KeywordBuilder fromText(final String keywordInput) {
        return new KeywordBuilder(keywordInput);
    }


    public KeywordBuilder withLanguageDetector(final LanguageDetector langDetector) {
        this.langDetector = langDetector;
        return this;
    }


    public KeywordBuilder withStopwords() {
        this.isStoppingEnabled = true;
        return this;
    }


    public KeywordBuilder withStemming() {
        this.isStemmingEnabled = true;
        return this;
    }


    public KeywordBuilder ignoreCase() {
        this.ignoreCase = true;
        return this;
    }


    public Keyword build(ParserPool parserPool) {
        Stopwatch sw = Stopwatch.createStarted();

        // step 3) Takes a parser from the parser-pool.
        Parser parser;
        try {
            parser = parserPool.take();
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

        KeywordPipeline pipeline = new KeywordPipeline(

                // the language detection model
                langDetector,

                // the textual input of the keyword
                keywordInput,

                // the parser that will be used for query parsing and term
                // detection
                parser,

                // the set of stopwords that will be filtered during tokenization
                isStoppingEnabled,

                // the stemmer class that will be used to stem the detected tokens
                isStemmingEnabled,

                // flag that forces every found token to be
                // lower case, matching, for example, the words
                // 'be' and 'Be' as the same token
                ignoreCase
        );

        // step 5) Process the document asynchronously.
        Keyword aux;
        try {
            aux = pipeline.call();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        final Keyword keyword = aux;

        // step 6) Place the parser back in the parser-pool.
        try {
            parserPool.place(parser);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

        logger.info("Completed building keywords '{}' in {}",
                keywordInput, sw.toString());
        return keyword;
    }
}
