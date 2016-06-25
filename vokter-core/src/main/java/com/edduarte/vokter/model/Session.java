package com.edduarte.vokter.model;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class Session {

    private final String clientUrl;

    private final String token;


    public Session(String clientUrl, String token) {
        this.clientUrl = clientUrl;
        this.token = token;
    }


    public String getClientUrl() {
        return clientUrl;
    }


    public String getToken() {
        return token;
    }
}
