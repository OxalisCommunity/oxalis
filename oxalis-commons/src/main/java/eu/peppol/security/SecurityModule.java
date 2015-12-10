/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.security;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * Google guice module holding various components related to keys, certificates etc.
 *
 * NOTE! Any unit test utilizing this module with the @Guice(modules = {SecurityModule.class})
 * will fail when executing unit tests with SureFire (maven) if there are any problems related to loading
 * the keystore.
 *
 * @author steinar
 *         Date: 17.11.2015
 *         Time: 18.50
 */
public class SecurityModule implements Module {

    @Override
    public void configure(Binder binder) {

        binder.bind(KeystoreManager.class).to(KeystoreManagerImpl.class).in(Singleton.class);
    }


}

