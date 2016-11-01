package eu.peppol.persistence.guice.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates to Google Guice that the method specified should be handled within a transaction
 *
 * Only works for methods that are public, package-private or protected (https://github.com/google/guice/wiki/AOP)
 *
 * @see TransactionalMethodInterceptor
 *
 * User: andy
 * Date: 1/19/12
 * Time: 12:03 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Transactional {

}
