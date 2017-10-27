package cyclops.companion;

import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.monads.Witness.maybe;
import cyclops.typeclasses.functions.MonoidK;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 10/07/2017.
 */
public class MonoidsTest {
    @Test
    public void kinds(){

        Monoid<Maybe<Integer>> m = Monoids.combineZippables(i->Maybe.just(i),Monoids.intMax);
        MonoidK<maybe, Integer> x = m.toMonoidK(Maybe.kindKleisli(), Maybe.kindCokleisli());

        assertThat(x.apply(Maybe.just(10),Maybe.just(2)),equalTo(Maybe.just(10)));

    }
}