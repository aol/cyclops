package cyclops.typeclasses;

import cyclops.control.Option;
import cyclops.instances.control.OptionInstances;
import org.junit.Test;

import static cyclops.control.Option.some;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Do2Test {

    @Test
    public void doOption2(){
        assertThat(Do.forEach(OptionInstances::monad)
                     .__(some(10))
                     .__(some(5))
                     .__(some(2))
                     .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doOptionUnbound2(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(5)
            .__(2)
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }

    @Test
    public void doOptionLazy2(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__((a,b)->some(2))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doOptionLazyA2(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            ._1(a->some(a/5))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doOptionLazyB2(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            ._2(b->some(b-3))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doOptionGuardSome2(){
        assertThat(Do.forEach(OptionInstances::monad)
                    .__(some(10))
                    .__(some(5))
                    .__(some(2))
                    .guard(OptionInstances.monadZero(),(a,b,c)->a+b+c>16)
                    .yield((a,b,c)->a+b+c),
            equalTo(some(17)));
    }
    @Test
    public void doOptionGuardNone2(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .guard(OptionInstances.monadZero(),(a,b,c)->a+b+c<17)
            .yield((a,b,c)->a+b+c),equalTo(Option.none()));
    }


}
