package com.aol.cyclops2.functions.collections.extensions.guava;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

import cyclops.collections.mutable.DequeX;
import com.google.common.collect.testing.TestStringQueueGenerator;
import cyclops.collections.mutable.QueueX;

public class QueueXGenerator extends  TestStringQueueGenerator {

    
    @Override
    public Queue<String> create(String... elements) {
       return QueueX.of(elements).type(Collectors.toCollection(()->new LinkedList<>()));
    }

}
