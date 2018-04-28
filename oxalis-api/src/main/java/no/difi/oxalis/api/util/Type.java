package no.difi.oxalis.api.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation works as a replacement of {@link javax.inject.Named} to allow for multiple
 * names for a given implementation. Used in combination with {@link no.difi.oxalis.commons.guice.OxalisModule}.
 *
 * @author erlend
 * @since 4.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Type {

    String[] value();

}
