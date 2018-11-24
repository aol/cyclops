package cyclops.typeclasses;

import com.oath.cyclops.hkt.DataWitness.option;
import cyclops.companion.Monoids;
import cyclops.control.Option;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Lambda;
import cyclops.instances.control.OptionInstances;
import cyclops.instances.data.LazySeqInstances;
import cyclops.instances.data.SeqInstances;
import cyclops.instances.data.VectorInstances;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static cyclops.control.Option.some;
import static cyclops.data.tuple.Tuple.tuple;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
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
            ._of(5)
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
        String s = Do.forEach(OptionInstances.monad())._of(10).show(new Show<option>(){}).fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("Some[10]"));
    }
    @Test
    public void doOptionShowDefault(){
        String s = Do.forEach(OptionInstances.monad())._of(10)._show(new Show<option>() {})
                        .yield((i,st)->st+i).fold(Option::narrowK).orElse(null);
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
                                     .yield((a,b)->a+b)
                                     .fold(Option::narrowK);

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
    public void doLazySeqReverse(){

        LazySeq<Integer> res = Do.forEach(LazySeqInstances::monad)
                                    .__(LazySeq.of(10,20))
                                    .reverse(LazySeqInstances::traverse)
                                    .fold(LazySeq::narrowK);

        assertThat(res,equalTo(LazySeq.of(20,10)));

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
    public void doVectorZip(){
        Vector<Tuple2<Integer, String>> seq = Do.forEach(VectorInstances::monad)
                                                .__(Vector.of(10,20))
                                                .zip(Vector.of("hello","world"), Tuple::tuple)
                                                .fold(Vector::narrowK);


        assertThat(seq,equalTo(Vector.of(tuple(10,"hello"), tuple(20,"world"))));

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


    @Test
    public void doVectorFoldMap(){
       String res =     Do.folds(VectorInstances::foldable)
                            .__(Vector.of(10,20))
                            .foldMap(Monoids.stringConcat,i->"hello"+i);



        assertThat(res,equalTo("hello10hello20"));

    }
    @Test
    public void doVectorAllMatchFalse(){
        boolean res =     Do.folds(VectorInstances::foldable)
                            .__(Vector.of(10,20))
                            .allMatch(i->i==10);



        assertFalse(res);

    }

    @Test
    public void doVectorAllMatchTrue(){
        boolean res =     Do.folds(VectorInstances::foldable)
                            .__(Vector.of(10,20))
                            .allMatch(i->i%2==0);



        assertTrue(res);

    }
    @Test
    public void doVectorAnyMatchFalse(){
        boolean res =     Do.folds(VectorInstances::foldable)
            .__(Vector.of(10,20))
            .anyMatch(i->i==5);



        assertFalse(res);

    }

    @Test
    public void doVectorAnyMatchTrue(){
        boolean res =     Do .folds(VectorInstances::foldable)
                            .__(Vector.of(10,20))
                            .anyMatch(i->i%2==0);



        assertTrue(res);

    }
    @Test
    public void doVectorSize(){
        long res =     Do .folds(VectorInstances::foldable)
                        .__(Vector.of(10,20))
                        .size();



        assertThat(res,equalTo(2l));

    }
    @Test
    public void doVectorGetAt(){
        Option<Integer> res =     Do .folds(VectorInstances::foldable)
                                    .__(Vector.of(10,20))
                                    .getAt(0);



        assertThat(res,equalTo(some(10)));

    }
    @Test
    public void doVectorFoldr(){
        int res =     Do .folds(VectorInstances::foldable)
                                    .__(Vector.of(10,20))
                                    .foldr(a->b->a+b,100);



        assertThat(res,equalTo(130));

    }
    @Test
    public void doVectorLazySeq(){
        LazySeq<Integer> res =     Do .folds(VectorInstances::foldable)
                                        .__(Vector.of(10,20))
                                        .lazySeq();



        assertThat(res,equalTo(LazySeq.of(10,20)));

    }
    @Test
    public void doVectorSeq(){
        Seq<Integer> res =     Do .folds(VectorInstances::foldable)
                                    .__(Vector.of(10,20))
                                    .seq();



        assertThat(res,equalTo(Seq.of(10,20)));

    }
    @Test
    public void doVector_Fold(){
        Vector<Long> res =     Do.forEach(VectorInstances::monad)
                                    .__(Vector.of(10,20))
                                    .__fold(VectorInstances::foldable,f->f.size())
                                    .yield((a,b)->a+b)
                                    .fold(Vector::narrowK);




        assertThat(res,equalTo(Vector.of(12l,22l)));

    }



    @Test
    public void doVectorStreamRequest1(){
        AtomicInteger recieved = new AtomicInteger(0);

        AtomicInteger errors = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        ReactiveSeq<Integer> res =     Do.folds(VectorInstances::foldable)
                                            .__(Vector.of(10,20))
                                            .stream();

        res.forEach(1,i->recieved.incrementAndGet(),e->errors.incrementAndGet(),()->complete.set(false));


        assertThat(recieved.get(),equalTo(1));
        assertThat(errors.get(),equalTo(0));
        assertThat(complete.get(),equalTo(false));

    }


}
