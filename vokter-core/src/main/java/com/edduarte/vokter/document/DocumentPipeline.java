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

package com.edduarte.vokter.document;

import com.edduarte.vokter.cleaner.AndCleaner;
import com.edduarte.vokter.cleaner.Cleaner;
import com.edduarte.vokter.cleaner.DiacriticCleaner;
import com.edduarte.vokter.cleaner.LowerCaseCleaner;
import com.edduarte.vokter.cleaner.NewLineCleaner;
import com.edduarte.vokter.cleaner.RepeatingSpacesCleaner;
import com.edduarte.vokter.cleaner.SpecialCharsCleaner;
import com.edduarte.vokter.persistence.Document;
import com.edduarte.vokter.processor.similarity.BandsProcessor;
import com.edduarte.vokter.processor.similarity.KShingler;
import com.edduarte.vokter.processor.similarity.KShinglesSigProcessor;
import com.edduarte.vokter.reader.Reader;
import com.edduarte.vokter.stopper.FileStopper;
import com.edduarte.vokter.stopper.Stopper;
import com.edduarte.vokter.util.OSGiManager;
import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import it.unimi.dsi.lang.MutableString;
import com.edduarte.vokter.similarity.HashProvider.HashMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A processing pipeline that reads, filters and tokenizes a content stream,
 * specifically a document. Every detected token is stored with a group
 * of common occurrences between different documents by using the provided
 * concurrent map structures.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DocumentPipeline implements Callable<Document> {

    private static final Logger logger =
            LoggerFactory.getLogger(DocumentPipeline.class);

    private final Class<? extends Document> documentClass;

    private final DocumentInput documentInput;

    private final int shingleLength;

    private final LanguageDetector langDetector;

    private final boolean filterStopwords;

    private final boolean ignoreCase;


    public DocumentPipeline(
            final Class<? extends Document> documentClass,
            final DocumentInput documentInput,
            final LanguageDetector langDetector,
            final boolean filterStopwords,
            final boolean ignoreCase,
            final int shingleLength) {
        this.documentClass = documentClass;
        this.documentInput = documentInput;
        this.shingleLength = shingleLength;
        this.langDetector = langDetector;
        this.filterStopwords = filterStopwords;
        this.ignoreCase = ignoreCase;
    }


    @Override
    public Document call() throws Exception {
        InputStream documentStream = documentInput.getStream();
        String url = documentInput.getUrl();
        String contentType = documentInput.getContentType();


        // reads and parses contents from input content stream
        Class<? extends Reader> readerClass = OSGiManager
                .getCompatibleReader(contentType);
        Reader reader = readerClass.newInstance();
        MutableString content = reader.readDocumentContents(documentStream);
        reader = null;
        documentStream.close();
        documentStream = null;
        documentInput.destroy();


        // filters the contents by cleaning characters of whole strings
        // according to each cleaner's implementation
        List<Cleaner> list = new ArrayList<>();
        list.add(new SpecialCharsCleaner(' '));
        list.add(new NewLineCleaner(' '));
        list.add(new RepeatingSpacesCleaner());
        list.add(new DiacriticCleaner());
        if (ignoreCase) {
            list.add(new LowerCaseCleaner());
        }
        Cleaner cleaner = AndCleaner.of(list);
        cleaner.clean(content);
        cleaner = null;


        // infers the document language
        String languageCodeAux = "en";
        if (langDetector != null) {
            TextObjectFactory textObjectFactory =
                    CommonTextObjectFactories.forDetectingOnLargeText();
            TextObject textObject = textObjectFactory.forText(content);
            Optional<LdLocale> lang = langDetector.detect(textObject);
            languageCodeAux = lang.isPresent() ? lang.get().getLanguage() : "en";
        }
        final String languageCode = languageCodeAux;


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


        // get k-shingles for the document text
        int k = shingleLength;
        if (shingleLength <= 0) {
            // "Mining of Massive Datasets", Cambridge University Press,
            // Rajaraman, A., & Ullman, J. D., page 78
            // "k should be picked large enough that the probability of any
            // given shingle appearing in any given document is low. (...) A
            // good rule of thumb is to imagine that there are only 20
            // characters and estimate the number of k-shingles as 20^k."

            // Since we are dealing with documents that can be large or small,
            // we need to determine an optimal k based on the document length.
            // Ideally we want an email (e.g. 430 characters) to have k = 5
            // and a wikipedia article (e.g. 7880 characters) to have k = 8.
            // For this we assume that there are 30 available characters in
            // the universal vocabulary (having filtered most special
            // characters). We estimate the value of k based on the assumption
            // that 30^k = L , where L is the document length.
            // 30 ^ k = L <=> K = log10(L) / log10(3)

            double estimatedK = Math.log10(content.length()) / Math.log10(3);
            k = (int) Math.floor(estimatedK);
        }
        KShingler kshingler = new KShingler(k, stopper);
        List<String> shingles = kshingler.process(content).call();

        KShinglesSigProcessor sigProcessor =
                new KShinglesSigProcessor(HashMethod.Murmur3, 200);
        int[] sig = sigProcessor.process(shingles).call();

        BandsProcessor bandsProcessor = new BandsProcessor(20, 5);
        int[] bands = bandsProcessor.process(sig).call();


        // delete mutable string content from memory
        String s = content.toString();
        content.delete(0, content.length());
        content = null;


        // creates a document that represents this pipeline processing result
        try {
             return documentClass.getConstructor(
                    String.class,
                    Date.class,
                    String.class,
                    String.class,
                    List.class,
                    int.class,
                    int[].class
            ).newInstance(url, new Date(), contentType, s, shingles, k, bands);
        } catch (ReflectiveOperationException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
