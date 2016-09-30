package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;

/**
 * A Fluent API for adding and removing collection elements
 * 
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface FluentCollectionX<T> extends CollectionX<T> {

    /**
     * Add an element to the collection
     * 
     * @param e Element to add
     * @return Collection with element added
     */
    default FluentCollectionX<T> plusInOrder(T e) {
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
    public FluentCollectionX<T> plusAll(Collection<? extends T> list);

    /**
     * Remove the specified element from this collection
     * 
     * @param e Element to remove
     * @return Collection with element removed
     */
    public FluentCollectionX<T> minus(Object e);

    /**
     * Remove all the specified elements from this collection
     * 
     * @param list of elements to remove
     * @return Collection with the elements removed
     */
    public FluentCollectionX<T> minusAll(Collection<?> list);

    /**
     * Create a new instance of the same colleciton type from the supplied collection
     * 
     * @param col Collection data to populate the new collection
     * @return Collection as the same type as this collection
     */
    public <R> FluentCollectionX<R> unit(Collection<R> col);
}
