package com.aol.cyclops.types;

import java.util.Iterator;
import java.util.function.Function;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.AnyMValue;

public interface MonadicValue1<T> extends MonadicValue<T> {
    public <T> MonadicValue1<T> unit(T unit);
    
    <R> MonadicValue<R>  map(Function<? super T,? extends R> fn);
    
    default AnyMValue<T> anyM(){
        return AnyM.ofValue(this);
    }
    default <R> MonadicValue<R> coflatMap(Function< ? super MonadicValue<T>,R> mapper){
        return mapper.andThen(r->unit(r)).apply(this);
    }
    
    
     /**
      * Eagerly combine two MonadicValues using the supplied monoid
      * 
      * <pre>
      * {@code 
      * 
      *  Monoid<Integer> add = Monoid.of(1,Semigroups.intSum);
      *  Maybe.of(10).combine(add,Maybe.none());
      *  //Maybe[10]
      *  
      *  Maybe.none().combine(add,Maybe.of(10));
      *  //Maybe[10]
      *  
      *  Maybe.none().combine(add,Maybe.none());
      *  //Maybe.none()
      *  
      *  Maybe.of(10).combine(add,Maybe.of(10));
      *  //Maybe[20]
      *  
      *  Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
      *  Maybe.of(10).combine(firstNonNull,Maybe.of(10));
      *  //Maybe[10]
      * }
      * 
      * @param monoid
      * @param v2
      * @return
      */
    default MonadicValue1<T> combine(Monoid<T> monoid, MonadicValue<? extends T> v2){
        return unit(this.<T>flatMap(t1-> v2.map(t2->monoid.combiner().apply(t1,t2)))
                                       .orElseGet(()->this.orElseGet(()->monoid.zero())));
    }
    
    
    <R> MonadicValue<R> flatMap(Function<? super T,? extends MonadicValue<? extends R>> mapper);
}
