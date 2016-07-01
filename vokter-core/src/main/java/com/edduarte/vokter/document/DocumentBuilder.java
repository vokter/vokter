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

import com.edduarte.vokter.model.mongodb.Document;
import com.edduarte.vokter.util.OSGiManager;
import com.google.common.base.Stopwatch;
import com.optimaize.langdetect.LanguageDetector;
import org.apache.tools.ant.filters.StringInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Builder class that loads documents streams and indexes them into a
 * {@link DocumentCollection} structure.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public final class DocumentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);

    /**
     * The low-footprint loader of the document, using a lazy stream.
     */
    private final Supplier<DocumentInput> documentLazySupplier;

    /**
     * The language detector that will assure that the right Stopword filter
     * and Stemmer are used for the input content.
     */
    private LanguageDetector langDetector;

    /**
     * Flag that sets usage of stopword filtering.
     */
    private boolean isStoppingEnabled = false;

//    /**
//     * Flag that sets usage of a porter stemmer.
//     */
//    private boolean isStemmingEnabled = false;

    /**
     * Flag that sets matching of equal occurrences with different casing.
     */
    private boolean ignoreCase = false;


    /**
     * The length of shingles k, to generate from the document content. If an
     * older snapshot of this document was already generated, this should be
     * set to the same k value. If not, then a new k value will be determined
     * based on the document length.
     */
    private int shingleLength = -1;


    private DocumentBuilder(final Supplier<DocumentInput> documentLazySupplier) {
        this.documentLazySupplier = documentLazySupplier;
    }


    /**
     * Instantiates a builder that collects a document from a specified web url,
     * fetching the content as a InputStream. An expected content-type can also
     * be provided. If this content-type is left as null, the default response
     * content-type will be used.
     */
    public static DocumentBuilder fromUrl(final String url,
                                          final String contentType) {
        return new DocumentBuilder(() -> {
            WebTarget target = ClientBuilder.newClient().target(url);
            Invocation.Builder builder;

            if (contentType != null) {
                builder = target.request(contentType);
            } else {
                builder = target.request();
            }

            Response response = builder.get();
            if (response.getStatus() == 200) {
                InputStream contentStream = response.readEntity(InputStream.class);
                String responseContentType = response.getHeaderString("Content-Type");
                MediaType mediaType = MediaType.valueOf(responseContentType);
                return new DocumentInput(
                        url,
                        contentStream,
                        mediaType.getType() + "/" + mediaType.getSubtype()
                );
            } else {
                throw new RuntimeException(response.readEntity(String.class));
            }
        });
    }


    /**
     * Instantiates a loader that collects a document from a specified input
     * stream. This constructor is mostly used for testing.
     */
    public static DocumentBuilder fromString(final String url,
                                             final String text,
                                             final String contentType) {
        return new DocumentBuilder(() -> {
            MediaType mediaType = MediaType.valueOf(contentType);
            return new DocumentInput(
                    url,
                    new StringInputStream(text),
                    mediaType.getType() + "/" + mediaType.getSubtype()
            );
        });
    }


//    public DocumentBuilder withLanguageDetector(LanguageDetector langDetector) {
//        this.langDetector = langDetector;
//        return this;
//    }


    public DocumentBuilder withStopwords() {
        this.isStoppingEnabled = true;
        return this;
    }


//    public DocumentBuilder withStemming() {
//        this.isStemmingEnabled = true;
//        return this;
//    }


    public DocumentBuilder ignoreCase() {
        this.ignoreCase = true;
        return this;
    }


    public DocumentBuilder withShingleLength(int shingleLength) {
        this.shingleLength = shingleLength;
        return this;
    }


    public boolean isReadable() {
        try {
            documentLazySupplier.get();
            return true;
        } catch (RuntimeException ex) {
            logger.info("There was an error accessing the document: {}",
                    ex.getMessage());
            return false;
        }
    }


    /**
     * Indexes the documents specified in the factory method and adds the index
     * files into the specified folder.
     * <p>
     * This method will perform all tasks associated with reading a corpus,
     * processing and indexing it, writing the results to disk persistence and
     * building cached systems that provide synchronous access to documents and
     * tokens.
     * <p>
     * The most recently accessed tokens and documents are kept in memory for 20
     * seconds before being destroyed. If a token and a document are not in cache,
     * the relevant data is read and parsed from the local files.
     *
     * @return the built index of the documents specified in the factory method
     */
    public Document build(LanguageDetector langDetector) {
        Stopwatch sw = Stopwatch.createStarted();

        // step 1) Perform a lazy loading of the document, by obtaining its url,
        // content stream and content type.
        DocumentInput input = documentLazySupplier.get();


        // step 2) Checks if the input document is supported by the server
        boolean isSupported = OSGiManager.getCompatibleReader(input.getContentType()) != null;
        if (!isSupported) {
            logger.info("Could not process document '{}': No compatible " +
                            "readers available for content-type '{}'.",
                    input.getUrl(),
                    input.getContentType()
            );
            return null;
        }


        // step 3) Process the document.
        DocumentPipeline pipeline = new DocumentPipeline(

                // the input document info, including its path and InputStream
                input,

                // the language detection model
                langDetector,

                // flag that sets that stopwords will be filtered during
                // k-shingling
                isStoppingEnabled,

                // flag that forces the document to be in lower case, so that
                // during difference matching, every match will be case
                // insensitive (regardless of the user setting "ignoreCase" as
                // false)
                ignoreCase,

                // shingle length for KShingling
                shingleLength

        );
        Document document;
        try {
            document = pipeline.call();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }

        sw.stop();
        logger.info("Completed processing document '{}' in {}.",
                document.getUrl(), sw.toString());

        return document;
    }
}