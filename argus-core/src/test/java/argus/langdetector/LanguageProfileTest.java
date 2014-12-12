package argus.langdetector;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Nakatani Shuyo
 */
public class LanguageProfileTest {

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link LanguageProfile#LanguageProfile()}.
     */
    @Test
    public final void testLangProfile() {
        LanguageProfile profile = new LanguageProfile();
        assertEquals(profile.name, null);
    }

    /**
     * Test method for {@link LanguageProfile#LanguageProfile(String)}.
     */
    @Test
    public final void testLangProfileStringInt() {
        LanguageProfile profile = new LanguageProfile("en");
        assertEquals(profile.name, "en");
    }

    /**
     * Test method for {@link LanguageProfile#add(String)}.
     */
    @Test
    public final void testAdd() {
        LanguageProfile profile = new LanguageProfile("en");
        profile.add("a");
        assertEquals((int)profile.freq.get("a"), 1);
        profile.add("a");
        assertEquals((int)profile.freq.get("a"), 2);
        profile.omitLessFreq();
    }

    
    /**
     * Illegal call test for {@link LanguageProfile#add(String)}
     */
    @Test
    public final void testAddIllegally1() {
        LanguageProfile profile = new LanguageProfile(); // Illegal ( available for only JSONIC ) but ignore
        profile.add("a"); // ignore
        assertEquals(profile.freq.get("a"), null); // ignored
    }

    /**
     * Illegal call test for {@link LanguageProfile#add(String)}
     */
    @Test
    public final void testAddIllegally2() {
        LanguageProfile profile = new LanguageProfile("en");
        profile.add("a");
        profile.add("");  // Illegal (string's length of parameter must be between 1 and 3) but ignore
        profile.add("abcd");  // as well
        assertEquals((int)profile.freq.get("a"), 1);
        assertEquals(profile.freq.get(""), null);     // ignored
        assertEquals(profile.freq.get("abcd"), null); // ignored
        
    }
    
    /**
     * Test method for {@link LanguageProfile#omitLessFreq()}.
     */
    @Test
    public final void testOmitLessFreq() {
        LanguageProfile profile = new LanguageProfile("en");
        String[] grams = "a b c \u3042 \u3044 \u3046 \u3048 \u304a \u304b \u304c \u304d \u304e \u304f".split(" ");
        for (int i=0;i<5;++i) for (String g : grams) {
            profile.add(g);
        }
        profile.add("\u3050");

        assertEquals((int)profile.freq.get("a"), 5);
        assertEquals((int)profile.freq.get("\u3042"), 5);
        assertEquals((int)profile.freq.get("\u3050"), 1);
        profile.omitLessFreq();
        assertEquals(profile.freq.get("a"), null); // omitted
        assertEquals((int)profile.freq.get("\u3042"), 5);
        assertEquals(profile.freq.get("\u3050"), null); // omitted
    }

    /**
     * Illegal call test for {@link LanguageProfile#omitLessFreq()}.
     */
    @Test
    public final void testOmitLessFreqIllegally() {
        LanguageProfile profile = new LanguageProfile();
        profile.omitLessFreq();  // ignore
    }

}
