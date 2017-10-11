package cyclops.typeclasses.monad;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.lazy.Eval;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.functor.Functor;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;

import java.util.function.BiFunction;
import java.util.function.Function;



public interface Applicative<CRE> extends Functor<CRE>,Pure<CRE> {

    default <T, R> Higher<CRE,Tuple2<T, R>> zip(Higher<CRE, T> fa, Higher<CRE, R> fb) {
        return ap(ap(unit(a-> b-> Tuple.tuple(a,b)), fa),fb);

    }
    default <T1, T2, R> Higher<CRE, R> zip(Higher<CRE, T1> fa, Higher<CRE, T2> fb, BiFunction<? super T1,? super T2,? extends R> f) {
        return map_(zip(fa, fb), in-> f.apply(in._1(),in._2()));
    }
    default <T1,T2,R> Eval<Higher<CRE,R>> lazyZip(Higher<CRE,T1> f1, Eval<Higher<CRE,T2>> lazy, BiFunction<? super T1,? super T2,? extends R> fn) {
        return lazy.map(e-> zip(f1,e,fn));
    }
    /**
     * Narrow the co/contra variance on Function stored within a HKT encoded type 
     * 
     * @param broad HKT encoded type with function to narrow variance on
     * @return HKT encoded type with narrowed function type
     */
    public static <CRE,T,R> Higher<CRE, Function<T,R>> narrow(Higher<CRE,? extends Function<? super T, ? extends R>> broad){
        return (Higher)broad;
    }
    /**
     * Narrow the co/contra variance on Function stored within a HKT encoded type 
     * 
     * @param broad HKT encoded type with function to narrow variance on
     * @return HKT encoded type with narrowed function type
     */
    public static <CRE,T,T2,R> Higher<CRE, Function<T,Function< T2,R>>> narrow2(Higher<CRE, ? extends Function<? super T, ? extends Function<? super T2, ? extends R>>> broad){
        return (Higher)broad;
    }
    /**
     * Narrow the co/contra variance on Function stored within a HKT encoded type 
     * 
     * @param broad HKT encoded type with function to narrow variance on
     * @return HKT encoded type with narrowed function type
     */
    public static <CRE,T,T2,T3,R> Higher<CRE, Function<T,Function<T2,Function<T3,R>>>> narrow3(Higher<CRE, ? extends Function<? super T, ? extends Function<? super T2, ? extends Function<? super T3, ? extends R>>>> broad){
        return (Higher)broad;
    }
    /**
     * Narrow the co/contra variance on BiFunction stored within a HKT encoded type 
     * 
     * @param broad HKT encoded type with function to narrow variance on
     * @return HKT encoded type with narrowed function type
     */
    public static <CRE,T,T2,R> Higher<CRE, BiFunction<T,T2,R>> narrowBiFn(Higher<CRE, ? extends BiFunction<? super T, ? super T2, ? extends R>> fn, Higher<CRE, T> apply, Higher<CRE, T2> broad){
        return (Higher)broad;
    }
    
    public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function<T, R>> fn, Higher<CRE, T> apply);



    
    /**
     * The default implementation of apBiFn is less efficient than ap2 (extra transform operation)
     * 
     * @param fn
     * @param apply
     * @param apply2
     * @return
     */
    default <T,T2,R> Higher<CRE,R> apBiFn(Higher<CRE, ? extends BiFunction<T, T2, R>> fn, Higher<CRE, T> apply, Higher<CRE, T2> apply2){
        return  ap(ap(map(Applicative::curry2,fn), apply), apply2);
    }
    
    default <T,T2,R> Higher<CRE,R> ap2(Higher<CRE, ? extends Function<T, ? extends Function<T2, R>>> fn, Higher<CRE, T> apply, Higher<CRE, T2> apply2){
        Higher<CRE,Function<T,  Function<T2, R>>> noVariance = (Higher<CRE, Function<T, Function<T2, R>>>) fn;
        return  ap(ap(noVariance, apply), apply2);
    }
    default <T,T2,T3,R> Higher<CRE,R> ap3(Higher<CRE, ? extends Function<T, ? extends Function<T2, ? extends Function<T3, R>>>> fn,
                                          Higher<CRE, T> apply, Higher<CRE, T2> apply2, Higher<CRE, T3> apply3){
        Higher<CRE, Function<T, Function<T2, Function<T3, R>>>> fnToUse =
                (Higher<CRE,  Function<T,  Function<T2, Function<T3, R>>>>) fn;
        Higher<CRE, Function<T2,Function<T3, R>>> ap1 = ap(fnToUse, apply);
        Higher<CRE, Function<T3, R>> ap2 = ap(ap1, apply2);
        return  ap(ap2,apply3);
    }
  
    
    public static <T1, T2, R> Function< T1, Function<T2, R>> curry2(
            final BiFunction<T1, T2, R> biFunc) {
        return t1 -> t2 -> biFunc.apply(t1, t2);
    }

}
