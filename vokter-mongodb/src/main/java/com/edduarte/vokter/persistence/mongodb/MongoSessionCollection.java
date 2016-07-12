package com.edduarte.vokter.persistence.mongodb;

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

    protected final DBCollection collection;


    /**
     * Instantiate the Collection object, which represents the core access to
     * sessions using MongoDB persistence mechanisms.
     */
    public MongoSessionCollection(DB db) {
        this.collection = db.getCollection("session");
    }


    @Override
    public Session add(String id, String clientToken) {
        MongoSession s = new MongoSession(id, clientToken);
        collection.insert(s);
        return s;
    }


    @Override
    public void removeSession(String id) {
        DBObject obj = collection.findOne(new BasicDBObject(MongoSession.ID, id));
        if (obj != null) {
            MongoSession s = new MongoSession((BasicDBObject) obj);
            collection.remove(s);
        }
    }


    @Override
    public Session validateToken(String id, String token) {
        DBObject obj = collection.findOne(new BasicDBObject(MongoSession.ID, id)
                .append(MongoSession.TOKEN, token));
        if (obj != null) {
            return new MongoSession((BasicDBObject) obj);
        } else {
            return null;
        }
    }
}
