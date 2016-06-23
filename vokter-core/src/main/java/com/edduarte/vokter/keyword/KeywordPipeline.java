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

import com.edduarte.vokter.cleaner.AndCleaner;
import com.edduarte.vokter.cleaner.Cleaner;
import com.edduarte.vokter.cleaner.DiacriticCleaner;
import com.edduarte.vokter.cleaner.SpecialCharsCleaner;
import com.edduarte.vokter.parser.Parser;
import com.edduarte.vokter.stemmer.Stemmer;
import com.edduarte.vokter.stopper.FileStopper;
import com.edduarte.vokter.stopper.Stopper;
import com.edduarte.vokter.util.PluginLoader;
import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * A processing pipeline that reads, filters and tokenizes a text input,
 * specifically a query.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class KeywordPipeline implements Callable<Keyword> {

    private static final Logger logger = LoggerFactory.getLogger(KeywordPipeline.class);

    private final LanguageDetector langDetector;

    private final String queryInput;

    private final Parser parser;

    private final boolean isStoppingEnabled;

    private final boolean isStemmingEnabled;

    private final boolean ignoreCase;


    public KeywordPipeline(final LanguageDetector langDetector,
                           final String queryInput,
                           final Parser parser,
                           final boolean isStoppingEnabled,
                           final boolean isStemmingEnabled,
                           final boolean ignoreCase) {
        this.langDetector = langDetector;
        this.queryInput = queryInput;
        this.parser = parser;
        this.isStoppingEnabled = isStoppingEnabled;
        this.isStemmingEnabled = isStemmingEnabled;
        this.ignoreCase = ignoreCase;
    }


    @Override
    public Keyword call() throws Exception {

        final MutableString content = new MutableString(queryInput);

        // filters the contents by cleaning characters of whole strings
        // according to each cleaner's implementation
        Cleaner cleaner = AndCleaner.of(new SpecialCharsCleaner(), new DiacriticCleaner());
        cleaner.clean(content);
        cleaner = null;


        // infers the document language
        String languageCode = "en";
        if (langDetector != null) {
            TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
            TextObject textObject = textObjectFactory.forText(content);
            Optional<LdLocale> lang = langDetector.detect(textObject);
            languageCode = lang.isPresent() ? lang.get().getLanguage() : "en";
        }


        // sets the parser's stopper according to the detected language
        // if the detected language is not supported, stopping is ignored
        Stopper stopper = null;
        if (isStoppingEnabled) {
            stopper = new FileStopper(languageCode);
            if (stopper.isEmpty()) {
                // if no compatible stopwords were found, use the english stopwords
                stopper = new FileStopper("en");
            }
        }


        // sets the parser's stemmer according to the detected language
        // if the detected language is not supported, stemming is ignored
        Stemmer stemmer = null;
        if (isStemmingEnabled) {
            Class<? extends Stemmer> stemmerClass = PluginLoader.getCompatibleStemmer(languageCode);
            if (stemmerClass != null) {
                stemmer = stemmerClass.newInstance();
            } else {
                // if no compatible stemmers were found, use the english stemmer
                stemmerClass = PluginLoader.getCompatibleStemmer("en");
                if (stemmerClass != null) {
                    stemmer = stemmerClass.newInstance();
                }
            }
        }


        // detects tokens from the document and loads them into separate
        // objects in memory
        List<Parser.Result> results = parser.parse(content, stopper, stemmer, ignoreCase);
        content.delete(0, content.length());

        if (stopper != null) {
            stopper.destroy();
        }

        if (stemmer != null) {
            stemmer = null;
        }


        // create a temporary in-memory term structure and converts parser results
        // into Term objects
        final Set<String> terms = new LinkedHashSet<>();
        for (Parser.Result r : results) {
            MutableString termText = r.text;
            terms.add(termText.toString());
        }
        results.clear();
        results = null;


        // adds the terms to the keyword object
        return new Keyword(queryInput, terms);
    }
}
