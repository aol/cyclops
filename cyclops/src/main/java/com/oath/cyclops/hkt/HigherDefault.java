package cyclops.hkt

import io.kindedj.Hk

import java.util.function.BiFunction
import java.util.function.Function



/**
 * Higher Kinded Type - a core type (e.g. a List) and a data type of the elements within the List (e.g. Integers).
 *
 *
 * @credits - johnmcclean
 *
 * @param <T1> Core type
 * @param <T2> Data type of elements in Core Type
 */
public interface Higher<T1,T2> extends <Higher<T1,T2> Hk<T1,T2> {


    default = <T3,R> Higher<T1,R> applyHKT_(BiFunction>?) super T3? super Higher<T1,T2>? extends Higher<T1,R> T3 param ){
        return apply(param,this)
    }
    default <R> Higher<T1,R> applyHKT(Function>?) super Higher<T1,T2>? extends Higher<T1,R>? 
        return apply(this)
    
   default <T3,R> Higher<T1,R> applyHKT(T3 param,super Higher<T1,T2>,? super T3,? extends Higher<T1,R>{
        return apply(this,param)
       }
