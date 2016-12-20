package com.aol.cyclops.control.monads.transformers;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.anyM.Witness;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class ListTSeqTest {

    ListT<Witness.list,Integer> trans;
    ListT<Witness.maybe,Integer> value;
    @Before
    public void setup(){
        trans = ListT.fromList(Arrays.asList(ListX.of(1,2,3),ListX.of(1,2,3)));
        value = ListT.fromMaybe(Maybe.just(ListX.of(1,2,3)));
        
    }
    
    @Test
    public void flatMapT(){
       System.out.println( ListT.fromOptional(Optional.of(ListX.of(1,2,3)))
             .flatMapT(i->ListT.fromOptional(Optional.of(ListX.of(i*10,5))))
             );
    }
    
    @Test
    public void cycle(){
        assertThat(trans.cycle(3).toString(),equalTo("ListTSeq[AnyMSeq[[[1, 2, 3, 1, 2, 3, 1, 2, 3], [1, 2, 3, 1, 2, 3, 1, 2, 3]]]]"));
    }
    @Test
    public void cycleValue(){
        assertThat(value.cycle(3).toString(),equalTo("ListTValue[AnyMValue[[1, 2, 3, 1, 2, 3, 1, 2, 3]]]"));
    }
   
}
