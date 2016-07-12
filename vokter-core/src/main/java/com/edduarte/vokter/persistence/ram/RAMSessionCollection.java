package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.persistence.Session;
import com.edduarte.vokter.persistence.SessionCollection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMSessionCollection implements SessionCollection {

    private final Map<String, Session> m;


    public RAMSessionCollection() {
        this.m = new ConcurrentHashMap<>();
    }


    @Override
    public Session add(String id, String token) {
        Session s = new RAMSession(id, token);
        m.put(id, s);
        return s;
    }


    @Override
    public Session validateToken(String id, String token) {
        Session s = m.get(id);
        if (token.equals(s.getToken())) {
            return s;
        } else {
            return null;
        }
    }


    @Override
    public void removeSession(String id) {
        m.remove(id);
    }
}
