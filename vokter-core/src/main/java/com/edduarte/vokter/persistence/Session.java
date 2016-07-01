package com.edduarte.vokter.persistence;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Session {


    String getClientUrl();


    String getClientContentType();


    String getToken();
}
