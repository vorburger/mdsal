/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.RpcComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.RpcComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class AugmentationSubstitutionTest extends AbstractBindingCodecTest {

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY = BA_TOP_LEVEL_LIST
            .augmentation(TreeLeafOnlyAugment.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> BA_TREE_COMPLEX_USES = BA_TOP_LEVEL_LIST
            .augmentation(TreeComplexUsesAugment.class);
    private static final QName SIMPLE_VALUE_QNAME = QName.create(TreeComplexUsesAugment.QNAME, "simple-value");

    @Test
    public void augmentationInGroupingSubstituted() {
        final TopLevelList baRpc = new TopLevelListBuilder()
            .withKey(TOP_FOO_KEY)
            .addAugmentation(RpcComplexUsesAugment.class, new RpcComplexUsesAugmentBuilder(createComplexData()).build())
            .build();
        final TopLevelList baTree = new TopLevelListBuilder()
            .withKey(TOP_FOO_KEY)
            .addAugmentation(TreeComplexUsesAugment.class, new TreeComplexUsesAugmentBuilder(createComplexData())
                .build())
            .build();
        final NormalizedNode<?, ?> domTreeEntry = registry.toNormalizedNode(BA_TOP_LEVEL_LIST, baTree).getValue();
        final NormalizedNode<?, ?> domRpcEntry = registry.toNormalizedNode(BA_TOP_LEVEL_LIST, baRpc).getValue();
        assertEquals(domTreeEntry, domRpcEntry);
    }

    @Test
    public void copyBuilderWithAugmenationsTest() {
        final TopLevelList manuallyConstructed = new TopLevelListBuilder()
            .withKey(TOP_FOO_KEY)
            .addAugmentation(TreeComplexUsesAugment.class, new TreeComplexUsesAugmentBuilder(createComplexData())
                .build())
            .build();
        final NormalizedNode<?, ?> domTreeEntry = registry.toNormalizedNode(BA_TOP_LEVEL_LIST, manuallyConstructed)
                .getValue();
        final TopLevelList deserialized = registry.deserializeFunction(BA_TOP_LEVEL_LIST)
                .apply(Optional.of(domTreeEntry)).get();
        assertEquals(manuallyConstructed, deserialized);
        final TopLevelList copiedFromDeserialized = new TopLevelListBuilder(deserialized).build();
        assertEquals(manuallyConstructed, copiedFromDeserialized);
    }

    private static RpcComplexUsesAugment createComplexData() {
        return new RpcComplexUsesAugmentBuilder()
                .setContainerWithUses(new ContainerWithUsesBuilder()
                    .setLeafFromGrouping("foo")
                    .build())
                .setListViaUses(Collections.emptyList())
                .build();
    }
}
