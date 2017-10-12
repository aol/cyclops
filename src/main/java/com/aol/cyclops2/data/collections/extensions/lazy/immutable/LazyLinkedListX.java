package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * An extended List type {@see java.util.List}
 * Extended List operations execute lazily e.g.
 * <pre>
 * {@code
 *    StreamX<Integer> q = StreamX.of(1,2,3)
 *                                      .transform(i->i*2);
 * }
 * </pre>
 * The transform operation above is not executed immediately. It will only be executed when (if) the data inside the
 * queue is accessed. This allows lazy operations to be chained and executed more efficiently e.g.
 *
 * <pre>
 * {@code
 *    DequeX<Integer> q = DequeX.of(1,2,3)
 *                              .transform(i->i*2);
 *                              .filter(i->i<5);
 * }
 * </pre>
 *
 * The operation above is more efficient than the equivalent operation with a ListX.
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this toX
 */
public class LazyLinkedListX<T> extends AbstractLazyPersistentCollection<T,PStack<T>> implements LinkedListX<T> {

    private final FoldToList<T> generator;

    public static final <T> Function<ReactiveSeq<PStack<T>>, PStack<T>> asyncLinkedList() {
        return r -> {
            CompletableLinkedListX<T> res = new CompletableLinkedListX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asLinkedListX();
        };
    }
    public LazyLinkedListX(PStack<T> list, ReactiveSeq<T> seq, Reducer<PStack<T>> reducer, FoldToList<T> generator,Evaluation strict) {
        super(strict,list, seq, reducer,asyncLinkedList());
        this.generator = generator;

        handleStrict();



    }
    public LazyLinkedListX(PStack<T> list, ReactiveSeq<T> seq, Reducer<PStack<T>> reducer,Evaluation strict) {
        super(strict,list, seq, reducer,asyncLinkedList());
        this.generator = new PStackGeneator<>();
        handleStrict();


    }
    public class PStackGeneator<T> implements FoldToList<T> {
         public PStack<T> from(final Iterator<T> i,int depth) {

            if(!i.hasNext())
                return ConsPStack.empty();
            T e = i.next();
            return  from(i,depth++).plus(e);
        }
    }
   
    public PStack<T> materializeList(ReactiveSeq<T> toUse){

        return toUse.visit(s -> {
            PStack<T> res = generator.from(toUse.iterator(),0);
            return new LazyLinkedListX<T>(
                    res,null, this.getCollectorInternal(),generator, evaluation());
                },
                r -> super.materializeList(toUse),
                a -> super.materializeList(toUse));



    }
    

    //@Override
    public LinkedListX<T> materialize() {
        get();
        return this;
    }


    @Override
    public LinkedListX<T> lazy() {
        return new LazyLinkedListX<T>(list,seq.get(),getCollectorInternal(),Evaluation.LAZY) ;
    }

    @Override
    public LinkedListX<T> eager() {
        return new LazyLinkedListX<T>(list,seq.get(),getCollectorInternal(),Evaluation.EAGER) ;
    }

    @Override
    public LinkedListX<T> type(Reducer<? extends PStack<T>> reducer) {
        return new LazyLinkedListX<T>(list,seq.get(),Reducer.narrow(reducer),generator, evaluation());
    }

    //  @Override
    public <X> LazyLinkedListX<X> fromStream(ReactiveSeq<X> stream) {

        Reducer<PStack<T>> reducer = getCollectorInternal();
        return new LazyLinkedListX<X>((PStack<X>)getList(),(ReactiveSeq)stream,(Reducer)reducer,(FoldToList)generator, evaluation());
    }

    @Override
    public <T1> LazyLinkedListX<T1> from(Collection<T1> c) {
        if(c instanceof PStack)
            return new LazyLinkedListX<T1>((PStack)c,null,(Reducer)getCollectorInternal(),(FoldToList)generator, evaluation());
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
    public <U> LazyLinkedListX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyLinkedListX<R> unit(Collection<R> col) {
        return from(col);
    }

    @Override
    public LinkedListX<T> plusLoop(int max, IntFunction<T> value) {
        return (LinkedListX<T>)super.plusLoop(max,value);
    }

    @Override
    public LinkedListX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (LinkedListX<T>)super.plusLoop(supplier);
    }

    @Override
    public T getOrElse(int index, T value) {
        List<T> x = get();
        if(index>x.size())
            return value;
        return x.get(index);
    }

    @Override
    public T getOrElseGet(int index, Supplier<? extends T> supplier) {
        List<T> x = get();
        if(index>x.size())
            return supplier.get();
        return x.get(index);
    }
}
