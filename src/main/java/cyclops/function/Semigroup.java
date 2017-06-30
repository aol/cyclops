package cyclops.function;

import cyclops.function.BinaryFn;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

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
   

}
