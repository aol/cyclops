package cyclops.typeclasses.foldable;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.factory.Unit;
import cyclops.control.Maybe;
import cyclops.monads.Witness;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Traverse;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.function.Function;

import static org.jooq.lambda.tuple.Tuple.tuple;


public interface Unfoldable<W> {

    <R, T> Higher<W, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn);

    default <T> Higher<W, T> replicate(long n, T value) {
        return unfold(n,i -> i>0? Optional.of(tuple(value, i<Long.MAX_VALUE? i-1 : i)) : Optional.empty());
    }

    default <W2, T> Higher<W2, Higher<W, T>> replicateA(long n, Higher<W2, T> ds, Applicative<W2> applicative, Traverse<W> traversable) {
        return traversable.sequenceA(applicative, replicate(n, ds));
    }
    default <T,R> Higher<W,R> none() {
        return unfold((T) null, t -> Optional.<Tuple2<R, T>>empty());
    }
    default <T> Higher<W,T> one(T a) {
        return replicate(1, a);
    }


    static class UnsafeValueUnfoldable<W> implements Unfoldable<W>{
        Pure<W> pure;
        @Override
        public <R, T> Higher<W, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
            Optional<Tuple2<R, T>> x = fn.apply(b);
            R r = x.map(t -> t.v1).orElse(null);
           return pure.<R>unit(r);
        }
    }
}