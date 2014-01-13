package eu.peppol.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.naming.InitialContext;

/**
 * Represents the Oxalis Home directory, which is located by inspecting various places in the file system in this
 * order:
 * <ol>
 * <li>Directory referenced by environment variable <code>OXALIS_HOME</code></li>
 * <li><code>.oxalis</code> directory inside the home directory of the user</li>
 * </ol>
 *
 * User: steinar
 * Date: 08.02.13
 * Time: 09:56
 */
class OxalisHomeDirectory {

    public static final Logger log = LoggerFactory.getLogger(OxalisHomeDirectory.class);
    static final String OXALIS_HOME_VAR_NAME = "OXALIS_HOME";

    public File locateDirectory() {

        log.debug("Attempting to locate home dir....");
        File oxalisHomeDir = null;

        if ((oxalisHomeDir = locateOxalisHomeFromEnvironmentVariable()) == null) {
            oxalisHomeDir = locateOxalisHomeDirRelativeToUserHome();
        }
        if (oxalisHomeDir == null) {
            log.error("OXALIS_HOME directory not located, this is going to cause you trouble!");
        }

        return oxalisHomeDir;
    }

    File locateOxalisHomeFromEnvironmentVariable() {
        File result = null;

        String oxalis_home = System.getenv(OXALIS_HOME_VAR_NAME);
        if (oxalis_home != null && oxalis_home.length() > 0) {
            result = new File(oxalis_home);
        }
        
        if (oxalis_home == null) {
	        try {
	        	oxalis_home = (String) new InitialContext().lookup( "java:comp/env/" + OXALIS_HOME_VAR_NAME);
	        } catch (Exception e) {
	        	log.warn(e.getMessage());
	        }
        }

        return result;
    }

    File locateOxalisHomeDirRelativeToUserHome() {

        File oxalisHomeDir = null;

        oxalisHomeDir = computeOxalisHomeRelativeToUserHome();

        if (!oxalisHomeDir.exists()) {
                throw new IllegalStateException(oxalisHomeDir + " does not exist!");
        } else if (!oxalisHomeDir.isDirectory()) {
                throw new IllegalStateException(oxalisHomeDir + " is not a directory");
        } else if (!oxalisHomeDir.canRead()) {
                throw new IllegalStateException(oxalisHomeDir + " exists, is a directory but can not be read");
        }

        return oxalisHomeDir;
    }

    File computeOxalisHomeRelativeToUserHome() {
        String userHome = System.getProperty("user.home");
        File userHomeDir = new File(userHome);

        File oxalisHomeDir;
        if (userHomeDir.isDirectory()) {
            oxalisHomeDir = new File(userHomeDir, "/.oxalis");
        } else {
            throw new IllegalStateException(userHome + " is not a directory");
        }
        return oxalisHomeDir;
    }
}