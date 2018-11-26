package cyclops.typeclasses;

import com.oath.cyclops.hkt.DataWitness;
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
                     .yield((a,b,c,d)->a+b+c+d)
                     .fold(Option::narrowK),equalTo(some(18)));
    }
    @Test
    public void doOptionUnbound3(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        ._of(5)
                        ._of(2)
                        ._of(1)
                        .yield((a,b,c,d)->a+b+c+d)
                        .fold(Option::narrowK),equalTo(some(18)));
    }

    @Test
    public void doOptionLazy3(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        .__(i->some(i/2))
                        .__((a,b)->some(a-b-3))
                        .__((a,b,c)->some(a-c-b-2))
                        .yield((a,b,c,d)->a+b+c+d)
                        .fold(Option::narrowK),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyA3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_1(a->some(a/5)))
            .__(__1(a->some(a/10)))
            .yield((a,b,c,d)->a+b+c+d)
            .fold(Option::narrowK),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyB3(){
        assertThat(Do.forEach(OptionInstances::monad)
                    ._of(10)
                    .__(i->some(i/2))
                    .__(_2(b->some(b-3)))
                    .__(__2(b->some(b-4)))
                    .yield((a,b,c,d)->a+b+c+d)
                    .fold(Option::narrowK),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyC3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__3(c->some(c-1)))
            .yield((a,b,c,d)->a+b+c+d)
            .fold(Option::narrowK),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyAB3(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__12((a,b)->some(a-b-4)))
            .yield((a,b,c,d)->a+b+c+d)
            .fold(Option::narrowK),equalTo(some(18)));
    }
    @Test
    public void doOptionLazyBC3(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        .__(i->some(i/2))
                        .__(_2(b->some(b-3)))
                        .__(__23((b,c)->some(b-c-2)))
                        .yield((a,b,c,d)->a+b+c+d)
                        .fold(Option::narrowK),equalTo(some(18)));
    }
    @Test
    public void doOptionGuardSome3(){
        assertThat(Do.forEach(OptionInstances::monad)
                        .__(some(10))
                        .__(some(5))
                        .__(some(2))
                        .__(some(1))
                        .guard(OptionInstances.monadZero(),(a,b,c,d)->a+b+c+d>17)
                        .yield((a,b,c,d)->a+b+c+d)
                        .fold(Option::narrowK),
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
            .yield((a,b,c,d)->a+b+c+d)
            .fold(Option::narrowK),equalTo(Option.none()));
    }
    @Test
    public void doOptionShow(){
        String s = Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(20)
            ._of(100)
            .show(new Show<DataWitness.option>(){})
            .yield((a,b,c)->a+b+c)
            .fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("30Some[100]"));
    }
    @Test
    public void doOptionShowDefault(){
        String s = Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(20)
            ._of(200)._show(new Show<DataWitness.option>() {})
            .yield((a,b,c,st)->st+a+b+c).fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("Some[200]1020200"));
    }

    @Test
    public void doOptionMap1(){
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            .map(i->i+1)
            .fold(Option::narrowK);

        assertThat(eleven,equalTo(some(1001)));

    }
    @Test
    public void doOptionPeek1(){
        AtomicInteger ai = new AtomicInteger(-1);
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            .peek(i->{
                ai.set(i);
            })
            .fold(Option::narrowK);

        assertThat(ai.get(),equalTo(1000));

    }
    @Test
    public void doOptionFlatten (){

        Option<Integer> res =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            ._flatten(some(some(10)))
            .yield((a,b,c,d)->a+b+c+d)
            .fold(Option::narrowK);

        assertThat(res,equalTo(some(1120)));

    }



    @Test
    public void doSeqAp(){
        Seq<Integer> seq = Do.forEach(SeqInstances::monad)
            ._of(10)
            ._of(20)
            ._of(1000)
            .ap(Seq.of(Lambda.λ((Integer i) -> i + 1)))
            .fold(Seq::narrowK);


        assertThat(seq,equalTo(Seq.of(1001)));

    }


}
