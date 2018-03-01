package cyclops.monads.transformers;

import cyclops.control.Maybe;
import cyclops.data.Vector;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.Witness.maybe;
import cyclops.reactive.collections.mutable.ListX;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class VectorTSeqTest {

    VectorT<list,Integer> trans;
    VectorT<maybe,Integer> value;
    @Before
    public void setup(){
        trans = VectorT.fromList(Arrays.asList(Vector.of(1,2,3),Vector.of(1,2,3)));
        value = VectorT.fromMaybe(Maybe.just(Vector.of(1,2,3)));

    }

    @Test
    public void flatMapT(){
       System.out.println( ListT.fromOptional(Optional.of(ListX.of(1,2,3)))
             .flatMapT(i->ListT.fromOptional(Optional.of(ListX.of(i*10,5))))
             );
    }

    @Test
    public void cycle(){
        System.out.println(Vector.of(1,2,3).toString());
        assertThat(trans.cycle(3).toString(),equalTo("VectorT[[[1, 2, 3, 1, 2, 3, 1, 2, 3], [1, 2, 3, 1, 2, 3, 1, 2, 3]]]"));
    }
    @Test
    public void cycleValue(){
        assertThat(value.cycle(3).toString(),equalTo("VectorT[Just[[1, 2, 3, 1, 2, 3, 1, 2, 3]]]"));
    }

}
