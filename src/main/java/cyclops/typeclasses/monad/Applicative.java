package cyclops.typeclasses.monad;

import com.aol.cyclops.hkt.Higher;
import cyclops.typeclasses.Unit;
import cyclops.typeclasses.functor.Functor;

import java.util.function.BiFunction;
import java.util.function.Function;



public interface Applicative<CRE> extends Functor<CRE>,Unit<CRE> {
    
    /**
     * Narrow the co/contra variance on Function stored within a HKT encoded type 
     * 
     * @param broad HKT encoded type with function to narrow variance on
     * @return HKT encoded type with narrowed function type
     */
    public static <CRE,T,R> Higher<CRE, Function<T,R>> narrow(Higher<CRE, Function<? super T, ? extends R>> broad){
        return (Higher)broad;
    }
    /**
     * Narrow the co/contra variance on Function stored within a HKT encoded type 
     * 
     * @param broad HKT encoded type with function to narrow variance on
     * @return HKT encoded type with narrowed function type
     */
    public static <CRE,T,T2,R> Higher<CRE, Function<T,Function< T2,R>>> narrow2(Higher<CRE, Function<? super T, ? extends Function<? super T2, ? extends R>>> broad){
        return (Higher)broad;
    }
    /**
     * Narrow the co/contra variance on Function stored within a HKT encoded type 
     * 
     * @param broad HKT encoded type with function to narrow variance on
     * @return HKT encoded type with narrowed function type
     */
    public static <CRE,T,T2,T3,R> Higher<CRE, Function<T,Function<T2,Function<T3,R>>>> narrow3(Higher<CRE, Function<? super T, ? extends Function<? super T2, ? extends Function<? super T3, ? extends R>>>> broad){
        return (Higher)broad;
    }
    /**
     * Narrow the co/contra variance on BiFunction stored within a HKT encoded type 
     * 
     * @param broad HKT encoded type with function to narrow variance on
     * @return HKT encoded type with narrowed function type
     */
    public static <CRE,T,T2,R> Higher<CRE, BiFunction<T,T2,R>> narrowBiFn(Higher<CRE, BiFunction<? super T, ? super T2, ? extends R>> fn, Higher<CRE, T> apply, Higher<CRE, T2> broad){
        return (Higher)broad;
    }
    
    public <T,R> Higher<CRE,R> ap(Higher<CRE, Function<T, R>> fn, Higher<CRE, T> apply);
    
   
    
    /**
     * The default implementation of apBiFn is less efficient than ap2 (extra map operation)
     * 
     * @param fn
     * @param apply
     * @param apply2
     * @return
     */
    default <T,T2,R> Higher<CRE,R> apBiFn(Higher<CRE, BiFunction<T, T2, R>> fn, Higher<CRE, T> apply, Higher<CRE, T2> apply2){
        return  ap(ap(map(Applicative::curry2,fn), apply), apply2);
    }
    
    default <T,T2,R> Higher<CRE,R> ap2(Higher<CRE, Function<T, Function<T2, R>>> fn, Higher<CRE, T> apply, Higher<CRE, T2> apply2){
        return  ap(ap(fn, apply), apply2);
    }
    default <T,T2,T3,R> Higher<CRE,R> ap3(Higher<CRE, Function<T, Function<T2, Function<T3, R>>>> fn,
                                          Higher<CRE, T> apply, Higher<CRE, T2> apply2, Higher<CRE, T3> apply3){
        return  ap(ap(ap(fn, apply), apply2),apply3);
    }
  
    
    public static <T1, T2, R> Function< T1, Function<T2, R>> curry2(
            final BiFunction<T1, T2, R> biFunc) {
        return t1 -> t2 -> biFunc.apply(t1, t2);
    }

}
