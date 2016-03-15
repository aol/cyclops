package com.aol.cyclops.functions.collections.extensions.guava;

import java.util.Queue;

import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.google.common.collect.testing.TestStringQueueGenerator;

public class QueueXGenerator extends  TestStringQueueGenerator {

    
    @Override
    public Queue<String> create(String... elements) {
       return DequeX.of(elements);
    }

}
