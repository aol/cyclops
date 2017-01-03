package com.aol.cyclops2.functions.collections.extensions.guava;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.google.common.collect.testing.QueueTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

import  junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    QueueXTestSuite.QTest.class
 })
public class QueueXTestSuite {
    
    public static class QTest {
        public static Test suite() {
            return new QTest().allTests();
        }
     
        public TestSuite allTests() {
            TestSuite suite =
                new TestSuite("com.aol.cyclops2.function.collections.extensions.guava");
            suite.addTest(testForOneToWayUseMySet());
            
            return suite;
        }
        public Test testForOneToWayUseMySet() {
            return QueueTestSuiteBuilder
                    .using(new QueueXGenerator())
                    .named("QueueX test")
                    .withFeatures(
                            CollectionSize.ANY,
                          
                            CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                            CollectionFeature.SUPPORTS_ADD,
                           
                            CollectionFeature.SUPPORTS_REMOVE,
                            CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                            CollectionFeature.SUPPORTS_REMOVE
                    )
                    .createTestSuite();
        }
    }
    
}