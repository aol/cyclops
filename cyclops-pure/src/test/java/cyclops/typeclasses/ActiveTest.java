package cyclops.typeclasses;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.lazySeq;
import com.oath.cyclops.hkt.DataWitness.seq;
import com.oath.cyclops.hkt.Higher;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.instances.data.LazySeqInstances;
import cyclops.instances.data.SeqInstances;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.companion.Monoids;
import cyclops.control.Maybe;
import cyclops.control.Either;
import com.oath.cyclops.data.ReactiveWitness.list;
import com.oath.cyclops.hkt.DataWitness.option;
import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import cyclops.hkt.Active;
import cyclops.instances.reactive.IterableInstances;
import cyclops.instances.reactive.collections.immutable.VectorXInstances;
import cyclops.instances.reactive.collections.mutable.ListXInstances;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.monad.MonadRec;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import static cyclops.instances.control.MaybeInstances.applicative;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 29/06/2017.
 */
public class ActiveTest {
    Active<list,Integer> active = Active.of(ListX.of(1,2,3), ListXInstances.definitions());
    Active<seq,Integer> activeSeq = Active.of(Seq.of(1,2,3), SeqInstances.definitions());
    Active<lazySeq,Integer> activeLazySeq = Active.of(LazySeq.of(1,2,3), LazySeqInstances.definitions());
    @Test
    public void toList(){
        assertThat(ListXInstances.allTypeclasses(ListX.of(1,2,3)).toLazySeq(),equalTo(LazySeq.of(1,2,3)));
    }
    @Test
    public void foldMap(){
        System.out.println(ListXInstances.allTypeclasses(ListX.of(1,2,3)).foldMap(Monoids.intMax,a->a=a+1));
    }
    @Test
    public void foldMapTraverse(){
        System.out.println(ListXInstances.allTypeclasses(ListX.of(1,2,3)).foldMap(Monoids.intSum,a->a+1));
    }
    @Test
    public void reverse(){
        assertThat(ListXInstances.allTypeclasses(ListX.of(1,2,3)).reverse().getSingle().convert(ListX::narrowK),equalTo(ListX.of(3,2,1)));

    }
    @Test
    public void zipWith(){
        ListX<Integer> res =ListXInstances.allTypeclasses(ListX.of(1,2,3))
                .zipWith(VectorXInstances.allTypeclasses(VectorX.of(10,20)), (a, b)->{
                    return b.visit(p->a+p,()->-1);
                })
                .getSingle()
                .convert(ListX::narrowK);

        res.printOut();

    }
    @Test
    public void zipWithIndex(){
        ListX<Tuple2<Integer, Long>> l = ListXInstances.allTypeclasses(ListX.of(1, 2, 3))
                                              .zipWithIndex()
                                              .getSingle()
                                              .convert(ListX::narrowK);
        assertThat(l,equalTo(ListX.of(1,2,3).zipWithIndex()));
    }
    public static void main(String[] args){

        Active<list,Integer> list = ListXInstances.allTypeclasses(ListX.of(1,2,3));

        list.concreteMonoid(ListXInstances.kindKleisli(),ListXInstances.kindCokleisli())
                .sum(Vector.of(ListX.of(1,2,3)));

        list.concreteFlatMap(ListXInstances.kindKleisli())
                .flatMap(i->ListX.of(1,2,3));

        list.concreteTailRec(ListXInstances.kindKleisli())
                .tailRec(1,i-> 1<100_000 ? ListX.of(Either.left(i+1)) : ListX.of(Either.right(i)));


    }

    @Test
    public void map() {

        Active<list,Integer> doubled = active.map(i->i*2);
        assertThat(doubled.getActive(),equalTo(ListX.of(2,4,6)));
    }

    @Test
    public void tailRec(){
        MonadRec<list> mr = ListXInstances.monadRec();
        mr.tailRec(0,i-> i<100_000 ? ListX.of(Either.left(i+1)) : ListX.of(Either.right(i+1)) )
                .convert(ListX::narrowK).printOut();
       /**
        active.concreteTailRec(ListX.kindKleisli())
                .tailRec(0,i-> i<100_000 ? ListX.of(Xor.lazyLeft(i+1)) : ListX.of(Xor.lazyRight(i)) )
                .concreteConversion(ListXInstances.kindCokleisli()).to(i->i).printOut();
        **/
    }
    @Test
    public void tailRecStream(){
        MonadRec<reactiveSeq> mr = IterableInstances.monadRec();
        mr.tailRec(0,i-> i<100_000 ? ReactiveSeq.of(Either.left(i+1)) : ReactiveSeq.of(Either.right(i+1)) )
                .convert(ReactiveSeq::narrowK).printOut();

    }
    @Test
    public void concreteConversion() {

        ListX<Integer> r = active.concreteFlatMap(ListXInstances.<Integer>kindKleisli())

                                 .flatMap(i -> ListX.of(i * 2, i * 3))
                                 .concreteConversion(ListXInstances.<Integer>kindCokleisli())
                                 .to(l -> l);

        assertThat(r,equalTo(ListX.of(2,3,4,6,6,9)));
    }

    @Test
    public void flatMap()  {
        Active<list,Integer> doubled = active.map(i->i*2);
        Active<list,Integer> doubledPlusOne = doubled.flatMap(i->ListX.of(i+1));
        assertThat(doubledPlusOne.getActive(),equalTo(ListX.of(3,5,7)));
    }

    @Test
    public void folds(){
       int res = active.foldLeft(Monoids.intMax);
       assertThat(res,equalTo(3));
    }
    @Test
    public void traverse(){
        System.out.println(active);
        Higher<option, Higher<list, Integer>> res = active
                .<option,Integer>flatTraverse(applicative(), t->Maybe.just(ListX.of(t*2)));

        System.out.println(active);
        System.out.println(res);
        System.out.println(res);
        System.out.println(res);

       Maybe<ListX<Integer>> raw = res.convert(Maybe::narrowK)
                                       .map(ListX::narrowK);
        System.out.println(raw);
        assertThat(raw,equalTo(Maybe.just(ListX.of(2,4,6))));
    }
    @Test
    public void traverseSeq(){


        Higher<option, Higher<seq, Integer>> res = activeSeq
            .<option,Integer>flatTraverse(applicative(), t->Maybe.just(Seq.of(t*2)));

        System.out.println(res);
        Maybe<Seq<Integer>> raw = res.convert(Maybe::narrowK)
            .map(Seq::narrowK);
        assertThat(raw,equalTo(Maybe.just(Seq.of(2,4,6))));
    }
    @Test
    public void traverseLazySeq(){

        Higher<option, Higher<lazySeq, Integer>> res = activeLazySeq
            .<option,Integer>flatTraverse(applicative(), t->Maybe.just(LazySeq.of(t*2)));

        System.out.println(res);
        Maybe<LazySeq<Integer>> raw = res.convert(Maybe::narrowK)
            .map(LazySeq::narrowK);
        assertThat(raw,equalTo(Maybe.just(Seq.of(2,4,6))));
    }

    @Test
    public void custom(){
        Active<list, Vector<Integer>> grouped = active.custom(ListX::narrowK, l -> l.grouped(10));
        assertThat(grouped.getActive()  ,equalTo(ListX.of(Vector.of(1,2,3))));

    }

}
