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
                new LowerCaseCleaner(),
                new RepeatingSpacesCleaner()
        );
        cleaner.clean(s);

        assertEquals("argus panoptes wikipedia the free encyclopedia argus panoptes from wikipedia the free encyclopedia jump to navigation search drawing of an image from a 5th century cafe cliche chloe s pada karel capek", s.toString());
    }
}
