package com.edduarte.vokter.persistence.mongodb;

import com.edduarte.vokter.persistence.HttpSessionCollection;
import com.edduarte.vokter.persistence.Session;
import com.mongodb.DB;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class HttpMongoSessionCollection extends MongoSessionCollection
        implements HttpSessionCollection {


    /**
     * Instantiate the Collection object, which represents the core access to
     * sessions using MongoDB persistence mechanisms.
     */
    public HttpMongoSessionCollection(DB db) {
        super(db);
    }


    @Override
    public Session add(String url, String contentType, String token) {
        HttpMongoSession s = new HttpMongoSession(url, contentType, token);
        collection.insert(s);
        return s;
    }
}
