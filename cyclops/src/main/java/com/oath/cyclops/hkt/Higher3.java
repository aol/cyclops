package com.oath.cyclops.hkt;

//import org.derive4j.hkt.__;


/**
 * Higher Kinded Type - a core type (e.g. a List) and a data type of the elements within the List (e.g. Integers).
 *
 *
 * @author johnmcclean
 *
 * @param <T1> Core type
 * @param <T2> Data type of elements in Core Type
 */
public interface Higher3<T1,T2,T3,T4> extends Higher2<Higher<T1,T2>,T3,T4>{// , __<T1, T2>{


}
