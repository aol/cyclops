package com.aol.cyclops.lambda.monads;

import java.util.function.Predicate;

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
	default   Filterable<T>  filter(Predicate<T> fn) {
		return withFilterable((Filterable)new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				getFilterable())
				.filter(getFilterable(), fn));
	}

	public Filterable<T> withFilterable(Filterable filter);

	public Object getFilterable();
}
