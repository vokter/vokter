/*
 * Copyright 2014 Ed Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package argus.langdetector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Nakatani Shuyo
 */
public class LanguageTest {

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
     * Test method for {@link argus.langdetector.Language(String, double)}.
     */
    @Test
    public final void testLanguage() {
        Language lang = new Language(null, 0);
        assertEquals(lang.lang, null);
        assertEquals(lang.prob, 0.0, 0.0001);
        assertEquals(lang.toString(), "");

        Language lang2 = new Language("en", 1.0);
        assertEquals(lang2.lang, "en");
        assertEquals(lang2.prob, 1.0, 0.0001);
        assertEquals(lang2.toString(), "en:1.0");

    }

}
