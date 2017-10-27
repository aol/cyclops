package com.oath.cyclops.comprehensions.donotation.typed;

import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import static cyclops.reactive.ReactiveSeq.range;
import static cyclops.data.tuple.Tuple.tuple;
public class DoTest {

    @Test
    public void doGen2(){

        ReactiveSeq.range(1,10)
                   .forEach2(i->range(0, i), (i,j)->tuple(i,j));

        //  .forEach(System.out::println);


    }

}
