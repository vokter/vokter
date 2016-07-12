package com.edduarte.vokter.rest;

import com.edduarte.vokter.Constants;
import com.edduarte.vokter.job.JobManager;
import com.edduarte.vokter.job.RestJobManagerListener;
import com.edduarte.vokter.parser.Parser;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.edduarte.vokter.persistence.SessionCollection;
import com.edduarte.vokter.persistence.mongodb.HttpMongoSessionCollection;
import com.edduarte.vokter.persistence.mongodb.MongoDiffCollection;
import com.edduarte.vokter.persistence.mongodb.MongoDocumentCollection;
import com.edduarte.vokter.persistence.mongodb.MongoSessionCollection;
import com.edduarte.vokter.rest.resources.CORSFilter;
import com.edduarte.vokter.rest.resources.v1.RestResource;
import com.edduarte.vokter.swagger.SwaggerBundle;
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

    private final JobManager jobManager;

    private final SessionCollection sessionCollection;


    public VokterApplication(JobManager jobManager,
                             SessionCollection sessionCollection) {
        this.jobManager = jobManager;
        this.sessionCollection = sessionCollection;
    }


    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        options.addOption("dbh", "db-host", true, "Database host. Defaults to localhost.");

        options.addOption("dbp", "db-port", true, "Database port. Defaults to 27017.");

        options.addOption("ic", "ignore-case", false, "Flag that forces the " +
                "document to be in lower-case, so that during difference " +
                "matching every match will be case insensitive (regardless " +
                "of the user setting \"ignoreCase\" as false in his request).");

        options.addOption("sw", "stopwords", false, "Flag that enables " +
                "filtering of stopwords during k-shingling of documents on " +
                "difference detection jobs. This composes a trade-off " +
                "between stopping all matching of common words that the " +
                "user might have specified as his desired keywords on " +
                "purpose, and reducing the total number of jobs triggered. " +
                "This option is only used on shingling / LSH, and has no " +
                "effect on the user's setting \"filterStopwords\", since " +
                "that one concerns keyword matching and not detection.");

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

        // the collection instance that will contain session / token
        // persistence data
        SessionCollection sessionCollection = new HttpMongoSessionCollection(db);

        for (int i = 1; i < Constants.MAX_THREADS; i++) {
            Parser p = new SimpleParser();
            parserPool.place(p);
        }

        // a manager for all Quartz jobs, handling scheduling and persistence of
        // asynchronous processes for document differences detection and
        // keyword-difference matching.
        JobManager jobManager = JobManager.create(
                "vokter_job_manager",
                parserPool,
                langDetector,
                isIgnoringCase,
                isStoppingEnabled
        ).listener(new RestJobManagerListener())
                .register(new MongoDocumentCollection(db))
                .register(new MongoDiffCollection(db))
                .register(sessionCollection)
                .initialize();

        new VokterApplication(jobManager, sessionCollection).run(args);
    }


    @Override
    public String getName() {
        return "vokter";
    }


    @Override
    public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
        bootstrap.addBundle((SwaggerBundle<ServerConfiguration>) ServerConfiguration::getConfig);
    }


    @Override
    public void run(ServerConfiguration configuration, Environment environment) {
        environment.healthChecks()
                .register("jobManager", new JobManagerHealthCheck(jobManager));
        environment.jersey()
                .register(new CORSFilter());
        environment.jersey()
                .register(new RestResource(jobManager, sessionCollection));
    }
}