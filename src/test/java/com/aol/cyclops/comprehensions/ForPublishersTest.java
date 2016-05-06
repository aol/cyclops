package com.aol.cyclops.comprehensions;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.control.For.Publishers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;

import reactor.core.publisher.Flux;

public class ForPublishersTest {

    
    @Test
    public void groupedT(){
        
       ListTSeq<Integer> nestedList = ReactiveSeq.of(1,2,3,4,5,6,7,8,9,10)
                                                 .groupedT(2)
                                                 .map(i->i*2);
       
       ListX<ListX<String>> listOfLists = nestedList.map(i->"nested:"+i)
                                                     .toListOfLists();
       System.out.println(listOfLists);
       
       //[[nested:2, nested:4], [nested:6, nested:8], [nested:10, nested:12], [nested:14, nested:16], [nested:18, nested:20]]

      
       
    }
    
    @Test
    public void listT(){
        
        
        ListTSeq<Integer> nestedList = ListT.fromIterable(SetX.of(ListX.of(11,22),ListX.of(100,200)));
        
        ListTSeq<Integer> doubled = nestedList.map(i->i*2);
        System.out.println(doubled);
        
        //ListTSeq[AnyMSeq[[[22, 44], [200, 400]]]]
        
    }
    @Test
    public void publishers(){
        
       // import static com.aol.cyclops.control.For.*;
        
        ReactiveSeq<Tuple2<Integer,Integer>> stream = Publishers.each2(Flux.just(1,2,3), i->  ReactiveSeq.range(i,5), 
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
