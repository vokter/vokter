package com.edduarte.vokter.persistence.mongodb;

import com.edduarte.vokter.Constants;
import com.edduarte.vokter.persistence.Session;
import com.edduarte.vokter.persistence.SessionCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class MongoSessionCollection implements SessionCollection {

    private final DBCollection collection;


    /**
     * Instantiate the Collection object, which represents the core access to
     * sessions using MongoDB persistence mechanisms.
     */
    public MongoSessionCollection(DB db) {
        this.collection = db.getCollection("session");
    }


    @Override
    public Session add(String clientUrl, String clientContentType, String clientToken) {
        MongoSession s = new MongoSession(clientUrl, clientContentType, clientToken);
        collection.insert(s);
        return s;
    }


    @Override
    public void removeSession(String clientUrl, String clientContentType) {
        DBObject obj = collection.findOne(
                new BasicDBObject(MongoSession.CLIENT_URL, clientUrl)
                        .append(MongoSession.CLIENT_CONTENT_TYPE, clientContentType));
        if (obj != null) {
            MongoSession s = new MongoSession((BasicDBObject) obj);
            collection.remove(s);
        }
    }


    @Override
    public Session validateToken(String clientUrl, String clientContentType, String token) {
        DBObject obj = collection.findOne(
                new BasicDBObject(MongoSession.CLIENT_URL, clientUrl)
                        .append(MongoSession.CLIENT_CONTENT_TYPE, clientContentType)
                        .append(MongoSession.TOKEN, token));
        if (obj != null) {
            return new MongoSession((BasicDBObject) obj);
        } else {
            return null;
        }
    }
}
