package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.persistence.Session;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMSession implements Session {

    private final String clientUrl;

    private final String clientContentType;

    private final String token;


    public RAMSession(String clientUrl, String clientContentType, String token) {
        this.clientUrl = clientUrl;
        this.clientContentType = clientContentType;
        this.token = token;
    }


    @Override
    public String getClientUrl() {
        return clientUrl;
    }


    @Override
    public String getClientContentType() {
        return clientContentType;
    }


    @Override
    public String getToken() {
        return token;
    }
}
