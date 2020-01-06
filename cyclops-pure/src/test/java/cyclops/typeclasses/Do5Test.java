package cyclops.typeclasses;

import com.oath.cyclops.hkt.DataWitness.option;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.function.Lambda;
import cyclops.instances.control.OptionInstances;
import cyclops.instances.data.SeqInstances;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static cyclops.control.Option.some;
import static cyclops.function.Function2._1;
import static cyclops.function.Function2._2;
import static cyclops.function.Function3.__1;
import static cyclops.function.Function3.__12;
import static cyclops.function.Function3.__2;
import static cyclops.function.Function3.__23;
import static cyclops.function.Function3.__3;
import static cyclops.function.Function4.___1;
import static cyclops.function.Function4.___12;
import static cyclops.function.Function4.___13;
import static cyclops.function.Function4.___14;
import static cyclops.function.Function4.___2;
import static cyclops.function.Function4.___23;
import static cyclops.function.Function4.___24;
import static cyclops.function.Function4.___3;
import static cyclops.function.Function4.___34;
import static cyclops.function.Function4.___4;
import static cyclops.function.Function5.____1;
import static cyclops.function.Function5.____12;
import static cyclops.function.Function5.____13;
import static cyclops.function.Function5.____14;
import static cyclops.function.Function5.____2;
import static cyclops.function.Function5.____23;
import static cyclops.function.Function5.____3;
import static cyclops.function.Function5.____34;
import static cyclops.function.Function5.____4;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Do5Test {

    @Test
    public void doOption5(){
        assertThat(Do.forEach(OptionInstances::monad)
                     .__(some(10))
                     .__(some(5))
                     .__(some(2))
                     .__(some(1))
                     .__(some(100))
                     .__(some(1000))
                     .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
                    .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionUnbound5(){
        assertThat(Do.forEach(OptionInstances::monad)
                     ._of(10)
                     ._of(5)
                     ._of(2)
                     ._of(1)
                     ._of(100)
                     .__(some(1000))
                     .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
                     .fold(Option::narrowK),
            equalTo(some(1118)));
    }

    @Test
    public void doOptionLazy5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__((a,b)->some(a-b-3))
            .__((a,b,c)->some(a-c-b-2))
            .__((a,b,c,d)->some(a+b+c+d+82))
            .__((a,b,c,d,e)->some(1000))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyA5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_1(a->some(a/5)))
            .__(__1(a->some(a/10)))
            .__(___1(a->some(a*10)))
            .__(____1(a->some(1000)))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyB5(){
        assertThat(Do.forEach(OptionInstances::monad)
                    ._of(10)
                    .__(i->some(i/2))
                    .__(_2(b->some(b-3)))
                    .__(__2(b->some(b-4)))
                    .__(___2(b->some(b*20)))
                    .__(____2(b->some(1000)))
                    .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
                     .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyC5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__3(c->some(c-1)))
            .__(___3(c->some(c*50))).__(____3(c->some(1000)))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyD5(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        .__(i->some(i/2))
                        .__(_2(b->some(b-3)))
                        .__(__3(c->some(c-1)))
                        .__(___4(d->some(d*100))).__(____4(d->some(1000)))
                        .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
                        .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyAB5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__12((a,b)->some(a-b-4)))
            .__(___12((a,b)->some(a+b+85)))
            .__(____12((a,b)->some(1000)))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyAC5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__12((a,b)->some(a-b-4)))
            .__(___13((a,c)->some(a+c+88)))
            .__(____13((a,c)->some(a+c+88)))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(some(218)));
    }
    @Test
    public void doOptionLazyBC5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .__(___23((b,c)->some(b+c+93)))
            .__(____23((b,c)->some(1000)))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyCD5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .__(___34((c,d)->some(d+c+97)))
            .__(____34((c,d)->some(1000)))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyAD5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .__(___14((a,d)->some(d+a+89)))
            .__(____14((a,d)->some(1000)))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(some(1118)));
    }
    @Test
    public void doOptionLazyBD5(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__23((b,c)->some(b-c-2)))
            .__(___24((b,d)->some(b+d+94)))
            .yield((a,b,c,d,e)->a+b+c+d+e)
            .fold(Option::narrowK),equalTo(some(118)));
    }
    @Test
    public void doOptionGuardSome3(){
        assertThat(Do.forEach(OptionInstances::monad)
                    .__(some(10))
                    .__(some(5))
                    .__(some(2))
                    .__(some(1))
                    .__(some(100))
                    .__(some(1000))
                    .guard(OptionInstances.monadZero(),(a,b,c,d,e,f)->a+b+c+d+e+f>117)
                    .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
                    .fold(Option::narrowK),
            equalTo(some(1118)));
    }
    @Test
    public void doOptionGuardNone3(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .__(some(1))
            .__(some(100))
            .__(some(1000))
            .guard(OptionInstances.monadZero(),(a,b,c,d,e,f)->a+b+c+d+e+f<117)
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK),equalTo(Option.none()));
    }
    @Test
    public void doOptionShow(){
        String s = Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(20)
            ._of(100)
            ._of(1000)
            ._of(10000)
            .show(new Show<option>(){})
            .yield((a,b,c,d,e)->a+b+c+d+e)
            .fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("1130Some[10000]"));
    }
    @Test
    public void doOptionShowDefault(){
        String s = Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(20)
            ._of(200)
            ._of(1000)
            ._of(10000)._show(new Show<option>() {})
            .yield((a,b,c,d,e,st)->st+a+b+c+d+e).fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("Some[1000]1020200100010000"));
    }

    @Test
    public void doOptionMap1(){
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            .map(i->i+1)
            .fold(Option::narrowK);

        assertThat(eleven,equalTo(some(100001)));

    }
    @Test
    public void doOptionPeek1(){
        AtomicInteger ai = new AtomicInteger(-1);
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            .peek(i->{
                ai.set(i);
            })
            .fold(Option::narrowK);

        assertThat(ai.get(),equalTo(100000));

    }
    @Test
    public void doOptionFlatten (){

        Option<Integer> res =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            ._flatten(some(some(10)))
            .yield((a,b,c,d,e,f)->a+b+c+d+e+f)
            .fold(Option::narrowK);

        assertThat(res,equalTo(some(111120)));

    }



    @Test
    public void doSeqAp(){
        Seq<Integer> seq = Do.forEach(SeqInstances::monad)
            ._of(10)
            ._of(20)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            .ap(Seq.of(Lambda.λ((Integer i) -> i + 1)))
            .fold(Seq::narrowK);


        assertThat(seq,equalTo(Seq.of(100001)));

    }

}
