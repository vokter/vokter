package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.persistence.Document;

import java.util.Date;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMDocument implements Document {

    private final String url;

    private final Date date;

    private final String contentType;

    private final String text;

    private final List<String> shingles;

    private final int k;

    private final int[] bands;


    public RAMDocument(String url, Date date, String contentType,
                       String text, List<String> shingles, int k,
                       int[] bands) {

        this.url = url;
        this.date = date;
        this.contentType = contentType;
        this.text = text;
        this.shingles = shingles;
        this.k = k;
        this.bands = bands;
    }


    @Override
    public String getUrl() {
        return url;
    }


    @Override
    public Date getDate() {
        return date;
    }


    @Override
    public String getContentType() {
        return contentType;
    }


    @Override
    public String getText() {
        return text;
    }


    @Override
    public List<String> getShingles() {
        return shingles;
    }


    @Override
    public int getShingleLength() {
        return k;
    }


    @Override
    public int[] getBands() {
        return bands;
    }


    @Override
    public Document clone() {
        return new RAMDocument(url, date, contentType, text, shingles, k, bands);
    }
}