package com.aol.cyclops2.types.extensability;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import cyclops.control.anym.AnyM;
import cyclops.control.anym.WitnessType;

import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;

/**
 * Interface for defining how Comprehensions should work for a type
 * Cyclops For Comprehensions will supply lazy a JDK 8 Predicate or Function
 * for filter / transform / flatMap
 * The comprehender should wrap these in a suitable type and make the call to the
 * underlying Monadic Type (T) the Comprehender implementation supports.
 * 
 * E.g. To support mapping for the Functional Java Option type wrap the supplied JDK 8 Function in a Functional Java
 * fj.F type, call the make call to option.transform( ) and retun the result.
 * 
 * <pre>{@code
 *  OptionComprehender<Option> {
 *    
 *     public Object transform(Option o, Function fn){
 *        return o.transform( a-> fn.applyHKT(a));
 *     }
 *     
 * }
 * }</pre>
 * 
 *
 * 
 * @author johnmcclean
 *
 * @param <T> Monadic Type being wrapped
 */
//TODO rename MonadAdapter
public interface FunctionalAdapter<W extends WitnessType<W>> {
    
    
    default <R> R visit(Function<? super FunctionalAdapter<W>,? extends R> fn1, Function<? super  ValueAdapter<W>, ? extends R> fn2){
        return fn1.apply(this);
    }
    default <T,T2,R> AnyM<W,R> ap2(AnyM<W,? extends Function<? super T,? extends Function<? super T2,? extends R>>> fn, AnyM<W,T> apply,AnyM<W,T2> apply2){
        return  ap(ap(fn, apply), apply2);
    }
    
    public <T,R> AnyM<W,R> ap(AnyM<W, ? extends Function<? super T,? extends R>> fn, AnyM<W,T> apply);
    
    default <T> AnyM<W,T> filter(AnyM<W,T> t,  Predicate<? super T> fn){
        return t;
    }
    
    public <T,R> AnyM<W,R> map(AnyM<W,T> t,  Function<? super T, ? extends R> fn);

   
    public <T,R> AnyM<W,R> flatMap(AnyM<W,T> t, Function<? super T, ? extends AnyM<W,? extends R>> fn);

    default boolean isFilterable(){
        return true;
    }
    @AllArgsConstructor
    static class ValueIterator<T> implements Iterator<T>{
        private final T value;
        int count =0;
        @Override
        public boolean hasNext() {
            return count == 0;
        }

        @Override
        public T next() {
            if(count++>0)
                throw new NoSuchElementException();
           return value;
        }
    }
    default <T> AnyM<W,T> unit(T o){
      
        return unitIterable(()->new ValueIterator<T>(o,0));
    }

    default  <T> ReactiveSeq<T> toStream(AnyM<W,T> t){
        return ReactiveSeq.fromIterable(toIterable(t));
    }
    <T> Iterable<T> toIterable(AnyM<W,T> t);
    
    default <T> AnyM<W,T> empty(){
        return this.<T>unit(null)
                   .<T>filter(t->false);
    }
    
    <T> AnyM<W,T> unitIterable(Iterable<T> it);

  

}
