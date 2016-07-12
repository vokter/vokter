package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.persistence.Session;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMSession implements Session {

    private final String id;

    private final String token;


    public RAMSession(String id, String token) {
        this.id = id;
        this.token = token;
    }


    @Override
    public String getId() {
        return id;
    }


    @Override
    public String getToken() {
        return token;
    }
}
