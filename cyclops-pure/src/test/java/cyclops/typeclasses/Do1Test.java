package cyclops.typeclasses;

import com.oath.cyclops.hkt.DataWitness.option;
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
            ._of(10)
            .__(5)
            .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doOptionLazy1(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
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

    @Test
    public void doOptionShow(){
        String s = Do.forEach(OptionInstances.monad())._of(10).show(new Show<option>(){});
        assertThat(s,equalTo("Some[10]"));
    }
    @Test
    public void doOptionShowDefault(){
        String s = Do.forEach(OptionInstances.monad())._of(10).show();
        assertThat(s,equalTo("Some[10]"));
    }

    @Test
    public void doOptionMap1(){
     Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
                                    ._of(10)
                                    .map(i->i+1)
                                    .fold(Option::narrowK);

     assertThat(eleven,equalTo(some(11)));

    }
    @Test
    public void doOptionPeek1(){
        AtomicInteger ai = new AtomicInteger(-1);
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
            ._of(10)
            .peek(i->{
                ai.set(i);
            })
            .fold(Option::narrowK);

        assertThat(ai.get(),equalTo(10));

    }
    @Test
    public void doOptionFlatten (){

        Option<Integer> res =   Do.forEach(OptionInstances.monad())
                                     ._of(10)._flatten(some(some(10)))
                                     .yield((a,b)->a+b,Option::narrowK);

        assertThat(res,equalTo(some(20)));

    }

    @Test
    public void doSeqReverse(){

        Seq<Integer> res = Do.forEach(SeqInstances::monad)
                             .__(Seq.of(10,20))
                                    .reverse(SeqInstances.traverse())
                                .fold(Seq::narrowK);

        assertThat(res,equalTo(Seq.of(20,10)));

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
                                .ap(Seq.of(Lambda.λ((Integer i) -> i + 1)))
                                .fold(Seq::narrowK);


        assertThat(seq,equalTo(Seq.of(11)));

    }
    @Test
    public void doSeqZipWithIndex(){
        Seq<Tuple2<Integer, Long>> seq = Do.forEach(SeqInstances::monad)
                                            .__(Seq.of(10,20))
                                            .zipWithIndex(SeqInstances::traverse)
            .fold(Seq::narrowK);


        assertThat(seq,equalTo(Seq.of(tuple(10,0l), tuple(20,1l))));

    }
    @Test
    public void doVectorZipWithIndex(){
        Vector<Tuple2<Integer, Long>> seq = Do.forEach(VectorInstances::monad)
                                                .__(Vector.of(10,20))
                                                .zipWithIndex(VectorInstances::traverse)
                                                .fold(Vector::narrowK);


        assertThat(seq,equalTo(Vector.of(tuple(10,0l), tuple(20,1l))));

    }
    @Test
    public void doLazySeqZipWithIndex(){
        LazySeq<Tuple2<Integer, Long>> seq = Do.forEach(LazySeqInstances::monad)
                                                .__(LazySeq.of(10,20))
                                                .zipWithIndex(LazySeqInstances::traverse)
                                                .fold(LazySeq::narrowK);


        seq.printOut();
        assertThat(seq,equalTo(LazySeq.of(tuple(10,0l), tuple(20,1l))));

    }
    public Option<Integer> credit(Integer amount){
        return Option.some(amount);
    }
    public Option<Integer> debit(Integer amount){
        return Option.some(amount);
    }


}
