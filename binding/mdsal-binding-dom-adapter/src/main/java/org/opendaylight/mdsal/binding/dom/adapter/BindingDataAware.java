/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Migration trait for exposing the Binding data object.
 *
 * @author Robert Varga
 */
@Deprecated
public interface BindingDataAware {

    DataObject bindingData();
}
