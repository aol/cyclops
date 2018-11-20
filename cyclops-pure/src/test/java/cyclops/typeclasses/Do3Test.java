package cyclops.typeclasses;

import cyclops.control.Option;
import cyclops.instances.control.OptionInstances;
import org.junit.Test;

import static cyclops.control.Option.some;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Do3Test {

    @Test
    public void doOption3(){
        assertThat(Do.forEach(OptionInstances::monad)
                     .__(some(10))
                     .__(some(5))
                     .__(some(2))
                     .__(some(1))
                     .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionUnbound2(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(5)
            .__(2)
            .__(1)
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }

    @Test
    public void doOptionLazy2(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(i->some(i/2))
            .__((a,b)->some(a-b-3))
            .__((a,b,c)->some(a-c-b-2))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyA2(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(i->some(i/2))
            ._1(a->some(a/5))
            ._1(a->some(a/10))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyB2(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(i->some(i/2))
            ._2(b->some(b-3))
            ._2(b->some(b-4))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyC2(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(i->some(i/2))
            ._2(b->some(b-3))
            ._3(c->some(c-1))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyAB2(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(i->some(i/2))
            ._2(b->some(b-3))
            ._12((a,b)->some(a-b-4))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyBC2(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .__(i->some(i/2))
            ._2(b->some(b-3))
            ._23((b,c)->some(b-c-2))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionGuardSome2(){
        assertThat(Do.forEach(OptionInstances::monad)
                    .__(some(10))
                    .__(some(5))
                    .__(some(2))
                    .__(some(1))
                    .guard(OptionInstances.monadZero(),(a,b,c,d)->a+b+c+d>17)
                    .yield((a,b,c,d)->a+b+c+d),
            equalTo(some(18)));
    }
    @Test
    public void doOptionGuardNone1(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .__(some(1))
            .guard(OptionInstances.monadZero(),(a,b,c,d)->a+b+c+d<17)
            .yield((a,b,c,d)->a+b+c+d),equalTo(Option.none()));
    }


}
