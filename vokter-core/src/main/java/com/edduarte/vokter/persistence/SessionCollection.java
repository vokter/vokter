package com.edduarte.vokter.persistence;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface SessionCollection {

    /**
     * Adds a new sessions for the pair (clientUrl, clientContentType),
     * generating a random unique token.
     */
    Session add(String clientUrl, String clientContentType, String clientToken);

    /**
     * Gets a session if the pair (clientUrl, clientContentType) matches with
     * the provided token. If not, return null
     */
    Session validateToken(String clientUrl, String clientContentType, String token);

    /**
     * Remove a session for the pair (clientUrl, clientContentType)
     */
    void removeSession(String clientUrl, String clientContentType);
}
