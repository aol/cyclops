package com.aol.cyclops2.data.collections.extensions;

import cyclops.control.Option;

import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * A Fluent API for adding and removing toX elements
 * 
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this toX
 */
public interface FluentCollectionX<T> extends CollectionX<T> {

    default FluentCollectionX<T> plusLoop(int max, IntFunction<T> value){
        FluentCollectionX<T> toUse = this;
        for(int i=0;i<max;i++){
            toUse = toUse.plus(value.apply(i));
        }
        return toUse;
    }
    default FluentCollectionX<T> plusLoop(Supplier<Option<T>> supplier){
        FluentCollectionX<T> toUse = this;

        Option<T> next =  supplier.get();
        while(next.isPresent()){
            toUse = toUse.plus(next.orElse(null));
            next = supplier.get();
        }
        return toUse;
    }
    /**
     * Add an element to the toX
     * 
     * @param e Element to add
     * @return Collection with element added
     */
    default FluentCollectionX<T> plusInOrder(final T e) {
        return plus(e);
    }

    /**
     * Add an element to this Collection
     * 
     * @param e Element to add 
     * @return Collection with element added
     */
    public FluentCollectionX<T> plus(T e);

    /**
     * Add all supplied elements to this Collection
     * 
     * @param list of elements to add
     * @return Collection with elements added
     */
    public FluentCollectionX<T> plusAll(Iterable<? extends T> list);

    /**
     * Remove the specified element from this toX
     * 
     * @param e Element to removeValue
     * @return Collection with element removed
     */
    public FluentCollectionX<T> removeValue(T e);

    /**
     * Remove all the specified elements from this toX
     * 
     * @param list of elements to removeValue
     * @return Collection with the elements removed
     */
    public FluentCollectionX<T> removeAll(Iterable<? extends T> list);

    /**
     * Create a new instance of the same colleciton type from the supplied toX
     * 
     * @param col Collection data to populate the new toX
     * @return Collection as the same type as this toX
     */
    public <R> FluentCollectionX<R> unit(Iterable<R> col);
}
