package com.aol.cyclops.data.collections.extensions.lazy;


import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An extended List type {@see java.util.List}
 * Extended List operations execute lazily e.g.
 * <pre>
 * {@code
 *    LazyListX<Integer> q = LazyListX.of(1,2,3)
 *                                      .map(i->i*2);
 * }
 * </pre>
 * The map operation above is not executed immediately. It will only be executed when (if) the data inside the
 * queue is accessed. This allows lazy operations to be chained and executed more efficiently e.g.
 *
 * <pre>
 * {@code
 *    LazyListX<Integer> q = LazyListX.of(1,2,3)
 *                                      .map(i->i*2);
 *                                      .filter(i->i<5);
 * }
 * </pre>
 *
 * The operation above is more efficient than the equivalent operation with a ListX.
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
//@AllArgsConstructor(access= AccessLevel.PRIVATE)
//@Wither
public class LazyListX<T> extends AbstractLazyCollection<T,List<T>> implements ListX<T> {


    public LazyListX(List<T> list, ReactiveSeq<T> seq, Collector<T, ?, List<T>> collector) {
        super(list, seq, collector);
        System.out.println("New lazy list " + list);
    }

    @Override
    public LazyListX<T> withCollector(Collector<T, ?, List<T>> collector){
        return (LazyListX)new LazyListX<T>(this.getList(),this.getSeq().get(),collector);
    }
    //@Override
    public ListX<T> materialize() {
        get();
        return this;
    }

    @Override
    public <T1> Collector<T1, ?, List<T1>> getCollector() {
        return (Collector)super.getCollectorInternal();
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        get().replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        get().sort(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return get().addAll(index,c);
    }

    @Override
    public T get(int index) {
        return get().get(index);
    }

    @Override
    public T set(int index, T element) {
        return get().set(index,element);
    }

    @Override
    public void add(int index, T element) {
        get().add(index,element);
    }

    @Override
    public T remove(int index) {
        return get().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return get().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return get().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return get().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return get().listIterator(index);
    }
    @Override
    public ListX<T> subList(final int fromIndex, final int toIndex) {
        List<T> list = get().subList(fromIndex, toIndex);
      return from(list);
    }
    /**
    @Override
    public ReactiveSeq<T> stream() {
        return lazy.stream();
    }
**/


    @Override
    public <X> LazyListX<X> fromStream(Stream<X> stream) {
        System.out.println("From stream!");
        return new LazyListX<X>((List)getList(),ReactiveSeq.fromStream(stream),(Collector)this.getCollectorInternal());
    }

    @Override
    public <T1> LazyListX<T1> from(Collection<T1> c) {
        if(c instanceof List)
            return new LazyListX<T1>((List)c,null,(Collector)this.getCollectorInternal());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <U> LazyListX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }


    public static void main(String[] args){

        ReactiveSeq<Integer> stream = ReactiveSeq.of(1,2,3);
        stream.map(i->i*2).printOut();
        ReactiveSeq<Integer> r2 = stream.map(i->i*2);
        r2.map(i->i*100).zipWithIndex().printOut();
        r2.map(i->i*1000).zipWithIndex().printOut();
       /**
        LazyListX<Integer> l2 = new LazyListX<>(null,ReactiveSeq.of(1,2,3), Collectors.toList());
        l2.map(i->i*100).printOut();
        l2.map(i->i*1000).printOut();
**/
        /**
       // LazyListX<Integer> list = new LazyListX<>(null,ReactiveSeq.of(1,2,3), Collectors.toList());
        LazyListX<Integer> list = new LazyListX<>(Arrays.asList(1,2,3),null, Collectors.toList());

        list.map(i->i*2).printOut();
        ListX<Integer> l2 = list.map(i->i*2);
        System.out.println(l2.getClass());
        list.map(i->i*3)
            .peek(System.out::println)
        .forEach(System.err::println);

        l2.map(i->i*100).printOut();
        System.out.println(l2.getClass() + " " + ((LazyListX)l2).getList());
        l2.map(i->i*1000).printOut();
**/
    }

    @Override
    public <R> LazyListX<R> unit(Collection<R> col) {
        return from(col);
    }
}
