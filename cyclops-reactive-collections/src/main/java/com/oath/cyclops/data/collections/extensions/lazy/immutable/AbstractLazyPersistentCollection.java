package com.oath.cyclops.data.collections.extensions.lazy.immutable;

import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import com.oath.cyclops.data.collections.extensions.LazyFluentCollection;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.Getter;


import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public abstract class AbstractLazyPersistentCollection<T, C extends PersistentCollection<T>> implements LazyFluentCollection<T, C>, LazyCollectionX<T> {
    @Getter(AccessLevel.PROTECTED)
    protected volatile C list;
    @Getter(AccessLevel.PROTECTED)
    protected final AtomicReference<ReactiveSeq<T>> seq;
    @Getter(AccessLevel.PROTECTED)
    private final Reducer<C,T> collectorInternal;

    private final Evaluation strict;
    final AtomicBoolean updating = new AtomicBoolean(false);
    final AtomicReference<Throwable> error = new AtomicReference<>(null);
    private final Function<ReactiveSeq<C>,C> fn;

    public AbstractLazyPersistentCollection(C list, ReactiveSeq<T> seq, Reducer<C,T> collector,Evaluation strict,Function<ReactiveSeq<C>,C> fn) {
        this.list = list;
        this.seq = new AtomicReference<>(seq);
        this.collectorInternal = collector;
        this.strict = strict;
        this.fn = fn;
        handleStrict();
    }
     AbstractLazyPersistentCollection(Evaluation strict,C list, ReactiveSeq<T> seq, Reducer<C,T> collector,Function<ReactiveSeq<C>,C> fn) {
        this.list = list;
        this.seq = new AtomicReference<>(seq);
        this.collectorInternal = collector;
        this.strict = strict;
        this.fn = fn;

    }


    @Override
    public <T> T unwrap(){
        return (T)get();
    }

    public C materializeList(ReactiveSeq<T> toUse){
        ReactiveSeq<C> mapped = ReactiveSeq.fromStream(collectorInternal.mapToType(toUse));

        return toUse.fold(s -> mapped.reduce(collectorInternal.zero(), collectorInternal),
                            r -> fn.apply(mapped.reduceAll(collectorInternal.zero(), collectorInternal)),
                            a -> fn.apply(mapped.reduceAll(collectorInternal.zero(), collectorInternal)));

    }
    @Override
    public boolean isLazy() {
        return strict == Evaluation.LAZY;
    }
    public boolean isMaterialized(){
        return seq.get()==null;
    }
    @Override
    public boolean isEager() {
        return strict == Evaluation.EAGER;
    }
    @Override
    public Evaluation evaluation(){
        return strict;
    }
    protected void handleStrict(){
        if(isEager())
            get();
    }
    @Override
    public C get() {
        if (seq.get() != null) {
            if(updating.compareAndSet(false, true)) { //check if can materialize

                try{
                    ReactiveSeq<T> toUse = seq.get();
                    if(toUse!=null){//dbl check - as we may pass null check on on thread and set updating false on another
                        list = materializeList(toUse);
                        seq.set(null);
                    }
                }catch(Throwable t){
                    error.set(t); //catch any errors for propagation on access

                }finally{
                    updating.set(false); //finished updating
                }
            }
            while(updating.get()){ //Check if another thread updating
                LockSupport.parkNanos(0l); //spin until updating thread completes
            }
            if(error.get()!=null) //if updating thread failed, throw error
                throw ExceptionSoftener.throwSoftenedException(error.get());

            return list;
        }

        return list;

    }

    @Override
    public Iterator<T> iterator() {

        return get().iterator();
    }

    @Override
    public ReactiveSeq<T> stream() {

        ReactiveSeq<T> toUse = seq.get();
        if (toUse != null) {
            return toUse;
        }
        return ReactiveSeq.fromIterable(list);
    }
    @Override
    public FluentCollectionX<T> plusLoop(int max, IntFunction<T> value) {
        PersistentCollection<T> toUse = get();
        for(int i=0;i<max;i++){
            toUse = toUse.plus(value.apply(i));
        }
        return this.unit(toUse);


    }

    @Override
    public FluentCollectionX<T> plusLoop(Supplier<Option<T>> supplier) {
        PersistentCollection<T> toUse = get();
        Option<T> next =  supplier.get();
        while(next.isPresent()){
            toUse = toUse.plus(next.orElse(null));
            next = supplier.get();
        }
        return unit(toUse);
    }
    @Override
    public int size(){
        return get().size();
    }

    @Override
    public boolean isEmpty(){
        return get().isEmpty();
    }

    @Override
    public boolean containsValue(T o){
        return get().containsValue(o);
    }
    @Override
    public boolean contains(Object o){
        return get().containsValue((T)o);
    }


    @Override
    public boolean add(T t){
        return false;
    }


    @Override
    public boolean remove(Object o){
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c){
        boolean res = false;
        for(Object next : c){
            res = contains(next);
            if(!res)
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c){
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c){
        return false;
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c){
        return false;
    }

    @Override
    public void clear(){

    }


    @Override
    public boolean equals(Object o){
        if(o instanceof PersistentCollection){
            return get().equals(o);
        }

        return get().equals(o);
    }

    @Override
    public int hashCode(){
        return get().hashCode();
    }

    @Override
    public Spliterator<T> spliterator() {
        return stream().spliterator();
    }

    @Override
    public Stream<T> parallelStream() {
        return stream().parallel();
    }

    @Override
    public String toString() {
        return get().toString();
    }
}
