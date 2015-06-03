package com.aol.cyclops.matcher;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface for implementing StreamOfResponsibility
 * 
 * Implementing Classes will be used as Predicate &amp; Action (Function) 
 * when building Cases.
 * 
 * @author johnmcclean
 *
 * @param <T> Input Value
 * @param <R> Return Value
 */
public interface ChainOfResponsibility<T,R> extends Predicate<T>, Function<T,R> {

}
