package argus.diff;

import argus.job.Job;
import argus.keyword.Keyword;
import argus.keyword.KeywordBuilder;
import argus.parser.GeniaParser;
import argus.parser.ParserPool;
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
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DiffMatcherTest {

    private static final Logger logger = LoggerFactory.getLogger(DiffMatcherTest.class);

    private static MongoClient mongoClient;
    private static DB termsDatabase;
    private static ParserPool parserPool;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        termsDatabase = mongoClient.getDB("terms_db");
        parserPool = new ParserPool();
        parserPool.place(new GeniaParser());
    }

    @Test
    public void testSimple() {
        String url = "http://www.bbc.com/news/uk/";
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

        Job job = new Job(url, keywords, 10, url);

        List<DiffDetector.Result> diffList = Lists.newArrayList(
                new DiffDetector.Result(DiffAction.inserted, "argus", "Argus Panoptes is the name of the 100 eyed giant in No"),
                new DiffDetector.Result(DiffAction.inserted, "panopt", "Argus Panoptes is the name of the 100 eyed giant in Norse mytho"),
                new DiffDetector.Result(DiffAction.inserted, "name", "Argus Panoptes is the name of the 100 eyed giant in Norse mythology"),
                new DiffDetector.Result(DiffAction.deleted, "greek", "is the of the 100 eyed giant in Greek mythology"),
                new DiffDetector.Result(DiffAction.inserted, "nors", "gus Panoptes is the name of the 100 eyed giant in Norse mythology")
        );

        DiffMatcher matcher = new DiffMatcher(job, diffList);
        Set<DiffMatcher.Result> matchSet = matcher.call();
        assertEquals(2, matchSet.size());
    }


    @Test
    public void testBBCNews() {
        String url = "http://www.bbc.com/news/uk/";

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
        Job job = new Job(url, keywords, 10, url);

        List<DiffDetector.Result> diffList = Lists.newArrayList(
                new DiffDetector.Result(DiffAction.deleted,"0", ""),
                new DiffDetector.Result(DiffAction.deleted,"52", "tics Education 8 December 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge (dismisse"),
                new DiffDetector.Result(DiffAction.inserted,"3", ""),
                new DiffDetector.Result(DiffAction.inserted,"07", "tics Education 8 December 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The (famil"),
                new DiffDetector.Result(DiffAction.deleted,"murder", " 8 December 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case (against Shr"),
                new DiffDetector.Result(DiffAction.deleted,"case", "mber 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against (Shrien D"),
                new DiffDetector.Result(DiffAction.deleted,"thrown", "2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien (Dewani a"),
                new DiffDetector.Result(DiffAction.deleted,"judg", "pdated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused (of "),
                new DiffDetector.Result(DiffAction.deleted,"dismiss", " at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused of (arranging "),
                new DiffDetector.Result(DiffAction.deleted,"case", "GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused of arranging (murde"),
                new DiffDetector.Result(DiffAction.deleted,"shrien", "rder case thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of (wife Anni "),
                new DiffDetector.Result(DiffAction.deleted,"dewani", "se thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni (in Sout"),
                new DiffDetector.Result(DiffAction.deleted,"accus", "wn out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South (Africa"),
                new DiffDetector.Result(DiffAction.deleted,"arrang", "e dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South Africa (Shrien Dewan"),
                new DiffDetector.Result(DiffAction.deleted,"murder", "s case against Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien (Dewani trial"),
                new DiffDetector.Result(DiffAction.deleted,"wife", "inst Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial (Reactio"),
                new DiffDetector.Result(DiffAction.deleted,"anni", "Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction (Pay"),
                new DiffDetector.Result(DiffAction.deleted,"south", "ewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay (benefits"),
                new DiffDetector.Result(DiffAction.deleted,"africa", "accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits (faster"),
                new DiffDetector.Result(DiffAction.deleted,"shrien", " of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster (to cut"),
                new DiffDetector.Result(DiffAction.deleted,"dewani", "anging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut (hunger"),
                new DiffDetector.Result(DiffAction.deleted,"trial", "murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut hunger (An in"),
                new DiffDetector.Result(DiffAction.deleted,"reaction", " of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut hunger An (income sque"),
                new DiffDetector.Result(DiffAction.inserted,"clear", " 8 December 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni (Dewani be"),
                new DiffDetector.Result(DiffAction.inserted,"honeymoon", " 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani (believe they ha"),
                new DiffDetector.Result(DiffAction.inserted,"murder", " updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani believe they (have been"),
                new DiffDetector.Result(DiffAction.inserted,"famili", " 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani believe they have been (failed by "),
                new DiffDetector.Result(DiffAction.inserted,"anni", " Dewani cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the (just"),
                new DiffDetector.Result(DiffAction.inserted,"dewani", "ni cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the (justice sys"),
                new DiffDetector.Result(DiffAction.inserted,"believ", "red of honeymoon murder The family of Anni Dewani believe they have been failed by the justice (system afte"),
                new DiffDetector.Result(DiffAction.inserted,"fail", " The family of Anni Dewani believe they have been failed by the justice system after millionaire (business"),
                new DiffDetector.Result(DiffAction.inserted,"justic", " Anni Dewani believe they have been failed by the justice system after millionaire businessman (Shrien Dewa"),
                new DiffDetector.Result(DiffAction.inserted,"system", "wani believe they have been failed by the justice system after millionaire businessman Shrien (Dewani is c"),
                new DiffDetector.Result(DiffAction.inserted,"millionair", "they have been failed by the justice system after millionaire businessman Shrien Dewani is (cleared of the hone"),
                new DiffDetector.Result(DiffAction.inserted,"businessman", "en failed by the justice system after millionaire businessman Shrien Dewani is cleared of (the honeymoon murder"),
                new DiffDetector.Result(DiffAction.inserted,"shrien", " the justice system after millionaire businessman Shrien Dewani is cleared of the honeymoon (murder Shrien"),
                new DiffDetector.Result(DiffAction.inserted,"dewani", "stice system after millionaire businessman Shrien Dewani is cleared of the honeymoon murder (Shrien Dewani"),
                new DiffDetector.Result(DiffAction.inserted,"clear", "em after millionaire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani (trial Reac"),
                new DiffDetector.Result(DiffAction.inserted,"honeymoon", "naire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial (Reaction Live How Dew"),
                new DiffDetector.Result(DiffAction.inserted,"murder", "nessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How (Dewani pro"),
                new DiffDetector.Result(DiffAction.inserted,"shrien", " Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani (prosecutio"),
                new DiffDetector.Result(DiffAction.inserted,"dewani", " Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani (prosecution fell "),
                new DiffDetector.Result(DiffAction.inserted,"trial", " is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell (apart "),
                new DiffDetector.Result(DiffAction.inserted,"reaction", "eared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell (apart From drea"),
                new DiffDetector.Result(DiffAction.inserted,"live", "the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream (wed"),
                new DiffDetector.Result(DiffAction.inserted,"dewani", "moon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream (wedding to fat"),
                new DiffDetector.Result(DiffAction.inserted,"prosecut", "rder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to (fatal hijacking"),
                new DiffDetector.Result(DiffAction.inserted,"fell", "Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking (Hiri"),
                new DiffDetector.Result(DiffAction.inserted,"apart", "i trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking (Hiring a h"),
                new DiffDetector.Result(DiffAction.inserted,"dream", "ction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman (in So"),
                new DiffDetector.Result(DiffAction.inserted,"wed", "Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in (South Afri"),
                new DiffDetector.Result(DiffAction.inserted,"fatal", "wani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa (Dewani"),
                new DiffDetector.Result(DiffAction.inserted,"hijack", "rosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa (Dewani family Ju"),
                new DiffDetector.Result(DiffAction.inserted,"hire", " fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family (Justice s"),
                new DiffDetector.Result(DiffAction.inserted,"hitman", "rt From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice (system fai"),
                new DiffDetector.Result(DiffAction.inserted,"south", "eam wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed (Watch"),
                new DiffDetector.Result(DiffAction.inserted,"africa", "dding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed (Watch The ba"),
                new DiffDetector.Result(DiffAction.inserted,"dewani", "o fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The (backgroun"),
                new DiffDetector.Result(DiffAction.inserted,"famili", " hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The (background to th"),
                new DiffDetector.Result(DiffAction.inserted,"justic", "ing Hiring a hitman in South Africa Dewani family Justice system failed Watch The background to (the Dewani"),
                new DiffDetector.Result(DiffAction.inserted,"system", "ng a hitman in South Africa Dewani family Justice system failed Watch The background to the (Dewani case W"),
                new DiffDetector.Result(DiffAction.inserted,"fail", "tman in South Africa Dewani family Justice system failed Watch The background to the Dewani case (Watch Pa"),
                new DiffDetector.Result(DiffAction.inserted,"watch", " South Africa Dewani family Justice system failed Watch The background to the Dewani case Watch (Pay bene"),
                new DiffDetector.Result(DiffAction.inserted,"background", "ica Dewani family Justice system failed Watch The background to the Dewani case Watch Pay (benefits faster to "),
                new DiffDetector.Result(DiffAction.inserted,"dewani", "Justice system failed Watch The background to the Dewani case Watch Pay benefits faster to cut (hunger An "),
                new DiffDetector.Result(DiffAction.inserted,"case", " system failed Watch The background to the Dewani case Watch Pay benefits faster to cut hunger An (incom"),
                new DiffDetector.Result(DiffAction.inserted,"watch", "em failed Watch The background to the Dewani case Watch Pay benefits faster to cut hunger An (income sque"),
                new DiffDetector.Result(DiffAction.deleted,"fresh", " Sydney Australia while on an outing with friends Fresh cracks appear in coalition Senior (Conservative a"),
                new DiffDetector.Result(DiffAction.deleted,"crack", "y Australia while on an outing with friends Fresh cracks appear in coalition Senior Conservative (and Lib "),
                new DiffDetector.Result(DiffAction.deleted,"appear", "alia while on an outing with friends Fresh cracks appear in coalition Senior Conservative and Lib (Dem min"),
                new DiffDetector.Result(DiffAction.deleted,"coalit", " on an outing with friends Fresh cracks appear in coalition Senior Conservative and Lib Dem (ministers critic"),
                new DiffDetector.Result(DiffAction.deleted,"senior", "ing with friends Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers (criticise eac"),
                new DiffDetector.Result(DiffAction.deleted,"conserv", "h friends Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers criticise (each other amid "),
                new DiffDetector.Result(DiffAction.deleted,"lib", "racks appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid (suggesti"),
                new DiffDetector.Result(DiffAction.deleted,"dem", "s appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions ("),
                new DiffDetector.Result(DiffAction.deleted,"minist", "pear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions (of widenin"),
                new DiffDetector.Result(DiffAction.deleted,"criticis", "alition Senior Conservative and Lib Dem ministers criticise each other amid suggestions of (widening division"),
                new DiffDetector.Result(DiffAction.deleted,"amid", "vative and Lib Dem ministers criticise each other amid suggestions of widening divisions in the (coaliti"),
                new DiffDetector.Result(DiffAction.deleted,"suggest", "e and Lib Dem ministers criticise each other amid suggestions of widening divisions in the (coalition governmen"),
                new DiffDetector.Result(DiffAction.deleted,"widen", "inisters criticise each other amid suggestions of widening divisions in the coalition government (Home owner"),
                new DiffDetector.Result(DiffAction.deleted,"divis", "criticise each other amid suggestions of widening divisions in the coalition government Home owners (could ha"),
                new DiffDetector.Result(DiffAction.deleted,"coalit", "her amid suggestions of widening divisions in the coalition government Home owners could handle (rate rise Th"),
                new DiffDetector.Result(DiffAction.deleted,"govern", "uggestions of widening divisions in the coalition government Home owners could handle rate rise (The majority "),
                new DiffDetector.Result(DiffAction.deleted,"home", "of widening divisions in the coalition government Home owners could handle rate rise The majority of (pe"),
                new DiffDetector.Result(DiffAction.deleted,"owner", "dening divisions in the coalition government Home owners could handle rate rise The majority of (people wi"),
                new DiffDetector.Result(DiffAction.deleted,"handl", "ons in the coalition government Home owners could handle rate rise The majority of people with (mortgages "),
                new DiffDetector.Result(DiffAction.deleted,"rate", "the coalition government Home owners could handle rate rise The majority of people with mortgages (could"),
                new DiffDetector.Result(DiffAction.deleted,"rise", "oalition government Home owners could handle rate rise The majority of people with mortgages could (cope"),
                new DiffDetector.Result(DiffAction.deleted,"major", "government Home owners could handle rate rise The majority of people with mortgages could cope with (a rise "),
                new DiffDetector.Result(DiffAction.deleted,"peop", ""),
                new DiffDetector.Result(DiffAction.inserted,"disabl", " Sydney Australia while on an outing with friends Disability fund closure ruled lawful A (government decision "),
                new DiffDetector.Result(DiffAction.inserted,"fund", "tralia while on an outing with friends Disability fund closure ruled lawful A government decision (to cl"),
                new DiffDetector.Result(DiffAction.inserted,"closur", "a while on an outing with friends Disability fund closure ruled lawful A government decision to (close a fu"),
                new DiffDetector.Result(DiffAction.inserted,"rule", "on an outing with friends Disability fund closure ruled lawful A government decision to close a (fund tha"),
                new DiffDetector.Result(DiffAction.inserted,"law", "outing with friends Disability fund closure ruled lawful A government decision to close a fund that (helps"),
                new DiffDetector.Result(DiffAction.inserted,"govern", "th friends Disability fund closure ruled lawful A government decision to close a fund that helps (disabled peo"),
                new DiffDetector.Result(DiffAction.inserted,"decis", "Disability fund closure ruled lawful A government decision to close a fund that helps disabled (people to li"),
                new DiffDetector.Result(DiffAction.inserted,"close", "und closure ruled lawful A government decision to close a fund that helps disabled people to live (and wo"),
                new DiffDetector.Result(DiffAction.inserted,"fund", "ure ruled lawful A government decision to close a fund that helps disabled people to live and work (in t"),
                new DiffDetector.Result(DiffAction.inserted,"help", "lawful A government decision to close a fund that helps disabled people to live and work in the (communit"),
                new DiffDetector.Result(DiffAction.inserted,"disabl", " A government decision to close a fund that helps disabled people to live and work in the (community is lawf"),
                new DiffDetector.Result(DiffAction.inserted,"peopl", "ment decision to close a fund that helps disabled people to live and work in the community is (lawful the "),
                new DiffDetector.Result(DiffAction.inserted,"live", "ion to close a fund that helps disabled people to live and work in the community is lawful the High (Cou"),
                new DiffDetector.Result(DiffAction.inserted,"work", "ose a fund that helps disabled people to live and work in the community is lawful the High Court (rules "),
                new DiffDetector.Result(DiffAction.inserted,"communiti", "hat helps disabled people to live and work in the community is lawful the High Court rules (Coalition to surv"),
                new DiffDetector.Result(DiffAction.inserted,"law", "abled people to live and work in the community is lawful the High Court rules Coalition to survive (despit"),
                new DiffDetector.Result(DiffAction.inserted,"high", "e to live and work in the community is lawful the High Court rules Coalition to survive despite (spats S"),
                new DiffDetector.Result(DiffAction.inserted,"court", "live and work in the community is lawful the High Court rules Coalition to survive despite spats (Senior "),
                new DiffDetector.Result(DiffAction.inserted,"rule", "nd work in the community is lawful the High Court rules Coalition to survive despite spats Senior (Lib De"),
                new DiffDetector.Result(DiffAction.inserted,"coalit", "k in the community is lawful the High Court rules Coalition to survive despite spats Senior Lib (Dem Danny Al"),
                new DiffDetector.Result(DiffAction.inserted,"surviv", "unity is lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny (Alexander ins"),
                new DiffDetector.Result(DiffAction.inserted,"despit", " lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander (insists tha"),
                new DiffDetector.Result(DiffAction.inserted,"spat", "the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that (trad"),
                new DiffDetector.Result(DiffAction.inserted,"senior", "gh Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that (trading ins"),
                new DiffDetector.Result(DiffAction.inserted,"lib", "t rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading (insults"),
                new DiffDetector.Result(DiffAction.inserted,"dem", "les Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults (wit"),
                new DiffDetector.Result(DiffAction.inserted,"danni", "Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults (with his "),
                new DiffDetector.Result(DiffAction.inserted,"alexand", "ion to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults with (his Conservati"),
                new DiffDetector.Result(DiffAction.inserted,"insist", "vive despite spats Senior Lib Dem Danny Alexander insists that trading insults with his (Conservatives part"),
                new DiffDetector.Result(DiffAction.inserted,"trade", "spats Senior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners (does not"),
                new DiffDetector.Result(DiffAction.inserted,"insult", "nior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners does (not undermi"),
                new DiffDetector.Result(DiffAction.inserted,"conserv", "y Alexander insists that trading insults with his Conservatives partners does not undermine the (parties ability "),
                new DiffDetector.Result(DiffAction.inserted,"partner", "sists that trading insults with his Conservatives partners does not undermine the parties (ability to work t"),
                new DiffDetector.Result(DiffAction.inserted,"undermin", " insults with his Conservatives partners does not undermine the parties ability to work (together Johnson cri"),
                new DiffDetector.Result(DiffAction.inserted,"parti", "his Conservatives partners does not undermine the parties ability to work together Johnson (criticises Fara"),
                new DiffDetector.Result(DiffAction.inserted,"abi", ""),
                new DiffDetector.Result(DiffAction.deleted,"m", ""),
                new DiffDetector.Result(DiffAction.inserted,"w", ""),
                new DiffDetector.Result(DiffAction.deleted,"tgag", ""),
                new DiffDetector.Result(DiffAction.deleted,"cope", ""),
                new DiffDetector.Result(DiffAction.deleted,"rise", ""),
                new DiffDetector.Result(DiffAction.deleted,"interest", ""),
                new DiffDetector.Result(DiffAction.deleted,"rate", ""),
                new DiffDetector.Result(DiffAction.deleted,"bank", ""),
                new DiffDetector.Result(DiffAction.deleted,"england", ""),
                new DiffDetector.Result(DiffAction.deleted,"said", ""),
                new DiffDetector.Result(DiffAction.inserted,"k", ""),
                new DiffDetector.Result(DiffAction.inserted,"togeth", ""),
                new DiffDetector.Result(DiffAction.inserted,"johnson", ""),
                new DiffDetector.Result(DiffAction.inserted,"criticis", ""),
                new DiffDetector.Result(DiffAction.inserted,"farag", ""),
                new DiffDetector.Result(DiffAction.inserted,"m4", ""),
                new DiffDetector.Result(DiffAction.inserted,"excus", ""),
                new DiffDetector.Result(DiffAction.inserted,"home", ""),
                new DiffDetector.Result(DiffAction.inserted,"owner", ""),
                new DiffDetector.Result(DiffAction.inserted,"handl", ""),
                new DiffDetector.Result(DiffAction.inserted,"rate", ""),
                new DiffDetector.Result(DiffAction.inserted,"rise", ""),
                new DiffDetector.Result(DiffAction.inserted,"playstat", ""),
                new DiffDetector.Result(DiffAction.inserted,"hit", ""),
                new DiffDetector.Result(DiffAction.inserted,"hack", ""),
                new DiffDetector.Result(DiffAction.inserted,"attack", ""),
                new DiffDetector.Result(DiffAction.deleted,"take", ""),
                new DiffDetector.Result(DiffAction.deleted,"care", ""),
                new DiffDetector.Result(DiffAction.deleted,"complaint", ""),
                new DiffDetector.Result(DiffAction.deleted,"serious", ""),
                new DiffDetector.Result(DiffAction.deleted,"defenc", ""),
                new DiffDetector.Result(DiffAction.deleted,"staff", ""),
                new DiffDetector.Result(DiffAction.deleted,"begin", ""),
                new DiffDetector.Result(DiffAction.deleted,"10", ""),
                new DiffDetector.Result(DiffAction.deleted,"day", ""),
                new DiffDetector.Result(DiffAction.deleted,"strike", ""),
                new DiffDetector.Result(DiffAction.deleted,"uk", ""),
                new DiffDetector.Result(DiffAction.deleted,"embassi", ""),
                new DiffDetector.Result(DiffAction.deleted,"cairo", ""),
                new DiffDetector.Result(DiffAction.deleted,"remain", ""),
                new DiffDetector.Result(DiffAction.deleted,"close", ""),
                new DiffDetector.Result(DiffAction.deleted,"teenag", ""),
                new DiffDetector.Result(DiffAction.deleted,"runaway", ""),
                new DiffDetector.Result(DiffAction.deleted,"lack", ""),
                new DiffDetector.Result(DiffAction.deleted,"refug", ""),
                new DiffDetector.Result(DiffAction.deleted,"pride", ""),
                new DiffDetector.Result(DiffAction.deleted,"win", ""),
                new DiffDetector.Result(DiffAction.deleted,"best", ""),
                new DiffDetector.Result(DiffAction.deleted,"film", ""),
                new DiffDetector.Result(DiffAction.deleted,"indi", ""),
                new DiffDetector.Result(DiffAction.deleted,"award", ""),
                new DiffDetector.Result(DiffAction.inserted,"uk", ""),
                new DiffDetector.Result(DiffAction.inserted,"embassi", ""),
                new DiffDetector.Result(DiffAction.inserted,"cairo", ""),
                new DiffDetector.Result(DiffAction.inserted,"remain", ""),
                new DiffDetector.Result(DiffAction.inserted,"close", ""),
                new DiffDetector.Result(DiffAction.deleted,"standard", ""),
                new DiffDetector.Result(DiffAction.deleted,"fear", ""),
                new DiffDetector.Result(DiffAction.deleted,"school", ""),
                new DiffDetector.Result(DiffAction.deleted,"cut", ""),
                new DiffDetector.Result(DiffAction.deleted,"alcohol", ""),
                new DiffDetector.Result(DiffAction.deleted,"price", ""),
                new DiffDetector.Result(DiffAction.deleted,"law", ""),
                new DiffDetector.Result(DiffAction.deleted,"save", ""),
                new DiffDetector.Result(DiffAction.deleted,"900m", ""),
                new DiffDetector.Result(DiffAction.deleted,"scotland", ""),
                new DiffDetector.Result(DiffAction.deleted,"oralba", ""),
                new DiffDetector.Result(DiffAction.deleted,"warn", ""),
                new DiffDetector.Result(DiffAction.deleted,"snow", ""),
                new DiffDetector.Result(DiffAction.deleted,"ice", ""),
                new DiffDetector.Result(DiffAction.deleted,"affect", ""),
                new DiffDetector.Result(DiffAction.deleted,"roa", ""),
                new DiffDetector.Result(DiffAction.inserted,"eight", ""),
                new DiffDetector.Result(DiffAction.inserted,"car", ""),
                new DiffDetector.Result(DiffAction.inserted,"damag", ""),
                new DiffDetector.Result(DiffAction.inserted,"arson", ""),
                new DiffDetector.Result(DiffAction.inserted,"attack", ""),
                new DiffDetector.Result(DiffAction.inserted,"farmer", ""),
                new DiffDetector.Result(DiffAction.inserted,"deliv", ""),
                new DiffDetector.Result(DiffAction.inserted,"down", ""),
                new DiffDetector.Result(DiffAction.inserted,"street", ""),
                new DiffDetector.Result(DiffAction.inserted,"tree", ""),
                new DiffDetector.Result(DiffAction.inserted,"scotland", ""),
                new DiffDetector.Result(DiffAction.inserted,"oralba", ""),
                new DiffDetector.Result(DiffAction.inserted,"woman", ""),
                new DiffDetector.Result(DiffAction.inserted,"kill", ""),
                new DiffDetector.Result(DiffAction.inserted,"ambul", ""),
                new DiffDetector.Result(DiffAction.inserted,"acci", ""),
                new DiffDetector.Result(DiffAction.deleted,"m", ""),
                new DiffDetector.Result(DiffAction.inserted,"b", ""),
                new DiffDetector.Result(DiffAction.deleted,"found", ""),
                new DiffDetector.Result(DiffAction.deleted,"dead", ""),
                new DiffDetector.Result(DiffAction.deleted,"close", ""),
                new DiffDetector.Result(DiffAction.deleted,"flat", ""),
                new DiffDetector.Result(DiffAction.inserted,"k", ""),
                new DiffDetector.Result(DiffAction.inserted,"accus", ""),
                new DiffDetector.Result(DiffAction.inserted,"fraud", ""),
                new DiffDetector.Result(DiffAction.inserted,"mortgag", ""),
                new DiffDetector.Result(DiffAction.deleted,"boy", ""),
                new DiffDetector.Result(DiffAction.deleted,"15", ""),
                new DiffDetector.Result(DiffAction.deleted,"kill", ""),
                new DiffDetector.Result(DiffAction.deleted,"fight", ""),
                new DiffDetector.Result(DiffAction.deleted,"name", ""),
                new DiffDetector.Result(DiffAction.inserted,"live", ""),
                new DiffDetector.Result(DiffAction.inserted,"son", ""),
                new DiffDetector.Result(DiffAction.inserted,"detain", ""),
                new DiffDetector.Result(DiffAction.inserted,"tri", ""),
                new DiffDetector.Result(DiffAction.inserted,"kill", ""),
                new DiffDetector.Result(DiffAction.inserted,"dad", ""),
                new DiffDetector.Result(DiffAction.inserted,"cap", ""),
                new DiffDetector.Result(DiffAction.deleted,"game", ""),
                new DiffDetector.Result(DiffAction.deleted,"bid", ""),
                new DiffDetector.Result(DiffAction.deleted,"revamp", ""),
                new DiffDetector.Result(DiffAction.deleted,"pass", ""),
                new DiffDetector.Result(DiffAction.inserted,"sport", "ap on Olympic sports is dropped Sportsday rolling sports news Live Magazine Why China sees itself (in Lowr"),
                new DiffDetector.Result(DiffAction.inserted,"drop", ""),
                new DiffDetector.Result(DiffAction.deleted,"scare", ""),
                new DiffDetector.Result(DiffAction.deleted,"biggest", ""),
                new DiffDetector.Result(DiffAction.deleted,"pest", ""),
                new DiffDetector.Result(DiffAction.deleted,"eleph", ""),
                new DiffDetector.Result(DiffAction.deleted,"need", ""),
                new DiffDetector.Result(DiffAction.deleted,"kept", ""),
                new DiffDetector.Result(DiffAction.deleted,"away", ""),
                new DiffDetector.Result(DiffAction.deleted,"farm", ""),
                new DiffDetector.Result(DiffAction.deleted,"insid", "farms Inside a giant spider Take a unique journey inside the body of a giant tarantula Democracy (Live Hou"),
                new DiffDetector.Result(DiffAction.deleted,"giant", "spider Take a unique journey inside the body of a giant tarantula Democracy Live House of Commons (Full c"),
                new DiffDetector.Result(DiffAction.deleted,"spider", ""),
                new DiffDetector.Result(DiffAction.deleted,"take", ""),
                new DiffDetector.Result(DiffAction.deleted,"uniqu", ""),
                new DiffDetector.Result(DiffAction.deleted,"journey", ""),
                new DiffDetector.Result(DiffAction.deleted,"insid", ""),
                new DiffDetector.Result(DiffAction.deleted,"bodi", ""),
                new DiffDetector.Result(DiffAction.deleted,"giant", ""),
                new DiffDetector.Result(DiffAction.deleted,"tarantula", ""),
                new DiffDetector.Result(DiffAction.inserted,"huge", ""),
                new DiffDetector.Result(DiffAction.inserted,"crab", ""),
                new DiffDetector.Result(DiffAction.inserted,"munch", ""),
                new DiffDetector.Result(DiffAction.inserted,"coconut", ""),
                new DiffDetector.Result(DiffAction.inserted,"gigant", ""),
                new DiffDetector.Result(DiffAction.inserted,"odd", ""),
                new DiffDetector.Result(DiffAction.inserted,"may", ""),
                new DiffDetector.Result(DiffAction.inserted,"endang", ""),
                new DiffDetector.Result(DiffAction.inserted,"scare", ""),
                new DiffDetector.Result(DiffAction.inserted,"biggest", ""),
                new DiffDetector.Result(DiffAction.inserted,"pest", ""),
                new DiffDetector.Result(DiffAction.inserted,"eleph", ""),
                new DiffDetector.Result(DiffAction.inserted,"need", ""),
                new DiffDetector.Result(DiffAction.inserted,"kept", ""),
                new DiffDetector.Result(DiffAction.inserted,"away", ""),
                new DiffDetector.Result(DiffAction.inserted,"farm", ""),
                new DiffDetector.Result(DiffAction.deleted,"hous", ""),
                new DiffDetector.Result(DiffAction.deleted,"common", "ocracy Live House of Commons Full coverage in the Commons as MPs debate private members bills Find (a repre"),
                new DiffDetector.Result(DiffAction.inserted,"crackdown", ""),
                new DiffDetector.Result(DiffAction.inserted,"uk", ""),
                new DiffDetector.Result(DiffAction.inserted,"s", ""),
                new DiffDetector.Result(DiffAction.inserted,"billion", ""),
                new DiffDetector.Result(DiffAction.inserted,"nuisanc", ""),
                new DiffDetector.Result(DiffAction.inserted,"call", "wn on UK s billion nuisance calls Watch01 20 Cold calls break law says report Listen Barclays (introduce "),
                new DiffDetector.Result(DiffAction.inserted,"watch01", ""),
                new DiffDetector.Result(DiffAction.inserted,"20", ""),
                new DiffDetector.Result(DiffAction.deleted,"traffick", ""),
                new DiffDetector.Result(DiffAction.deleted,"slaveri", ""),
                new DiffDetector.Result(DiffAction.deleted,"uk", ""),
                new DiffDetector.Result(DiffAction.deleted,"watch01", ""),
                new DiffDetector.Result(DiffAction.deleted,"45", ""),
                new DiffDetector.Result(DiffAction.deleted,"radio", ""),
                new DiffDetector.Result(DiffAction.deleted,"5", ""),
                new DiffDetector.Result(DiffAction.deleted,"live", ""),
                new DiffDetector.Result(DiffAction.deleted,"live", ""),
                new DiffDetector.Result(DiffAction.deleted,"featur", ""),
                new DiffDetector.Result(DiffAction.deleted,"analysi", ""),
                new DiffDetector.Result(DiffAction.deleted,"dig", ""),
                new DiffDetector.Result(DiffAction.deleted,"danger", ""),
                new DiffDetector.Result(DiffAction.deleted,"man", ""),
                new DiffDetector.Result(DiffAction.deleted,"found", ""),
                new DiffDetector.Result(DiffAction.deleted,"100", ""),
                new DiffDetector.Result(DiffAction.deleted,"bomb", ""),
                new DiffDetector.Result(DiffAction.deleted,"afghanistan", ""),
                new DiffDetector.Result(DiffAction.inserted,"featur", ""),
                new DiffDetector.Result(DiffAction.inserted,"analysi", ""),
                new DiffDetector.Result(DiffAction.inserted,"throe", ""),
                new DiffDetector.Result(DiffAction.inserted,"chang", ""),
                new DiffDetector.Result(DiffAction.inserted,"someth", ""),
                new DiffDetector.Result(DiffAction.inserted,"stir", ""),
                new DiffDetector.Result(DiffAction.inserted,"high", ""),
                new DiffDetector.Result(DiffAction.inserted,"street", ""),
                new DiffDetector.Result(DiffAction.inserted,"dig", ""),
                new DiffDetector.Result(DiffAction.inserted,"danger", ""),
                new DiffDetector.Result(DiffAction.inserted,"man", ""),
                new DiffDetector.Result(DiffAction.inserted,"found", ""),
                new DiffDetector.Result(DiffAction.inserted,"100", ""),
                new DiffDetector.Result(DiffAction.inserted,"bomb", ""),
                new DiffDetector.Result(DiffAction.inserted,"afghanistan", ""),
                new DiffDetector.Result(DiffAction.inserted,"young", ""),
                new DiffDetector.Result(DiffAction.inserted,"hungri", ""),
                new DiffDetector.Result(DiffAction.inserted,"peopl", ""),
                new DiffDetector.Result(DiffAction.inserted,"struggl", ""),
                new DiffDetector.Result(DiffAction.inserted,"put", ""),
                new DiffDetector.Result(DiffAction.inserted,"food", ""),
                new DiffDetector.Result(DiffAction.inserted,"tabl", ""),
                new DiffDetector.Result(DiffAction.inserted,"interest", ""),
                new DiffDetector.Result(DiffAction.inserted,"rate", ""),
                new DiffDetector.Result(DiffAction.inserted,"rise", ""),
                new DiffDetector.Result(DiffAction.inserted,"might", ""),
                new DiffDetector.Result(DiffAction.inserted,"wors", ""),
                new DiffDetector.Result(DiffAction.inserted,"famili", ""),
                new DiffDetector.Result(DiffAction.inserted,"economi", ""),
                new DiffDetector.Result(DiffAction.deleted,"72", ""),
                new DiffDetector.Result(DiffAction.deleted,"christma", ""),
                new DiffDetector.Result(DiffAction.deleted,"tree", ""),
                new DiffDetector.Result(DiffAction.deleted,"52", ""),
                new DiffDetector.Result(DiffAction.deleted,"room", ""),
                new DiffDetector.Result(DiffAction.deleted,"hous", ""),
                new DiffDetector.Result(DiffAction.deleted,"decor", ""),
                new DiffDetector.Result(DiffAction.inserted,"inbox", ""),
                new DiffDetector.Result(DiffAction.inserted,"fatigu", ""),
                new DiffDetector.Result(DiffAction.inserted,"take", ""),
                new DiffDetector.Result(DiffAction.inserted,"back", ""),
                new DiffDetector.Result(DiffAction.inserted,"control", ""),
                new DiffDetector.Result(DiffAction.inserted,"email", ""),
                new DiffDetector.Result(DiffAction.deleted,"magic", ""),
                new DiffDetector.Result(DiffAction.deleted,"moment", ""),
                new DiffDetector.Result(DiffAction.deleted,"make", ""),
                new DiffDetector.Result(DiffAction.deleted,"live", ""),
                new DiffDetector.Result(DiffAction.deleted,"busi", ""),
                new DiffDetector.Result(DiffAction.deleted,"show", "")
        );

        DiffMatcher matcher = new DiffMatcher(job, diffList);
        Set<DiffMatcher.Result> matchSet = matcher.call();
        assertEquals(11, matchSet.size());

    }


    @AfterClass
    public static void close() {
        termsDatabase.dropDatabase();
        mongoClient.close();
    }
}

