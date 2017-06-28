package cyclops.typeclasses;

import cyclops.collections.mutable.ListX;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 29/06/2017.
 */
public class ActiveTest {
    Active<list,Integer> active = Active.of(ListX.of(1,2,3), ListX.Instances.definitions());
    @Test
    public void map() {

        Active<list,Integer> doubled = active.map(i->i*2);
        assertThat(doubled.getActive(),equalTo(ListX.of(2,4,6)));
    }

    @Test
    public void flatMap()  {
        Active<list,Integer> doubled = active.map(i->i*2);
        Active<list,Integer> doubledPlusOne = doubled.flatMap(i->ListX.of(i+1));
        assertThat(doubledPlusOne.getActive(),equalTo(ListX.of(3,5,7)));
    }

}