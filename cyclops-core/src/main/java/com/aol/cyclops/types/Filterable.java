package com.aol.cyclops.types;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;

import com.aol.cyclops.util.stream.StreamUtils;

/**
 * Trait that represents any class with a single argument Filter method
 * Will coerce that method into accepting JDK 8 java.util.function.Predicates
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Filterable<T> {
	
	Filterable<T>  filter(Predicate<? super T> fn);
	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * // (1, 2, 3) SequenceM.of(1, "a", 2, "b",3).ofType(Integer.class)
	 * 
	 */
	@SuppressWarnings("unchecked")
	default <U> Filterable<U> ofType(Class<U> type){
		return (Filterable<U>)filter(type::isInstance);
	}
	
	default Filterable<T>  filterNot(Predicate<? super T> fn){
		return filter(fn.negate());
	}
	default Filterable<T> notNull(){
		return filter(t->t!=null);
	}
	
}
