package pt.ua.adis.argus;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import pt.ua.adis.argus.web.server.Server;


/**
 * Server version of the main application.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Context.installUncaughtExceptionHandler();

        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        options.addOption("port", "port", true, "Server port.");

        options.addOption("t", "threads", true,
                "Number of threads. By default, if more than one core is " +
                        "available, it is the number of cores minus 1.");

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
        int numThreads = Runtime.getRuntime().availableProcessors() - 1;
        numThreads = numThreads > 0 ? numThreads : 1;
        if (commandLine.hasOption('t')) {
            String threadsText = commandLine.getOptionValue('t');
            numThreads = Integer.parseInt(threadsText);
            if (numThreads <= 0 || numThreads > 32) {
                logger.error("Illegal number of threads. " +
                        "Must be between 1 and 32.");
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

        try {
            Server server = Server.getInstance();
            server.initialize(port, numThreads);

            server.start();
            logger.info("Server started at localhost:" + port);
            logger.info("Press Cmd-C / Ctrl+C to shutdown the server...");
            server.getServer().await();

        } catch (Exception ex) {
            logger.info("Shutting down the server...");

        }
    }
}

