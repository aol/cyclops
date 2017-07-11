package cyclops.typeclasses;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Monoids;
import cyclops.control.Maybe;
import cyclops.control.Xor;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.Witness.maybe;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.monad.MonadRec;
import org.junit.Test;

import static cyclops.control.Maybe.Instances.applicative;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 29/06/2017.
 */
public class ActiveTest {
    Active<list,Integer> active = Active.of(ListX.of(1,2,3), ListX.Instances.definitions());
    @Test
    public void map() {

        Active<list,Integer> doubled = active.map(i->i*2);
        assertThat(doubled.getActive(),equalTo(ListX.of(2,4,6)));
    }

    @Test
    public void tailRec(){
        MonadRec<list> mr = ListX.Instances.monadRec();
        mr.tailRec(0,i-> i<100_000 ? ListX.of(Xor.secondary(i+1)) : ListX.of(Xor.primary(i+1)) )
                .convert(ListX::narrowK).printOut();
       /**
        active.concreteTailRec(ListX.kindKleisli())
                .tailRec(0,i-> i<100_000 ? ListX.of(Xor.secondary(i+1)) : ListX.of(Xor.primary(i)) )
                .concreteConversion(ListX.kindCokleisli()).to(i->i).printOut();
        **/
    }
    @Test
    public void tailRecStream(){
        MonadRec<reactiveSeq> mr = ReactiveSeq.Instances.monadRec();
        mr.tailRec(0,i-> i<100_000 ? ReactiveSeq.of(Xor.secondary(i+1)) : ReactiveSeq.of(Xor.primary(i+1)) )
                .convert(ReactiveSeq::narrowK).printOut();
        /**
         active.concreteTailRec(ListX.kindKleisli())
         .tailRec(0,i-> i<100_000 ? ListX.of(Xor.secondary(i+1)) : ListX.of(Xor.primary(i)) )
         .concreteConversion(ListX.kindCokleisli()).to(i->i).printOut();
         **/
    }
    @Test
    public void concreteConversion() {

        ListX<Integer> r = active.concreteFlatMap(ListX.<Integer>kindKleisli())
                                 .flatMap(i -> ListX.of(i * 2, i * 3))
                                 .concreteConversion(ListX.<Integer>kindCokleisli())
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
       int res = active.folds()
                .get()
                .foldLeft(Monoids.intMax);
       assertThat(res,equalTo(3));
    }
    @Test
    public void traverse(){

        Higher<maybe, Higher<list, Integer>> res = active.traverse()
                .get()
                .<maybe,Integer>flatTraverse(applicative(), t->Maybe.just(ListX.of(t*2)));

        Maybe<ListX<Integer>> raw = res.convert(Maybe::narrowK)
                                       .map(ListX::narrowK);
        assertThat(raw,equalTo(Maybe.just(ListX.of(2,4,6))));
    }

    @Test
    public void custom(){
        Active<list, ListX<Integer>> grouped = active.custom(ListX::narrowK, l -> l.grouped(10));
        assertThat(grouped,equalTo(ListX.of(ListX.of(1,2,3))));

    }

}