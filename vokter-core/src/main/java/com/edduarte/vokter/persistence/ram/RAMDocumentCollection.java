package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.persistence.Document;
import com.edduarte.vokter.persistence.DocumentCollection;
import com.edduarte.vokter.util.Params;
import com.optimaize.langdetect.LanguageDetector;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMDocumentCollection implements DocumentCollection {

    private final Map<Params, Pair> m;


    public RAMDocumentCollection() {
        this.m = new HashMap<>();
    }


    @Override
    public Document addNewDocument(String documentUrl, String documentContentType,
                                   LanguageDetector langDetector,
                                   boolean filterStopwords, boolean ignoreCase) {
        DocumentBuilder builder = DocumentBuilder
                .fromUrl(documentUrl, documentContentType);
        if (filterStopwords) {
            builder.filterStopwords();
        }
        if (ignoreCase) {
            builder.ignoreCase();
        }

        Document d = builder.build(langDetector, RAMDocument.class);
        if (d != null) {
            m.put(
                    Params.of(d.getUrl(), d.getContentType()),
                    Pair.of(d, d.clone())
            );
        }
        return d;
    }


    @Override
    public Document addNewSnapshot(Document oldDocument,
                                   LanguageDetector langDetector,
                                   boolean filterStopwords, boolean ignoreCase) {
        DocumentBuilder builder = DocumentBuilder
                .fromUrl(oldDocument.getUrl(), oldDocument.getContentType())
                .withShingleLength(oldDocument.getShingleLength());
        if (filterStopwords) {
            builder.filterStopwords();
        }
        if (ignoreCase) {
            builder.ignoreCase();
        }

        Document d = builder.build(langDetector, RAMDocument.class);
        if (d != null) {
            m.put(
                    Params.of(d.getUrl(), d.getContentType()),
                    Pair.of(oldDocument, d)
            );
        }
        return d;
    }


    @Override
    public void remove(String url, String contentType) {
        m.remove(Params.of(url, contentType));
    }


    @Override
    public Pair get(String url, String contentType) {
        return m.get(Params.of(url, contentType));
    }


    @Override
    public void invalidateCache() {
    }
}
