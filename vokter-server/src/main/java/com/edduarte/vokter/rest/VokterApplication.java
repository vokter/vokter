package com.edduarte.vokter.rest;

import com.edduarte.vokter.Constants;
import com.edduarte.vokter.diff.DiffEvent;
import com.edduarte.vokter.job.JobManager;
import com.edduarte.vokter.job.RestNotificationHandler;
import com.edduarte.vokter.parser.Parser;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.edduarte.vokter.persistence.DiffCollection;
import com.edduarte.vokter.persistence.DocumentCollection;
import com.edduarte.vokter.persistence.Session;
import com.edduarte.vokter.persistence.SessionCollection;
import com.edduarte.vokter.persistence.mongodb.MongoDiffCollection;
import com.edduarte.vokter.persistence.mongodb.MongoDocumentCollection;
import com.edduarte.vokter.persistence.mongodb.MongoSessionCollection;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class VokterApplication extends Application<ServerConfiguration> {

    private static final Logger logger =
            LoggerFactory.getLogger(VokterApplication.class);

    private static VokterApplication instance = new VokterApplication();

    public SessionCollection sessionCollection;

    private JobManager jobManager;


    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        options.addOption("dbh", "db-host", true, "Database host. Defaults to localhost.");

        options.addOption("dbp", "db-port", true, "Database port. Defaults to 27017.");

        options.addOption("ic", "ignore-case", false, "Flag that forces the " +
                "document to be in lower-case, so that during difference " +
                "matching every match will be case insensitive (regardless " +
                "of the user setting \"ignoreCase\" as false in his request).");

        options.addOption("sw", "stopwords", false, "Flag that sets " +
                "stopwords to be filtered when k-shingling receiving " +
                "documents on difference detection. This composes a " +
                "trade-off between losing matching of common words (user " +
                "has stopwords in his keywords which won't be matched) and " +
                "reducing the number of under-used matching jobs, triggered " +
                "by differences in these words with non-important significance. " +
                "This option is only used on shingling / LSH, and has no " +
                "effect on the user's setting \"filterStopwords\", since that " +
                "one concerns keyword matching and not detection.");

        options.addOption("h", "help", false, "Shows this help prompt.");


        CommandLine commandLine;
        try {
            // Parse the program arguments
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.error("There was a problem processing the input arguments. " +
                    "Write -h or --help to show the list of available commands.");
            logger.error(ex.getMessage(), ex);
            return;
        }


        if (commandLine.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar vokter-core.jar", options);
            return;
        }

        String dbHost = "localhost";
        if (commandLine.hasOption("dbh")) {
            dbHost = commandLine.getOptionValue("dbh");
        }

        int dbPort = 27017;
        if (commandLine.hasOption("dbp")) {
            String portString = commandLine.getOptionValue("dbp");
            dbPort = Integer.parseInt(portString);
        }

        boolean isIgnoringCase = true;
        if (commandLine.hasOption("ic")) {
            isIgnoringCase = false;
        }

        boolean isStoppingEnabled = false;
        if (commandLine.hasOption("sw")) {
            isStoppingEnabled = true;
        }

        // A parser-pool that contains a set number of parsers. When the last
        // parser from the pool is removed, future parsing workers will be
        // locked until a used parser is placed back in the pool. This limits
        // number of simultaneous tasks, so the number of parsers available
        // should correspond to the number of available threads.
        ParserPool parserPool = new ParserPool();

        // The client for the used MongoDB database.
        MongoClient mongoClient = new MongoClient(dbHost, dbPort);

        // the db used by vokter
        DB db = mongoClient.getDB("vokter");

        // the bayesian detection model that allows language detection.
        List<LanguageProfile> languageProfiles =
                new LanguageProfileReader().readAllBuiltIn();
        LanguageDetector langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();

        // the collection instances that will contain all persistence data
        DocumentCollection documentCollection = new MongoDocumentCollection(db);
        DiffCollection diffCollection = new MongoDiffCollection(db);
        SessionCollection sessionCollection = new MongoSessionCollection(db);

        logger.info("Starting parsers...");
        for (int i = 1; i < Constants.MAX_THREADS; i++) {
            Parser p = new SimpleParser();
            parserPool.place(p);
        }

        logger.info("Starting jobs...");

        // a manager for all Quartz jobs, handling scheduling and persistence of
        // asynchronous processes for document differences detection and
        // keyword-difference matching.
        JobManager jobManager = JobManager.create(
                "vokter_job_manager",
                documentCollection,
                diffCollection,
                sessionCollection,
                parserPool,
                langDetector,
                isIgnoringCase,
                isStoppingEnabled,
                new RestNotificationHandler()
        );
        jobManager.initialize();

        instance.jobManager = jobManager;
        instance.sessionCollection = sessionCollection;

        instance.run(args);
    }


    public static VokterApplication getInstance() {
        return instance;
    }


    @Override
    public String getName() {
        return "vokter";
    }


    @Override
    public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
        // nothing to do yet
    }


    @Override
    public void run(ServerConfiguration configuration, Environment environment) {
        // nothing to do yet
    }


    public Session createJob(
            String documentUrl, String documentContentType,
            String clientUrl, String clientContentType,
            List<String> keywords, List<DiffEvent> events,
            boolean filterStopwords, boolean enableStemming, boolean ignoreCase,
            int snippetOffset, int interval) {
        return jobManager.createJob(
                documentUrl, documentContentType,
                clientUrl, clientContentType,
                keywords, events,
                filterStopwords, enableStemming, ignoreCase,
                snippetOffset, interval
        );
    }


    public boolean cancelJob(String documentUrl, String documentContentType,
                             String clientUrl, String clientContentType) {
        return jobManager.cancelJob(
                documentUrl, documentContentType,
                clientUrl, clientContentType
        );
    }


    public Session validateToken(String clientUrl, String clientContentType, String token) {
        return sessionCollection.validateToken(clientUrl, clientContentType, token);
    }
}