package com.edduarte.vokter.cleaner;

import it.unimi.dsi.lang.MutableString;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class DiacriticCleanerTest {

    @Test
    public void test() {
        MutableString s =
                new MutableString("café cliché Chloë's pādā Karel Čapek");

        Cleaner cleaner = new DiacriticCleaner();
        cleaner.clean(s);

        assertEquals("cafe cliche Chloe's pada Karel Capek", s.toString());
    }
}
