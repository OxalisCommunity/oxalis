package eu.peppol.start.identifier;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * @author $Author$ (of last change)
 *         Created by
 *         User: steinar
 *         Date: 29.11.11
 *         Time: 14:26
 */
public class IdentifierNameTest {
    @Test
    public void testValueOfIdentifier() throws Exception {

        for (IdentifierName id : IdentifierName.values()) {

            IdentifierName id2 = IdentifierName.valueOfIdentifier(id.stringValue());
            assertTrue(id2 == id, "Unknown identifier " + id.name());
        }
    }
}
