package cyclops.typeclasses;

import cyclops.control.Option;
import cyclops.instances.control.OptionInstances;
import org.junit.Test;

import static cyclops.control.Option.some;
import static cyclops.function.Function2._1;
import static cyclops.function.Function2._2;
import static cyclops.function.Function3.*;
import static cyclops.function.Function4.___1;
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
    public void doOptionUnbound3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            ._of(5)
            ._of(2)
            ._of(1)
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }

    @Test
    public void doOptionLazy3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__((a,b)->some(a-b-3))
            .__((a,b,c)->some(a-c-b-2))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyA3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_1(a->some(a/5)))
            .__(__1(a->some(a/10)))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyB3(){
        assertThat(Do.forEach(OptionInstances::monad)
                    ._of(10)
                    .__(i->some(i/2))
                    .__(_2(b->some(b-3)))
                    .__(__2(b->some(b-4)))
                    .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyC3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__3(c->some(c-1)))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyAB3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__12((a,b)->some(a-b-4)))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyBC3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doOptionGuardSome3(){
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
    public void doOptionGuardNone3(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .__(some(1))
            .guard(OptionInstances.monadZero(),(a,b,c,d)->a+b+c+d<17)
            .yield((a,b,c,d)->a+b+c+d),equalTo(Option.none()));
    }


}
