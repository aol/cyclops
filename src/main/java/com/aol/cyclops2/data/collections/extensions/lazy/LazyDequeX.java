package com.aol.cyclops2.data.collections.extensions.lazy;


import cyclops.collections.mutable.DequeX;
import cyclops.stream.ReactiveSeq;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.stream.Collector;
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
 *    DequeX<Integer> q = DequeX.of(1,2,3)
 *                              .map(i->i*2);
 *                              .filter(i->i<5);
 * }
 * </pre>
 *
 * The operation above is more efficient than the equivalent operation with a ListX.
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
@EqualsAndHashCode(of = { "deque" })
public class LazyDequeX<T> extends AbstractLazyMutableCollection<T,Deque<T>> implements DequeX<T> {


    public LazyDequeX(Deque<T> list, ReactiveSeq<T> seq, Collector<T, ?, Deque<T>> collector) {
        super(list, seq, collector);

    }
    public LazyDequeX(Deque<T> list, Collector<T, ?, Deque<T>> collector) {
        super(list, null, collector);

    }

    public LazyDequeX(ReactiveSeq<T> seq, Collector<T, ?, Deque<T>> collector) {
        super(null, seq, collector);

    }

    @Override
    public LazyDequeX<T> withCollector(Collector<T, ?, Deque<T>> collector){
        return (LazyDequeX)new LazyDequeX<T>(this.getList(),this.getSeq().get(),collector);
    }
    //@Override
    public DequeX<T> materialize() {
        get();
        return this;
    }

    @Override
    public <T1> Collector<T1, ?, Deque<T1>> getCollector() {
        return (Collector)super.getCollectorInternal();
    }



    @Override
    public <X> LazyDequeX<X> fromStream(Stream<X> stream) {

        return new LazyDequeX<X>((Deque)getList(),ReactiveSeq.fromStream(stream),(Collector)this.getCollectorInternal());
    }

    @Override
    public <T1> LazyDequeX<T1> from(Collection<T1> c) {
        if(c instanceof Deque)
            return new LazyDequeX<T1>((Deque)c,null,(Collector)this.getCollectorInternal());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <U> LazyDequeX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyDequeX<R> unit(Collection<R> col) {
        return from(col);
    }

    @Override
    public void addFirst(T t) {
        get().addFirst(t);
    }

    @Override
    public void addLast(T t) {
        get().addLast(t);
    }

    @Override
    public boolean offerFirst(T t) {
        return get().offerFirst(t);
    }

    @Override
    public boolean offerLast(T t) {
        return get().offerLast(t);
    }

    @Override
    public T removeFirst() {
        return get().removeFirst();
    }

    @Override
    public T removeLast() {
        return get().removeLast();
    }

    @Override
    public T pollFirst() {
        return get().pollFirst();
    }

    @Override
    public T pollLast() {
        return get().pollLast();
    }

    @Override
    public T getFirst() {
        return get().getFirst();
    }

    @Override
    public T getLast() {
        return get().getLast();
    }

    @Override
    public T peekFirst() {
        return get().peekFirst();
    }

    @Override
    public T peekLast() {
        return get().peekLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return get().removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return get().removeLastOccurrence(o);
    }

    @Override
    public boolean offer(T t) {
        return get().offer(t);
    }

    @Override
    public T remove() {
        return get().remove();
    }

    @Override
    public T poll() {
        return get().poll();
    }

    @Override
    public T element() {
        return get().element();
    }

    @Override
    public T peek() {
        return get().peek();
    }

    @Override
    public void push(T t) {
        get().push(t);
    }

    @Override
    public T pop() {
        return get().pop();
    }

    @Override
    public Iterator<T> descendingIterator() {
        return get().descendingIterator();
    }
}
