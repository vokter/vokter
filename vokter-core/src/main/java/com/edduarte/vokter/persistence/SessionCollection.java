package com.edduarte.vokter.persistence;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface SessionCollection {

    /**
     * Adds a new session that identifies the unique client id with a random
     * unique token.
     */
    Session add(String id, String token);

    /**
     * Gets a session if the client identifier matches with the provided token.
     * If not, return null.
     */
    Session validateToken(String id, String token);

    /**
     * Remove the session corresponding to the client identifier
     */
    void removeSession(String id);
}
