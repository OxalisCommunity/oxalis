/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.sendregning.oxalis;

import eu.peppol.persistence.MessageRepository;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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

        InputStream is = urlClassLoader.getResourceAsStream("META-INF/services/eu.peppol.persistence.MessageRepository");
        assertNotNull(is);

        ServiceLoader<MessageRepository> messageRepositoryServiceLoader = ServiceLoader.load(MessageRepository.class, urlClassLoader);
        for (MessageRepository repository : messageRepositoryServiceLoader) {
//            repository.saveOutBoundMessage("x", new PeppolMessageHeader(), (Document)null);
        }
    }
}
