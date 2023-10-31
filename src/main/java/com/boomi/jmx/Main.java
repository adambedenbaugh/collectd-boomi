package com.boomi.jmx;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;


public class Main {

    static final String logFileName = "jmx-cloudwatch-boomi.log";
    // TODO: Use a library for logging to better handle log rotation.
    static final String logLevelDefault = "SEVERE";
    static int port;
    private static final Logger logger = Logger.getLogger("GLOBAL");

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption("h", "host", true, "Host to connect to");
        options.addOption("p", "port", true, "Port to connect to");
        options.addOption("l", "log-level", true, "Set Log Level");
        options.addOption("d", "log-directory", true, "Set Log Directory");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (!cmd.hasOption("h") || !cmd.hasOption("p")) {
            System.out.println("Usage: java -cp jmx-cloudwatch-boomi.jar -h <host> -p <port> [-i <interval>] [-l <log-level>] [-d <log-directory>]");
            System.exit(1);
        }

        configureLogger(cmd.getOptionValue("d"), cmd.getOptionValue("l", logLevelDefault));

        String host = cmd.getOptionValue("h");
        port = Integer.parseInt(cmd.getOptionValue("p"));
        logger.info("Connecting to host: " + host + " on port: " + port);

        try {
            JMXConnection connection = new JMXConnection(host, port);
            connection.connect();
            connection.getAtomStatus();
            connection.getQueueHealthStatus();
            connection.getRestarting();
            connection.getLowMemoryMode();
            connection.getClusterProblem();
            connection.getAtomEncounteredOOME();
            connection.getHeadNodeCloudlet();
            System.out.flush();
        } catch (Exception e) {
            logger.severe("Error connecting to JMX. Error Message: " + e);
        }
    }

    /*
        * Configure the logger to log to a file. If logDirectory is null, then use the current working directory.
     */
    private static void configureLogger(String logDirectory, String logLevel) throws IOException {
        logDirectory = logDirectory == null ? System.getProperty("user.dir") : logDirectory;
        String path = Paths.get(logDirectory, logFileName).toString();
        FileHandler fileHandler = new FileHandler(path, true);
        fileHandler.setFormatter(new LogFormatter());

        logger.addHandler(fileHandler);
        setLogLevel(logLevel);
        logger.info("Logging to: " + path);
    }

    private static void setLogLevel(String logLevel) {
        switch (logLevel) {
            case "SEVERE":
                logger.setLevel(Level.SEVERE);
                break;
            case "WARNING":
                logger.setLevel(Level.WARNING);
                break;
            case "CONFIG":
                logger.setLevel(Level.CONFIG);
                break;
            case "FINE":
                logger.setLevel(Level.FINE);
                break;
            case "FINER":
                logger.setLevel(Level.FINER);
                break;
            case "FINEST":
                logger.setLevel(Level.FINEST);
                break;
            case "INFO":
            default:
                logger.setLevel(Level.INFO);
                break;
        }
    }
}