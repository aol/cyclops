package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ListXImpl<T> implements ListX<T> {

    private final List<T> list;
    @Getter
    private final Collector<T, ?, List<T>> collector;

    public ListXImpl(List<T> list) {
        this.list = list;
        this.collector = ListX.defaultCollector();
    }

    public ListXImpl() {
        this.collector = ListX.defaultCollector();
        this.list = (List) this.collector.supplier()
                                         .get();
    }

    
    @Override
    public void forEach(Consumer<? super T> action) {
        list.forEach(action);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean contains(Object e) {
        return list.contains(e);
    }

    @Override
    public boolean equals(Object o) {
        return list.equals(o);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T e) {
        return list.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    
    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }

    
    @Override
    public long count() {
        return this.size();
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        list.replaceAll(operator);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return list.removeIf(filter);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        list.sort(c);
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        list.add(index, element);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public Stream<T> parallelStream() {
        return list.parallelStream();
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public ListX<T> subList(int fromIndex, int toIndex) {
        return new ListXImpl<>(
                               list.subList(fromIndex, toIndex), getCollector());
    }

    @Override
    public Spliterator<T> spliterator() {
        return list.spliterator();
    }

   
    @Override
    public int compareTo(T o) {
        if (o instanceof List) {
            List l = (List) o;
            if (this.size() == l.size()) {
                Iterator i1 = iterator();
                Iterator i2 = l.iterator();
                if (i1.hasNext()) {
                    if (i2.hasNext()) {
                        int comp = Comparator.<Comparable> naturalOrder()
                                             .compare((Comparable) i1.next(), (Comparable) i2.next());
                        if (comp != 0)
                            return comp;
                    }
                    return 1;
                } else {
                    if (i2.hasNext())
                        return -1;
                    else
                        return 0;
                }
            }
            return this.size() - ((List) o).size();
        } else
            return 1;

    }

}
