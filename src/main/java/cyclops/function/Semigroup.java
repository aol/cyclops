package cyclops.function;

import com.aol.cyclops2.hkt.Higher;
import cyclops.function.BinaryFn;
import cyclops.typeclasses.Cokleisli;
import cyclops.typeclasses.Kleisli;
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
 * @param <T> Data type of elements toNested be combined
 */
@FunctionalInterface
public interface Semigroup<T> extends BinaryFn<T>,BinaryOperator<T> {
    
    /* (non-Javadoc)
     * @see java.util.function.BiFunction#applyHKT(java.lang.Object, java.lang.Object)
     */
    @Override
    T apply(T t, T u);

    default <W> SemigroupK<W,T> toSemigroupK(Kleisli<W,T,T> widen,Cokleisli<W,T,T> narrow){
        return  (a,b)-> widen.apply(Semigroup.this.apply(narrow.apply(a), narrow.apply(b)));
    }

}
