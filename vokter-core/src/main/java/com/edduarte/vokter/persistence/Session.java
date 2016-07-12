package com.edduarte.vokter.persistence;

import com.edduarte.vokter.parser.Parser;
import com.edduarte.vokter.parser.SimpleParser;
import com.edduarte.vokter.util.Duo;
import it.unimi.dsi.lang.MutableString;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Session {


    String getId();


    String getToken();
}
