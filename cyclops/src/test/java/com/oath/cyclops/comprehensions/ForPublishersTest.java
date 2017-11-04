package com.oath.cyclops.comprehensions;

import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;

public class ForPublishersTest {



    @Test
    public void publishers(){

       // import static com.oath.cyclops.control.For.*;

        ReactiveSeq<Tuple2<Integer,Integer>> stream = ReactiveSeq.of(1,2,3).forEach2(i->  ReactiveSeq.range(i,5),
                                                                            Tuple::tuple)
                                                                    .stream();

        stream.printOut();
        /*
           (1, 1)
           (1, 2)
           (1, 3)
           (1, 4)
           (2, 2)
           (2, 3)
           (2, 4)
           (3, 3)
           (3, 4)

         */

    }


}
