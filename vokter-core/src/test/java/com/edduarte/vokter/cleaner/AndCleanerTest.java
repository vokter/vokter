package com.edduarte.vokter.cleaner;

import it.unimi.dsi.lang.MutableString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class AndCleanerTest {

    @Test
    public void test() {
        MutableString s =
                new MutableString("Argus Panoptes - Wikipedia, the free encyclopedia   Argus Panoptes\n" +
                        "\n" +
                        "From Wikipedia, the free encyclopedia  Jump to: navigation , search     \n" +
                        " Drawing of an image from a 5th-century \n" +
                        "café cliché Chloë's pādā Karel Čapek");

        Cleaner cleaner = AndCleaner.of(
                new SpecialCharsCleaner(' '),
                new NewLineCleaner(' '),
                new DiacriticCleaner(),
                new RepeatingSpacesCleaner()
        );
        cleaner.clean(s);

        assertEquals("Argus Panoptes Wikipedia the free encyclopedia Argus Panoptes From Wikipedia the free encyclopedia Jump to navigation search Drawing of an image from a 5th century cafe cliche Chloe s pada Karel Capek", s.toString());
    }
}
