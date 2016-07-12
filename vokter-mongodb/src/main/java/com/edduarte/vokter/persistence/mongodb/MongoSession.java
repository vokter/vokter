package com.edduarte.vokter.persistence.mongodb;

import com.edduarte.vokter.persistence.Session;
import com.mongodb.BasicDBObject;

import java.io.Serializable;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class MongoSession extends BasicDBObject
        implements Session, Serializable {

    public static final String ID = "id";

    public static final String TOKEN = "token";


    public MongoSession(String id, String token) {
        super();
        append(ID, id);
        append(TOKEN, token);
    }


    public MongoSession(BasicDBObject dbObject) {
        super(dbObject);
    }


    @Override
    public String getId() {
        return getString(ID);
    }


    @Override
    public String getToken() {
        return getString(TOKEN);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MongoSession that = (MongoSession) o;
        return this.getId().equals(that.getId()) &&
                this.getToken().equals(that.getToken());
    }


    @Override
    public int hashCode() {
        return getId().hashCode() *
                getToken().hashCode();
    }
}
