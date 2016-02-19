package com.aol.cyclops.types;

import java.util.function.Predicate;

import org.hamcrest.Matcher;

import com.aol.cyclops.control.ReactiveSeq;

public interface IterableCollectable<T> extends Iterable<T>{
	default ReactiveSeq<T> stream(){
		return ReactiveSeq.fromIterable(this);
	}
	/**
	 * [equivalent to count]
	 * 
	 * @return size
	
	default int size() {
		return stream().size();
	} */

	
	default boolean xMatch(int x,Matcher<? super T> m){
		return stream().xMatch(x,t->m.matches(t));
	}
	/**
	 * Check that there are specified number of matches of predicate in the
	 * Stream
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(ReactiveSeq.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	 * }
	 * </pre>
	 * 
	 */
	default boolean xMatch(int num, Predicate<? super T> c){
		return stream().xMatch(num, c);
	}
}
