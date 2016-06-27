package com.edduarte.vokter.model.mongodb;

import com.mongodb.BasicDBObject;

import java.io.Serializable;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class Session extends BasicDBObject implements Serializable {

    public static final String CLIENT_URL = "url";

    public static final String CLIENT_CONTENT_TYPE = "content_type";

    public static final String TOKEN = "token";


    public Session(String clientUrl, String clientContentType, String token) {
        super();
        append(CLIENT_URL, clientUrl);
        append(CLIENT_CONTENT_TYPE, clientContentType);
        append(TOKEN, token);
    }


    public Session(BasicDBObject dbObject) {
        super(dbObject);
    }


    public String getClientUrl() {
        return getString(CLIENT_URL);
    }


    public String getClientContentType() {
        return getString(CLIENT_CONTENT_TYPE);
    }


    public String getToken() {
        return getString(TOKEN);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Session that = (Session) o;
        return this.getClientUrl().equals(that.getClientUrl()) &&
                this.getClientContentType().equals(that.getClientContentType()) &&
                this.getToken().equals(that.getToken());
    }


    @Override
    public int hashCode() {
        return getClientUrl().hashCode() *
                getClientContentType().hashCode() *
                getToken().hashCode();
    }
}
