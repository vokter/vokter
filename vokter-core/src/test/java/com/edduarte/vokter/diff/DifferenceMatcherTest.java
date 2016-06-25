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

import com.edduarte.vokter.model.mongodb.Keyword;
import com.edduarte.vokter.keyword.KeywordBuilder;
import com.edduarte.vokter.model.mongodb.Difference;
import com.edduarte.vokter.model.v2.Match;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.google.common.collect.Lists;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class DifferenceMatcherTest {

    private static final Logger logger = LoggerFactory.getLogger(DifferenceMatcherTest.class);

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
    public void testSimple() {
        List<String> words = Lists.newArrayList(
                "the greek",
                "argus panoptes"
        );
        List<Keyword> keywords = words
                .stream()
                .map(string -> KeywordBuilder.fromText(string)
                        .ignoreCase()
                        .withStopwords()
                        .withStemming()
                        .build(parserPool))
                .collect(Collectors.toList());

        List<Difference> diffList = Lists.newArrayList(
                new Difference(DifferenceEvent.inserted, "argus", 0),
                new Difference(DifferenceEvent.inserted, "panopt", 0),
                new Difference(DifferenceEvent.inserted, "name", 0),
                new Difference(DifferenceEvent.deleted, "greek", 32),
                new Difference(DifferenceEvent.inserted, "nors", 52)
        );

        DifferenceMatcher matcher = new DifferenceMatcher(keywords, diffList, false, false);
        Set<Match> matchSet = matcher.call();
        Iterator<Match> it = matchSet.iterator();

        Match m = it.next();
        // detect if the first match is for keyword 'the greek' or 'argus panoptes'
        if (m.getKeyword().getOriginalInput().equalsIgnoreCase("the greek")) {
            assertEquals(m.getEvent(), DifferenceEvent.deleted);
            assertEquals(m.getKeyword().getOriginalInput(), "the greek");

            m = it.next();
            assertEquals(m.getEvent(), DifferenceEvent.inserted);
            assertEquals(m.getKeyword().getOriginalInput(), "argus panoptes");

        } else {
            assertEquals(m.getEvent(), DifferenceEvent.inserted);
            assertEquals(m.getKeyword().getOriginalInput(), "argus panoptes");

            m = it.next();
            assertEquals(m.getEvent(), DifferenceEvent.deleted);
            assertEquals(m.getKeyword().getOriginalInput(), "the greek");
        }
    }


//    @Test
//    public void testBBCNews() {
//        List<String> words = Lists.newArrayList(
//                "House of Commons",
//                "Shrien Dewani"
//        );
//        List<Keyword> keywords = words
//                .stream()
//                .map(string -> KeywordBuilder.fromText(string)
//                        .ignoreCase()
//                        .withStopwords()
//                        .withStemming()
//                        .build(parserPool))
//                .collect(Collectors.toList());
//
//        List<Difference> diffList = Lists.newArrayList(
//                new Difference(DifferenceEvent.deleted, "0", ""),
//                new Difference(DifferenceEvent.deleted, "52", "tics Education 8 December 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge (dismisse"),
//                new Difference(DifferenceEvent.inserted, "3", ""),
//                new Difference(DifferenceEvent.inserted, "07", "tics Education 8 December 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The (famil"),
//                new Difference(DifferenceEvent.deleted, "murder", " 8 December 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case (against Shr"),
//                new Difference(DifferenceEvent.deleted, "case", "mber 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against (Shrien D"),
//                new Difference(DifferenceEvent.deleted, "thrown", "2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien (Dewani a"),
//                new Difference(DifferenceEvent.deleted, "judg", "pdated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused (of "),
//                new Difference(DifferenceEvent.deleted, "dismiss", " at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused of (arranging "),
//                new Difference(DifferenceEvent.deleted, "case", "GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused of arranging (murde"),
//                new Difference(DifferenceEvent.deleted, "shrien", "rder case thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of (wife Anni "),
//                new Difference(DifferenceEvent.deleted, "dewani", "se thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni (in Sout"),
//                new Difference(DifferenceEvent.deleted, "accus", "wn out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South (Africa"),
//                new Difference(DifferenceEvent.deleted, "arrang", "e dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South Africa (Shrien Dewan"),
//                new Difference(DifferenceEvent.deleted, "murder", "s case against Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien (Dewani trial"),
//                new Difference(DifferenceEvent.deleted, "wife", "inst Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial (Reactio"),
//                new Difference(DifferenceEvent.deleted, "anni", "Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction (Pay"),
//                new Difference(DifferenceEvent.deleted, "south", "ewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay (benefits"),
//                new Difference(DifferenceEvent.deleted, "africa", "accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits (faster"),
//                new Difference(DifferenceEvent.deleted, "shrien", " of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster (to cut"),
//                new Difference(DifferenceEvent.deleted, "dewani", "anging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut (hunger"),
//                new Difference(DifferenceEvent.deleted, "trial", "murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut hunger (An in"),
//                new Difference(DifferenceEvent.deleted, "reaction", " of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut hunger An (income sque"),
//                new Difference(DifferenceEvent.inserted, "clear", " 8 December 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni (Dewani be"),
//                new Difference(DifferenceEvent.inserted, "honeymoon", " 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani (believe they ha"),
//                new Difference(DifferenceEvent.inserted, "murder", " updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani believe they (have been"),
//                new Difference(DifferenceEvent.inserted, "famili", " 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani believe they have been (failed by "),
//                new Difference(DifferenceEvent.inserted, "anni", " Dewani cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the (just"),
//                new Difference(DifferenceEvent.inserted, "dewani", "ni cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the (justice sys"),
//                new Difference(DifferenceEvent.inserted, "believ", "red of honeymoon murder The family of Anni Dewani believe they have been failed by the justice (system afte"),
//                new Difference(DifferenceEvent.inserted, "fail", " The family of Anni Dewani believe they have been failed by the justice system after millionaire (business"),
//                new Difference(DifferenceEvent.inserted, "justic", " Anni Dewani believe they have been failed by the justice system after millionaire businessman (Shrien Dewa"),
//                new Difference(DifferenceEvent.inserted, "system", "wani believe they have been failed by the justice system after millionaire businessman Shrien (Dewani is c"),
//                new Difference(DifferenceEvent.inserted, "millionair", "they have been failed by the justice system after millionaire businessman Shrien Dewani is (cleared of the hone"),
//                new Difference(DifferenceEvent.inserted, "businessman", "en failed by the justice system after millionaire businessman Shrien Dewani is cleared of (the honeymoon murder"),
//                new Difference(DifferenceEvent.inserted, "shrien", " the justice system after millionaire businessman Shrien Dewani is cleared of the honeymoon (murder Shrien"),
//                new Difference(DifferenceEvent.inserted, "dewani", "stice system after millionaire businessman Shrien Dewani is cleared of the honeymoon murder (Shrien Dewani"),
//                new Difference(DifferenceEvent.inserted, "clear", "em after millionaire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani (trial Reac"),
//                new Difference(DifferenceEvent.inserted, "honeymoon", "naire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial (Reaction Live How Dew"),
//                new Difference(DifferenceEvent.inserted, "murder", "nessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How (Dewani pro"),
//                new Difference(DifferenceEvent.inserted, "shrien", " Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani (prosecutio"),
//                new Difference(DifferenceEvent.inserted, "dewani", " Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani (prosecution fell "),
//                new Difference(DifferenceEvent.inserted, "trial", " is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell (apart "),
//                new Difference(DifferenceEvent.inserted, "reaction", "eared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell (apart From drea"),
//                new Difference(DifferenceEvent.inserted, "live", "the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream (wed"),
//                new Difference(DifferenceEvent.inserted, "dewani", "moon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream (wedding to fat"),
//                new Difference(DifferenceEvent.inserted, "prosecut", "rder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to (fatal hijacking"),
//                new Difference(DifferenceEvent.inserted, "fell", "Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking (Hiri"),
//                new Difference(DifferenceEvent.inserted, "apart", "i trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking (Hiring a h"),
//                new Difference(DifferenceEvent.inserted, "dream", "ction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman (in So"),
//                new Difference(DifferenceEvent.inserted, "wed", "Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in (South Afri"),
//                new Difference(DifferenceEvent.inserted, "fatal", "wani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa (Dewani"),
//                new Difference(DifferenceEvent.inserted, "hijack", "rosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa (Dewani family Ju"),
//                new Difference(DifferenceEvent.inserted, "hire", " fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family (Justice s"),
//                new Difference(DifferenceEvent.inserted, "hitman", "rt From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice (system fai"),
//                new Difference(DifferenceEvent.inserted, "south", "eam wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed (Watch"),
//                new Difference(DifferenceEvent.inserted, "africa", "dding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed (Watch The ba"),
//                new Difference(DifferenceEvent.inserted, "dewani", "o fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The (backgroun"),
//                new Difference(DifferenceEvent.inserted, "famili", " hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The (background to th"),
//                new Difference(DifferenceEvent.inserted, "justic", "ing Hiring a hitman in South Africa Dewani family Justice system failed Watch The background to (the Dewani"),
//                new Difference(DifferenceEvent.inserted, "system", "ng a hitman in South Africa Dewani family Justice system failed Watch The background to the (Dewani case W"),
//                new Difference(DifferenceEvent.inserted, "fail", "tman in South Africa Dewani family Justice system failed Watch The background to the Dewani case (Watch Pa"),
//                new Difference(DifferenceEvent.inserted, "watch", " South Africa Dewani family Justice system failed Watch The background to the Dewani case Watch (Pay bene"),
//                new Difference(DifferenceEvent.inserted, "background", "ica Dewani family Justice system failed Watch The background to the Dewani case Watch Pay (benefits faster to "),
//                new Difference(DifferenceEvent.inserted, "dewani", "Justice system failed Watch The background to the Dewani case Watch Pay benefits faster to cut (hunger An "),
//                new Difference(DifferenceEvent.inserted, "case", " system failed Watch The background to the Dewani case Watch Pay benefits faster to cut hunger An (incom"),
//                new Difference(DifferenceEvent.inserted, "watch", "em failed Watch The background to the Dewani case Watch Pay benefits faster to cut hunger An (income sque"),
//                new Difference(DifferenceEvent.deleted, "fresh", " Sydney Australia while on an outing with friends Fresh cracks appear in coalition Senior (Conservative a"),
//                new Difference(DifferenceEvent.deleted, "crack", "y Australia while on an outing with friends Fresh cracks appear in coalition Senior Conservative (and Lib "),
//                new Difference(DifferenceEvent.deleted, "appear", "alia while on an outing with friends Fresh cracks appear in coalition Senior Conservative and Lib (Dem min"),
//                new Difference(DifferenceEvent.deleted, "coalit", " on an outing with friends Fresh cracks appear in coalition Senior Conservative and Lib Dem (ministers critic"),
//                new Difference(DifferenceEvent.deleted, "senior", "ing with friends Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers (criticise eac"),
//                new Difference(DifferenceEvent.deleted, "conserv", "h friends Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers criticise (each other amid "),
//                new Difference(DifferenceEvent.deleted, "lib", "racks appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid (suggesti"),
//                new Difference(DifferenceEvent.deleted, "dem", "s appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions ("),
//                new Difference(DifferenceEvent.deleted, "minist", "pear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions (of widenin"),
//                new Difference(DifferenceEvent.deleted, "criticis", "alition Senior Conservative and Lib Dem ministers criticise each other amid suggestions of (widening division"),
//                new Difference(DifferenceEvent.deleted, "amid", "vative and Lib Dem ministers criticise each other amid suggestions of widening divisions in the (coaliti"),
//                new Difference(DifferenceEvent.deleted, "suggest", "e and Lib Dem ministers criticise each other amid suggestions of widening divisions in the (coalition governmen"),
//                new Difference(DifferenceEvent.deleted, "widen", "inisters criticise each other amid suggestions of widening divisions in the coalition government (Home owner"),
//                new Difference(DifferenceEvent.deleted, "divis", "criticise each other amid suggestions of widening divisions in the coalition government Home owners (could ha"),
//                new Difference(DifferenceEvent.deleted, "coalit", "her amid suggestions of widening divisions in the coalition government Home owners could handle (rate rise Th"),
//                new Difference(DifferenceEvent.deleted, "govern", "uggestions of widening divisions in the coalition government Home owners could handle rate rise (The majority "),
//                new Difference(DifferenceEvent.deleted, "home", "of widening divisions in the coalition government Home owners could handle rate rise The majority of (pe"),
//                new Difference(DifferenceEvent.deleted, "owner", "dening divisions in the coalition government Home owners could handle rate rise The majority of (people wi"),
//                new Difference(DifferenceEvent.deleted, "handl", "ons in the coalition government Home owners could handle rate rise The majority of people with (mortgages "),
//                new Difference(DifferenceEvent.deleted, "rate", "the coalition government Home owners could handle rate rise The majority of people with mortgages (could"),
//                new Difference(DifferenceEvent.deleted, "rise", "oalition government Home owners could handle rate rise The majority of people with mortgages could (cope"),
//                new Difference(DifferenceEvent.deleted, "major", "government Home owners could handle rate rise The majority of people with mortgages could cope with (a rise "),
//                new Difference(DifferenceEvent.deleted, "peop", ""),
//                new Difference(DifferenceEvent.inserted, "disabl", " Sydney Australia while on an outing with friends Disability fund closure ruled lawful A (government decision "),
//                new Difference(DifferenceEvent.inserted, "fund", "tralia while on an outing with friends Disability fund closure ruled lawful A government decision (to cl"),
//                new Difference(DifferenceEvent.inserted, "closur", "a while on an outing with friends Disability fund closure ruled lawful A government decision to (close a fu"),
//                new Difference(DifferenceEvent.inserted, "rule", "on an outing with friends Disability fund closure ruled lawful A government decision to close a (fund tha"),
//                new Difference(DifferenceEvent.inserted, "law", "outing with friends Disability fund closure ruled lawful A government decision to close a fund that (helps"),
//                new Difference(DifferenceEvent.inserted, "govern", "th friends Disability fund closure ruled lawful A government decision to close a fund that helps (disabled peo"),
//                new Difference(DifferenceEvent.inserted, "decis", "Disability fund closure ruled lawful A government decision to close a fund that helps disabled (people to li"),
//                new Difference(DifferenceEvent.inserted, "close", "und closure ruled lawful A government decision to close a fund that helps disabled people to live (and wo"),
//                new Difference(DifferenceEvent.inserted, "fund", "ure ruled lawful A government decision to close a fund that helps disabled people to live and work (in t"),
//                new Difference(DifferenceEvent.inserted, "help", "lawful A government decision to close a fund that helps disabled people to live and work in the (communit"),
//                new Difference(DifferenceEvent.inserted, "disabl", " A government decision to close a fund that helps disabled people to live and work in the (community is lawf"),
//                new Difference(DifferenceEvent.inserted, "peopl", "ment decision to close a fund that helps disabled people to live and work in the community is (lawful the "),
//                new Difference(DifferenceEvent.inserted, "live", "ion to close a fund that helps disabled people to live and work in the community is lawful the High (Cou"),
//                new Difference(DifferenceEvent.inserted, "work", "ose a fund that helps disabled people to live and work in the community is lawful the High Court (rules "),
//                new Difference(DifferenceEvent.inserted, "communiti", "hat helps disabled people to live and work in the community is lawful the High Court rules (Coalition to surv"),
//                new Difference(DifferenceEvent.inserted, "law", "abled people to live and work in the community is lawful the High Court rules Coalition to survive (despit"),
//                new Difference(DifferenceEvent.inserted, "high", "e to live and work in the community is lawful the High Court rules Coalition to survive despite (spats S"),
//                new Difference(DifferenceEvent.inserted, "court", "live and work in the community is lawful the High Court rules Coalition to survive despite spats (Senior "),
//                new Difference(DifferenceEvent.inserted, "rule", "nd work in the community is lawful the High Court rules Coalition to survive despite spats Senior (Lib De"),
//                new Difference(DifferenceEvent.inserted, "coalit", "k in the community is lawful the High Court rules Coalition to survive despite spats Senior Lib (Dem Danny Al"),
//                new Difference(DifferenceEvent.inserted, "surviv", "unity is lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny (Alexander ins"),
//                new Difference(DifferenceEvent.inserted, "despit", " lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander (insists tha"),
//                new Difference(DifferenceEvent.inserted, "spat", "the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that (trad"),
//                new Difference(DifferenceEvent.inserted, "senior", "gh Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that (trading ins"),
//                new Difference(DifferenceEvent.inserted, "lib", "t rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading (insults"),
//                new Difference(DifferenceEvent.inserted, "dem", "les Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults (wit"),
//                new Difference(DifferenceEvent.inserted, "danni", "Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults (with his "),
//                new Difference(DifferenceEvent.inserted, "alexand", "ion to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults with (his Conservati"),
//                new Difference(DifferenceEvent.inserted, "insist", "vive despite spats Senior Lib Dem Danny Alexander insists that trading insults with his (Conservatives part"),
//                new Difference(DifferenceEvent.inserted, "trade", "spats Senior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners (does not"),
//                new Difference(DifferenceEvent.inserted, "insult", "nior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners does (not undermi"),
//                new Difference(DifferenceEvent.inserted, "conserv", "y Alexander insists that trading insults with his Conservatives partners does not undermine the (parties ability "),
//                new Difference(DifferenceEvent.inserted, "partner", "sists that trading insults with his Conservatives partners does not undermine the parties (ability to work t"),
//                new Difference(DifferenceEvent.inserted, "undermin", " insults with his Conservatives partners does not undermine the parties ability to work (together Johnson cri"),
//                new Difference(DifferenceEvent.inserted, "parti", "his Conservatives partners does not undermine the parties ability to work together Johnson (criticises Fara"),
//                new Difference(DifferenceEvent.inserted, "abi", ""),
//                new Difference(DifferenceEvent.deleted, "m", ""),
//                new Difference(DifferenceEvent.inserted, "w", ""),
//                new Difference(DifferenceEvent.deleted, "tgag", ""),
//                new Difference(DifferenceEvent.deleted, "cope", ""),
//                new Difference(DifferenceEvent.deleted, "rise", ""),
//                new Difference(DifferenceEvent.deleted, "interest", ""),
//                new Difference(DifferenceEvent.deleted, "rate", ""),
//                new Difference(DifferenceEvent.deleted, "bank", ""),
//                new Difference(DifferenceEvent.deleted, "england", ""),
//                new Difference(DifferenceEvent.deleted, "said", ""),
//                new Difference(DifferenceEvent.inserted, "k", ""),
//                new Difference(DifferenceEvent.inserted, "togeth", ""),
//                new Difference(DifferenceEvent.inserted, "johnson", ""),
//                new Difference(DifferenceEvent.inserted, "criticis", ""),
//                new Difference(DifferenceEvent.inserted, "farag", ""),
//                new Difference(DifferenceEvent.inserted, "m4", ""),
//                new Difference(DifferenceEvent.inserted, "excus", ""),
//                new Difference(DifferenceEvent.inserted, "home", ""),
//                new Difference(DifferenceEvent.inserted, "owner", ""),
//                new Difference(DifferenceEvent.inserted, "handl", ""),
//                new Difference(DifferenceEvent.inserted, "rate", ""),
//                new Difference(DifferenceEvent.inserted, "rise", ""),
//                new Difference(DifferenceEvent.inserted, "playstat", ""),
//                new Difference(DifferenceEvent.inserted, "hit", ""),
//                new Difference(DifferenceEvent.inserted, "hack", ""),
//                new Difference(DifferenceEvent.inserted, "attack", ""),
//                new Difference(DifferenceEvent.deleted, "take", ""),
//                new Difference(DifferenceEvent.deleted, "care", ""),
//                new Difference(DifferenceEvent.deleted, "complaint", ""),
//                new Difference(DifferenceEvent.deleted, "serious", ""),
//                new Difference(DifferenceEvent.deleted, "defenc", ""),
//                new Difference(DifferenceEvent.deleted, "staff", ""),
//                new Difference(DifferenceEvent.deleted, "begin", ""),
//                new Difference(DifferenceEvent.deleted, "10", ""),
//                new Difference(DifferenceEvent.deleted, "day", ""),
//                new Difference(DifferenceEvent.deleted, "strike", ""),
//                new Difference(DifferenceEvent.deleted, "uk", ""),
//                new Difference(DifferenceEvent.deleted, "embassi", ""),
//                new Difference(DifferenceEvent.deleted, "cairo", ""),
//                new Difference(DifferenceEvent.deleted, "remain", ""),
//                new Difference(DifferenceEvent.deleted, "close", ""),
//                new Difference(DifferenceEvent.deleted, "teenag", ""),
//                new Difference(DifferenceEvent.deleted, "runaway", ""),
//                new Difference(DifferenceEvent.deleted, "lack", ""),
//                new Difference(DifferenceEvent.deleted, "refug", ""),
//                new Difference(DifferenceEvent.deleted, "pride", ""),
//                new Difference(DifferenceEvent.deleted, "win", ""),
//                new Difference(DifferenceEvent.deleted, "best", ""),
//                new Difference(DifferenceEvent.deleted, "film", ""),
//                new Difference(DifferenceEvent.deleted, "indi", ""),
//                new Difference(DifferenceEvent.deleted, "award", ""),
//                new Difference(DifferenceEvent.inserted, "uk", ""),
//                new Difference(DifferenceEvent.inserted, "embassi", ""),
//                new Difference(DifferenceEvent.inserted, "cairo", ""),
//                new Difference(DifferenceEvent.inserted, "remain", ""),
//                new Difference(DifferenceEvent.inserted, "close", ""),
//                new Difference(DifferenceEvent.deleted, "standard", ""),
//                new Difference(DifferenceEvent.deleted, "fear", ""),
//                new Difference(DifferenceEvent.deleted, "school", ""),
//                new Difference(DifferenceEvent.deleted, "cut", ""),
//                new Difference(DifferenceEvent.deleted, "alcohol", ""),
//                new Difference(DifferenceEvent.deleted, "price", ""),
//                new Difference(DifferenceEvent.deleted, "law", ""),
//                new Difference(DifferenceEvent.deleted, "save", ""),
//                new Difference(DifferenceEvent.deleted, "900m", ""),
//                new Difference(DifferenceEvent.deleted, "scotland", ""),
//                new Difference(DifferenceEvent.deleted, "oralba", ""),
//                new Difference(DifferenceEvent.deleted, "warn", ""),
//                new Difference(DifferenceEvent.deleted, "snow", ""),
//                new Difference(DifferenceEvent.deleted, "ice", ""),
//                new Difference(DifferenceEvent.deleted, "affect", ""),
//                new Difference(DifferenceEvent.deleted, "roa", ""),
//                new Difference(DifferenceEvent.inserted, "eight", ""),
//                new Difference(DifferenceEvent.inserted, "car", ""),
//                new Difference(DifferenceEvent.inserted, "damag", ""),
//                new Difference(DifferenceEvent.inserted, "arson", ""),
//                new Difference(DifferenceEvent.inserted, "attack", ""),
//                new Difference(DifferenceEvent.inserted, "farmer", ""),
//                new Difference(DifferenceEvent.inserted, "deliv", ""),
//                new Difference(DifferenceEvent.inserted, "down", ""),
//                new Difference(DifferenceEvent.inserted, "street", ""),
//                new Difference(DifferenceEvent.inserted, "tree", ""),
//                new Difference(DifferenceEvent.inserted, "scotland", ""),
//                new Difference(DifferenceEvent.inserted, "oralba", ""),
//                new Difference(DifferenceEvent.inserted, "woman", ""),
//                new Difference(DifferenceEvent.inserted, "kill", ""),
//                new Difference(DifferenceEvent.inserted, "ambul", ""),
//                new Difference(DifferenceEvent.inserted, "acci", ""),
//                new Difference(DifferenceEvent.deleted, "m", ""),
//                new Difference(DifferenceEvent.inserted, "b", ""),
//                new Difference(DifferenceEvent.deleted, "found", ""),
//                new Difference(DifferenceEvent.deleted, "dead", ""),
//                new Difference(DifferenceEvent.deleted, "close", ""),
//                new Difference(DifferenceEvent.deleted, "flat", ""),
//                new Difference(DifferenceEvent.inserted, "k", ""),
//                new Difference(DifferenceEvent.inserted, "accus", ""),
//                new Difference(DifferenceEvent.inserted, "fraud", ""),
//                new Difference(DifferenceEvent.inserted, "mortgag", ""),
//                new Difference(DifferenceEvent.deleted, "boy", ""),
//                new Difference(DifferenceEvent.deleted, "15", ""),
//                new Difference(DifferenceEvent.deleted, "kill", ""),
//                new Difference(DifferenceEvent.deleted, "fight", ""),
//                new Difference(DifferenceEvent.deleted, "name", ""),
//                new Difference(DifferenceEvent.inserted, "live", ""),
//                new Difference(DifferenceEvent.inserted, "son", ""),
//                new Difference(DifferenceEvent.inserted, "detain", ""),
//                new Difference(DifferenceEvent.inserted, "tri", ""),
//                new Difference(DifferenceEvent.inserted, "kill", ""),
//                new Difference(DifferenceEvent.inserted, "dad", ""),
//                new Difference(DifferenceEvent.inserted, "cap", ""),
//                new Difference(DifferenceEvent.deleted, "game", ""),
//                new Difference(DifferenceEvent.deleted, "bid", ""),
//                new Difference(DifferenceEvent.deleted, "revamp", ""),
//                new Difference(DifferenceEvent.deleted, "pass", ""),
//                new Difference(DifferenceEvent.inserted, "sport", "ap on Olympic sports is dropped Sportsday rolling sports news Live Magazine Why China sees itself (in Lowr"),
//                new Difference(DifferenceEvent.inserted, "drop", ""),
//                new Difference(DifferenceEvent.deleted, "scare", ""),
//                new Difference(DifferenceEvent.deleted, "biggest", ""),
//                new Difference(DifferenceEvent.deleted, "pest", ""),
//                new Difference(DifferenceEvent.deleted, "eleph", ""),
//                new Difference(DifferenceEvent.deleted, "need", ""),
//                new Difference(DifferenceEvent.deleted, "kept", ""),
//                new Difference(DifferenceEvent.deleted, "away", ""),
//                new Difference(DifferenceEvent.deleted, "farm", ""),
//                new Difference(DifferenceEvent.deleted, "insid", "farms Inside a giant spider Take a unique journey inside the body of a giant tarantula Democracy (Live Hou"),
//                new Difference(DifferenceEvent.deleted, "giant", "spider Take a unique journey inside the body of a giant tarantula Democracy Live House of Commons (Full c"),
//                new Difference(DifferenceEvent.deleted, "spider", ""),
//                new Difference(DifferenceEvent.deleted, "take", ""),
//                new Difference(DifferenceEvent.deleted, "uniqu", ""),
//                new Difference(DifferenceEvent.deleted, "journey", ""),
//                new Difference(DifferenceEvent.deleted, "insid", ""),
//                new Difference(DifferenceEvent.deleted, "bodi", ""),
//                new Difference(DifferenceEvent.deleted, "giant", ""),
//                new Difference(DifferenceEvent.deleted, "tarantula", ""),
//                new Difference(DifferenceEvent.inserted, "huge", ""),
//                new Difference(DifferenceEvent.inserted, "crab", ""),
//                new Difference(DifferenceEvent.inserted, "munch", ""),
//                new Difference(DifferenceEvent.inserted, "coconut", ""),
//                new Difference(DifferenceEvent.inserted, "gigant", ""),
//                new Difference(DifferenceEvent.inserted, "odd", ""),
//                new Difference(DifferenceEvent.inserted, "may", ""),
//                new Difference(DifferenceEvent.inserted, "endang", ""),
//                new Difference(DifferenceEvent.inserted, "scare", ""),
//                new Difference(DifferenceEvent.inserted, "biggest", ""),
//                new Difference(DifferenceEvent.inserted, "pest", ""),
//                new Difference(DifferenceEvent.inserted, "eleph", ""),
//                new Difference(DifferenceEvent.inserted, "need", ""),
//                new Difference(DifferenceEvent.inserted, "kept", ""),
//                new Difference(DifferenceEvent.inserted, "away", ""),
//                new Difference(DifferenceEvent.inserted, "farm", ""),
//                new Difference(DifferenceEvent.deleted, "hous", ""),
//                new Difference(DifferenceEvent.deleted, "common", "ocracy Live House of Commons Full coverage in the Commons as MPs debate private members bills Find (a repre"),
//                new Difference(DifferenceEvent.inserted, "crackdown", ""),
//                new Difference(DifferenceEvent.inserted, "uk", ""),
//                new Difference(DifferenceEvent.inserted, "s", ""),
//                new Difference(DifferenceEvent.inserted, "billion", ""),
//                new Difference(DifferenceEvent.inserted, "nuisanc", ""),
//                new Difference(DifferenceEvent.inserted, "call", "wn on UK s billion nuisance calls Watch01 20 Cold calls break law says report Listen Barclays (introduce "),
//                new Difference(DifferenceEvent.inserted, "watch01", ""),
//                new Difference(DifferenceEvent.inserted, "20", ""),
//                new Difference(DifferenceEvent.deleted, "traffick", ""),
//                new Difference(DifferenceEvent.deleted, "slaveri", ""),
//                new Difference(DifferenceEvent.deleted, "uk", ""),
//                new Difference(DifferenceEvent.deleted, "watch01", ""),
//                new Difference(DifferenceEvent.deleted, "45", ""),
//                new Difference(DifferenceEvent.deleted, "radio", ""),
//                new Difference(DifferenceEvent.deleted, "5", ""),
//                new Difference(DifferenceEvent.deleted, "live", ""),
//                new Difference(DifferenceEvent.deleted, "live", ""),
//                new Difference(DifferenceEvent.deleted, "featur", ""),
//                new Difference(DifferenceEvent.deleted, "analysi", ""),
//                new Difference(DifferenceEvent.deleted, "dig", ""),
//                new Difference(DifferenceEvent.deleted, "danger", ""),
//                new Difference(DifferenceEvent.deleted, "man", ""),
//                new Difference(DifferenceEvent.deleted, "found", ""),
//                new Difference(DifferenceEvent.deleted, "100", ""),
//                new Difference(DifferenceEvent.deleted, "bomb", ""),
//                new Difference(DifferenceEvent.deleted, "afghanistan", ""),
//                new Difference(DifferenceEvent.inserted, "featur", ""),
//                new Difference(DifferenceEvent.inserted, "analysi", ""),
//                new Difference(DifferenceEvent.inserted, "throe", ""),
//                new Difference(DifferenceEvent.inserted, "chang", ""),
//                new Difference(DifferenceEvent.inserted, "someth", ""),
//                new Difference(DifferenceEvent.inserted, "stir", ""),
//                new Difference(DifferenceEvent.inserted, "high", ""),
//                new Difference(DifferenceEvent.inserted, "street", ""),
//                new Difference(DifferenceEvent.inserted, "dig", ""),
//                new Difference(DifferenceEvent.inserted, "danger", ""),
//                new Difference(DifferenceEvent.inserted, "man", ""),
//                new Difference(DifferenceEvent.inserted, "found", ""),
//                new Difference(DifferenceEvent.inserted, "100", ""),
//                new Difference(DifferenceEvent.inserted, "bomb", ""),
//                new Difference(DifferenceEvent.inserted, "afghanistan", ""),
//                new Difference(DifferenceEvent.inserted, "young", ""),
//                new Difference(DifferenceEvent.inserted, "hungri", ""),
//                new Difference(DifferenceEvent.inserted, "peopl", ""),
//                new Difference(DifferenceEvent.inserted, "struggl", ""),
//                new Difference(DifferenceEvent.inserted, "put", ""),
//                new Difference(DifferenceEvent.inserted, "food", ""),
//                new Difference(DifferenceEvent.inserted, "tabl", ""),
//                new Difference(DifferenceEvent.inserted, "interest", ""),
//                new Difference(DifferenceEvent.inserted, "rate", ""),
//                new Difference(DifferenceEvent.inserted, "rise", ""),
//                new Difference(DifferenceEvent.inserted, "might", ""),
//                new Difference(DifferenceEvent.inserted, "wors", ""),
//                new Difference(DifferenceEvent.inserted, "famili", ""),
//                new Difference(DifferenceEvent.inserted, "economi", ""),
//                new Difference(DifferenceEvent.deleted, "72", ""),
//                new Difference(DifferenceEvent.deleted, "christma", ""),
//                new Difference(DifferenceEvent.deleted, "tree", ""),
//                new Difference(DifferenceEvent.deleted, "52", ""),
//                new Difference(DifferenceEvent.deleted, "room", ""),
//                new Difference(DifferenceEvent.deleted, "hous", ""),
//                new Difference(DifferenceEvent.deleted, "decor", ""),
//                new Difference(DifferenceEvent.inserted, "inbox", ""),
//                new Difference(DifferenceEvent.inserted, "fatigu", ""),
//                new Difference(DifferenceEvent.inserted, "take", ""),
//                new Difference(DifferenceEvent.inserted, "back", ""),
//                new Difference(DifferenceEvent.inserted, "control", ""),
//                new Difference(DifferenceEvent.inserted, "email", ""),
//                new Difference(DifferenceEvent.deleted, "magic", ""),
//                new Difference(DifferenceEvent.deleted, "moment", ""),
//                new Difference(DifferenceEvent.deleted, "make", ""),
//                new Difference(DifferenceEvent.deleted, "live", ""),
//                new Difference(DifferenceEvent.deleted, "busi", ""),
//                new Difference(DifferenceEvent.deleted, "show", "")
//        );
//
//        DifferenceMatcher matcher = new DifferenceMatcher(keywords, diffList, false, false);
//        Set<DifferenceMatcher.Result> matchSet = matcher.call();
//        assertEquals(11, matchSet.size());
//    }
}

