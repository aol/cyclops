package com.aol.cyclops.collections.extensions;

import java.util.Collection;

public interface FluentCollectionX<T> extends CollectionX<T> {

	public FluentCollectionX<T> plus(T e);
	
	public FluentCollectionX<T> plusAll(Collection<? extends T> list) ;
	
	public FluentCollectionX<T> minus(Object e);
	
	public FluentCollectionX<T> minusAll(Collection<?> list);
}
