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

package network.oxalis.persistence.guice;

import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.persistence.annotation.Repository;
import network.oxalis.persistence.annotation.Transactional;
import network.oxalis.persistence.aop.JdbcTxManagerImpl;
import network.oxalis.persistence.aop.RepositoryConnectionMethodInterceptor;
import network.oxalis.persistence.aop.TransactionalMethodInterceptor;
import network.oxalis.persistence.api.JdbcTxManager;

/**
 * @author andy
 * @author thore
 */
public class AopJdbcTxManagerModule extends OxalisModule {

    @Override
    protected void configure() {

        // The tx Manager needs to be a singleton so that the same reference to the threadlocal containing the
        // connection is shared amongst repositories etc.
        bind(JdbcTxManager.class)
                .to(JdbcTxManagerImpl.class)
                .in(Singleton.class);

        //creates the transactional method interceptor which handles methods annotated with @Transactional
        final TransactionalMethodInterceptor transactionalMethodInterceptor = new TransactionalMethodInterceptor();

        // Creates the  method interceptor which handles methods annotated with @Repository
        final RepositoryConnectionMethodInterceptor repositoryConnectionMethodInterceptor =
                new RepositoryConnectionMethodInterceptor();

        //injects the JdbcTxManager into the method interceptor for methods annotated with @Transactional.
        requestInjection(transactionalMethodInterceptor);

        //injects the JdbcTxManager into the method interceptor for methods annotated with @Repository
        requestInjection(repositoryConnectionMethodInterceptor);

        //makes individual methods in a class transactional.
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionalMethodInterceptor);

        //makes all methods in the class transactional
        bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(),
                transactionalMethodInterceptor);
        bindInterceptor(Matchers.annotatedWith(Repository.class), Matchers.any(),
                repositoryConnectionMethodInterceptor);
    }
}
