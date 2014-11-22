package argus;

import argus.rest.Context;
import argus.util.StopwordFileLoader;
import argus.util.Util;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileDeleteStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Set;


/**
 * Server version of the main application.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 * @since 1.0
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        installUncaughtExceptionHandler();

        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        Option o;

        if (args.length == 0) {
            String usage = "[-port] [-t] -c <corpus> [-nocase] [-stop] [-stop_file] [-stem] [-ev] [-ev_noslop] [-ev_file]";
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar assignment01.jar " + usage, options);
            return;
        }

        options.addOption("t", "threads", true, "Number of max threads to be used "
                + "for computation and indexing processes. By default, this will "
                + "be the number of available cores.");

        options.addOption("port", "port", true, "Server port.");

        options.addOption("nocase", "ignore-case", false, "Ignores differentiation "
                + "between equal words with different casing.");

        options.addOption("stop", "stopwords", false, "Perform stopword filtering.");

        o = new Option("stop_file", "stopwords file", false, "Loads a custom "
                + "stopword file (instead of using the default one). This is "
                + "only used if the 'stop' argument was called.");
        o.setArgs(1);
        options.addOption(o);

        options.addOption("stem", "stemming", false, "Perform stemming.");

        o = new Option("c", "corpus", true, "The corpus to be processed");
        o.setArgs(1);
        options.addOption(o);

        CommandLine commandLine;
        try {
            // Parse the program arguments
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.error("There was a problem processing the input arguments.");
            logger.error(ex.getMessage(), ex);
            return;
        }

        // Get threads
        int maxThreads = Runtime.getRuntime().availableProcessors() - 1;
        maxThreads = maxThreads > 0 ? maxThreads : 1;
        if (commandLine.hasOption('t')) {
            String threadsText = commandLine.getOptionValue('t');
            maxThreads = Integer.parseInt(threadsText);
            if (maxThreads <= 0 || maxThreads > 32) {
                logger.error("Invalid number of threads. Must be a number between 1 and 32.");
                return;
            }
        }

        // Get port
        int port = 8080;
        if (commandLine.hasOption("port")) {
            String portString = commandLine.getOptionValue("port");
            port = Integer.parseInt(portString);
        }

        // Set JSP to always use Standard JavaC
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");


        boolean isStoppingEnabled = false;
        boolean isStemmingEnabled = false;
        boolean isIgnoringCase = false;
        boolean isIgnoringSlops = false;

        if (commandLine.hasOption("stop")) {
            isStoppingEnabled = true;
        }

        if (commandLine.hasOption("stem")) {
            isStemmingEnabled = true;
        }

        if (commandLine.hasOption("nocase")) {
            isIgnoringCase = true;
        }


        String corpusDir = null;
        if (commandLine.hasOption("c")) {
            String dirPath = commandLine.getOptionValue("c");

            File f = new File(dirPath);
            if (f.exists() && f.isDirectory()) {
                corpusDir = dirPath;
            }
        }

        if (corpusDir == null) {
            logger.info("Please specify the corpus parent folder to be indexed.");
            return;
        }


        File stopwordFile = null;
        if (commandLine.hasOption("stop_file")) {
            String dirPath = commandLine.getOptionValue("stop_file");

            File f = new File(dirPath);
            if (f.exists() && f.isDirectory()) {
                stopwordFile = f;
            }
        }
        InputStream stopwordsFileStream;
        if (stopwordFile != null) {
            // loads the specified stopwords file.
            try {
                stopwordsFileStream = new FileInputStream(stopwordFile);
            } catch (FileNotFoundException ex) {
                logger.error(ex.getMessage(), ex);
                return;
            }
        } else {
            // loads the default stopwords file.
            stopwordsFileStream = Main.class.getResourceAsStream("stopwords.txt");
        }
        StopwordFileLoader stopLoader = new StopwordFileLoader();
        Set<MutableString> loadedStopwords = stopLoader.load(stopwordsFileStream);
        stopLoader = null;


        File indexCacheFolder = new File(Util.INSTALL_DIR, "index");
        if (indexCacheFolder.exists()) {
            try {
                FileDeleteStrategy.FORCE.delete(indexCacheFolder);
            } catch (IOException ex) {
                indexCacheFolder.deleteOnExit();
            }
        }
        indexCacheFolder.mkdirs();


        File documentsCacheFolder = new File(Util.INSTALL_DIR, "documents");
        if (documentsCacheFolder.exists()) {
            try {
                FileDeleteStrategy.FORCE.delete(documentsCacheFolder);
            } catch (IOException ex) {
                documentsCacheFolder.deleteOnExit();
            }
        }
        documentsCacheFolder.mkdirs();



            try {
                Context context = Context.getInstance();
                context.start(port, maxThreads, loadedStopwords);
                context.setStopwordsEnabled(isStoppingEnabled);
                context.setStemmingEnabled(isStemmingEnabled);
                context.setIgnoreCase(isIgnoringCase);

                context.createCollectionFromDir(corpusDir, indexCacheFolder, documentsCacheFolder);

                context.join();

            } catch (Exception ex) {
                ex.printStackTrace();
                logger.info("Shutting down the server...");
                System.exit(1);
            }
    }


    private static void installUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e instanceof ThreadDeath) {
                logger.warn("Ignoring uncaught ThreadDead exception.");
                return;
            }
            logger.error("Uncaught exception on cli thread, aborting.", e);
            System.exit(0);
        });
    }
}

