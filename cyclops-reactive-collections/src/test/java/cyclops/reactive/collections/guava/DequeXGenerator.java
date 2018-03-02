package cyclops.reactive.collections.guava;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

import cyclops.reactive.collections.mutable.DequeX;
import com.google.common.collect.testing.TestStringQueueGenerator;

public class DequeXGenerator extends  TestStringQueueGenerator {


    @Override
    public Queue<String> create(String... elements) {
       return DequeX.of(elements).type(Collectors.toCollection(()->new LinkedList<>()));
    }

}
