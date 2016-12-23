package com.aol.cyclops.data.collections.extensions.lazy;

import com.aol.cyclops.data.collections.extensions.LazyFluentCollection;
import com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX;
import com.aol.cyclops.util.ExceptionSoftener;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public abstract class AbstractLazyMutableCollection<T, C extends Collection<T>> implements LazyFluentCollection<T, C>, MutableCollectionX<T> {
    @Getter(AccessLevel.PROTECTED)
    private volatile C list;
    @Getter(AccessLevel.PROTECTED)
    private final AtomicReference<ReactiveSeq<T>> seq;
    @Getter(AccessLevel.PROTECTED)
    private final Collector<T, ?, C> collectorInternal;
    AtomicBoolean updating = new AtomicBoolean(false);
    AtomicReference<Throwable> error = new AtomicReference<>(null);

    public AbstractLazyMutableCollection(C list, ReactiveSeq<T> seq, Collector<T, ?, C> collector) {
        this.list = list;
        this.seq = new AtomicReference<>(seq);
        this.collectorInternal = collector;
    }



    @Override
    public C get() {
        if (seq.get() != null) {
            if(updating.compareAndSet(false, true)) { //check if can materialize
                try{
                    ReactiveSeq<T> toUse = seq.get();
                    if(toUse!=null){//dbl check - as we may pass null check on on thread and set updating false on another
                        list = toUse.collect(collectorInternal);
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
        System.out.println("Stream!!");
        ReactiveSeq<T> toUse = seq.get();
        if (toUse != null) {
            return toUse;
        }
        return ReactiveSeq.fromIterable(list);
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
    public boolean contains(Object o){
        return get().contains(o);
    }



    @Override
    public Object[] toArray(){
        return get().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a){
        return get().toArray(a);
    }

    @Override
    public boolean add(T t){
        return get().add(t);
    }

    @Override
    public boolean remove(Object o){
        return get().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c){
        return get().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c){
        return get().addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c){
        return get().removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return get().removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c){
        return get().retainAll(c);
    }

    @Override
    public void clear(){
        get().clear();
    }

    @Override
    public boolean equals(Object o){
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
        return get().parallelStream();
    }

    @Override
    public String toString() {
        return get().toString();
    }
}
