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

import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.persistence.Diff;
import com.edduarte.vokter.persistence.Document;
import com.edduarte.vokter.persistence.ram.RAMDiff;
import com.edduarte.vokter.persistence.ram.RAMDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static com.edduarte.vokter.diff.DiffEvent.deleted;
import static com.edduarte.vokter.diff.DiffEvent.inserted;
import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DiffDetectorTest {

    private static final Logger logger = LoggerFactory.getLogger(DiffDetectorTest.class);

    private static LanguageDetector langDetector;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
    }


    @Test
    public void testSimple() throws JsonProcessingException {
        String url = "http://www.bbc.com/news/uk/";
        String type = MediaType.TEXT_PLAIN;
        String oldSnapshot = "is the of the 100-eyed giant in Greek mythology.";
        String newSnapshot = "Argus Panoptes is the name of the 100-eyed giant in Norse mythology.";

        Document oldSnapshotDoc = DocumentBuilder
                .fromString(url, oldSnapshot, type)
                .build(langDetector, RAMDocument.class);

        Document newSnapshotDoc = DocumentBuilder
                .fromString(url, newSnapshot, type)
                .build(langDetector, RAMDocument.class);

        DiffDetector comparison = new DiffDetector(
                oldSnapshotDoc,
                newSnapshotDoc,
                RAMDiff.class
        );
        List<Diff> diffList = comparison.call();

        assertEquals(4, diffList.size());

        Diff d = diffList.get(0);
        assertEquals(deleted, d.getEvent());
        assertEquals("is th", d.getText());
        assertEquals(0, d.getStartIndex());

        d = diffList.get(1);
        assertEquals(inserted, d.getEvent());
        assertEquals("Argus Panoptes is the nam", d.getText());
        assertEquals(0, d.getStartIndex());

        d = diffList.get(2);
        assertEquals(deleted, d.getEvent());
        assertEquals("Greek", d.getText());
        assertEquals(32, d.getStartIndex());

        d = diffList.get(3);
        assertEquals(inserted, d.getEvent());
        assertEquals("Norse", d.getText());
        assertEquals(52, d.getStartIndex());
    }


    @Test
    public void testBBCNews() throws IOException {
        String url = "http://www.bbc.com/news/uk/";
        String contentType = MediaType.TEXT_HTML;
        InputStream oldStream = getClass().getResourceAsStream("bbc_news_8_12_2014_11_00.html");
        InputStream newStream = getClass().getResourceAsStream("bbc_news_8_12_2014_13_00.html");
        String oldSnapshot = IOUtils.toString(oldStream);
        String newSnapshot = IOUtils.toString(newStream);

        Document oldSnapshotDoc = DocumentBuilder
                .fromString(url, oldSnapshot, contentType)
                .build(langDetector, RAMDocument.class);

        Document newSnapshotDoc = DocumentBuilder
                .fromString(url, newSnapshot, contentType)
                .build(langDetector, RAMDocument.class);

        DiffDetector comparison = new DiffDetector(
                oldSnapshotDoc,
                newSnapshotDoc,
                RAMDiff.class
        );
        List<Diff> diffList = comparison.call();

        assertEquals(25, diffList.size());

        Iterator<Diff> it = diffList.iterator();

        Diff d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("0 52", d.getText());
        assertEquals(463, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("3 07", d.getText());
        assertEquals(463, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("murder case thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the justice system after millionaire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The background to the Dewani case Watch", d.getText());
        assertEquals(479, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions of widening divisions in the coalition government Home owners could handle rate rise The majority of people with mortgages could cope with a rise in interest rates the Bank of England has said", d.getText());
        assertEquals(1375, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("Disability fund closure ruled lawful A government decision to close a fund that helps disabled people to live and work in the community is lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners does not undermine the parties ability to work together Johnson criticises Farage M4 excuse Home owners could handle rate rise", d.getText());
        assertEquals(1628, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("PlayStation hit by hack attack ", d.getText());
        assertEquals(2153, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("Take care complaints seriously Defence staff begin 10 day strike UK embassy in Cairo remains closed Teenage runaways lack refuges Pride wins best film at indie awards", d.getText());
        assertEquals(1860, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("UK embassy in Cairo remains closed", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("Stand", d.getText());
        assertEquals(2235, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("Eight c", d.getText());
        assertEquals(2502, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("d", d.getText());
        assertEquals(2242, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("fear over school cuts Alcohol price law could save 900m Scotland orAlba Warning as snow and ice affect roads Man found dead in close of flats England Shrien Dewani trial Reaction Boy 15 killed in fight is name", d.getText());
        assertEquals(2245, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("damaged in arson attack Farmer delivers Downing Street tree Scotland orAlba Woman killed in ambulance accident Bank accused of fraud over mortgages England Shrien Dewani trial Reaction Live Son detained for trying to kill da", d.getText());
        assertEquals(2513, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("Cap on ", d.getText());
        assertEquals(3238, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("Games bid revamp is pass", d.getText());
        assertEquals(2963, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("sports is dropp", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("ow to scare off the biggest pest Elephants need to be kept away from farms Inside a giant spider Take a unique journey inside the body of a giant tarantula Democracy Live House of Commons", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("uge crabs that munch on coconuts They are gigantic odd and may be endangered How to scare off the biggest pest Elephants need to be kept away from farms Democracy Live", d.getText());
        assertEquals(4050, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("Crackdown on UK s billion nuisance calls Watch01 20 ", d.getText());
        assertEquals(51, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("Trafficking and slavery in the UK Watch01 45 Radio 5 live Live Features Analysis Digging for danger The man who found 100 bombs in Afghanistan", d.getText());
        assertEquals(4449, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("Features Analysis Throes of change Is something stirring on the High Street Digging for danger The man who found 100 bombs in Afghanistan Young and hungry The people struggling to put food on the table Interest rates Why a rise might be worse for some families than the economy", d.getText());
        assertEquals(4762, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("72 Christmas trees And 52 rooms in house to decorate", d.getText());
        assertEquals(4649, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("Inbox fatigue How to take back control of your email", d.getText());
        assertEquals(5097, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("Magic moments Making a living in the business of show ", d.getText());
        assertEquals(4778, d.getStartIndex());

    }
}

