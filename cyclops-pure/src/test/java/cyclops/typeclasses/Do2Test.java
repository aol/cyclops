package cyclops.typeclasses;

import com.oath.cyclops.hkt.DataWitness;
import cyclops.control.Option;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Lambda;
import cyclops.instances.control.OptionInstances;
import cyclops.instances.data.LazySeqInstances;
import cyclops.instances.data.SeqInstances;
import cyclops.instances.data.VectorInstances;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static cyclops.control.Option.some;
import static cyclops.data.tuple.Tuple.tuple;
import static cyclops.function.Function2._1;
import static cyclops.function.Function2._2;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Do2Test {

    @Test
    public void doOption2(){
        assertThat(Do.forEach(OptionInstances::monad)
                     .__(some(10))
                     .__(some(5))
                     .__(some(2))
                     .yield((a,b,c)->a+b+c)
                     .fold(Option::narrowK),equalTo(some(17)));
    }
    @Test
    public void doOptionUnbound2(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        ._of(5)
                        ._of(2)
                        .yield((a,b,c)->a+b+c)
                        .fold(Option::narrowK),equalTo(some(17)));
    }

    @Test
    public void doOptionLazy2(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        .__(i->some(i/2))
                        .__((a,b)->some(2))
                        .yield((a,b,c)->a+b+c)
                        .fold(Option::narrowK),equalTo(some(17)));
    }
    @Test
    public void doOptionLazyA2(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        .__(i->some(i/2))
                        .__(_1(a->some(a/5)))
                        .yield((a,b,c)->a+b+c)
                        .fold(Option::narrowK),equalTo(some(17)));
    }
    @Test
    public void doOptionLazyA2Fn(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        .__(i->some(i/2))
                        .__(_1(a->some(a/5)))
                        .yield((a,b,c)->a+b+c)
                        .fold(Option::narrowK),equalTo(some(17)));
    }
    @Test
    public void doOptionLazyB2(){
        assertThat(Do.forEach(OptionInstances::monad)
                        ._of(10)
                        .__(i->some(i/2))
                        .__(_2(b->some(b-3)))
                        .yield((a,b,c)->a+b+c)
                        .fold(Option::narrowK),equalTo(some(17)));
    }
    @Test
    public void doOptionGuardSome2(){
        assertThat(Do.forEach(OptionInstances::monad)
                    .__(some(10))
                    .__(some(5))
                    .__(some(2))
                    .guard(OptionInstances.monadZero(),(a,b,c)->a+b+c>16)
                    .yield((a,b,c)->a+b+c)
                    .fold(Option::narrowK),
            equalTo(some(17)));
    }
    @Test
    public void doOptionGuardNone2(){
        assertThat(Do.forEach(OptionInstances::monad)
                        .__(some(10))
                        .__(some(5))
                        .__(some(2))
                        .guard(OptionInstances.monadZero(),(a,b,c)->a+b+c<17)
                        .yield((a,b,c)->a+b+c)
                        .fold(Option::narrowK),equalTo(Option.none()));
    }
    @Test
    public void doOptionShow(){
        String s = Do.forEach(OptionInstances.monad())
                        ._of(10)
                        ._of(20)
                        .show(new Show<DataWitness.option>(){})
                        .yield((a,b)->a+b)
                        .fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("10Some[20]"));
    }
    @Test
    public void doOptionShowDefault(){
        String s = Do.forEach(OptionInstances.monad())
                     ._of(10)
                     ._of(20)._show(new Show<DataWitness.option>() {})
                     .yield((a,b,st)->st+a+b).fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("Some[20]1020"));
    }

    @Test
    public void doOptionMap1(){
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
                                        ._of(10)
                                        ._of(100)
                                        .map(i->i+1)
                                        .fold(Option::narrowK);

        assertThat(eleven,equalTo(some(101)));

    }
    @Test
    public void doOptionPeek1(){
        AtomicInteger ai = new AtomicInteger(-1);
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            .peek(i->{
                ai.set(i);
            })
            .fold(Option::narrowK);

        assertThat(ai.get(),equalTo(100));

    }
    @Test
    public void doOptionFlatten (){

        Option<Integer> res =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._flatten(some(some(10)))
            .yield((a,b,c)->a+b+c)
            .fold(Option::narrowK);

        assertThat(res,equalTo(some(120)));

    }


    @Test
    public void doSeqPlus(){

        Seq<Integer> res = Do.forEach(SeqInstances::monad)
            .__(Seq.of(10,20)).plus(SeqInstances::monadPlus,Seq.of(30))

            .fold(Seq::narrowK);

        assertThat(res,equalTo(Seq.of(30,10,20)));

    }
    @Test
    public void doSeqAp(){
        Seq<Integer> seq = Do.forEach(SeqInstances::monad)
            ._of(10)
            ._of(20)
            .ap(Seq.of(Lambda.λ((Integer i) -> i + 1)))
            .fold(Seq::narrowK);


        assertThat(seq,equalTo(Seq.of(21)));

    }




}
