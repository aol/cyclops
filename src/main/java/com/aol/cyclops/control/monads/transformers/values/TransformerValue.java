package com.aol.cyclops.control.monads.transformers.values;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.applicative.Applicativable;

public interface TransformerValue<T> extends  MonadicValue<T>,
                                            Supplier<T>, 
                                            ConvertableFunctor<T>, 
                                            Filterable<T>,
                                            Applicativable<T>,
                                            Matchable.ValueAndOptionalMatcher<T>{

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    TransformerValue<T> filter(Predicate<? super T> fn) ;

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    <T> TransformerValue<T> unit(T unit);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
     <R> TransformerValue<R> map(Function<? super T, ? extends R> fn);

   
    

}
