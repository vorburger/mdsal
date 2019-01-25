/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring;

import com.google.inject.Provides;
import javax.inject.Singleton;
import org.opendaylight.infrautils.inject.guice.AutoWiringModule;
import org.opendaylight.infrautils.inject.guice.GuiceClassPathBinder;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;

/**
 * Guice Module which binds the mdsal (not controller) {@link DataBroker} & Co.
 * in-memory implementation.
 *
 * @author Michael Vorburger.ch
 */
public class MdsalModule extends AutoWiringModule {

    public MdsalModule(GuiceClassPathBinder classPathBinder) {
        super(classPathBinder, "org.opendaylight.mdsal");
    }

    @Provides
    @Singleton DOMSchemaService getSchemaService(MdsalWiring mdsalWiring) {
        return mdsalWiring.getDOMSchemaService();
    }

    @Provides
    @Singleton ClassLoadingStrategy getClassLoadingStrategy(MdsalWiring mdsalWiring) {
        return mdsalWiring.getClassLoadingStrategy();
    }
}
