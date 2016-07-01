package com.edduarte.vokter.cleaner;

import it.unimi.dsi.lang.MutableString;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RepeatingSpacesCleaner extends Cleaner {

    public RepeatingSpacesCleaner() {
    }


    @Override
    protected void setup(MutableString s) {
    }


    @Override
    protected boolean clean(MutableString s, int i, char last, char curr) {
        return Character.isWhitespace(last) && Character.isWhitespace(curr);
    }
}
