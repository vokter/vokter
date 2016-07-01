package com.edduarte.vokter.util;

public class Params {

    private final String url;

    private final String contentType;


    private Params(String url, String contentType) {
        this.url = url;
        if (contentType == null) {
            this.contentType = "";
        } else {
            this.contentType = contentType;
        }
    }


    public static Params of(String url, String contentType) {
        return new Params(url, contentType);
    }


    public String getUrl() {
        return url;
    }


    public String getContentType() {
        return contentType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Params that = (Params) o;
        return this.url.equals(that.url) &&
                this.contentType.equals(that.contentType);

    }


    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + contentType.hashCode();
        return result;
    }
}