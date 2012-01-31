package eu.sendregning.oxalis;

import eu.peppol.start.identifier.PeppolMessageHeader;
import eu.peppol.start.persistence.MessageRepository;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import static org.testng.Assert.*;

/**
 * Experiment to determine whether it is possible to load an external service in a separate class loader.
 *
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 28.12.11
 *         Time: 13:47
 */
public class ClassLoaderTest {

    @Test(enabled = false)
    public void loadClass() throws MalformedURLException {

        File jarfile = new File("/Users/steinar/src/sr-peppol/oxalis-persistence/target/oxalis-persistence-1.0-SNAPSHOT.jar");

        assertTrue(jarfile.exists(),"Jar file does not exist " + jarfile.toString());
        URL[] urls = { jarfile.toURI().toURL()};

        URLClassLoader urlClassLoader = new URLClassLoader(urls, ClassLoaderTest.class.getClassLoader());

        InputStream is = urlClassLoader.getResourceAsStream("META-INF/services/eu.peppol.start.persistence.MessageRepository");
        assertNotNull(is);

        ServiceLoader<MessageRepository> messageRepositoryServiceLoader = ServiceLoader.load(MessageRepository.class, urlClassLoader);
        for (MessageRepository repository : messageRepositoryServiceLoader) {
//            repository.saveOutBoundMessage("x", new PeppolMessageHeader(), (Document)null);
        }
    }
}
