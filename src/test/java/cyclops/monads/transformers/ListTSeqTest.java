package cyclops.monads.transformers;

import cyclops.control.lazy.Maybe;
import cyclops.collections.mutable.ListX;
import cyclops.monads.Witness;
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
        assertThat(trans.cycle(3).toString(),equalTo("ListT[[[1, 2, 3, 1, 2, 3, 1, 2, 3], [1, 2, 3, 1, 2, 3, 1, 2, 3]]]"));
    }
    @Test
    public void cycleValue(){
        assertThat(value.cycle(3).toString(),equalTo("ListT[Just[[1, 2, 3, 1, 2, 3, 1, 2, 3]]]"));
    }
   
}
