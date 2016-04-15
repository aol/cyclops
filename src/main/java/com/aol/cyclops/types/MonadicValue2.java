package com.aol.cyclops.types;

import java.util.function.Function;

import com.aol.cyclops.Monoid;

public interface MonadicValue2<T1,T2> extends MonadicValue<T2>{
    <R1,R2> MonadicValue2<R1,R2> flatMap(Function<? super T2,? extends MonadicValue2<? extends R1,? extends R2>> mapper);
    
    /**
     * Eagerly combine two MonadicValues using the supplied monoid
     * 
     * <pre>
     * {@code 
     * 
     *  Monoid<Integer> add = Mondoid.of(1,Semigroups.intSum);
     *  Maybe.of(10).plus(add,Maybe.none());
     *  //Maybe[10]
     *  
     *  Maybe.none().plus(add,Maybe.of(10));
     *  //Maybe[10]
     *  
     *  Maybe.none().plus(add,Maybe.none());
     *  //Maybe.none()
     *  
     *  Maybe.of(10).plus(add,Maybe.of(10));
     *  //Maybe[20]
     *  
     *  Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
     *  Maybe.of(10).plus(firstNonNull,Maybe.of(10));
     *  //Maybe[10]
     * }
     * 
     * @param monoid
     * @param v2
     * @return
     */
   default MonadicValue2<T1,T2> combine(Monoid<T2> monoid, MonadicValue2<? extends T1,? extends T2> v2){
       return unit(this.<T1,T2>flatMap(t1-> v2.map(t2->monoid.combiner().apply(t1,t2)))
                                      .orElseGet(()->this.orElseGet(()->monoid.zero())));
   }
   <R> MonadicValue2<T1,R>  map(Function<? super T2,? extends R> fn);
   <T2> MonadicValue2<T1,T2> unit(T2 unit);
}
