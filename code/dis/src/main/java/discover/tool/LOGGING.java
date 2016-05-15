package discover.tool;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * This class just initializes the logging(Log4j).
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-09-2010
 */
public final class LOGGING {

    @SuppressWarnings("FieldCanBeLocal")
    private static Logger LOGGER;
    private static boolean INITIALISED = false;
    private static String FILE_NAME = "log4j.properties";

    /**
     * Singularity.
     */
    private LOGGING() {
        // Documented
    }

    /**
     * Configures the logging.
     */
    public static void configure() {
        InputStream inputStream = null;
        try {
            if (INITIALISED) {
                return;
            }
            INITIALISED = Boolean.TRUE;
            try {
                // First check the external logging properties file
                File log4JPropertiesFile = FILE.findFileRecursively(new File("."), FILE_NAME);
                if (log4JPropertiesFile != null && log4JPropertiesFile.exists() && log4JPropertiesFile.canRead()) {
                    inputStream = log4JPropertiesFile.toURI().toURL().openStream();
                    System.out.println("Logging configuration : " + log4JPropertiesFile.toURI().toURL());
                }
                if (inputStream == null) {
                    // Try the class loader
                    URL url = LOGGING.class.getResource(FILE_NAME);
                    if (url != null) {
                        inputStream = url.openStream();
                        System.out.println("Logging configuration : " + url);
                    } else {
                        // Nope, try the class loader on a stream
                        inputStream = LOGGING.class.getResourceAsStream(FILE_NAME);
                        if (inputStream == null) {
                            // Finally try the system class loader
                            inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(FILE_NAME);
                            System.out.println("Logging configuration : System class loader.");
                        }
                    }
                }
                if (inputStream != null) {
                    Properties properties = new Properties();
                    properties.load(inputStream);
                    PropertyConfigurator.configure(properties);
                } else {
                    System.err.println("Logging properties file not found : " + FILE_NAME);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            LOGGER = Logger.getLogger(LOGGING.class);
        } finally {
            FILE.close(inputStream);
        }
    }

}