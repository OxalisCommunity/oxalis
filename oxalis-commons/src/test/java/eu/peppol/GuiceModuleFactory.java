package eu.peppol;

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
public class GuiceModuleFactory implements IModuleFactory {

    Logger log = LoggerFactory.getLogger(GuiceModuleFactory.class);

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
}
