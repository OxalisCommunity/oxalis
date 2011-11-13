package eu.peppol.inbound.util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 12:45:40 PM
 */
public class TestBase {

    @BeforeClass
    public void beforeTestBaseClass() {
        System.out.println("___________________________________________ " + getClass().getName());
    }

    public static void signal(Throwable t) throws Throwable {
        StackTraceElement[] stackTrace = t.getStackTrace();

        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!stackTraceElement.getClassName().startsWith("org.testng")) {
                System.out.println("");
                System.out.println("     *** " + t + " at " + stackTraceElement);
                System.out.println("");
                break;
            }
        }

        throw t;
    }
}
