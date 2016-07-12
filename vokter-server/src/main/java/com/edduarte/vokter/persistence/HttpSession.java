package com.edduarte.vokter.persistence;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpSession extends Session {


    public static String idFromUrl(String url, String contentType) {
        // we use pipe because it's guaranteed to be a unique character that
        // can be used to separate an URL and a Content-Type and parsed
        // afterwards, since it is an invalid URL and Content-Type character
        // that will only appear be escaped / encoded.
        return url + "|" + contentType;
    }

    @Override
    default String getId() {
        return idFromUrl(getUrl(), getContentType());
    }

    String getUrl();

    String getContentType();
}
