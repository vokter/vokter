package argus.diff;

import argus.job.workers.DiffMatcherJob;
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
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DiffMatcherTest {

    private static final Logger logger = LoggerFactory.getLogger(DiffMatcherTest.class);

    private static MongoClient mongoClient;
    private static DB occurrencesDB;
    private static ParserPool parserPool;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        mongoClient = new MongoClient("localhost", 27017);
        occurrencesDB = mongoClient.getDB("test_terms_db");
        parserPool = new ParserPool();
        parserPool.place(new GeniaParser());
    }

    @AfterClass
    public static void close() {
        occurrencesDB.dropDatabase();
        mongoClient.close();
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

        DiffMatcherJob worker = new DiffMatcherJob(url, keywords, 10, url);

        List<DiffFinder.Result> diffList = Lists.newArrayList(
                new DiffFinder.Result(DiffAction.inserted, "argus", "Argus Panoptes is the name of the 100 eyed giant in No"),
                new DiffFinder.Result(DiffAction.inserted, "panopt", "Argus Panoptes is the name of the 100 eyed giant in Norse mytho"),
                new DiffFinder.Result(DiffAction.inserted, "name", "Argus Panoptes is the name of the 100 eyed giant in Norse mythology"),
                new DiffFinder.Result(DiffAction.deleted, "greek", "is the of the 100 eyed giant in Greek mythology"),
                new DiffFinder.Result(DiffAction.inserted, "nors", "gus Panoptes is the name of the 100 eyed giant in Norse mythology")
        );

        DiffMatcher matcher = new DiffMatcher(worker, diffList);
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
        DiffMatcherJob worker = new DiffMatcherJob(url, keywords, 10, url);

        List<DiffFinder.Result> diffList = Lists.newArrayList(
                new DiffFinder.Result(DiffAction.deleted, "0", ""),
                new DiffFinder.Result(DiffAction.deleted, "52", "tics Education 8 December 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge (dismisse"),
                new DiffFinder.Result(DiffAction.inserted, "3", ""),
                new DiffFinder.Result(DiffAction.inserted, "07", "tics Education 8 December 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The (famil"),
                new DiffFinder.Result(DiffAction.deleted, "murder", " 8 December 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case (against Shr"),
                new DiffFinder.Result(DiffAction.deleted, "case", "mber 2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against (Shrien D"),
                new DiffFinder.Result(DiffAction.deleted, "thrown", "2014 Last updated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien (Dewani a"),
                new DiffFinder.Result(DiffAction.deleted, "judg", "pdated at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused (of "),
                new DiffFinder.Result(DiffAction.deleted, "dismiss", " at 10 52 GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused of (arranging "),
                new DiffFinder.Result(DiffAction.deleted, "case", "GMT Dewani murder case thrown out Judge dismisses case against Shrien Dewani accused of arranging (murde"),
                new DiffFinder.Result(DiffAction.deleted, "shrien", "rder case thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of (wife Anni "),
                new DiffFinder.Result(DiffAction.deleted, "dewani", "se thrown out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni (in Sout"),
                new DiffFinder.Result(DiffAction.deleted, "accus", "wn out Judge dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South (Africa"),
                new DiffFinder.Result(DiffAction.deleted, "arrang", "e dismisses case against Shrien Dewani accused of arranging murder of wife Anni in South Africa (Shrien Dewan"),
                new DiffFinder.Result(DiffAction.deleted, "murder", "s case against Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien (Dewani trial"),
                new DiffFinder.Result(DiffAction.deleted, "wife", "inst Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial (Reactio"),
                new DiffFinder.Result(DiffAction.deleted, "anni", "Shrien Dewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction (Pay"),
                new DiffFinder.Result(DiffAction.deleted, "south", "ewani accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay (benefits"),
                new DiffFinder.Result(DiffAction.deleted, "africa", "accused of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits (faster"),
                new DiffFinder.Result(DiffAction.deleted, "shrien", " of arranging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster (to cut"),
                new DiffFinder.Result(DiffAction.deleted, "dewani", "anging murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut (hunger"),
                new DiffFinder.Result(DiffAction.deleted, "trial", "murder of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut hunger (An in"),
                new DiffFinder.Result(DiffAction.deleted, "reaction", " of wife Anni in South Africa Shrien Dewani trial Reaction Pay benefits faster to cut hunger An (income sque"),
                new DiffFinder.Result(DiffAction.inserted, "clear", " 8 December 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni (Dewani be"),
                new DiffFinder.Result(DiffAction.inserted, "honeymoon", " 2014 Last updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani (believe they ha"),
                new DiffFinder.Result(DiffAction.inserted, "murder", " updated at 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani believe they (have been"),
                new DiffFinder.Result(DiffAction.inserted, "famili", " 13 07 GMT Dewani cleared of honeymoon murder The family of Anni Dewani believe they have been (failed by "),
                new DiffFinder.Result(DiffAction.inserted, "anni", " Dewani cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the (just"),
                new DiffFinder.Result(DiffAction.inserted, "dewani", "ni cleared of honeymoon murder The family of Anni Dewani believe they have been failed by the (justice sys"),
                new DiffFinder.Result(DiffAction.inserted, "believ", "red of honeymoon murder The family of Anni Dewani believe they have been failed by the justice (system afte"),
                new DiffFinder.Result(DiffAction.inserted, "fail", " The family of Anni Dewani believe they have been failed by the justice system after millionaire (business"),
                new DiffFinder.Result(DiffAction.inserted, "justic", " Anni Dewani believe they have been failed by the justice system after millionaire businessman (Shrien Dewa"),
                new DiffFinder.Result(DiffAction.inserted, "system", "wani believe they have been failed by the justice system after millionaire businessman Shrien (Dewani is c"),
                new DiffFinder.Result(DiffAction.inserted, "millionair", "they have been failed by the justice system after millionaire businessman Shrien Dewani is (cleared of the hone"),
                new DiffFinder.Result(DiffAction.inserted, "businessman", "en failed by the justice system after millionaire businessman Shrien Dewani is cleared of (the honeymoon murder"),
                new DiffFinder.Result(DiffAction.inserted, "shrien", " the justice system after millionaire businessman Shrien Dewani is cleared of the honeymoon (murder Shrien"),
                new DiffFinder.Result(DiffAction.inserted, "dewani", "stice system after millionaire businessman Shrien Dewani is cleared of the honeymoon murder (Shrien Dewani"),
                new DiffFinder.Result(DiffAction.inserted, "clear", "em after millionaire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani (trial Reac"),
                new DiffFinder.Result(DiffAction.inserted, "honeymoon", "naire businessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial (Reaction Live How Dew"),
                new DiffFinder.Result(DiffAction.inserted, "murder", "nessman Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How (Dewani pro"),
                new DiffFinder.Result(DiffAction.inserted, "shrien", " Shrien Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani (prosecutio"),
                new DiffFinder.Result(DiffAction.inserted, "dewani", " Dewani is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani (prosecution fell "),
                new DiffFinder.Result(DiffAction.inserted, "trial", " is cleared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell (apart "),
                new DiffFinder.Result(DiffAction.inserted, "reaction", "eared of the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell (apart From drea"),
                new DiffFinder.Result(DiffAction.inserted, "live", "the honeymoon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream (wed"),
                new DiffFinder.Result(DiffAction.inserted, "dewani", "moon murder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream (wedding to fat"),
                new DiffFinder.Result(DiffAction.inserted, "prosecut", "rder Shrien Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to (fatal hijacking"),
                new DiffFinder.Result(DiffAction.inserted, "fell", "Dewani trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking (Hiri"),
                new DiffFinder.Result(DiffAction.inserted, "apart", "i trial Reaction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking (Hiring a h"),
                new DiffFinder.Result(DiffAction.inserted, "dream", "ction Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman (in So"),
                new DiffFinder.Result(DiffAction.inserted, "wed", "Live How Dewani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in (South Afri"),
                new DiffFinder.Result(DiffAction.inserted, "fatal", "wani prosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa (Dewani"),
                new DiffFinder.Result(DiffAction.inserted, "hijack", "rosecution fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa (Dewani family Ju"),
                new DiffFinder.Result(DiffAction.inserted, "hire", " fell apart From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family (Justice s"),
                new DiffFinder.Result(DiffAction.inserted, "hitman", "rt From dream wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice (system fai"),
                new DiffFinder.Result(DiffAction.inserted, "south", "eam wedding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed (Watch"),
                new DiffFinder.Result(DiffAction.inserted, "africa", "dding to fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed (Watch The ba"),
                new DiffFinder.Result(DiffAction.inserted, "dewani", "o fatal hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The (backgroun"),
                new DiffFinder.Result(DiffAction.inserted, "famili", " hijacking Hiring a hitman in South Africa Dewani family Justice system failed Watch The (background to th"),
                new DiffFinder.Result(DiffAction.inserted, "justic", "ing Hiring a hitman in South Africa Dewani family Justice system failed Watch The background to (the Dewani"),
                new DiffFinder.Result(DiffAction.inserted, "system", "ng a hitman in South Africa Dewani family Justice system failed Watch The background to the (Dewani case W"),
                new DiffFinder.Result(DiffAction.inserted, "fail", "tman in South Africa Dewani family Justice system failed Watch The background to the Dewani case (Watch Pa"),
                new DiffFinder.Result(DiffAction.inserted, "watch", " South Africa Dewani family Justice system failed Watch The background to the Dewani case Watch (Pay bene"),
                new DiffFinder.Result(DiffAction.inserted, "background", "ica Dewani family Justice system failed Watch The background to the Dewani case Watch Pay (benefits faster to "),
                new DiffFinder.Result(DiffAction.inserted, "dewani", "Justice system failed Watch The background to the Dewani case Watch Pay benefits faster to cut (hunger An "),
                new DiffFinder.Result(DiffAction.inserted, "case", " system failed Watch The background to the Dewani case Watch Pay benefits faster to cut hunger An (incom"),
                new DiffFinder.Result(DiffAction.inserted, "watch", "em failed Watch The background to the Dewani case Watch Pay benefits faster to cut hunger An (income sque"),
                new DiffFinder.Result(DiffAction.deleted, "fresh", " Sydney Australia while on an outing with friends Fresh cracks appear in coalition Senior (Conservative a"),
                new DiffFinder.Result(DiffAction.deleted, "crack", "y Australia while on an outing with friends Fresh cracks appear in coalition Senior Conservative (and Lib "),
                new DiffFinder.Result(DiffAction.deleted, "appear", "alia while on an outing with friends Fresh cracks appear in coalition Senior Conservative and Lib (Dem min"),
                new DiffFinder.Result(DiffAction.deleted, "coalit", " on an outing with friends Fresh cracks appear in coalition Senior Conservative and Lib Dem (ministers critic"),
                new DiffFinder.Result(DiffAction.deleted, "senior", "ing with friends Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers (criticise eac"),
                new DiffFinder.Result(DiffAction.deleted, "conserv", "h friends Fresh cracks appear in coalition Senior Conservative and Lib Dem ministers criticise (each other amid "),
                new DiffFinder.Result(DiffAction.deleted, "lib", "racks appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid (suggesti"),
                new DiffFinder.Result(DiffAction.deleted, "dem", "s appear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions ("),
                new DiffFinder.Result(DiffAction.deleted, "minist", "pear in coalition Senior Conservative and Lib Dem ministers criticise each other amid suggestions (of widenin"),
                new DiffFinder.Result(DiffAction.deleted, "criticis", "alition Senior Conservative and Lib Dem ministers criticise each other amid suggestions of (widening division"),
                new DiffFinder.Result(DiffAction.deleted, "amid", "vative and Lib Dem ministers criticise each other amid suggestions of widening divisions in the (coaliti"),
                new DiffFinder.Result(DiffAction.deleted, "suggest", "e and Lib Dem ministers criticise each other amid suggestions of widening divisions in the (coalition governmen"),
                new DiffFinder.Result(DiffAction.deleted, "widen", "inisters criticise each other amid suggestions of widening divisions in the coalition government (Home owner"),
                new DiffFinder.Result(DiffAction.deleted, "divis", "criticise each other amid suggestions of widening divisions in the coalition government Home owners (could ha"),
                new DiffFinder.Result(DiffAction.deleted, "coalit", "her amid suggestions of widening divisions in the coalition government Home owners could handle (rate rise Th"),
                new DiffFinder.Result(DiffAction.deleted, "govern", "uggestions of widening divisions in the coalition government Home owners could handle rate rise (The majority "),
                new DiffFinder.Result(DiffAction.deleted, "home", "of widening divisions in the coalition government Home owners could handle rate rise The majority of (pe"),
                new DiffFinder.Result(DiffAction.deleted, "owner", "dening divisions in the coalition government Home owners could handle rate rise The majority of (people wi"),
                new DiffFinder.Result(DiffAction.deleted, "handl", "ons in the coalition government Home owners could handle rate rise The majority of people with (mortgages "),
                new DiffFinder.Result(DiffAction.deleted, "rate", "the coalition government Home owners could handle rate rise The majority of people with mortgages (could"),
                new DiffFinder.Result(DiffAction.deleted, "rise", "oalition government Home owners could handle rate rise The majority of people with mortgages could (cope"),
                new DiffFinder.Result(DiffAction.deleted, "major", "government Home owners could handle rate rise The majority of people with mortgages could cope with (a rise "),
                new DiffFinder.Result(DiffAction.deleted, "peop", ""),
                new DiffFinder.Result(DiffAction.inserted, "disabl", " Sydney Australia while on an outing with friends Disability fund closure ruled lawful A (government decision "),
                new DiffFinder.Result(DiffAction.inserted, "fund", "tralia while on an outing with friends Disability fund closure ruled lawful A government decision (to cl"),
                new DiffFinder.Result(DiffAction.inserted, "closur", "a while on an outing with friends Disability fund closure ruled lawful A government decision to (close a fu"),
                new DiffFinder.Result(DiffAction.inserted, "rule", "on an outing with friends Disability fund closure ruled lawful A government decision to close a (fund tha"),
                new DiffFinder.Result(DiffAction.inserted, "law", "outing with friends Disability fund closure ruled lawful A government decision to close a fund that (helps"),
                new DiffFinder.Result(DiffAction.inserted, "govern", "th friends Disability fund closure ruled lawful A government decision to close a fund that helps (disabled peo"),
                new DiffFinder.Result(DiffAction.inserted, "decis", "Disability fund closure ruled lawful A government decision to close a fund that helps disabled (people to li"),
                new DiffFinder.Result(DiffAction.inserted, "close", "und closure ruled lawful A government decision to close a fund that helps disabled people to live (and wo"),
                new DiffFinder.Result(DiffAction.inserted, "fund", "ure ruled lawful A government decision to close a fund that helps disabled people to live and work (in t"),
                new DiffFinder.Result(DiffAction.inserted, "help", "lawful A government decision to close a fund that helps disabled people to live and work in the (communit"),
                new DiffFinder.Result(DiffAction.inserted, "disabl", " A government decision to close a fund that helps disabled people to live and work in the (community is lawf"),
                new DiffFinder.Result(DiffAction.inserted, "peopl", "ment decision to close a fund that helps disabled people to live and work in the community is (lawful the "),
                new DiffFinder.Result(DiffAction.inserted, "live", "ion to close a fund that helps disabled people to live and work in the community is lawful the High (Cou"),
                new DiffFinder.Result(DiffAction.inserted, "work", "ose a fund that helps disabled people to live and work in the community is lawful the High Court (rules "),
                new DiffFinder.Result(DiffAction.inserted, "communiti", "hat helps disabled people to live and work in the community is lawful the High Court rules (Coalition to surv"),
                new DiffFinder.Result(DiffAction.inserted, "law", "abled people to live and work in the community is lawful the High Court rules Coalition to survive (despit"),
                new DiffFinder.Result(DiffAction.inserted, "high", "e to live and work in the community is lawful the High Court rules Coalition to survive despite (spats S"),
                new DiffFinder.Result(DiffAction.inserted, "court", "live and work in the community is lawful the High Court rules Coalition to survive despite spats (Senior "),
                new DiffFinder.Result(DiffAction.inserted, "rule", "nd work in the community is lawful the High Court rules Coalition to survive despite spats Senior (Lib De"),
                new DiffFinder.Result(DiffAction.inserted, "coalit", "k in the community is lawful the High Court rules Coalition to survive despite spats Senior Lib (Dem Danny Al"),
                new DiffFinder.Result(DiffAction.inserted, "surviv", "unity is lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny (Alexander ins"),
                new DiffFinder.Result(DiffAction.inserted, "despit", " lawful the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander (insists tha"),
                new DiffFinder.Result(DiffAction.inserted, "spat", "the High Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that (trad"),
                new DiffFinder.Result(DiffAction.inserted, "senior", "gh Court rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that (trading ins"),
                new DiffFinder.Result(DiffAction.inserted, "lib", "t rules Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading (insults"),
                new DiffFinder.Result(DiffAction.inserted, "dem", "les Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults (wit"),
                new DiffFinder.Result(DiffAction.inserted, "danni", "Coalition to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults (with his "),
                new DiffFinder.Result(DiffAction.inserted, "alexand", "ion to survive despite spats Senior Lib Dem Danny Alexander insists that trading insults with (his Conservati"),
                new DiffFinder.Result(DiffAction.inserted, "insist", "vive despite spats Senior Lib Dem Danny Alexander insists that trading insults with his (Conservatives part"),
                new DiffFinder.Result(DiffAction.inserted, "trade", "spats Senior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners (does not"),
                new DiffFinder.Result(DiffAction.inserted, "insult", "nior Lib Dem Danny Alexander insists that trading insults with his Conservatives partners does (not undermi"),
                new DiffFinder.Result(DiffAction.inserted, "conserv", "y Alexander insists that trading insults with his Conservatives partners does not undermine the (parties ability "),
                new DiffFinder.Result(DiffAction.inserted, "partner", "sists that trading insults with his Conservatives partners does not undermine the parties (ability to work t"),
                new DiffFinder.Result(DiffAction.inserted, "undermin", " insults with his Conservatives partners does not undermine the parties ability to work (together Johnson cri"),
                new DiffFinder.Result(DiffAction.inserted, "parti", "his Conservatives partners does not undermine the parties ability to work together Johnson (criticises Fara"),
                new DiffFinder.Result(DiffAction.inserted, "abi", ""),
                new DiffFinder.Result(DiffAction.deleted, "m", ""),
                new DiffFinder.Result(DiffAction.inserted, "w", ""),
                new DiffFinder.Result(DiffAction.deleted, "tgag", ""),
                new DiffFinder.Result(DiffAction.deleted, "cope", ""),
                new DiffFinder.Result(DiffAction.deleted, "rise", ""),
                new DiffFinder.Result(DiffAction.deleted, "interest", ""),
                new DiffFinder.Result(DiffAction.deleted, "rate", ""),
                new DiffFinder.Result(DiffAction.deleted, "bank", ""),
                new DiffFinder.Result(DiffAction.deleted, "england", ""),
                new DiffFinder.Result(DiffAction.deleted, "said", ""),
                new DiffFinder.Result(DiffAction.inserted, "k", ""),
                new DiffFinder.Result(DiffAction.inserted, "togeth", ""),
                new DiffFinder.Result(DiffAction.inserted, "johnson", ""),
                new DiffFinder.Result(DiffAction.inserted, "criticis", ""),
                new DiffFinder.Result(DiffAction.inserted, "farag", ""),
                new DiffFinder.Result(DiffAction.inserted, "m4", ""),
                new DiffFinder.Result(DiffAction.inserted, "excus", ""),
                new DiffFinder.Result(DiffAction.inserted, "home", ""),
                new DiffFinder.Result(DiffAction.inserted, "owner", ""),
                new DiffFinder.Result(DiffAction.inserted, "handl", ""),
                new DiffFinder.Result(DiffAction.inserted, "rate", ""),
                new DiffFinder.Result(DiffAction.inserted, "rise", ""),
                new DiffFinder.Result(DiffAction.inserted, "playstat", ""),
                new DiffFinder.Result(DiffAction.inserted, "hit", ""),
                new DiffFinder.Result(DiffAction.inserted, "hack", ""),
                new DiffFinder.Result(DiffAction.inserted, "attack", ""),
                new DiffFinder.Result(DiffAction.deleted, "take", ""),
                new DiffFinder.Result(DiffAction.deleted, "care", ""),
                new DiffFinder.Result(DiffAction.deleted, "complaint", ""),
                new DiffFinder.Result(DiffAction.deleted, "serious", ""),
                new DiffFinder.Result(DiffAction.deleted, "defenc", ""),
                new DiffFinder.Result(DiffAction.deleted, "staff", ""),
                new DiffFinder.Result(DiffAction.deleted, "begin", ""),
                new DiffFinder.Result(DiffAction.deleted, "10", ""),
                new DiffFinder.Result(DiffAction.deleted, "day", ""),
                new DiffFinder.Result(DiffAction.deleted, "strike", ""),
                new DiffFinder.Result(DiffAction.deleted, "uk", ""),
                new DiffFinder.Result(DiffAction.deleted, "embassi", ""),
                new DiffFinder.Result(DiffAction.deleted, "cairo", ""),
                new DiffFinder.Result(DiffAction.deleted, "remain", ""),
                new DiffFinder.Result(DiffAction.deleted, "close", ""),
                new DiffFinder.Result(DiffAction.deleted, "teenag", ""),
                new DiffFinder.Result(DiffAction.deleted, "runaway", ""),
                new DiffFinder.Result(DiffAction.deleted, "lack", ""),
                new DiffFinder.Result(DiffAction.deleted, "refug", ""),
                new DiffFinder.Result(DiffAction.deleted, "pride", ""),
                new DiffFinder.Result(DiffAction.deleted, "win", ""),
                new DiffFinder.Result(DiffAction.deleted, "best", ""),
                new DiffFinder.Result(DiffAction.deleted, "film", ""),
                new DiffFinder.Result(DiffAction.deleted, "indi", ""),
                new DiffFinder.Result(DiffAction.deleted, "award", ""),
                new DiffFinder.Result(DiffAction.inserted, "uk", ""),
                new DiffFinder.Result(DiffAction.inserted, "embassi", ""),
                new DiffFinder.Result(DiffAction.inserted, "cairo", ""),
                new DiffFinder.Result(DiffAction.inserted, "remain", ""),
                new DiffFinder.Result(DiffAction.inserted, "close", ""),
                new DiffFinder.Result(DiffAction.deleted, "standard", ""),
                new DiffFinder.Result(DiffAction.deleted, "fear", ""),
                new DiffFinder.Result(DiffAction.deleted, "school", ""),
                new DiffFinder.Result(DiffAction.deleted, "cut", ""),
                new DiffFinder.Result(DiffAction.deleted, "alcohol", ""),
                new DiffFinder.Result(DiffAction.deleted, "price", ""),
                new DiffFinder.Result(DiffAction.deleted, "law", ""),
                new DiffFinder.Result(DiffAction.deleted, "save", ""),
                new DiffFinder.Result(DiffAction.deleted, "900m", ""),
                new DiffFinder.Result(DiffAction.deleted, "scotland", ""),
                new DiffFinder.Result(DiffAction.deleted, "oralba", ""),
                new DiffFinder.Result(DiffAction.deleted, "warn", ""),
                new DiffFinder.Result(DiffAction.deleted, "snow", ""),
                new DiffFinder.Result(DiffAction.deleted, "ice", ""),
                new DiffFinder.Result(DiffAction.deleted, "affect", ""),
                new DiffFinder.Result(DiffAction.deleted, "roa", ""),
                new DiffFinder.Result(DiffAction.inserted, "eight", ""),
                new DiffFinder.Result(DiffAction.inserted, "car", ""),
                new DiffFinder.Result(DiffAction.inserted, "damag", ""),
                new DiffFinder.Result(DiffAction.inserted, "arson", ""),
                new DiffFinder.Result(DiffAction.inserted, "attack", ""),
                new DiffFinder.Result(DiffAction.inserted, "farmer", ""),
                new DiffFinder.Result(DiffAction.inserted, "deliv", ""),
                new DiffFinder.Result(DiffAction.inserted, "down", ""),
                new DiffFinder.Result(DiffAction.inserted, "street", ""),
                new DiffFinder.Result(DiffAction.inserted, "tree", ""),
                new DiffFinder.Result(DiffAction.inserted, "scotland", ""),
                new DiffFinder.Result(DiffAction.inserted, "oralba", ""),
                new DiffFinder.Result(DiffAction.inserted, "woman", ""),
                new DiffFinder.Result(DiffAction.inserted, "kill", ""),
                new DiffFinder.Result(DiffAction.inserted, "ambul", ""),
                new DiffFinder.Result(DiffAction.inserted, "acci", ""),
                new DiffFinder.Result(DiffAction.deleted, "m", ""),
                new DiffFinder.Result(DiffAction.inserted, "b", ""),
                new DiffFinder.Result(DiffAction.deleted, "found", ""),
                new DiffFinder.Result(DiffAction.deleted, "dead", ""),
                new DiffFinder.Result(DiffAction.deleted, "close", ""),
                new DiffFinder.Result(DiffAction.deleted, "flat", ""),
                new DiffFinder.Result(DiffAction.inserted, "k", ""),
                new DiffFinder.Result(DiffAction.inserted, "accus", ""),
                new DiffFinder.Result(DiffAction.inserted, "fraud", ""),
                new DiffFinder.Result(DiffAction.inserted, "mortgag", ""),
                new DiffFinder.Result(DiffAction.deleted, "boy", ""),
                new DiffFinder.Result(DiffAction.deleted, "15", ""),
                new DiffFinder.Result(DiffAction.deleted, "kill", ""),
                new DiffFinder.Result(DiffAction.deleted, "fight", ""),
                new DiffFinder.Result(DiffAction.deleted, "name", ""),
                new DiffFinder.Result(DiffAction.inserted, "live", ""),
                new DiffFinder.Result(DiffAction.inserted, "son", ""),
                new DiffFinder.Result(DiffAction.inserted, "detain", ""),
                new DiffFinder.Result(DiffAction.inserted, "tri", ""),
                new DiffFinder.Result(DiffAction.inserted, "kill", ""),
                new DiffFinder.Result(DiffAction.inserted, "dad", ""),
                new DiffFinder.Result(DiffAction.inserted, "cap", ""),
                new DiffFinder.Result(DiffAction.deleted, "game", ""),
                new DiffFinder.Result(DiffAction.deleted, "bid", ""),
                new DiffFinder.Result(DiffAction.deleted, "revamp", ""),
                new DiffFinder.Result(DiffAction.deleted, "pass", ""),
                new DiffFinder.Result(DiffAction.inserted, "sport", "ap on Olympic sports is dropped Sportsday rolling sports news Live Magazine Why China sees itself (in Lowr"),
                new DiffFinder.Result(DiffAction.inserted, "drop", ""),
                new DiffFinder.Result(DiffAction.deleted, "scare", ""),
                new DiffFinder.Result(DiffAction.deleted, "biggest", ""),
                new DiffFinder.Result(DiffAction.deleted, "pest", ""),
                new DiffFinder.Result(DiffAction.deleted, "eleph", ""),
                new DiffFinder.Result(DiffAction.deleted, "need", ""),
                new DiffFinder.Result(DiffAction.deleted, "kept", ""),
                new DiffFinder.Result(DiffAction.deleted, "away", ""),
                new DiffFinder.Result(DiffAction.deleted, "farm", ""),
                new DiffFinder.Result(DiffAction.deleted, "insid", "farms Inside a giant spider Take a unique journey inside the body of a giant tarantula Democracy (Live Hou"),
                new DiffFinder.Result(DiffAction.deleted, "giant", "spider Take a unique journey inside the body of a giant tarantula Democracy Live House of Commons (Full c"),
                new DiffFinder.Result(DiffAction.deleted, "spider", ""),
                new DiffFinder.Result(DiffAction.deleted, "take", ""),
                new DiffFinder.Result(DiffAction.deleted, "uniqu", ""),
                new DiffFinder.Result(DiffAction.deleted, "journey", ""),
                new DiffFinder.Result(DiffAction.deleted, "insid", ""),
                new DiffFinder.Result(DiffAction.deleted, "bodi", ""),
                new DiffFinder.Result(DiffAction.deleted, "giant", ""),
                new DiffFinder.Result(DiffAction.deleted, "tarantula", ""),
                new DiffFinder.Result(DiffAction.inserted, "huge", ""),
                new DiffFinder.Result(DiffAction.inserted, "crab", ""),
                new DiffFinder.Result(DiffAction.inserted, "munch", ""),
                new DiffFinder.Result(DiffAction.inserted, "coconut", ""),
                new DiffFinder.Result(DiffAction.inserted, "gigant", ""),
                new DiffFinder.Result(DiffAction.inserted, "odd", ""),
                new DiffFinder.Result(DiffAction.inserted, "may", ""),
                new DiffFinder.Result(DiffAction.inserted, "endang", ""),
                new DiffFinder.Result(DiffAction.inserted, "scare", ""),
                new DiffFinder.Result(DiffAction.inserted, "biggest", ""),
                new DiffFinder.Result(DiffAction.inserted, "pest", ""),
                new DiffFinder.Result(DiffAction.inserted, "eleph", ""),
                new DiffFinder.Result(DiffAction.inserted, "need", ""),
                new DiffFinder.Result(DiffAction.inserted, "kept", ""),
                new DiffFinder.Result(DiffAction.inserted, "away", ""),
                new DiffFinder.Result(DiffAction.inserted, "farm", ""),
                new DiffFinder.Result(DiffAction.deleted, "hous", ""),
                new DiffFinder.Result(DiffAction.deleted, "common", "ocracy Live House of Commons Full coverage in the Commons as MPs debate private members bills Find (a repre"),
                new DiffFinder.Result(DiffAction.inserted, "crackdown", ""),
                new DiffFinder.Result(DiffAction.inserted, "uk", ""),
                new DiffFinder.Result(DiffAction.inserted, "s", ""),
                new DiffFinder.Result(DiffAction.inserted, "billion", ""),
                new DiffFinder.Result(DiffAction.inserted, "nuisanc", ""),
                new DiffFinder.Result(DiffAction.inserted, "call", "wn on UK s billion nuisance calls Watch01 20 Cold calls break law says report Listen Barclays (introduce "),
                new DiffFinder.Result(DiffAction.inserted, "watch01", ""),
                new DiffFinder.Result(DiffAction.inserted, "20", ""),
                new DiffFinder.Result(DiffAction.deleted, "traffick", ""),
                new DiffFinder.Result(DiffAction.deleted, "slaveri", ""),
                new DiffFinder.Result(DiffAction.deleted, "uk", ""),
                new DiffFinder.Result(DiffAction.deleted, "watch01", ""),
                new DiffFinder.Result(DiffAction.deleted, "45", ""),
                new DiffFinder.Result(DiffAction.deleted, "radio", ""),
                new DiffFinder.Result(DiffAction.deleted, "5", ""),
                new DiffFinder.Result(DiffAction.deleted, "live", ""),
                new DiffFinder.Result(DiffAction.deleted, "live", ""),
                new DiffFinder.Result(DiffAction.deleted, "featur", ""),
                new DiffFinder.Result(DiffAction.deleted, "analysi", ""),
                new DiffFinder.Result(DiffAction.deleted, "dig", ""),
                new DiffFinder.Result(DiffAction.deleted, "danger", ""),
                new DiffFinder.Result(DiffAction.deleted, "man", ""),
                new DiffFinder.Result(DiffAction.deleted, "found", ""),
                new DiffFinder.Result(DiffAction.deleted, "100", ""),
                new DiffFinder.Result(DiffAction.deleted, "bomb", ""),
                new DiffFinder.Result(DiffAction.deleted, "afghanistan", ""),
                new DiffFinder.Result(DiffAction.inserted, "featur", ""),
                new DiffFinder.Result(DiffAction.inserted, "analysi", ""),
                new DiffFinder.Result(DiffAction.inserted, "throe", ""),
                new DiffFinder.Result(DiffAction.inserted, "chang", ""),
                new DiffFinder.Result(DiffAction.inserted, "someth", ""),
                new DiffFinder.Result(DiffAction.inserted, "stir", ""),
                new DiffFinder.Result(DiffAction.inserted, "high", ""),
                new DiffFinder.Result(DiffAction.inserted, "street", ""),
                new DiffFinder.Result(DiffAction.inserted, "dig", ""),
                new DiffFinder.Result(DiffAction.inserted, "danger", ""),
                new DiffFinder.Result(DiffAction.inserted, "man", ""),
                new DiffFinder.Result(DiffAction.inserted, "found", ""),
                new DiffFinder.Result(DiffAction.inserted, "100", ""),
                new DiffFinder.Result(DiffAction.inserted, "bomb", ""),
                new DiffFinder.Result(DiffAction.inserted, "afghanistan", ""),
                new DiffFinder.Result(DiffAction.inserted, "young", ""),
                new DiffFinder.Result(DiffAction.inserted, "hungri", ""),
                new DiffFinder.Result(DiffAction.inserted, "peopl", ""),
                new DiffFinder.Result(DiffAction.inserted, "struggl", ""),
                new DiffFinder.Result(DiffAction.inserted, "put", ""),
                new DiffFinder.Result(DiffAction.inserted, "food", ""),
                new DiffFinder.Result(DiffAction.inserted, "tabl", ""),
                new DiffFinder.Result(DiffAction.inserted, "interest", ""),
                new DiffFinder.Result(DiffAction.inserted, "rate", ""),
                new DiffFinder.Result(DiffAction.inserted, "rise", ""),
                new DiffFinder.Result(DiffAction.inserted, "might", ""),
                new DiffFinder.Result(DiffAction.inserted, "wors", ""),
                new DiffFinder.Result(DiffAction.inserted, "famili", ""),
                new DiffFinder.Result(DiffAction.inserted, "economi", ""),
                new DiffFinder.Result(DiffAction.deleted, "72", ""),
                new DiffFinder.Result(DiffAction.deleted, "christma", ""),
                new DiffFinder.Result(DiffAction.deleted, "tree", ""),
                new DiffFinder.Result(DiffAction.deleted, "52", ""),
                new DiffFinder.Result(DiffAction.deleted, "room", ""),
                new DiffFinder.Result(DiffAction.deleted, "hous", ""),
                new DiffFinder.Result(DiffAction.deleted, "decor", ""),
                new DiffFinder.Result(DiffAction.inserted, "inbox", ""),
                new DiffFinder.Result(DiffAction.inserted, "fatigu", ""),
                new DiffFinder.Result(DiffAction.inserted, "take", ""),
                new DiffFinder.Result(DiffAction.inserted, "back", ""),
                new DiffFinder.Result(DiffAction.inserted, "control", ""),
                new DiffFinder.Result(DiffAction.inserted, "email", ""),
                new DiffFinder.Result(DiffAction.deleted, "magic", ""),
                new DiffFinder.Result(DiffAction.deleted, "moment", ""),
                new DiffFinder.Result(DiffAction.deleted, "make", ""),
                new DiffFinder.Result(DiffAction.deleted, "live", ""),
                new DiffFinder.Result(DiffAction.deleted, "busi", ""),
                new DiffFinder.Result(DiffAction.deleted, "show", "")
        );

        DiffMatcher matcher = new DiffMatcher(worker, diffList);
        Set<DiffMatcher.Result> matchSet = matcher.call();
        assertEquals(11, matchSet.size());
    }
}

