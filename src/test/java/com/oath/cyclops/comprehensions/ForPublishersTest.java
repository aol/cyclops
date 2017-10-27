package com.oath.cyclops.comprehensions;

import cyclops.monads.Witness;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import cyclops.monads.transformers.ListT;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;

public class ForPublishersTest {


    @Test
    public void groupedT(){

       ListT<reactiveSeq,Integer> nestedList = ReactiveSeq.of(1,2,3,4,5,6,7,8,9,10)
                                                             .groupedT(2)
                                                             .map(i->i*2);

       ListX<ListX<String>> listOfLists = nestedList.map(i->"nest:"+i)
                                                     .toListOfLists();
       System.out.println(listOfLists);

       //[[nest:2, nest:4], [nest:6, nest:8], [nest:10, nest:12], [nest:14, nest:16], [nest:18, nest:20]]



    }

    @Test
    public void listT(){


        ListT<Witness.set,Integer> nestedList = ListT.fromSet(SetX.of(ListX.of(11,22),ListX.of(100,200)));

        ListT<Witness.set,Integer> doubled = nestedList.map(i->i*2);
        System.out.println(doubled);

        //ListTSeq[AnyMSeq[[[22, 44], [200, 400]]]]

    }
    @Test
    public void publishers(){

       // import static com.aol.cyclops2.control.For.*;

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
