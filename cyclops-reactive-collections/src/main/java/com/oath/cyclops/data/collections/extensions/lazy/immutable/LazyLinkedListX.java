package com.oath.cyclops.data.collections.extensions.lazy.immutable;


import com.oath.cyclops.types.persistent.PersistentList;
import com.oath.cyclops.types.foldable.Evaluation;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;


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
 *                                      .map(i->i*2);
 * }
 * </pre>
 * The transform operation above is not executed immediately. It will only be executed when (if) the data inside the
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
 * @param <T> the type of elements held in this toX
 */
public class LazyLinkedListX<T> extends AbstractLazyPersistentCollection<T,PersistentList<T>> implements LinkedListX<T> {

    private final FoldToList<T> generator;

    public static final <T> Function<ReactiveSeq<PersistentList<T>>, PersistentList<T>> asyncLinkedList() {
        return r -> {
            CompletableLinkedListX<T> res = new CompletableLinkedListX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asLinkedListX();
        };
    }
    public LazyLinkedListX(PersistentList<T> list, ReactiveSeq<T> seq, Reducer<PersistentList<T>,T> reducer, FoldToList<T> generator, Evaluation strict) {
        super(strict,list, seq, reducer,asyncLinkedList());
        this.generator = generator;

        handleStrict();



    }
    public LazyLinkedListX(PersistentList<T> list, ReactiveSeq<T> seq, Reducer<PersistentList<T>,T> reducer, Evaluation strict) {
        super(strict,list, seq, reducer,asyncLinkedList());
        this.generator = new PStackGeneator<>();
        handleStrict();


    }
    public class PStackGeneator<T> implements FoldToList<T> {
         public PersistentList<T> from(final Iterator<T> i, int depth) {

             if (!i.hasNext())
                 return Seq.empty();
             return Seq.<T>empty().prependAll(() -> i);

         }
    }

    public PersistentList<T> materializeList(ReactiveSeq<T> toUse){

        return toUse.fold(s -> {
            PersistentList<T> res = generator.from(toUse.iterator(),0);
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
    public LinkedListX<T> type(Reducer<? extends PersistentList<T>,T> reducer) {
        return new LazyLinkedListX<T>(list,seq.get(),Reducer.narrow(reducer),generator, evaluation());
    }

    //  @Override
    public <X> LazyLinkedListX<X> fromStream(ReactiveSeq<X> stream) {

        Reducer<PersistentList<T>,T> reducer = getCollectorInternal();
        return new LazyLinkedListX<X>((PersistentList<X>)getList(),(ReactiveSeq)stream,(Reducer)reducer,(FoldToList)generator, evaluation());
    }

    @Override
    public <T1> LazyLinkedListX<T1> from(Iterable<T1> c) {
        if(c instanceof PersistentList)
            return new LazyLinkedListX<T1>((PersistentList)c,null,(Reducer)getCollectorInternal(),(FoldToList)generator, evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
    }
    public <T1> LazyLinkedListX<T1> from(PersistentList<T1> c) {

            return new LazyLinkedListX<T1>(c,null,(Reducer)getCollectorInternal(),(FoldToList)generator, evaluation());

    }


    @Override
    public LinkedListX<T> removeAll(Iterable<? extends T> list) {
        return from(get().removeAll(list));
    }

    @Override
    public LinkedListX<T> removeValue(T remove) {
        return from(get().removeValue(remove));
    }

    @Override
    public LinkedListX<T> updateAt(int i, T e) {
        return from(get().updateAt(i,e));
    }

    @Override
    public LinkedListX<T> insertAt(int i, T e) {
        return from(get().insertAt(i,e));
    }

    @Override
    public LinkedListX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public LinkedListX<T> plusAll(Iterable<? extends T> list) {
        return from(get().plusAll(list));
    }

    @Override
    public LinkedListX<T> insertAt(int i, Iterable<? extends T> list) {
        return from(get().insertAt(i,list));
    }

    @Override
    public LinkedListX<T> removeAt(int i) {
        return from(get().removeAt(i));
    }


    @Override
    public boolean equals(Object o) {
       if(o instanceof List){
           return equalToIteration((List)o);
       }
       return super.equals(o);
    }


    @Override
    public <U> LazyLinkedListX<U> unitIterable(Iterable<U> it) {
        return fromStream(ReactiveSeq.fromIterable(it));
    }



    @Override
    public <R> LazyLinkedListX<R> unit(Iterable<R> col) {
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
    public Option<T> get(int index) {
        PersistentList<T> x = get();
        return x.get(index);
    }

    @Override
    public T getOrElse(int index, T value) {
        PersistentList<T> x = get();
        if(index>x.size())
            return value;
        return x.getOrElse(index,value);
    }

    @Override
    public T getOrElseGet(int index, Supplier<? extends T> supplier) {
        PersistentList<T> x = get();
        if(index>x.size())
            return supplier.get();
        return x.getOrElseGet(index,supplier);
    }
}
