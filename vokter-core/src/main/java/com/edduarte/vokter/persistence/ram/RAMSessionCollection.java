package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.persistence.Session;
import com.edduarte.vokter.persistence.SessionCollection;
import com.edduarte.vokter.util.Params;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMSessionCollection implements SessionCollection {

    private final Map<Params, Session> sessionMap;


    public RAMSessionCollection() {
        this.sessionMap = new HashMap<>();
    }


    @Override
    public Session add(String clientUrl, String clientContentType, String token) {
        Session session = new RAMSession(clientUrl, clientContentType, token);
        sessionMap.put(Params.of(clientUrl, clientContentType), session);
        return session;
    }


    @Override
    public Session validateToken(String clientUrl, String clientContentType, String token) {
        Session session = sessionMap.get(Params.of(clientUrl, clientContentType));
        if (token.equals(session.getToken())) {
            return session;
        } else {
            return null;
        }
    }


    @Override
    public void removeSession(String clientUrl, String clientContentType) {
        sessionMap.remove(Params.of(clientUrl, clientContentType));
    }
}
