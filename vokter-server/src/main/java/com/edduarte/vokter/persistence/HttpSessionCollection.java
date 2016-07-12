package com.edduarte.vokter.persistence;

import com.edduarte.vokter.parser.Parser;
import com.edduarte.vokter.parser.SimpleParser;
import it.unimi.dsi.lang.MutableString;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpSessionCollection extends SessionCollection {


    @Override
    default Session add(String id, String token) {
        SimpleParser parser = new SimpleParser('|');
        List<Parser.Result> r = parser.parse(new MutableString(id));
        String url = r.get(0).text.toString();
        String contentType = r.get(1).text.toString();
        return add(url, contentType, token);
    }


    Session add(String url, String contentType, String token);
}
