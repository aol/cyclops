package com.aol.cyclops.types.extensability;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.WitnessType;

/**
 * Interface for defining how Comprehensions should work for a type
 * Cyclops For Comprehensions will supply either a JDK 8 Predicate or Function
 * for filter / map / flatMap
 * The comprehender should wrap these in a suitable type and make the call to the
 * underlying Monadic Type (T) the Comprehender implementation supports.
 * 
 * E.g. To support mapping for the Functional Java Option type wrap the supplied JDK 8 Function in a Functional Java
 * fj.F type, call the make call to option.map( ) and retun the result.
 * 
 * <pre>{@code
 *  OptionComprehender<Option> {
 *    
 *     public Object map(Option o, Function fn){
 *        return o.map( a-> fn.apply(a));
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
public interface Comprehender<W extends WitnessType> {
    
    default <T,T2,R> AnyM<W,R> ap2(AnyM<W, Function<? super T,? extends Function<? super T2,? extends R>>> fn, AnyM<W,T> apply,AnyM<W,T2> apply2){
        return  ap(ap(fn, apply), apply2);
    }
    public <T,R> AnyM<W,R> ap(AnyM<W, Function<? super T,? extends R>> fn, AnyM<W,T> apply);
    default <T> AnyM<W,T> filter(AnyM<W,T> t,  Predicate<? super T> fn){
        return t;
    }
    
    public <T,R> AnyM<W,R> map(AnyM<W,T> t,  Function<? super T, ? extends R> fn);

   
    public <T,R> AnyM<W,R> flatMap(AnyM<W,T> t, Function<? super T, ? extends AnyM<W,? extends R>> fn);

   

    public <T> AnyM<W,T> unit(Object o);

    

    public <T> AnyM<W,T> empty();

  

}
