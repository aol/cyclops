package com.aol.cyclops.lambda.monads;

import java.util.function.Predicate;

import lombok.val;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;

/**
 * Trait that represents any class with a single argument Filter method
 * Will coerce that method into accepting JDK 8 java.util.function.Predicates
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Filterable<T> {
	
	default   Filterable<T>  filter(Predicate<? super T> fn) {
		T filterable = (T)new ComprehenderSelector().selectComprehender(
				getFilterable())
				.filter(getFilterable(), fn);
		return withFilterable( filterable );
	}

	public Filterable<T> withFilterable(T filter);

	public Object getFilterable();
}
