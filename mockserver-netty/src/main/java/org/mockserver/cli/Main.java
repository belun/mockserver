package org.mockserver.cli;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import org.mockserver.Line;
import org.mockserver.logging.Logging;
import org.mockserver.mockserver.MockServerBuilder;
import org.mockserver.proxy.ProxyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author jamesdbloom
 */
public class Main {
    public static final String PROXY_PORT_KEY = "proxyPort";
    public static final String SERVER_PORT_KEY = "serverPort";
    public static final String USAGE = "" +
            "   java -jar <path to mockserver-jetty-jar-with-dependencies.jar> [-serverPort <port>] [-proxyPort <port>]" + Line.SEPARATOR +
            "   " + Line.SEPARATOR +
            "     valid options are:" + Line.SEPARATOR +
            "        -serverPort <port>           specifies the HTTP and HTTPS port for the         " + Line.SEPARATOR +
            "                                     MockServer port unification is used to            " + Line.SEPARATOR +
            "                                     support HTTP and HTTPS on the same port           " + Line.SEPARATOR +
            "                                                                                       " + Line.SEPARATOR +
            "        -proxyPort <path>            specifies the HTTP, HTTPS, SOCKS and HTTP         " + Line.SEPARATOR +
            "                                     CONNECT port for proxy, port unification          " + Line.SEPARATOR +
            "                                     supports for all protocols on the same port       " + Line.SEPARATOR;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    @VisibleForTesting
    static ProxyBuilder proxyBuilder = new ProxyBuilder();
    @VisibleForTesting
    static MockServerBuilder mockServerBuilder = new MockServerBuilder();
    @VisibleForTesting
    static PrintStream outputPrintStream = System.out;
    @VisibleForTesting
    static boolean shutdownOnUsage = true;
    private static boolean usagePrinted = false;

    /**
     * Run the MockServer directly providing the parseArguments for the server and proxyBuilder as the only input parameters (if not provided the server port defaults to 8080 and the proxyBuilder is not started).
     *
     * @param arguments the entries are in pairs:
     *                  - the first  pair is "-serverPort" followed by the server port if not provided the MockServer is not started,
     *                  - the second pair is "-proxyPort"  followed by the proxyBuilder  port if not provided the proxyBuilder      is not started
     */
    public static void main(String... arguments) {
        usagePrinted = false;

        Map<String, Integer> parseIntegerArguments = new HashMap<String, Integer>();

        parseArguments(parseIntegerArguments, arguments);

        if (logger.isDebugEnabled()) {
            logger.debug(Line.SEPARATOR + Line.SEPARATOR + "Using command line options: " +
                    Joiner.on(", ").withKeyValueSeparator("=").join(parseIntegerArguments) + Line.SEPARATOR);
        }
        Logging.overrideLogLevel(System.getProperty("mockserver.logLevel"));

        if (parseIntegerArguments.size() > 0) {
            if (parseIntegerArguments.containsKey(PROXY_PORT_KEY)) {
                proxyBuilder.withLocalPort(parseIntegerArguments.get(PROXY_PORT_KEY)).build();
            }
            if (parseIntegerArguments.containsKey(SERVER_PORT_KEY)) {
                mockServerBuilder.withHTTPPort(parseIntegerArguments.get(SERVER_PORT_KEY)).build();
            }
        } else {
            showUsage();
        }
    }

    private static Map<String, Integer> parseArguments(Map<String, Integer> parsedIntegerArguments, String... arguments) {
        Iterator<String> argumentsIterator = Arrays.asList(arguments).iterator();
        while (argumentsIterator.hasNext()) {
            String argumentName = argumentsIterator.next();
            if (argumentsIterator.hasNext()) {
                String argumentValue = argumentsIterator.next();
                if (!parsePort(parsedIntegerArguments, SERVER_PORT_KEY, argumentName, argumentValue)
                        && !parsePort(parsedIntegerArguments, PROXY_PORT_KEY, argumentName, argumentValue)) {
                    showUsage();
                }
            } else {
                showUsage();
            }
        }
        return parsedIntegerArguments;
    }

    private static boolean parsePort(Map<String, Integer> parsedArguments, final String key, final String argumentName, final String argumentValue) {
        if (argumentName.equals("-" + key)) {
            try {
                parsedArguments.put(key, Integer.parseInt(argumentValue));
                return true;
            } catch (NumberFormatException nfe) {
                logger.error("Please provide a value integer for -" + key + ", [" + argumentValue + "] is not a valid integer", nfe);
            }
        }
        return false;
    }

    private static void showUsage() {
        if (!usagePrinted) {
            outputPrintStream.println(USAGE);
            if (shutdownOnUsage) {
                System.exit(1);
            }
            usagePrinted = true;
        }
    }

}
