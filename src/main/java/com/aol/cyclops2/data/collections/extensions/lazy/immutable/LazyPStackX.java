package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import com.aol.cyclops2.types.mixins.Printable;
import cyclops.collections.immutable.LinkedListX;
import cyclops.companion.Reducers;
import cyclops.function.Reducer;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import lombok.Getter;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
 * queue is accessed. This allows lazy operations toNested be chained and executed more efficiently e.g.
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
public class LazyPStackX<T> extends AbstractLazyPersistentCollection<T,PStack<T>> implements LinkedListX<T> {



    public LazyPStackX(PStack<T> list, ReactiveSeq<T> seq) {
        super(list, seq, Reducers.toPStack());


    }
    public LazyPStackX(PStack<T> list, ReactiveSeq<T> seq, Reducer<PStack<T>> reducer) {
        super(list, seq, reducer);


    }
    public LazyPStackX(PStack<T> list) {
        super(list, null, Reducers.toPStack());


    }

    public LazyPStackX(ReactiveSeq<T> seq) {
        super(null, seq, Reducers.toPStack());


    }
    private static <E> PStack<E> from(final Iterator<E> i,int depth,ReactiveSeq<E> toUse) {
        if(depth>1000){
            return Reducers.<E>toPStack().mapReduce(toUse);
        }
        if(!i.hasNext())
            return ConsPStack.empty();
        E e = i.next();
        return from(i,depth++,toUse).plus(e);
    }
    public PStack<T> materializeList(ReactiveSeq<T> toUse){
        PStack<T> res = from(toUse.iterator(),0,toUse);//ConsPStack.<T> empty();
        return new LazyPStackX<>(
                res);
    }


    //@Override
    public LinkedListX<T> materialize() {
        get();
        return this;
    }


    @Override
    public LinkedListX<T> type(Reducer<? extends PStack<T>> reducer) {
        return new LazyPStackX<T>(list,seq.get(),Reducer.narrow(reducer));
    }

    //  @Override
    public <X> LazyPStackX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyPStackX<X>((PStack)getList(),ReactiveSeq.fromStream(stream));
    }

    @Override
    public <T1> LazyPStackX<T1> from(Collection<T1> c) {
        if(c instanceof PStack)
            return new LazyPStackX<T1>((PStack)c,null);
        return fromStream(ReactiveSeq.fromIterable(c));
    }




    @Override
    public LinkedListX<T> minusAll(Collection<?> list) {
        return from(get().minusAll(list));
    }

    @Override
    public LinkedListX<T> minus(Object remove) {
        return from(get().minus(remove));
    }

    @Override
    public LinkedListX<T> with(int i, T e) {
        return from(get().with(i,e));
    }

    @Override
    public LinkedListX<T> plus(int i, T e) {
        return from(get().plus(i,e));
    }

    @Override
    public LinkedListX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public LinkedListX<T> plusAll(Collection<? extends T> list) {
        return from(get().plusAll(list));
    }

    @Override
    public LinkedListX<T> plusAll(int i, Collection<? extends T> list) {
        return from(get().plusAll(i,list));
    }

    @Override
    public LinkedListX<T> minus(int i) {
        return from(get().minus(i));
    }

    @Override
    public LinkedListX<T> subList(int start, int end) {
        return from(get().subList(start,end));
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
    public PStack<T> subList(int start) {
        return get().subList(start);
    }

    @Override
    public <U> LazyPStackX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPStackX<R> unit(Collection<R> col) {
        return from(col);
    }

    @Override
    public LinkedListX<T> plusLoop(int max, IntFunction<T> value) {
        return (LinkedListX<T>)super.plusLoop(max,value);
    }

    @Override
    public LinkedListX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (LinkedListX<T>)super.plusLoop(supplier);
    }
}
