package cyclops.typeclasses;

import cyclops.control.Option;
import cyclops.instances.control.OptionInstances;
import org.junit.Test;

import static cyclops.control.Option.some;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Do1Test {

    @Test
    public void doOption1(){
        assertThat(Do.forEach(OptionInstances::monad)
                     .__(some(10))
                     .__(some(5))
                     .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doOptionUnbound1(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(5)
            .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doOptionLazy1(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(i->some(i/2))
            .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doOptionGuardSome1(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(some(5))
            .guard(OptionInstances.monadZero(),(a,b)->a+b>14)
            .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doOptionGuardNone1(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(some(5))
            .guard(OptionInstances.monadZero(),(a,b)->a+b<15)
            .yield((a,b)->a+b),equalTo(Option.none()));
    }


}
