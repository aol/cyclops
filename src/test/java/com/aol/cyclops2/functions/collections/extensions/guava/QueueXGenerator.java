package com.aol.cyclops2.functions.collections.extensions.guava;

import java.util.Queue;

import cyclops.collections.mutable.DequeX;
import com.google.common.collect.testing.TestStringQueueGenerator;

public class QueueXGenerator extends  TestStringQueueGenerator {

    
    @Override
    public Queue<String> create(String... elements) {
       return DequeX.of(elements);
    }

}
