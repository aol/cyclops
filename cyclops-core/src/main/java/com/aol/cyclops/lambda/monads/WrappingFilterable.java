package com.aol.cyclops.lambda.monads;

import java.util.function.Predicate;

import com.aol.cyclops.types.Filterable;

public interface WrappingFilterable<T> extends Filterable<T> {
	default   Filterable<T>  filter(Predicate<? super T> fn) {
		T filterable = (T)new ComprehenderSelector().selectComprehender(
				getFilterable())
				.filter(getFilterable(), fn);
		return withFilterable( filterable );
	}

	//ofType
	public Filterable<T> withFilterable(T filter);

	public Object getFilterable();
}
