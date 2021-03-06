/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;


/**
 * The DataBrokerImpl simply defers to the DOMDataBroker for all its operations.
 * All transactions and listener registrations are wrapped by the DataBrokerImpl
 * to allow binding aware components to use the DataBroker transparently.
 *
 * <p>
 * Besides this the DataBrokerImpl and it's collaborators also cache data that
 * is already transformed from the binding independent to binding aware format
 *
 */
public class BindingDOMDataBrokerAdapter extends AbstractForwardedDataBroker implements
        DataBroker, DataTreeChangeService {


    static final Factory<DataBroker> BUILDER_FACTORY = Builder::new;
    private final DataTreeChangeService treeChangeService;

    public BindingDOMDataBrokerAdapter(final DOMDataBroker domDataBroker, final BindingToNormalizedNodeCodec codec) {
        super(domDataBroker, codec);
        final DOMDataTreeChangeService domTreeChange = domDataBroker.getExtensions()
                .getInstance(DOMDataTreeChangeService.class);
        if (domTreeChange != null) {
            treeChangeService = BindingDOMDataTreeChangeServiceAdapter.create(codec, domTreeChange);
        } else {
            treeChangeService = null;
        }
    }

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        return new BindingDOMReadTransactionAdapter(getDelegate().newReadOnlyTransaction(), getCodec());
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return new BindingDOMWriteTransactionAdapter<>(getDelegate().newWriteOnlyTransaction(), getCodec());
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return new BindingDOMReadWriteTransactionAdapter(getDelegate().newReadWriteTransaction(), getCodec());
    }

    @Override
    public TransactionChain createTransactionChain(final TransactionChainListener listener) {
        return new BindingDOMTransactionChainAdapter(getDelegate(), getCodec(), listener);
    }

    private static class Builder extends BindingDOMAdapterBuilder<DataBroker> {

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMDataBroker.class);
        }

        @Override
        protected DataBroker createInstance(final BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMDataBroker domDataBroker = delegates.getInstance(DOMDataBroker.class);
            return new BindingDOMDataBrokerAdapter(domDataBroker, codec);
        }

    }

    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
            registerDataTreeChangeListener(
            final DataTreeIdentifier<T> treeId, final L listener) {
        if (treeChangeService == null) {
            throw new UnsupportedOperationException("Underlying data broker does not expose DOMDataTreeChangeService.");
        }
        return treeChangeService.registerDataTreeChangeListener(treeId, listener);
    }
}
