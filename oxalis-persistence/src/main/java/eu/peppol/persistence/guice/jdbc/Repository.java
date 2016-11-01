package eu.peppol.persistence.guice.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates to Google Guice where that the java class is a Repository.
 * This enables us to automatically fetch the connection before a method is invoked.
 *
 * Only works for methods that are public, package-private or protected (https://github.com/google/guice/wiki/AOP)
 *
 * @see RepositoryConnectionMethodInterceptor
 *
 * User: andy
 * Date: 1/19/12
 * Time: 12:03 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Repository {

}
