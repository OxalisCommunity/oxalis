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
package eu.peppol.util;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;

/**
 * Created by soc on 07.12.2015.
 */
public class GuiceTestModuleFactory implements IModuleFactory {

    Logger log = LoggerFactory.getLogger(GuiceTestModuleFactory.class);

    @Override
    public Module createModule(ITestContext iTestContext, Class<?> aClass) {
        log.debug("Inspecting " + aClass.getSimpleName());
        Annotation[] annotations = aClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Test) {
                Test testAnnotation = (Test) annotation;
                log.debug(testAnnotation.toString());
            }
            if (annotation instanceof Guice) {
                Guice guiceAnnotation = (Guice) annotation;
            }
        }


        return null;
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            Binder binder = binder();

        }
    }
}
