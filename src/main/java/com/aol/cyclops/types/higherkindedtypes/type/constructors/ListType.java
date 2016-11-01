package com.aol.cyclops.types.higherkindedtypes.type.constructors;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.derive4j.hkt.Higher;
import org.derive4j.hkt.__;

import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Simulates Higher Kinded Types for List's
 * 
 * ListType is a List and a Higher Kinded Type (ListType.µ,T)
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the List
 */

public interface ListType<T> extends Higher<ListType.µ, T>, List<T> {
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class µ {
    }

    /**
     * Convert a List to a simulated HigherKindedType that captures List nature
     * and List element data type separately. Recover via @see ListType#narrow
     * 
     * If the supplied List implements ListType it is returned already, otherwise it
     * is wrapped into a List implementation that does implement ListType
     * 
     * @param list List to widen to a ListType
     * @return ListType encoding HKT info about Lists
     */
    public static <T> ListType<T> widen(final List<T> list) {
        if (list instanceof ListType)
            return (ListType<T>) list;
        return new Box<>(
                         ListX.fromIterable(list));
    }

    /**
     * Convert the HigherKindedType definition for a List into
     * 
     * @param list Type Constructor to convert back into narrowed type
     * @return ListX from Higher Kinded Type
     */
    public static <T> ListX<T> narrow(final Higher<ListType.µ, T> list) {
        if (list instanceof List)
            return ListX.fromIterable((List) list);
        final Box<T> type = (Box<T>) list;
        return type.narrow();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Box<T> implements ListType<T> {

        private final List<T> boxed;

        /**
         * @return This back as a ListX
         */
        public ListX<T> narrow() {
            return ListX.fromIterable(boxed);
        }

        
        @Override
        public int size() {
            return boxed.size();
        }

        
        @Override
        public boolean isEmpty() {
            return boxed.isEmpty();
        }

        
        @Override
        public boolean contains(final Object o) {
            return boxed.contains(o);
        }

        
        @Override
        public Iterator<T> iterator() {
            return boxed.iterator();
        }

        
        @Override
        public Object[] toArray() {
            return boxed.toArray();
        }

        
        @Override
        public <T> T[] toArray(final T[] a) {
            return boxed.toArray(a);
        }

        
        @Override
        public boolean add(final T e) {
            return boxed.add(e);
        }

        
        @Override
        public boolean remove(final Object o) {
            return boxed.remove(o);
        }

        /**
         * @param c
         * @return
         * @see java.util.List#containsAll(java.util.Collection)
         */
        @Override
        public boolean containsAll(final Collection<?> c) {
            return boxed.containsAll(c);
        }

        
        @Override
        public boolean addAll(final Collection<? extends T> c) {
            return boxed.addAll(c);
        }

        
        @Override
        public boolean addAll(final int index, final Collection<? extends T> c) {
            return boxed.addAll(index, c);
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            return boxed.removeAll(c);
        }

       
        @Override
        public boolean retainAll(final Collection<?> c) {
            return boxed.retainAll(c);
        }

        
        @Override
        public void clear() {
            boxed.clear();
        }

        
        @Override
        public boolean equals(final Object o) {
            return boxed.equals(o);
        }

        
        @Override
        public int hashCode() {
            return boxed.hashCode();
        }

        
        @Override
        public T get(final int index) {
            return boxed.get(index);
        }

        
        @Override
        public T set(final int index, final T element) {
            return boxed.set(index, element);
        }

        
        @Override
        public void add(final int index, final T element) {
            boxed.add(index, element);
        }

        
        @Override
        public T remove(final int index) {
            return boxed.remove(index);
        }

        
        @Override
        public int indexOf(final Object o) {
            return boxed.indexOf(o);
        }

        
        @Override
        public int lastIndexOf(final Object o) {
            return boxed.lastIndexOf(o);
        }

       
        @Override
        public ListIterator<T> listIterator() {
            return boxed.listIterator();
        }

        
        @Override
        public ListIterator<T> listIterator(final int index) {
            return boxed.listIterator(index);
        }

        
        @Override
        public List<T> subList(final int fromIndex, final int toIndex) {
            return boxed.subList(fromIndex, toIndex);
        }

    }

}
