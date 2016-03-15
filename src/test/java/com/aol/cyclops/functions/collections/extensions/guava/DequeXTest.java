package com.aol.cyclops.functions.collections.extensions.guava;

import com.google.common.collect.testing.QueueTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

import  junit.framework.Test;
import junit.framework.TestSuite;

public class DequeXTest {
    
    public static Test suite() {
        return new DequeXTest().allTests();
    }
 
    public TestSuite allTests() {
        TestSuite suite =
            new TestSuite("com.aol.cyclops.functions.collections.extensions.guava");
        suite.addTest(testForOneToWayUseMySet());
        
        return suite;
    }
    public Test testForOneToWayUseMySet() {
        return QueueTestSuiteBuilder
                .using(new DequeXGenerator())
                .named("DequeX test")
                .withFeatures(
                        CollectionSize.ANY,
                      //  CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SUPPORTS_ADD,
                       
                        CollectionFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.SUPPORTS_REMOVE
                )
                .createTestSuite();
    }
    
}