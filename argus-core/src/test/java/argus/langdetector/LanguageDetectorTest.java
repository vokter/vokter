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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link LanguageDetector} and {@link LanguageDetectorFactory}.
 *
 * @author Nakatani Shuyo
 */
public class LanguageDetectorTest {

    private static final String TRAINING_EN = "a a a b b c c d e";

    private static final String TRAINING_FR = "a b b c c c d d d";

    private static final String TRAINING_JA = "\u3042 \u3042 \u3042 \u3044 \u3046 \u3048 \u3048";

    private static final String JSON_LANG1 = "{\"freq\":{\"A\":3,\"B\":6,\"C\":3,\"AB\":2,\"BC\":1,\"ABC\":2,\"BBC\":1,\"CBA\":1},\"n_words\":[12,3,4],\"name\":\"lang1\"}";

    private static final String JSON_LANG2 = "{\"freq\":{\"A\":6,\"B\":3,\"C\":3,\"AA\":3,\"AB\":2,\"ABC\":1,\"ABA\":1,\"CAA\":1},\"n_words\":[12,5,3],\"name\":\"lang2\"}";


    @Before
    public void setUp() throws Exception {
        LanguageDetectorFactory.clear();

        LanguageProfile profile_en = new LanguageProfile("en");
        for (String w : TRAINING_EN.split(" "))
            profile_en.add(w);
        LanguageDetectorFactory.addProfile(profile_en, 0, 3);

        LanguageProfile profile_fr = new LanguageProfile("fr");
        for (String w : TRAINING_FR.split(" "))
            profile_fr.add(w);
        LanguageDetectorFactory.addProfile(profile_fr, 1, 3);

        LanguageProfile profile_ja = new LanguageProfile("ja");
        for (String w : TRAINING_JA.split(" "))
            profile_ja.add(w);
        LanguageDetectorFactory.addProfile(profile_ja, 2, 3);
    }


    @After
    public void tearDown() throws Exception {
    }


    @Test
    public final void testDetector1() throws LanguageDetectorException {
        LanguageDetector detect = LanguageDetectorFactory.create();
        detect.append("a");
        assertEquals(detect.detect(), "en");
    }


    @Test
    public final void testDetector2() throws LanguageDetectorException {
        LanguageDetector detect = LanguageDetectorFactory.create();
        detect.append("b d");
        assertEquals(detect.detect(), "fr");
    }


    @Test
    public final void testDetector3() throws LanguageDetectorException {
        LanguageDetector detect = LanguageDetectorFactory.create();
        detect.append("d e");
        assertEquals(detect.detect(), "en");
    }


    @Test
    public final void testDetector4() throws LanguageDetectorException {
        LanguageDetector detect = LanguageDetectorFactory.create();
        detect.append("\u3042\u3042\u3042\u3042a");
        assertEquals(detect.detect(), "ja");
    }


    @Test
    public final void testLangList() throws LanguageDetectorException {
        List<String> langList = LanguageDetectorFactory.getLangList();
        assertEquals(langList.size(), 3);
        assertEquals(langList.get(0), "en");
        assertEquals(langList.get(1), "fr");
        assertEquals(langList.get(2), "ja");
    }


    @Test(expected = UnsupportedOperationException.class)
    public final void testLangListException() throws LanguageDetectorException {
        List<String> langList = LanguageDetectorFactory.getLangList();
        langList.add("hoge");
    }


    @Test
    public final void testFactoryFromJsonString() throws LanguageDetectorException {
        LanguageDetectorFactory.clear();
        List<String> profiles = new ArrayList<>();
        profiles.add(JSON_LANG1);
        profiles.add(JSON_LANG2);
        LanguageDetectorFactory.loadProfile(profiles);
        List<String> langList = LanguageDetectorFactory.getLangList();
        assertEquals(langList.size(), 2);
        assertEquals(langList.get(0), "lang1");
        assertEquals(langList.get(1), "lang2");
    }
}