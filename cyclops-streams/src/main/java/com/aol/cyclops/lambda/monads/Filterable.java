package com.aol.cyclops.lambda.monads;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;

import com.aol.cyclops.streams.StreamUtils;

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
	<U> Filterable<U> ofType(Class<U> type);
	
	default Filterable<T>  filterNot(Predicate<? super T> fn){
		return filter(fn.negate());
	}
	default Filterable<T> notNull(){
		return filter(t->t!=null);
	}
	default Filterable<T> removeAll(Stream<T> stream){
		Set<T> set= stream.collect(Collectors.toSet());
		return filterNot(i-> set.contains(i));
	}
	default  Filterable<T> removeAll(Iterable<T> it){
		return removeAll(StreamUtils.stream(it));
	}
	
	default  Filterable<T> removeAll(T... values){
		return removeAll(Stream.of(values));
		
	}
	default  Filterable<T> retainAll(Iterable<T> it){
		return retainAll(StreamUtils.stream(it));
	}

	default  Filterable<T> retainAll(Stream<T> stream){
		Set<T> set= stream.collect(Collectors.toSet());
		return filter(i-> set.contains(i));
	}
	default  Filterable<T> retainAll(T... values){
		return retainAll(Stream.of(values));
	}
	default Filterable<T> retainMatches(Matcher<T> m){
		return filter(t->m.matches(t));
	}
	default Filterable<T> removeMatches(Matcher<T> m){
		return filter(t->!m.matches(t));
	}
}
