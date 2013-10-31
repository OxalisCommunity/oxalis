package eu.peppol;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 18:06
 */
public class BusDoxProtocolTest {
    @Test
    public void testInstanceFrom() throws Exception {
        BusDoxProtocol busDoxProtocol = BusDoxProtocol.instanceFrom("busdox-transport-start");
        assertEquals(busDoxProtocol, BusDoxProtocol.START);
    }
}
