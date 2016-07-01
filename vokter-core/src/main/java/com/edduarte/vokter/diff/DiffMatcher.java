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

package com.edduarte.vokter.diff;

import com.edduarte.vokter.model.mongodb.Diff;
import com.edduarte.vokter.model.mongodb.Keyword;
import com.edduarte.vokter.parser.Parser;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.stemmer.Stemmer;
import com.edduarte.vokter.stopper.FileStopper;
import com.edduarte.vokter.stopper.Stopper;
import com.edduarte.vokter.util.OSGiManager;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.hash.BloomFilter;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import it.unimi.dsi.lang.MutableString;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.3
 * @since 1.0.0
 */
public class DiffMatcher implements Callable<Set<Match>> {

    private static final Logger logger =
            LoggerFactory.getLogger(DiffMatcher.class);

    private final String oldText;

    private final String newText;

    private final List<Keyword> keywords;

    private final List<Diff> diffs;

    private final ParserPool parserPool;

    private final LanguageDetector langDetector;

    private final boolean filterStopwords;

    private final boolean enableStemming;

    private final boolean ignoreCase;

    private final boolean ignoreAdded;

    private final boolean ignoreRemoved;

    private final int snippetOffset;


    public DiffMatcher(final String oldText,
                       final String newText,
                       final List<Keyword> keywords,
                       final List<Diff> diffs,
                       final ParserPool parserPool,
                       final LanguageDetector langDetector,
                       final boolean filterStopwords,
                       final boolean enableStemming,
                       final boolean ignoreCase,
                       final boolean ignoreAdded,
                       final boolean ignoreRemoved,
                       final int snippetOffset) {
        this.oldText = oldText;
        this.newText = newText;
        this.keywords = keywords;
        this.diffs = diffs;
        this.parserPool = parserPool;
        this.langDetector = langDetector;
        this.filterStopwords = filterStopwords;
        this.enableStemming = enableStemming;
        this.ignoreCase = ignoreCase;
        this.ignoreAdded = ignoreAdded;
        this.ignoreRemoved = ignoreRemoved;
        this.snippetOffset = snippetOffset;
    }


    @Override
    public Set<Match> call() {
        Stopwatch sw = Stopwatch.createStarted();

        Set<Match> matchedDiffs = new ConcurrentHashSet<>();


        // infers the document language
        String languageCodeAux = "en";
        if (langDetector != null) {
            TextObjectFactory textObjectFactory =
                    CommonTextObjectFactories.forDetectingOnLargeText();
            TextObject textObject = textObjectFactory.forText(oldText + newText);
            Optional<LdLocale> lang = langDetector.detect(textObject);
            languageCodeAux = lang.isPresent() ? lang.get().getLanguage() : "en";
        }
        final String languageCode = languageCodeAux;


        diffs.parallelStream()
                .unordered()
                .filter(d -> !(ignoreAdded && d.getEvent()
                        .equals(DiffEvent.inserted)))
                .filter(d -> !(ignoreRemoved && d.getEvent()
                        .equals(DiffEvent.deleted))).forEach(d -> {
            String s = d.getText();


            // sets the parser's stopper according to the detected language
            // if the detected language is not supported, stopword filtering is
            // ignored
            Stopper stopper = null;
            if (filterStopwords) {
                stopper = new FileStopper(languageCode);
                if (stopper.isEmpty()) {
                    // if no compatible stopwords were found, use the
                    // english stopwords
                    stopper = new FileStopper("en");
                }
            }


            // sets the parser's stemmer according to the detected language
            // if the detected language is not supported, stemming is ignored
            Stemmer stemmer = null;
            try {
                if (enableStemming) {
                    Class<? extends Stemmer> stemmerClass =
                            OSGiManager.getCompatibleStemmer(languageCode);
                    if (stemmerClass != null) {
                        stemmer = stemmerClass.newInstance();
                    } else {
                        // if no compatible stemmers were found, use the english
                        // stemmer
                        stemmerClass = OSGiManager.getCompatibleStemmer("en");
                        if (stemmerClass != null) {
                            stemmer = stemmerClass.newInstance();
                        }
                    }
                }
            } catch (ReflectiveOperationException ex) {
                logger.error(ex.getMessage(), ex);
                // unexpected class loading error, so stemming is ignored
            }


            // take out one of the available parsers for the entire context
            Parser parser;
            try {
                parser = parserPool.take();
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
                return;
            }


            List<Parser.Result> parserResults = parser.parse(
                    new MutableString(s),
                    stopper,
                    stemmer,
                    ignoreCase
            );


            // return the used parser so that other threads can use it
            try {
                parserPool.place(parser);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
                return;
            }


            if (stopper != null) {
                stopper.destroy();
            }


//            if (stemmer != null) {
//                stemmer.destroy();
//            }


            List<MutableString> tokens = parserResults.parallelStream()
                    .map(t -> t.text)
                    .collect(Collectors.toList());


            BloomFilter<MutableString> bloomFilter = BloomFilter.create(
                    (from, into) -> into.putUnencodedChars(from),
                    tokens.size()
            );


            // put all of the individual words in the difference text on the
            // bloom filter
            tokens.forEach(bloomFilter::put);


            // use the BloomFilter to check if AT LEAST ONE of the keywords has
            // ALL of its words contained in the diff text, even if they come in
            // different order
            keywords.parallelStream()
                    .unordered()
                    // check if all of the words in the keyword exist in the
                    // bloom filter. If at least one of the words in the keyword
                    // is said by the BloomFilter to not exist (guaranteed), we
                    // skip this keyword
                    .filter(kw -> kw.textStream()
                            .allMatch(bloomFilter::mightContain))
                    // if the keyword passes the bloom filter testing,
                    // we test with more certainty if the candidate keyword
                    // actually matches any of the difference tokens, using
                    // String.equals() this time
                    .filter((kw) -> kw.textStream().allMatch(kwText ->
                            tokens.stream().anyMatch(kwText::equals)))
                    .map((kw) -> {
                        // if a candidate passes the equality test, it's a true
                        // match, so we generate a snippet for it and return it
                        int start = d.getStartIndex() - snippetOffset;
                        if (start < 0) {
                            start = 0;
                        }
                        int end = d.getEndIndex() + snippetOffset;
                        switch (d.getEvent()) {
                            case inserted: {
                                if (end > newText.length()) {
                                    end = newText.length();
                                }
                                String snippet = newText.substring(start, end);
                                return new Match(d.getEvent(), kw, s, snippet);
                            }

                            case deleted: {
                                if (end > oldText.length()) {
                                    end = oldText.length();
                                }
                                String snippet = oldText.substring(start, end);
                                return new Match(d.getEvent(), kw, s, snippet);
                            }
                        }
                        // should not occur
                        return null;
                    })
                    .filter(diff -> diff != null)
                    .forEach(matchedDiffs::add);
        });


        sw.stop();
        logger.info("Completed difference matching for keywords '{}' in {}",
                keywords.toString(), sw.toString());
        return matchedDiffs;
    }
}
