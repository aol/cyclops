package com.aol.cyclops.functions.collections.extensions.guava;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

import  junit.framework.Test;
import junit.framework.TestSuite;
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SortedSetTestSuite.STest.class
 })
public class SortedSetTestSuite {
    
    public static class STest {
        public static Test suite() {
            return new STest().allTests();
        }
     
        public TestSuite allTests() {
            TestSuite suite =
                new TestSuite("com.aol.cyclops.function.collections.extensions.guava");
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
    
}