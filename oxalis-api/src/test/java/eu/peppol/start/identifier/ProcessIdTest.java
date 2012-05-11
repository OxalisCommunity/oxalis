/* Created by steinar on 11.05.12 at 15:45 */
package eu.peppol.start.identifier;

import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class ProcessIdTest {

    @Test(expectedExceptions = IllegalStateException.class )
    public void unknownProcessId() {
        ProcessId processId = ProcessId.valueFor("jallah");
    }

    @Test(expectedExceptions = IllegalStateException.class )
    public void nullForProcessId() {
        ProcessId processId = ProcessId.valueFor(null);
    }
}
