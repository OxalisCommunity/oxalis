/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.commons.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.logging.Configurator;
import network.oxalis.api.settings.Settings;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * Configures the Logback logging configuration. Is triggered only in the case of explicit override.
 *
 * @author steinar
 * Date: 04.10.12
 * Time: 13:43
 * @author erlend
 */
@Slf4j
@SuppressWarnings("unused")
public class LogbackConfigurator implements Configurator {

    private final Settings<LoggingConf> settings;

    private final Path confPath;

    /**
     * Simply uses the default configuration
     */
    @Inject
    public LogbackConfigurator(Settings<LoggingConf> settings, @Named("conf") Path confPath) {
        this.settings = settings;
        this.confPath = confPath;
    }

    public void execute() {
        File file = settings.getPath(LoggingConf.CONFIG, confPath).toFile();

        System.out.println("Configuring Logback with configuration: " + file.getAbsolutePath());
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        // Call context.reset() to clear any previous configuration, e.g. default
        // configuration. For multi-step configuration, omit calling context.reset().
        loggerContext.reset();
        try {
            configurator.doConfigure(file);

            // Not needed as this is the default behaviour from logback
            // StatusPrinter.print(loggerContext);
        } catch (JoranException e) {
            log.error(e.getMessage(), e);
        }

        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
    }
}
