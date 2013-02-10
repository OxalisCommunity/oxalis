package eu.peppol.statistics;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Objects implementing this interface are capable of storing and retrieving raw data pertaining to the sending and
 * receiving of PEPPOL messages in order to provide statistics.
 * <p/>
 * User: steinar
 * Date: 30.01.13
 * Time: 19:28
 */
public interface StatisticsRepository {

    Integer persist(RawStatistics rawStatistics);
}
