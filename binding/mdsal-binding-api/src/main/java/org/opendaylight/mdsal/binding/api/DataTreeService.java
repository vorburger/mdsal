/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * A {@link BindingService} providing access to the conceptual data tree. Interactions with the data
 * tree are split into data producers and consumers (listeners). Each of them operate on a set of
 * subtrees, which need to be declared at instantiation time.
 *
 *<p>
 * Returned instances are not thread-safe and expected to be used by a single thread at a time.
 * Furthermore, producers may not be accessed from consumer callbacks unless they were specified
 * when the listener is registered.
 *
 *<p>
 * The service maintains a loop-free topology of producers and consumers. What this means is that a
 * consumer is not allowed to access a producer, which affects any of the subtrees it is subscribed
 * to. This restriction is in place to ensure the system does not go into a feedback loop, where it
 * is impossible to block either a producer or a consumer without accumulating excess work in the
 * backlog stemming from its previous activity.
 */
public interface DataTreeService extends DataTreeProducerFactory, BindingService {
    /**
     * Register a {@link DataTreeListener} instance. Once registered, the listener will start
     * receiving changes on the selected subtrees. If the listener cannot keep up with the rate of
     * changes, and allowRxMerges is set to true, this service is free to merge the changes, so that
     * a smaller number of them will be reported, possibly hiding some data transitions (like
     * flaps).
     *
     *<p>
     * If the listener wants to write into any producer, that producer has to be mentioned in the
     * call to this method. Those producers will be bound exclusively to the registration, so that
     * accessing them outside of this listener's callback will trigger an error. Any producers
     * mentioned must be idle, e.g. they may not have an open transaction at the time this method is
     * invoked.
     *
     *<p>
     * Each listener instance can be registered at most once. Implementations of this interface have
     * to guarantee that the listener's methods will not be invoked concurrently from multiple
     * threads.
     *
     * @param listener {@link DataTreeListener} that is being registered
     * @param subtrees Conceptual subtree identifier of subtrees which should be monitored for
     *        changes. May not be null or empty.
     * @param allowRxMerges True if the backend may perform ingress state compression.
     * @param producers {@link DataTreeProducer} instances to bind to the listener.
     * @return A listener registration. Once closed, the listener will no longer be invoked and the
     *         producers will be unbound.
     * @throws IllegalArgumentException if subtrees is empty or the listener is already bound
     * @throws DataTreeLoopException if the registration of the listener to the specified subtrees
     *         with specified producers would form a feedback loop
     */
    <T extends DataTreeListener> @NonNull ListenerRegistration<T> registerListener(@NonNull T listener,
            @NonNull Collection<DataTreeIdentifier<?>> subtrees, boolean allowRxMerges,
            @NonNull Collection<DataTreeProducer> producers) throws DataTreeLoopException;
}
