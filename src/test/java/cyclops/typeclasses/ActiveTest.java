package cyclops.typeclasses;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Monoids;
import cyclops.companion.Optionals;
import cyclops.control.Maybe;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.Witness.maybe;
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

}