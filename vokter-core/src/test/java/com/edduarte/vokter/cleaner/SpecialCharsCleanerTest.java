package com.edduarte.vokter.cleaner;

import it.unimi.dsi.lang.MutableString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class SpecialCharsCleanerTest {

    @Test
    public void test() {
        MutableString s =
                new MutableString("Argus Panoptes - Wikipedia, the free encyclopedia   Argus Panoptes\n" +
                        "\n" +
                        "From Wikipedia, the free encyclopedia  Jump to: navigation , search     \n" +
                        " Drawing of an image from a 5th-century ");

        Cleaner cleaner = new SpecialCharsCleaner();
        cleaner.clean(s);

        assertEquals("Argus Panoptes   Wikipedia  the free encyclopedia   Argus Panoptes  From Wikipedia  the free encyclopedia  Jump to  navigation   search       Drawing of an image from a 5th century ", s.toString());
    }
}
