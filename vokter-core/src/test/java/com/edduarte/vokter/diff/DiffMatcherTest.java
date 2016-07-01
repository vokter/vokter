/*
 * Copyright 2015 Eduardo Duarte
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

package com.edduarte.vokter.diff;

import com.edduarte.vokter.keyword.Keyword;
import com.edduarte.vokter.keyword.KeywordBuilder;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.edduarte.vokter.persistence.Diff;
import com.edduarte.vokter.persistence.ram.RAMDiff;
import com.google.common.collect.Lists;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.edduarte.vokter.diff.DiffEvent.deleted;
import static com.edduarte.vokter.diff.DiffEvent.inserted;
import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DiffMatcherTest {

    private static final Logger logger = LoggerFactory.getLogger(DiffMatcherTest.class);

    private static ParserPool parserPool;

    private static LanguageDetector langDetector;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        parserPool = new ParserPool();
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        parserPool.place(new SimpleParser());
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
    }


    @AfterClass
    public static void close() {
        parserPool.clear();
    }


    @Test
    public void testSimple() throws Exception {
        List<String> words = Lists.newArrayList(
                "the greek",
                "argus panoptes"
        );
        List<Keyword> keywords = words
                .parallelStream()
                .map(string -> KeywordBuilder.fromText(string)
                        .ignoreCase()
                        .withStopwords()
                        .withStemming()
                        .build(parserPool))
                .collect(Collectors.toList());

        String oldSnapshot = "is the of the 100-eyed giant in Greek mythology.";
        String newSnapshot = "Argus Panoptes is the name of the 100-eyed giant in Norse mythology.";

        List<Diff> diffs = Lists.newArrayList(
                new RAMDiff(DiffEvent.deleted, "is th", 0),
                new RAMDiff(DiffEvent.inserted, "argus panoptes is the nam", 0),
                new RAMDiff(DiffEvent.deleted, "greek", 32),
                new RAMDiff(DiffEvent.inserted, "norse", 52)
        );

        DiffMatcher matcher = new DiffMatcher(
                oldSnapshot, newSnapshot,
                keywords, diffs, parserPool, langDetector,
                true, true, true,
                false, false,
                50
        );

        List<Match> matchList = new ArrayList<>(matcher.call());
        Collections.sort(matchList, (o1, o2) ->
                o1.getKeyword().getOriginalInput()
                        .compareTo(o2.getKeyword().getOriginalInput()));
        Iterator<Match> it = matchList.iterator();

        Match m = it.next();
        assertEquals(m.getEvent(), DiffEvent.inserted);
        assertEquals(m.getKeyword().getOriginalInput(), "argus panoptes");

        m = it.next();
        assertEquals(m.getEvent(), DiffEvent.deleted);
        assertEquals(m.getKeyword().getOriginalInput(), "the greek");
    }


    @Test
    public void testBBCNews() throws IOException {
        List<String> words = Lists.newArrayList(
                "House of Commons",
                "Shrien Dewani"
        );
        List<Keyword> keywords = words
                .parallelStream()
                .map(string -> KeywordBuilder.fromText(string)
                        .ignoreCase()
                        .withStopwords()
                        .withStemming()
                        .build(parserPool))
                .collect(Collectors.toList());

        List<Diff> diffs = Lists.newArrayList(
                new RAMDiff(deleted, "0 52", 463),
                new RAMDiff(inserted, "3 07", 463),
                new RAMDiff(deleted, "murder case thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction", 0),
                new RAMDiff(inserted, "cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the justice system after millionaire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The background to the Dewani case Watch", 479),
                new RAMDiff(deleted, "Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions of widening divisions in the coalition government Home owners could handle rate rise The majority of people with mortgages could cope with a rise in interest rates the Bank of England has said", 1375),
                new RAMDiff(inserted, "Disability fund closure ruled lawful A government decision to close a fund that helps disabled people to live and work in the community is lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners does not undermine the parties ability to work together Johnson criticises Farage M4 excuse Home owners could handle rate rise", 1628),
                new RAMDiff(inserted, "PlayStation hit by hack attack ", 2153),
                new RAMDiff(deleted, "Take care complaints seriously Defence staff begin 10 day strike UK embassy in Cairo remains closed Teenage runaways lack refuges Pride wins best film at indie awards", 1860),
                new RAMDiff(inserted, "UK embassy in Cairo remains closed", 0),
                new RAMDiff(deleted, "Stand", 2235),
                new RAMDiff(inserted, "Eight c", 2502),
                new RAMDiff(deleted, "d", 2242),
                new RAMDiff(deleted, "fear over school cuts Alcohol price law could save 900m Scotland orAlba Warning as snow and ice affect roads Man found dead in close of flats England Shrien Dewani trial Reaction Boy 15 killed in fight is name", 2245),
                new RAMDiff(inserted, "damaged in arson attack Farmer delivers Downing Street tree Scotland orAlba Woman killed in ambulance accident Bank accused of fraud over mortgages England Shrien Dewani trial Reaction Live Son detained for trying to kill da", 2513),
                new RAMDiff(inserted, "Cap on ", 3238),
                new RAMDiff(deleted, "Games bid revamp is pass", 2963),
                new RAMDiff(inserted, "sports is dropp", 0),
                new RAMDiff(deleted, "ow to scare off the biggest pest Elephants need to be kept away from farms Inside a giant spider Take a unique journey inside the body of a giant tarantula Democracy Live House of Commons", 0),
                new RAMDiff(inserted, "uge crabs that munch on coconuts They are gigantic odd and may be endangered How to scare off the biggest pest Elephants need to be kept away from farms Democracy Live", 4050),
                new RAMDiff(inserted, "Crackdown on UK s billion nuisance calls Watch01 20 ", 51),
                new RAMDiff(deleted, "Trafficking and slavery in the UK Watch01 45 Radio 5 live Live Features Analysis Digging for danger The man who found 100 bombs in Afghanistan", 4449),
                new RAMDiff(inserted, "Features Analysis Throes of change Is something stirring on the High Street Digging for danger The man who found 100 bombs in Afghanistan Young and hungry The people struggling to put food on the table Interest rates Why a rise might be worse for some families than the economy", 4762),
                new RAMDiff(deleted, "72 Christmas trees And 52 rooms in house to decorate", 4649),
                new RAMDiff(inserted, "Inbox fatigue How to take back control of your email", 5097)

        );

        InputStream oldStream = getClass().getResourceAsStream("bbc_news_8_12_2014_11_00.html");
        InputStream newStream = getClass().getResourceAsStream("bbc_news_8_12_2014_13_00.html");
        String oldSnapshot = IOUtils.toString(oldStream);
        String newSnapshot = IOUtils.toString(newStream);

        DiffMatcher matcher = new DiffMatcher(
                oldSnapshot, newSnapshot,
                keywords, diffs, parserPool, langDetector,
                true, true, true,
                false, false,
                50
        );

        List<Match> matchList = new ArrayList<>(matcher.call());
        Collections.sort(matchList, (o1, o2) -> {
            int compare = o1.getKeyword().toString()
                    .compareTo(o2.getKeyword().toString());
            if (compare == 0) {
                compare = o1.getSnippet().compareTo(o2.getSnippet());
            }
            return compare;
        });
        Iterator<Match> it = matchList.iterator();

        Match m = it.next();
        assertEquals(deleted, m.getEvent());
        assertEquals("hous common", m.getKeyword().toString());
        assertEquals("ow to scare off the biggest pest Elephants need to be kept away from farms Inside a giant spider Take a unique journey inside the body of a giant tarantula Democracy Live House of Commons", m.getText());

        m = it.next();
        assertEquals(deleted, m.getEvent());
        assertEquals("shrien dewani", m.getKeyword().toString());
        assertEquals("fear over school cuts Alcohol price law could save 900m Scotland orAlba Warning as snow and ice affect roads Man found dead in close of flats England Shrien Dewani trial Reaction Boy 15 killed in fight is name", m.getText());

        m = it.next();
        assertEquals(deleted, m.getEvent());
        assertEquals("shrien dewani", m.getKeyword().toString());
        assertEquals("murder case thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction", m.getText());

        m = it.next();
        assertEquals(inserted, m.getEvent());
        assertEquals("shrien dewani", m.getKeyword().toString());
        assertEquals("damaged in arson attack Farmer delivers Downing Street tree Scotland orAlba Woman killed in ambulance accident Bank accused of fraud over mortgages England Shrien Dewani trial Reaction Live Son detained for trying to kill da", m.getText());

        m = it.next();
        assertEquals(inserted, m.getEvent());
        assertEquals("shrien dewani", m.getKeyword().toString());
        assertEquals("cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the justice system after millionaire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The background to the Dewani case Watch", m.getText());

    }
}

