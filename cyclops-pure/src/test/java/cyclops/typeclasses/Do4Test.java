package cyclops.typeclasses;

import cyclops.control.Option;
import cyclops.instances.control.OptionInstances;
import org.junit.Test;

import static cyclops.control.Option.some;
import static cyclops.function.Function2._1;
import static cyclops.function.Function2._2;
import static cyclops.function.Function3.*;
import static cyclops.function.Function4.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Do4Test {

    @Test
    public void doOption4(){
        assertThat(Do.forEach(OptionInstances::monad)
                     .__(some(10))
                     .__(some(5))
                     .__(some(2))
                     .__(some(1))
                     .__(some(100))
                     .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionUnbound4(){
        assertThat(Do.forEach(OptionInstances::monad)
                     ._of(10)
                     ._of(5)
                     ._of(2)
                     ._of(1)
                     ._of(100)
                     .yield((a,b,c,d,e)->a+b+c+d+e),
            equalTo(some(118)));
    }

    @Test
    public void doOptionLazy4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__((a,b)->some(a-b-3))
            .__((a,b,c)->some(a-c-b-2))
            .__((a,b,c,d)->some(a+b+c+d+82))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyA4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_1(a->some(a/5)))
            .__(__1(a->some(a/10)))
            .__(___1(a->some(a*10)))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyB4(){
        assertThat(Do.forEach(OptionInstances::monad)
                    ._of(10)
                    .__(i->some(i/2))
                    .__(_2(b->some(b-3)))
                    .__(__2(b->some(b-4)))
                    .__(___2(b->some(b*20)))
                    .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyC4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__3(c->some(c-1)))
            .__(___3(c->some(c*50)))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyD4(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        .__(i->some(i/2))
                        .__(_2(b->some(b-3)))
                        .__(__3(c->some(c-1)))
                        .__(___4(d->some(d*100)))
                        .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyAB4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__12((a,b)->some(a-b-4)))
            .__(___12((a,b)->some(a+b+85)))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyAC4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__12((a,b)->some(a-b-4)))
            .__(___13((a,c)->some(a+c+88)))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyBC4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .__(___23((b,c)->some(b+c+93)))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyCD4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .__(___34((c,d)->some(d+c+97)))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyAD4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .__(___14((a,d)->some(d+a+89)))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionLazyBD4(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .__(___24((b,d)->some(b+d+94)))
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(some(118)));
    }
    @Test
    public void doOptionGuardSome3(){
        assertThat(Do.forEach(OptionInstances::monad)
                    .__(some(10))
                    .__(some(5))
                    .__(some(2))
                    .__(some(1))
                    .__(some(100))
                    .guard(OptionInstances.monadZero(),(a,b,c,d,e)->a+b+c+d+e>117)
                    .yield((a,b,c,d,e)->a+b+c+d+e),
            equalTo(some(118)));
    }
    @Test
    public void doOptionGuardNone3(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .__(some(1))
            .__(some(100))
            .guard(OptionInstances.monadZero(),(a,b,c,d,e)->a+b+c+d+e<117)
            .yield((a,b,c,d,e)->a+b+c+d+e),equalTo(Option.none()));
    }


}
