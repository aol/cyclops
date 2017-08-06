package cyclops.function;

import com.aol.cyclops2.hkt.Higher;
import cyclops.companion.Semigroups;
import cyclops.control.Maybe;
import cyclops.function.BinaryFn;
import cyclops.monads.Witness;
import cyclops.monads.Witness.maybe;
import cyclops.typeclasses.Cokleisli;
import cyclops.typeclasses.Kleisli;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.functions.SemigroupK;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * An (associative) binary operation for combining values.
 * Implementations should obey associativity laws.
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements to be combined
 */
@FunctionalInterface
public interface Semigroup<T> extends BinaryFn<T>,BinaryOperator<T> {
    
    /* (non-Javadoc)
     * @see java.util.function.BiFunction#applyHKT(java.lang.Object, java.lang.Object)
     */
    @Override
    T apply(T t, T u);



    /**
     * Example
     *
     * <pre>
     *     {@code
     *
     *        Semigroup<Maybe<Integer>> m = Semigroups.combineZippables(Semigroups.intMax);
              SemigroupK<maybe, Integer> x = m.toSemigroupK(Maybe.kindKleisli(), Maybe.kindCokleisli());

           }
     * </pre>
     */
    default <W,R> SemigroupK<W,R> toSemigroupK(Kleisli<W,T,R> widen,Cokleisli<W,R,T> narrow){
        return  (a,b)-> widen.apply(Semigroup.this.apply(narrow.apply(a), narrow.apply(b)));
    }


}
