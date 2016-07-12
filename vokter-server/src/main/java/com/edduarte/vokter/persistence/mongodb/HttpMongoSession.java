package com.edduarte.vokter.persistence.mongodb;

import com.edduarte.vokter.persistence.HttpSession;
import com.mongodb.BasicDBObject;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class HttpMongoSession extends MongoSession implements HttpSession {

    public static final String URL = "url";

    public static final String CONTENT_TYPE = "content_type";


    public HttpMongoSession(String url, String contentType, String token) {
        super(HttpSession.idFromUrl(url, contentType), token);
        append(URL, url);
        append(CONTENT_TYPE, contentType);
    }


    public HttpMongoSession(BasicDBObject dbObject) {
        super(dbObject);
    }


    @Override
    public String getUrl() {
        return getString(URL);
    }


    @Override
    public String getContentType() {
        return getString(CONTENT_TYPE);
    }
}
