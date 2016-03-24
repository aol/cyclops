package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.stream.Stream;

import com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX;
import com.aol.cyclops.types.Unit;

public interface FluentCollectionX<T> extends CollectionX<T> {
    <X> FluentCollectionX<X> stream(Stream<X> stream);
    default FluentCollectionX<T> plusLazy(T e){
        add(e);
        return this;
    }
    
    default FluentCollectionX<T> plusAllLazy(Collection<? extends T> list){
        addAll(list);
        return this;
    }
    
    default FluentCollectionX<T> minusLazy(Object e){
        remove(e);
        return this;
    }
    
    default FluentCollectionX<T> minusAllLazy(Collection<?> list){
        removeAll(list);
        return this;
    }

    
	default FluentCollectionX<T> plusInOrder(T e){
		return plus(e);
	}
	public FluentCollectionX<T> plus(T e);
	
	public FluentCollectionX<T> plusAll(Collection<? extends T> list) ;
	
	public FluentCollectionX<T> minus(Object e);
	
	public FluentCollectionX<T> minusAll(Collection<?> list);
	
	public <R> FluentCollectionX<R> unit(Collection<R> col);
}
