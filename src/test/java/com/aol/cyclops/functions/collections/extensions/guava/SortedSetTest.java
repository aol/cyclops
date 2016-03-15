package com.aol.cyclops.functions.collections.extensions.guava;

import  junit.framework.Test;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

import junit.framework.TestSuite;

public class SortedSetTest {
    
    public static Test suite() {
        return new SortedSetTest().allTests();
    }
 
    public TestSuite allTests() {
        TestSuite suite =
            new TestSuite("com.aol.cyclops.functions.collections.extensions.guava");
        suite.addTest(testForOneToWayUseMySet());
        
        return suite;
    }
    public Test testForOneToWayUseMySet() {
        return SetTestSuiteBuilder
                .using(new SetXGenerator())
                .named("setX test")
                .withFeatures(
                        CollectionSize.ANY,
                        CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SUPPORTS_ADD,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.SUPPORTS_REMOVE
                )
                .createTestSuite();
    }
    
}