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

import com.edduarte.vokter.model.mongodb.Document;
import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.model.mongodb.Difference;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static com.edduarte.vokter.diff.DifferenceEvent.deleted;
import static com.edduarte.vokter.diff.DifferenceEvent.inserted;
import static com.edduarte.vokter.diff.DifferenceEvent.nothing;
import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DifferenceDetectorTest {

    private static final Logger logger = LoggerFactory.getLogger(DifferenceDetectorTest.class);

    private static MongoClient mongoClient;

    private static DB occurrencesDB;

    private static ParserPool parserPool;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        occurrencesDB = mongoClient.getDB("test_terms_db");
        parserPool = new ParserPool();
        parserPool.place(new SimpleParser());
    }


    @AfterClass
    public static void close() {
        occurrencesDB.dropDatabase();
        mongoClient.close();
    }


    @Test
    public void testSimple() throws JsonProcessingException {
        String url = "http://www.bbc.com/news/uk/";
        String type = MediaType.TEXT_PLAIN;
        String oldSnapshot = "is the of the 100-eyed giant in Greek mythology.";
        String newSnapshot = "Argus Panoptes is the name of the 100-eyed giant in Norse mythology.";

        Document oldSnapshotDoc = DocumentBuilder
                .fromString(url, oldSnapshot, type)
                .ignoreCase()
//                .withStopwords()
//                .withStemming()
                .build(occurrencesDB, parserPool);

        Document newSnapshotDoc = DocumentBuilder
                .fromString(url, newSnapshot, type)
                .ignoreCase()
//                .withStopwords()
//                .withStemming()
                .build(occurrencesDB, parserPool);

        DifferenceDetector comparison = new DifferenceDetector(
                oldSnapshotDoc,
                newSnapshotDoc,
                parserPool
        );
        List<Difference> diffList = comparison.call();

        assertEquals(6, diffList.size());

        Difference d = diffList.get(0);
        assertEquals(deleted, d.getEvent());
        assertEquals("is th", d.getText());
        assertEquals(0, d.getStartIndex());

        d = diffList.get(1);
        assertEquals(inserted, d.getEvent());
        assertEquals("argus panoptes is the nam", d.getText());
        assertEquals(0, d.getStartIndex());

        d = diffList.get(2);
        assertEquals(nothing, d.getEvent());
        assertEquals("e of the 100 eyed giant in ", d.getText());
        assertEquals(5, d.getStartIndex());

        d = diffList.get(3);
        assertEquals(deleted, d.getEvent());
        assertEquals("greek", d.getText());
        assertEquals(32, d.getStartIndex());

        d = diffList.get(4);
        assertEquals(inserted, d.getEvent());
        assertEquals("norse", d.getText());
        assertEquals(52, d.getStartIndex());

        d = diffList.get(5);
        assertEquals(nothing, d.getEvent());
        assertEquals(" mythology", d.getText());
        assertEquals(37, d.getStartIndex());
    }


    @Test
    public void testBBCNews() throws IOException {
        String url = "http://www.bbc.com/news/uk/";
        String type = MediaType.TEXT_HTML;
        InputStream oldStream = getClass().getResourceAsStream("bbc_news_8_12_2014_11_00.html");
        InputStream newStream = getClass().getResourceAsStream("bbc_news_8_12_2014_13_00.html");
        String oldSnapshot = IOUtils.toString(oldStream);
        String newSnapshot = IOUtils.toString(newStream);

        Document oldSnapshotDoc = DocumentBuilder
                .fromString(url, oldSnapshot, type)
                .ignoreCase()
//                .withStopwords()
//                .withStemming()
                .build(occurrencesDB, parserPool);

        Document newSnapshotDoc = DocumentBuilder
                .fromString(url, newSnapshot, type)
                .ignoreCase()
//                .withStopwords()
//                .withStemming()
                .build(occurrencesDB, parserPool);

        DifferenceDetector comparison = new DifferenceDetector(
                oldSnapshotDoc,
                newSnapshotDoc,
                parserPool
        );
        List<Difference> diffList = comparison.call();
        ObjectWriter mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
        String s = mapper.writeValueAsString(diffList);
        logger.info("{}", s);

        assertEquals(57, diffList.size());

        Iterator<Difference> it = diffList.iterator();

        Difference d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("bbc news uk for a better experience on your device try our mobile site accessibility links skip to content skip to local navigation accessibility help bbc navigation news sport weather earth future shop tv radio more search term uk rss feed home uk africa asia australia europe latin america mid east us canada business health sci environment tech entertainment video england northern ireland scotland wales uk politics education 8 december 2014 last updated at 1", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("0 52", d.getText());
        assertEquals(463, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("3 07", d.getText());
        assertEquals(463, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" gmt dewani ", d.getText());
        assertEquals(4, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("murder case thrown out judge dismisses case against shrien dewani accused of arranging murder of wife anni in south africa shrien dewani trial reaction", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("cleared of honeymoon murder the family of anni dewani believe they have been failed by the justice system after millionaire businessman shrien dewani is cleared of the honeymoon murder shrien dewani trial reaction live how dewani prosecution fell apart from dream wedding to fatal hijacking hiring a hitman in south africa dewani family justice system failed watch the background to the dewani case watch", d.getText());
        assertEquals(479, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" pay benefits faster to cut hunger an income squeeze benefit delays and high utility bills are blamed by a cross party committee of mps for a rise in hunger in the uk young and hungry in 2014 britain archbishop urges help for uk hungry rise in demand for ni food banks christmas dining on a food parcel bus wheelchair ruling overturned bus companies are not required by law to force parents with buggies to make way for wheelchair users senior judges rule appeal over bus wheelchair ruling bus firm appeals wheelchair decision watch disabled man wins bus company case firm did not breach equality act uk base jumper dies in australia fall a british base jumper dies after falling from a cliff in sydney australia while on an outing with friends ", d.getText());
        assertEquals(167, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("fresh cracks appear in coalition senior conservative and lib dem ministers criticise each other amid suggestions of widening divisions in the coalition government home owners could handle rate rise the majority of people with mortgages could cope with a rise in interest rates the bank of england has said", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("disability fund closure ruled lawful a government decision to close a fund that helps disabled people to live and work in the community is lawful the high court rules coalition to survive despite spats senior lib dem danny alexander insists that trading insults with his conservatives partners does not undermine the parties ability to work together johnson criticises farage m4 excuse home owners could handle rate rise", d.getText());
        assertEquals(1628, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" air pollution public health crisis goals defined for uk s lunar mission royal couple arrive in new york ", d.getText());
        assertEquals(83, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("playstation hit by hack attack ", d.getText());
        assertEquals(2153, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("terror police make more fraud charges maths and science cameron s priority ", d.getText());
        assertEquals(1785, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("take care complaints seriously defence staff begin 10 day strike uk embassy in cairo remains closed teenage runaways lack refuges pride wins best film at indie awards", d.getText());
        assertEquals(1860, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("uk embassy in cairo remains closed", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" also in the news mp played candy crush during committee sheeran returns to top of uk chart local news weather northern ireland four held as man dies in stabbing 8m compensation for disabled boy wales orcymru ", d.getText());
        assertEquals(34, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("stand", d.getText());
        assertEquals(2235, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("eight c", d.getText());
        assertEquals(2502, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("ar", d.getText());
        assertEquals(2240, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("d", d.getText());
        assertEquals(2242, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("s ", d.getText());
        assertEquals(2511, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("fear over school cuts alcohol price law could save 900m", d.getText());
        assertEquals(2245, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("damaged in arson attack farmer delivers downing street tree", d.getText());
        assertEquals(2513, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" scotland oralba w", d.getText());
        assertEquals(4, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("arn", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("oman killed ", d.getText());
        assertEquals(2590, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("in", d.getText());
        assertEquals(2321, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("g", d.getText());
        assertEquals(2323, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" a", d.getText());
        assertEquals(2324, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("s snow and ice affect roads man found dead in clo", d.getText());
        assertEquals(2326, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("mbulance accident bank accu", d.getText());
        assertEquals(2606, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("se", d.getText());
        assertEquals(2375, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("d", d.getText());
        assertEquals(2635, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" of f", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("lat", d.getText());
        assertEquals(2382, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("raud over mortgage", d.getText());
        assertEquals(2641, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("s england shrien dewani trial reaction ", d.getText());
        assertEquals(8, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("boy 15 killed in fight is name", d.getText());
        assertEquals(2424, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("live son detained for trying to kill da", d.getText());
        assertEquals(2698, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("d our expert article written by mark easton mark easton home editor more from mark follow mark on twitter learning the facts about learning 26 november 2014 government commissioned research suggests many of the approaches to education we think work are actually useless or even make matters worse read full article more from the uk sport state memorial for hughes cancelled a state memorial service for australia cricketer phillip hughes is cancelled because of the outpouring of grief at his funeral ", d.getText());
        assertEquals(26, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("cap on ", d.getText());
        assertEquals(3238, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("olympic ", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("games bid revamp is pass", d.getText());
        assertEquals(2963, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("sports is dropp", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("ed sportsday rolling sports news live magazine why china sees itself in lowry s paintings of industrial britain ls lowry s paintings of industrial working class britain brought him popular fame in the uk but his works were never the subject of a solo exhibition abroad until now tim hetherington views after the wave living longer in lovely hill uk politics salmond announces uk parliament bid former snp leader and first minister alex salmond announces he is to stand for a seat at westminster at next may s general election clegg osborne s plans don t add up producer gobby quits bbc for ukip from bbc travel footage of a death defying ride a stunt rider takes on notorious mountain peaks where norway meets new zealand a glacier carved land of breathtaking views from bbc earth h", d.getText());
        assertEquals(559, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("ow to scare off the biggest pest elephants need to be kept away from farms inside a giant spider take a unique journey inside the body of a giant tarantula democracy live house of commons", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("uge crabs that munch on coconuts they are gigantic odd and may be endangered how to scare off the biggest pest elephants need to be kept away from farms democracy live", d.getText());
        assertEquals(4050, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" full coverage in the commons as mps debate private members bills find a representative search terms enter the name of who you are looking for a place or full postcode e g cf10 3nq watch listen ls lowry exhibited in china watch02 03 ", d.getText());
        assertEquals(35, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("crackdown on uk s billion nuisance calls watch01 20 ", d.getText());
        assertEquals(51, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("cold calls break law says report listen barclays introduce video banking listen schools at risk from deadly pollution watch01 06 light christmas uk s brightest home watch00 34 meet the retail ombudsman watch01 21 call for more kinship carer support watch01 32 ", d.getText());
        assertEquals(51, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("trafficking and slavery in the uk watch01 45 radio 5 live live features analysis digging for danger the man who found 100 bombs in afghanistan", d.getText());
        assertEquals(0, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("features analysis throes of change is something stirring on the high street digging for danger the man who found 100 bombs in afghanistan young and hungry the people struggling to put food on the table interest rates why a rise might be worse for some families than the economy", d.getText());
        assertEquals(4762, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" brunel s icon 150 years of the clifton suspension bridge ", d.getText());
        assertEquals(5039, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("72 christmas trees and 52 rooms in house to decorate", d.getText());
        assertEquals(4649, d.getStartIndex());

        d = it.next();
        assertEquals(inserted, d.getEvent());
        assertEquals("inbox fatigue how to take back control of your email", d.getText());
        assertEquals(5097, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals(" a mother s dream watch toy stores create autism friendly christmas shopping ", d.getText());
        assertEquals(4701, d.getStartIndex());

        d = it.next();
        assertEquals(deleted, d.getEvent());
        assertEquals("magic moments making a living in the business of show ", d.getText());
        assertEquals(4778, d.getStartIndex());

        d = it.next();
        assertEquals(nothing, d.getEvent());
        assertEquals("mortgage misery papers speculate on the prospects of a rate rise most popular from uk in the last week sunday drone involved in heathrow near miss saturday uk to set up mid east military base friday prison book ban ruled unlawful thursday foetal alcohol damages case rejected wednesday lee rigby s killers lose appeal bids elsewhere on the bbc whale of a time revel in the rare chance to see canada s orcas in the wild services mobile connected tv news feeds alerts e mail news about bbc news editors blog bbc college of journalism news sources media action editorial guidelines bbc links mobile site terms of use about the bbc advertise with us privacy accessibility help ad choices cookies contact the bbc parental guidance bbc 2014 the bbc is not responsible for the content of external sites read more this page is best viewed in an up to date web browser with style sheets css enabled while you will be able to view the content of this page in your current browser you will not be able to get the full visual experience please consider upgrading your browser software or enabling style sheets css if you are able to do so", d.getText());
        assertEquals(4832, d.getStartIndex());

    }
}

