package no.sendregning.oxalis;

/**
 * User: ravnholt
 */
public class TestStandAloneWSClient {

    public static void main(String[] args) throws Exception {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        new TestDaemon().run();
    }
}
