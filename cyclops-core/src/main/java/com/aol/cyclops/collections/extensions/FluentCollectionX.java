package com.aol.cyclops.collections.extensions;

import java.util.Collection;

import com.aol.cyclops.lambda.monads.Unit;

public interface FluentCollectionX<T> extends CollectionX<T> {

	default FluentCollectionX<T> plusInOrder(T e){
		return plus(e);
	}
	public FluentCollectionX<T> plus(T e);
	
	public FluentCollectionX<T> plusAll(Collection<? extends T> list) ;
	
	public FluentCollectionX<T> minus(Object e);
	
	public FluentCollectionX<T> minusAll(Collection<?> list);
	
	public <R> FluentCollectionX<R> unit(Collection<R> col);
}
