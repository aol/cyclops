package com.aol.cyclops2.hkt;

import java.util.function.BiFunction;
import java.util.function.Function;

//import org.derive4j.hkt.__;


/**
 * Higher Kinded Type - a core type (e.g. a List) and a data type of the elements within the List (e.g. Integers).
 * A fluent semantic alias for  org.derive4j.hkt.__ (awaiting https://github.com/derive4j/hkt/issues/13).
 *
 * @author johnmcclean
 *
 * @param <T1> Core type
 * @param <T2> Data type of elements in Core Type
 */
public interface Higher2<T1,T2,T3> extends Higher<Higher<T1,T2>,T3>{// , __<T1, T2>{


}