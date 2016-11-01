package eu.peppol.persistence;

import java.net.URI;
import java.nio.file.Path;

/**
 * @author steinar
 *         Date: 27.10.2016
 *         Time: 13.07
 */
public interface RepositoryConfiguration {

    String BASE_PATH_NAME = "BaseDir";

    /** Provides the {@link java.nio.file.Path} to the root directory of the file based message repository */
    Path getBasePath();

    URI getJdbcConnectionUri();

    String getJdbcDriverClassPath();

    String getJdbcDriverClassName();

    String getJdbcUsername();

    String getJdbcPassword();

    String getValidationQuery();
}
