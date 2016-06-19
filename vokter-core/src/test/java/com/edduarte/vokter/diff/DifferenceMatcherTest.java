/*
 * Copyright 2015 Ed Duarte
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
import com.google.common.collect.Lists;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
                new Difference(DifferenceAction.inserted, "argus", "Argus Panoptes is the name of the 100 eyed giant in No"),
                new Difference(DifferenceAction.inserted, "panopt", "Argus Panoptes is the name of the 100 eyed giant in Norse mytho"),
                new Difference(DifferenceAction.inserted, "name", "Argus Panoptes is the name of the 100 eyed giant in Norse mythology"),
                new Difference(DifferenceAction.deleted, "greek", "is the of the 100 eyed giant in Greek mythology"),
                new Difference(DifferenceAction.inserted, "nors", "gus Panoptes is the name of the 100 eyed giant in Norse mythology")
        );

        DifferenceMatcher matcher = new DifferenceMatcher(keywords, diffList, false, false);
        Set<DifferenceMatcher.Result> matchSet = matcher.call();
        assertEquals(2, matchSet.size());
    }


    @Test
    public void testBBCNews() {
        List<String> words = Lists.newArrayList(
                "House of Commons",
                "Shrien Dewani"
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
                new Difference(DifferenceAction.deleted, "0", ""),
                new Difference(DifferenceAction.deleted, "52", "tics Education 8 December 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge (dismisse"),
                new Difference(DifferenceAction.inserted, "3", ""),
                new Difference(DifferenceAction.inserted, "07", "tics Education 8 December 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The (famil"),
                new Difference(DifferenceAction.deleted, "murder", " 8 December 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case (against Shr"),
                new Difference(DifferenceAction.deleted, "case", "mber 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against (Shrien D"),
                new Difference(DifferenceAction.deleted, "thrown", "2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien (Dewani a"),
                new Difference(DifferenceAction.deleted, "judg", "pdated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused (of "),
                new Difference(DifferenceAction.deleted, "dismiss", " at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused of (arranging "),
                new Difference(DifferenceAction.deleted, "case", "GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused of arranging (murde"),
                new Difference(DifferenceAction.deleted, "shrien", "rder case thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of (wife Anni "),
                new Difference(DifferenceAction.deleted, "dewani", "se thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni (in Sout"),
                new Difference(DifferenceAction.deleted, "accus", "wn out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South (Africa"),
                new Difference(DifferenceAction.deleted, "arrang", "e dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South Africa (Shrien Dewan"),
                new Difference(DifferenceAction.deleted, "murder", "s case against Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien (Dewani trial"),
                new Difference(DifferenceAction.deleted, "wife", "inst Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial (Reactio"),
                new Difference(DifferenceAction.deleted, "anni", "Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction (Pay"),
                new Difference(DifferenceAction.deleted, "south", "ewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay (benefits"),
                new Difference(DifferenceAction.deleted, "africa", "accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits (faster"),
                new Difference(DifferenceAction.deleted, "shrien", " of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster (to cut"),
                new Difference(DifferenceAction.deleted, "dewani", "anging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut (hunger"),
                new Difference(DifferenceAction.deleted, "trial", "murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut hunger (An in"),
                new Difference(DifferenceAction.deleted, "reaction", " of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut hunger An (income sque"),
                new Difference(DifferenceAction.inserted, "clear", " 8 December 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni (Dewani be"),
                new Difference(DifferenceAction.inserted, "honeymoon", " 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani (believe they ha"),
                new Difference(DifferenceAction.inserted, "murder", " updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani believe they (have been"),
                new Difference(DifferenceAction.inserted, "famili", " 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani believe they have been (failed by "),
                new Difference(DifferenceAction.inserted, "anni", " Dewani cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the (just"),
                new Difference(DifferenceAction.inserted, "dewani", "ni cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the (justice sys"),
                new Difference(DifferenceAction.inserted, "believ", "red of honeymoon murder The family of Anni Dewani believe they have been failed by the justice (system afte"),
                new Difference(DifferenceAction.inserted, "fail", " The family of Anni Dewani believe they have been failed by the justice system after millionaire (business"),
                new Difference(DifferenceAction.inserted, "justic", " Anni Dewani believe they have been failed by the justice system after millionaire businessman (Shrien Dewa"),
                new Difference(DifferenceAction.inserted, "system", "wani believe they have been failed by the justice system after millionaire businessman Shrien (Dewani is c"),
                new Difference(DifferenceAction.inserted, "millionair", "they have been failed by the justice system after millionaire businessman Shrien Dewani is (cleared of the hone"),
                new Difference(DifferenceAction.inserted, "businessman", "en failed by the justice system after millionaire businessman Shrien Dewani is cleared of (the honeymoon murder"),
                new Difference(DifferenceAction.inserted, "shrien", " the justice system after millionaire businessman Shrien Dewani is cleared of the honeymoon (murder Shrien"),
                new Difference(DifferenceAction.inserted, "dewani", "stice system after millionaire businessman Shrien Dewani is cleared of the honeymoon murder (Shrien Dewani"),
                new Difference(DifferenceAction.inserted, "clear", "em after millionaire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani (trial Reac"),
                new Difference(DifferenceAction.inserted, "honeymoon", "naire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial (Reaction Live How Dew"),
                new Difference(DifferenceAction.inserted, "murder", "nessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How (Dewani pro"),
                new Difference(DifferenceAction.inserted, "shrien", " Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani (prosecutio"),
                new Difference(DifferenceAction.inserted, "dewani", " Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani (prosecution fell "),
                new Difference(DifferenceAction.inserted, "trial", " is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell (apart "),
                new Difference(DifferenceAction.inserted, "reaction", "eared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell (apart From drea"),
                new Difference(DifferenceAction.inserted, "live", "the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream (wed"),
                new Difference(DifferenceAction.inserted, "dewani", "moon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream (wedding to fat"),
                new Difference(DifferenceAction.inserted, "prosecut", "rder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to (fatal hijacking"),
                new Difference(DifferenceAction.inserted, "fell", "Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking (Hiri"),
                new Difference(DifferenceAction.inserted, "apart", "i trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking (Hiring a h"),
                new Difference(DifferenceAction.inserted, "dream", "ction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman (in So"),
                new Difference(DifferenceAction.inserted, "wed", "Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in (South Afri"),
                new Difference(DifferenceAction.inserted, "fatal", "wani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa (Dewani"),
                new Difference(DifferenceAction.inserted, "hijack", "rosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa (Dewani family Ju"),
                new Difference(DifferenceAction.inserted, "hire", " fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family (Justice s"),
                new Difference(DifferenceAction.inserted, "hitman", "rt From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice (system fai"),
                new Difference(DifferenceAction.inserted, "south", "eam wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed (Watch"),
                new Difference(DifferenceAction.inserted, "africa", "dding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed (Watch The ba"),
                new Difference(DifferenceAction.inserted, "dewani", "o fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The (backgroun"),
                new Difference(DifferenceAction.inserted, "famili", " hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The (background to th"),
                new Difference(DifferenceAction.inserted, "justic", "ing Hiring a hitman in South Africa Dewani family Justice system failed Watch The background to (the Dewani"),
                new Difference(DifferenceAction.inserted, "system", "ng a hitman in South Africa Dewani family Justice system failed Watch The background to the (Dewani case W"),
                new Difference(DifferenceAction.inserted, "fail", "tman in South Africa Dewani family Justice system failed Watch The background to the Dewani case (Watch Pa"),
                new Difference(DifferenceAction.inserted, "watch", " South Africa Dewani family Justice system failed Watch The background to the Dewani case Watch (Pay bene"),
                new Difference(DifferenceAction.inserted, "background", "ica Dewani family Justice system failed Watch The background to the Dewani case Watch Pay (benefits faster to "),
                new Difference(DifferenceAction.inserted, "dewani", "Justice system failed Watch The background to the Dewani case Watch Pay benefits faster to cut (hunger An "),
                new Difference(DifferenceAction.inserted, "case", " system failed Watch The background to the Dewani case Watch Pay benefits faster to cut hunger An (incom"),
                new Difference(DifferenceAction.inserted, "watch", "em failed Watch The background to the Dewani case Watch Pay benefits faster to cut hunger An (income sque"),
                new Difference(DifferenceAction.deleted, "fresh", " Sydney Australia while on an outing with friends Fresh cracks appear in coalition Senior (Conservative a"),
                new Difference(DifferenceAction.deleted, "crack", "y Australia while on an outing with friends Fresh cracks appear in coalition Senior Conservative (and Lib "),
                new Difference(DifferenceAction.deleted, "appear", "alia while on an outing with friends Fresh cracks appear in coalition Senior Conservative and Lib (Dem min"),
                new Difference(DifferenceAction.deleted, "coalit", " on an outing with friends Fresh cracks appear in coalition Senior Conservative and Lib Dem (ministers critic"),
                new Difference(DifferenceAction.deleted, "senior", "ing with friends Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers (criticise eac"),
                new Difference(DifferenceAction.deleted, "conserv", "h friends Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers criticise (each other amid "),
                new Difference(DifferenceAction.deleted, "lib", "racks appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid (suggesti"),
                new Difference(DifferenceAction.deleted, "dem", "s appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions ("),
                new Difference(DifferenceAction.deleted, "minist", "pear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions (of widenin"),
                new Difference(DifferenceAction.deleted, "criticis", "alition Senior Conservative and Lib Dem ministers criticise each other amid suggestions of (widening division"),
                new Difference(DifferenceAction.deleted, "amid", "vative and Lib Dem ministers criticise each other amid suggestions of widening divisions in the (coaliti"),
                new Difference(DifferenceAction.deleted, "suggest", "e and Lib Dem ministers criticise each other amid suggestions of widening divisions in the (coalition governmen"),
                new Difference(DifferenceAction.deleted, "widen", "inisters criticise each other amid suggestions of widening divisions in the coalition government (Home owner"),
                new Difference(DifferenceAction.deleted, "divis", "criticise each other amid suggestions of widening divisions in the coalition government Home owners (could ha"),
                new Difference(DifferenceAction.deleted, "coalit", "her amid suggestions of widening divisions in the coalition government Home owners could handle (rate rise Th"),
                new Difference(DifferenceAction.deleted, "govern", "uggestions of widening divisions in the coalition government Home owners could handle rate rise (The majority "),
                new Difference(DifferenceAction.deleted, "home", "of widening divisions in the coalition government Home owners could handle rate rise The majority of (pe"),
                new Difference(DifferenceAction.deleted, "owner", "dening divisions in the coalition government Home owners could handle rate rise The majority of (people wi"),
                new Difference(DifferenceAction.deleted, "handl", "ons in the coalition government Home owners could handle rate rise The majority of people with (mortgages "),
                new Difference(DifferenceAction.deleted, "rate", "the coalition government Home owners could handle rate rise The majority of people with mortgages (could"),
                new Difference(DifferenceAction.deleted, "rise", "oalition government Home owners could handle rate rise The majority of people with mortgages could (cope"),
                new Difference(DifferenceAction.deleted, "major", "government Home owners could handle rate rise The majority of people with mortgages could cope with (a rise "),
                new Difference(DifferenceAction.deleted, "peop", ""),
                new Difference(DifferenceAction.inserted, "disabl", " Sydney Australia while on an outing with friends Disability fund closure ruled lawful A (government decision "),
                new Difference(DifferenceAction.inserted, "fund", "tralia while on an outing with friends Disability fund closure ruled lawful A government decision (to cl"),
                new Difference(DifferenceAction.inserted, "closur", "a while on an outing with friends Disability fund closure ruled lawful A government decision to (close a fu"),
                new Difference(DifferenceAction.inserted, "rule", "on an outing with friends Disability fund closure ruled lawful A government decision to close a (fund tha"),
                new Difference(DifferenceAction.inserted, "law", "outing with friends Disability fund closure ruled lawful A government decision to close a fund that (helps"),
                new Difference(DifferenceAction.inserted, "govern", "th friends Disability fund closure ruled lawful A government decision to close a fund that helps (disabled peo"),
                new Difference(DifferenceAction.inserted, "decis", "Disability fund closure ruled lawful A government decision to close a fund that helps disabled (people to li"),
                new Difference(DifferenceAction.inserted, "close", "und closure ruled lawful A government decision to close a fund that helps disabled people to live (and wo"),
                new Difference(DifferenceAction.inserted, "fund", "ure ruled lawful A government decision to close a fund that helps disabled people to live and work (in t"),
                new Difference(DifferenceAction.inserted, "help", "lawful A government decision to close a fund that helps disabled people to live and work in the (communit"),
                new Difference(DifferenceAction.inserted, "disabl", " A government decision to close a fund that helps disabled people to live and work in the (community is lawf"),
                new Difference(DifferenceAction.inserted, "peopl", "ment decision to close a fund that helps disabled people to live and work in the community is (lawful the "),
                new Difference(DifferenceAction.inserted, "live", "ion to close a fund that helps disabled people to live and work in the community is lawful the High (Cou"),
                new Difference(DifferenceAction.inserted, "work", "ose a fund that helps disabled people to live and work in the community is lawful the High Court (rules "),
                new Difference(DifferenceAction.inserted, "communiti", "hat helps disabled people to live and work in the community is lawful the High Court rules (Coalition to surv"),
                new Difference(DifferenceAction.inserted, "law", "abled people to live and work in the community is lawful the High Court rules Coalition to survive (despit"),
                new Difference(DifferenceAction.inserted, "high", "e to live and work in the community is lawful the High Court rules Coalition to survive despite (spats S"),
                new Difference(DifferenceAction.inserted, "court", "live and work in the community is lawful the High Court rules Coalition to survive despite spats (Senior "),
                new Difference(DifferenceAction.inserted, "rule", "nd work in the community is lawful the High Court rules Coalition to survive despite spats Senior (Lib De"),
                new Difference(DifferenceAction.inserted, "coalit", "k in the community is lawful the High Court rules Coalition to survive despite spats Senior Lib (Dem Danny Al"),
                new Difference(DifferenceAction.inserted, "surviv", "unity is lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny (Alexander ins"),
                new Difference(DifferenceAction.inserted, "despit", " lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander (insists tha"),
                new Difference(DifferenceAction.inserted, "spat", "the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that (trad"),
                new Difference(DifferenceAction.inserted, "senior", "gh Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that (trading ins"),
                new Difference(DifferenceAction.inserted, "lib", "t rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading (insults"),
                new Difference(DifferenceAction.inserted, "dem", "les Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults (wit"),
                new Difference(DifferenceAction.inserted, "danni", "Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults (with his "),
                new Difference(DifferenceAction.inserted, "alexand", "ion to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults with (his Conservati"),
                new Difference(DifferenceAction.inserted, "insist", "vive despite spats Senior Lib Dem Danny Alexander insists that trading insults with his (Conservatives part"),
                new Difference(DifferenceAction.inserted, "trade", "spats Senior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners (does not"),
                new Difference(DifferenceAction.inserted, "insult", "nior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners does (not undermi"),
                new Difference(DifferenceAction.inserted, "conserv", "y Alexander insists that trading insults with his Conservatives partners does not undermine the (parties ability "),
                new Difference(DifferenceAction.inserted, "partner", "sists that trading insults with his Conservatives partners does not undermine the parties (ability to work t"),
                new Difference(DifferenceAction.inserted, "undermin", " insults with his Conservatives partners does not undermine the parties ability to work (together Johnson cri"),
                new Difference(DifferenceAction.inserted, "parti", "his Conservatives partners does not undermine the parties ability to work together Johnson (criticises Fara"),
                new Difference(DifferenceAction.inserted, "abi", ""),
                new Difference(DifferenceAction.deleted, "m", ""),
                new Difference(DifferenceAction.inserted, "w", ""),
                new Difference(DifferenceAction.deleted, "tgag", ""),
                new Difference(DifferenceAction.deleted, "cope", ""),
                new Difference(DifferenceAction.deleted, "rise", ""),
                new Difference(DifferenceAction.deleted, "interest", ""),
                new Difference(DifferenceAction.deleted, "rate", ""),
                new Difference(DifferenceAction.deleted, "bank", ""),
                new Difference(DifferenceAction.deleted, "england", ""),
                new Difference(DifferenceAction.deleted, "said", ""),
                new Difference(DifferenceAction.inserted, "k", ""),
                new Difference(DifferenceAction.inserted, "togeth", ""),
                new Difference(DifferenceAction.inserted, "johnson", ""),
                new Difference(DifferenceAction.inserted, "criticis", ""),
                new Difference(DifferenceAction.inserted, "farag", ""),
                new Difference(DifferenceAction.inserted, "m4", ""),
                new Difference(DifferenceAction.inserted, "excus", ""),
                new Difference(DifferenceAction.inserted, "home", ""),
                new Difference(DifferenceAction.inserted, "owner", ""),
                new Difference(DifferenceAction.inserted, "handl", ""),
                new Difference(DifferenceAction.inserted, "rate", ""),
                new Difference(DifferenceAction.inserted, "rise", ""),
                new Difference(DifferenceAction.inserted, "playstat", ""),
                new Difference(DifferenceAction.inserted, "hit", ""),
                new Difference(DifferenceAction.inserted, "hack", ""),
                new Difference(DifferenceAction.inserted, "attack", ""),
                new Difference(DifferenceAction.deleted, "take", ""),
                new Difference(DifferenceAction.deleted, "care", ""),
                new Difference(DifferenceAction.deleted, "complaint", ""),
                new Difference(DifferenceAction.deleted, "serious", ""),
                new Difference(DifferenceAction.deleted, "defenc", ""),
                new Difference(DifferenceAction.deleted, "staff", ""),
                new Difference(DifferenceAction.deleted, "begin", ""),
                new Difference(DifferenceAction.deleted, "10", ""),
                new Difference(DifferenceAction.deleted, "day", ""),
                new Difference(DifferenceAction.deleted, "strike", ""),
                new Difference(DifferenceAction.deleted, "uk", ""),
                new Difference(DifferenceAction.deleted, "embassi", ""),
                new Difference(DifferenceAction.deleted, "cairo", ""),
                new Difference(DifferenceAction.deleted, "remain", ""),
                new Difference(DifferenceAction.deleted, "close", ""),
                new Difference(DifferenceAction.deleted, "teenag", ""),
                new Difference(DifferenceAction.deleted, "runaway", ""),
                new Difference(DifferenceAction.deleted, "lack", ""),
                new Difference(DifferenceAction.deleted, "refug", ""),
                new Difference(DifferenceAction.deleted, "pride", ""),
                new Difference(DifferenceAction.deleted, "win", ""),
                new Difference(DifferenceAction.deleted, "best", ""),
                new Difference(DifferenceAction.deleted, "film", ""),
                new Difference(DifferenceAction.deleted, "indi", ""),
                new Difference(DifferenceAction.deleted, "award", ""),
                new Difference(DifferenceAction.inserted, "uk", ""),
                new Difference(DifferenceAction.inserted, "embassi", ""),
                new Difference(DifferenceAction.inserted, "cairo", ""),
                new Difference(DifferenceAction.inserted, "remain", ""),
                new Difference(DifferenceAction.inserted, "close", ""),
                new Difference(DifferenceAction.deleted, "standard", ""),
                new Difference(DifferenceAction.deleted, "fear", ""),
                new Difference(DifferenceAction.deleted, "school", ""),
                new Difference(DifferenceAction.deleted, "cut", ""),
                new Difference(DifferenceAction.deleted, "alcohol", ""),
                new Difference(DifferenceAction.deleted, "price", ""),
                new Difference(DifferenceAction.deleted, "law", ""),
                new Difference(DifferenceAction.deleted, "save", ""),
                new Difference(DifferenceAction.deleted, "900m", ""),
                new Difference(DifferenceAction.deleted, "scotland", ""),
                new Difference(DifferenceAction.deleted, "oralba", ""),
                new Difference(DifferenceAction.deleted, "warn", ""),
                new Difference(DifferenceAction.deleted, "snow", ""),
                new Difference(DifferenceAction.deleted, "ice", ""),
                new Difference(DifferenceAction.deleted, "affect", ""),
                new Difference(DifferenceAction.deleted, "roa", ""),
                new Difference(DifferenceAction.inserted, "eight", ""),
                new Difference(DifferenceAction.inserted, "car", ""),
                new Difference(DifferenceAction.inserted, "damag", ""),
                new Difference(DifferenceAction.inserted, "arson", ""),
                new Difference(DifferenceAction.inserted, "attack", ""),
                new Difference(DifferenceAction.inserted, "farmer", ""),
                new Difference(DifferenceAction.inserted, "deliv", ""),
                new Difference(DifferenceAction.inserted, "down", ""),
                new Difference(DifferenceAction.inserted, "street", ""),
                new Difference(DifferenceAction.inserted, "tree", ""),
                new Difference(DifferenceAction.inserted, "scotland", ""),
                new Difference(DifferenceAction.inserted, "oralba", ""),
                new Difference(DifferenceAction.inserted, "woman", ""),
                new Difference(DifferenceAction.inserted, "kill", ""),
                new Difference(DifferenceAction.inserted, "ambul", ""),
                new Difference(DifferenceAction.inserted, "acci", ""),
                new Difference(DifferenceAction.deleted, "m", ""),
                new Difference(DifferenceAction.inserted, "b", ""),
                new Difference(DifferenceAction.deleted, "found", ""),
                new Difference(DifferenceAction.deleted, "dead", ""),
                new Difference(DifferenceAction.deleted, "close", ""),
                new Difference(DifferenceAction.deleted, "flat", ""),
                new Difference(DifferenceAction.inserted, "k", ""),
                new Difference(DifferenceAction.inserted, "accus", ""),
                new Difference(DifferenceAction.inserted, "fraud", ""),
                new Difference(DifferenceAction.inserted, "mortgag", ""),
                new Difference(DifferenceAction.deleted, "boy", ""),
                new Difference(DifferenceAction.deleted, "15", ""),
                new Difference(DifferenceAction.deleted, "kill", ""),
                new Difference(DifferenceAction.deleted, "fight", ""),
                new Difference(DifferenceAction.deleted, "name", ""),
                new Difference(DifferenceAction.inserted, "live", ""),
                new Difference(DifferenceAction.inserted, "son", ""),
                new Difference(DifferenceAction.inserted, "detain", ""),
                new Difference(DifferenceAction.inserted, "tri", ""),
                new Difference(DifferenceAction.inserted, "kill", ""),
                new Difference(DifferenceAction.inserted, "dad", ""),
                new Difference(DifferenceAction.inserted, "cap", ""),
                new Difference(DifferenceAction.deleted, "game", ""),
                new Difference(DifferenceAction.deleted, "bid", ""),
                new Difference(DifferenceAction.deleted, "revamp", ""),
                new Difference(DifferenceAction.deleted, "pass", ""),
                new Difference(DifferenceAction.inserted, "sport", "ap on Olympic sports is dropped Sportsday rolling sports news Live Magazine Why China sees itself (in Lowr"),
                new Difference(DifferenceAction.inserted, "drop", ""),
                new Difference(DifferenceAction.deleted, "scare", ""),
                new Difference(DifferenceAction.deleted, "biggest", ""),
                new Difference(DifferenceAction.deleted, "pest", ""),
                new Difference(DifferenceAction.deleted, "eleph", ""),
                new Difference(DifferenceAction.deleted, "need", ""),
                new Difference(DifferenceAction.deleted, "kept", ""),
                new Difference(DifferenceAction.deleted, "away", ""),
                new Difference(DifferenceAction.deleted, "farm", ""),
                new Difference(DifferenceAction.deleted, "insid", "farms Inside a giant spider Take a unique journey inside the body of a giant tarantula Democracy (Live Hou"),
                new Difference(DifferenceAction.deleted, "giant", "spider Take a unique journey inside the body of a giant tarantula Democracy Live House of Commons (Full c"),
                new Difference(DifferenceAction.deleted, "spider", ""),
                new Difference(DifferenceAction.deleted, "take", ""),
                new Difference(DifferenceAction.deleted, "uniqu", ""),
                new Difference(DifferenceAction.deleted, "journey", ""),
                new Difference(DifferenceAction.deleted, "insid", ""),
                new Difference(DifferenceAction.deleted, "bodi", ""),
                new Difference(DifferenceAction.deleted, "giant", ""),
                new Difference(DifferenceAction.deleted, "tarantula", ""),
                new Difference(DifferenceAction.inserted, "huge", ""),
                new Difference(DifferenceAction.inserted, "crab", ""),
                new Difference(DifferenceAction.inserted, "munch", ""),
                new Difference(DifferenceAction.inserted, "coconut", ""),
                new Difference(DifferenceAction.inserted, "gigant", ""),
                new Difference(DifferenceAction.inserted, "odd", ""),
                new Difference(DifferenceAction.inserted, "may", ""),
                new Difference(DifferenceAction.inserted, "endang", ""),
                new Difference(DifferenceAction.inserted, "scare", ""),
                new Difference(DifferenceAction.inserted, "biggest", ""),
                new Difference(DifferenceAction.inserted, "pest", ""),
                new Difference(DifferenceAction.inserted, "eleph", ""),
                new Difference(DifferenceAction.inserted, "need", ""),
                new Difference(DifferenceAction.inserted, "kept", ""),
                new Difference(DifferenceAction.inserted, "away", ""),
                new Difference(DifferenceAction.inserted, "farm", ""),
                new Difference(DifferenceAction.deleted, "hous", ""),
                new Difference(DifferenceAction.deleted, "common", "ocracy Live House of Commons Full coverage in the Commons as MPs debate private members bills Find (a repre"),
                new Difference(DifferenceAction.inserted, "crackdown", ""),
                new Difference(DifferenceAction.inserted, "uk", ""),
                new Difference(DifferenceAction.inserted, "s", ""),
                new Difference(DifferenceAction.inserted, "billion", ""),
                new Difference(DifferenceAction.inserted, "nuisanc", ""),
                new Difference(DifferenceAction.inserted, "call", "wn on UK s billion nuisance calls Watch01 20 Cold calls break law says report Listen Barclays (introduce "),
                new Difference(DifferenceAction.inserted, "watch01", ""),
                new Difference(DifferenceAction.inserted, "20", ""),
                new Difference(DifferenceAction.deleted, "traffick", ""),
                new Difference(DifferenceAction.deleted, "slaveri", ""),
                new Difference(DifferenceAction.deleted, "uk", ""),
                new Difference(DifferenceAction.deleted, "watch01", ""),
                new Difference(DifferenceAction.deleted, "45", ""),
                new Difference(DifferenceAction.deleted, "radio", ""),
                new Difference(DifferenceAction.deleted, "5", ""),
                new Difference(DifferenceAction.deleted, "live", ""),
                new Difference(DifferenceAction.deleted, "live", ""),
                new Difference(DifferenceAction.deleted, "featur", ""),
                new Difference(DifferenceAction.deleted, "analysi", ""),
                new Difference(DifferenceAction.deleted, "dig", ""),
                new Difference(DifferenceAction.deleted, "danger", ""),
                new Difference(DifferenceAction.deleted, "man", ""),
                new Difference(DifferenceAction.deleted, "found", ""),
                new Difference(DifferenceAction.deleted, "100", ""),
                new Difference(DifferenceAction.deleted, "bomb", ""),
                new Difference(DifferenceAction.deleted, "afghanistan", ""),
                new Difference(DifferenceAction.inserted, "featur", ""),
                new Difference(DifferenceAction.inserted, "analysi", ""),
                new Difference(DifferenceAction.inserted, "throe", ""),
                new Difference(DifferenceAction.inserted, "chang", ""),
                new Difference(DifferenceAction.inserted, "someth", ""),
                new Difference(DifferenceAction.inserted, "stir", ""),
                new Difference(DifferenceAction.inserted, "high", ""),
                new Difference(DifferenceAction.inserted, "street", ""),
                new Difference(DifferenceAction.inserted, "dig", ""),
                new Difference(DifferenceAction.inserted, "danger", ""),
                new Difference(DifferenceAction.inserted, "man", ""),
                new Difference(DifferenceAction.inserted, "found", ""),
                new Difference(DifferenceAction.inserted, "100", ""),
                new Difference(DifferenceAction.inserted, "bomb", ""),
                new Difference(DifferenceAction.inserted, "afghanistan", ""),
                new Difference(DifferenceAction.inserted, "young", ""),
                new Difference(DifferenceAction.inserted, "hungri", ""),
                new Difference(DifferenceAction.inserted, "peopl", ""),
                new Difference(DifferenceAction.inserted, "struggl", ""),
                new Difference(DifferenceAction.inserted, "put", ""),
                new Difference(DifferenceAction.inserted, "food", ""),
                new Difference(DifferenceAction.inserted, "tabl", ""),
                new Difference(DifferenceAction.inserted, "interest", ""),
                new Difference(DifferenceAction.inserted, "rate", ""),
                new Difference(DifferenceAction.inserted, "rise", ""),
                new Difference(DifferenceAction.inserted, "might", ""),
                new Difference(DifferenceAction.inserted, "wors", ""),
                new Difference(DifferenceAction.inserted, "famili", ""),
                new Difference(DifferenceAction.inserted, "economi", ""),
                new Difference(DifferenceAction.deleted, "72", ""),
                new Difference(DifferenceAction.deleted, "christma", ""),
                new Difference(DifferenceAction.deleted, "tree", ""),
                new Difference(DifferenceAction.deleted, "52", ""),
                new Difference(DifferenceAction.deleted, "room", ""),
                new Difference(DifferenceAction.deleted, "hous", ""),
                new Difference(DifferenceAction.deleted, "decor", ""),
                new Difference(DifferenceAction.inserted, "inbox", ""),
                new Difference(DifferenceAction.inserted, "fatigu", ""),
                new Difference(DifferenceAction.inserted, "take", ""),
                new Difference(DifferenceAction.inserted, "back", ""),
                new Difference(DifferenceAction.inserted, "control", ""),
                new Difference(DifferenceAction.inserted, "email", ""),
                new Difference(DifferenceAction.deleted, "magic", ""),
                new Difference(DifferenceAction.deleted, "moment", ""),
                new Difference(DifferenceAction.deleted, "make", ""),
                new Difference(DifferenceAction.deleted, "live", ""),
                new Difference(DifferenceAction.deleted, "busi", ""),
                new Difference(DifferenceAction.deleted, "show", "")
        );

        DifferenceMatcher matcher = new DifferenceMatcher(keywords, diffList, false, false);
        Set<DifferenceMatcher.Result> matchSet = matcher.call();
        assertEquals(11, matchSet.size());
    }
}

