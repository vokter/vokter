package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.persistence.Document;
import com.edduarte.vokter.persistence.DocumentCollection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMDocumentCollection implements DocumentCollection {

    private final Map<Params, Pair> documents;


    public RAMDocumentCollection() {
        this.documents = new HashMap<>();
    }


    @Override
    public void add(Document d) {
        documents.put(
                Params.of(d.getUrl(), d.getContentType()),
                Pair.of(d, d.clone())
        );
    }


    @Override
    public void remove(String url, String contentType) {
        documents.remove(Params.of(url, contentType));
    }


    @Override
    public Pair get(String url, String contentType) {
        return documents.get(Params.of(url, contentType));
    }


    @Override
    public void destroy() {
        documents.clear();
    }
}
