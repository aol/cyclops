package cyclops.typeclasses.foldable;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Traverse;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.function.Function;

import static org.jooq.lambda.tuple.Tuple.tuple;


public interface Unfoldable<W> {

    <R, T> Higher<W, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn);

    default <T> Higher<W, T> replicate(int n, T value) {
        return unfold(n,i -> Optional.of(tuple(value, i-1)));
    }

    default <W2, T> Higher<W2, Higher<W, T>> replicateA(int n, Higher<W2, T> ds, Applicative<W2> applicative, Traverse<W> traversable) {
        return traversable.sequenceA(applicative, replicate(n, ds));
    }


}