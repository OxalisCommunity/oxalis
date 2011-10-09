package eu.peppol.start.util;

import org.testng.annotations.BeforeClass;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 9:12:11 AM
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
