package com.aol.cyclops.functions.collections.extensions.guava;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;

import  junit.framework.Test;
import junit.framework.TestSuite;

public class ListXTest {
    
    public static Test suite() {
        return new ListXTest().allTests();
    }
 
    public TestSuite allTests() {
        TestSuite suite =
            new TestSuite("com.aol.cyclops.functions.collections.extensions.guava");
        suite.addTest(testForOneToWayUseMySet());
        
        return suite;
    }
    public Test testForOneToWayUseMySet() {
        return ListTestSuiteBuilder
                .using(new ListXGenerator())
                .named("listX test")
                .withFeatures(
                        CollectionSize.ANY,
                        CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SUPPORTS_ADD,
                        ListFeature.SUPPORTS_SET,
                        CollectionFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.SUPPORTS_REMOVE
                )
                .createTestSuite();
    }
    
}