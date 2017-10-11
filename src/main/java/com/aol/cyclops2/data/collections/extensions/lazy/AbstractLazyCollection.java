package com.aol.cyclops2.data.collections.extensions.lazy;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.LazyFluentCollection;
import com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX;
import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public abstract class AbstractLazyCollection<T, C extends Collection<T>> implements LazyFluentCollection<T, C>, LazyCollectionX<T> {
    @Getter(AccessLevel.PROTECTED)
    private volatile C list;
    @Getter(AccessLevel.PROTECTED)
    private final AtomicReference<ReactiveSeq<T>> seq = new AtomicReference<>(null);
    @Getter(AccessLevel.PROTECTED)
    private final Collector<T, ?, C> collectorInternal;

    //@Getter//(AccessLevel.PROTECTED)
    private final Evaluation strict;
    final AtomicBoolean updating = new AtomicBoolean(false);
    final AtomicReference<Throwable> error = new AtomicReference<>(null);


    private final Function<ReactiveSeq<C>,C> fn;
    public AbstractLazyCollection(C list, ReactiveSeq<T> seq, Collector<T, ?, C> collector,Evaluation strict,Function<ReactiveSeq<C>,C> fn) {
        this.list = list;
        this.seq.set(seq);
        this.collectorInternal = collector;
        this.strict = strict;
        this.fn = fn;
        handleStrict();
    }



    @Override
    public <T> T unwrap(){
        return (T)get();
    }
    @Override
    public Iterator<T> iterator() {
        return get().iterator();
    }
    @Override
    public C get() {
        if (seq.get() != null) {
            if(updating.compareAndSet(false, true)) { //check if can materialize

                try{

                    ReactiveSeq<T> toUse = seq.get();
                    if(toUse!=null){//dbl check - as we may pass null check on on thread and set updating false on another


                        list = toUse.visit(s->toUse.collect(collectorInternal),
                                            r->fn.apply(toUse.collectStream(collectorInternal)),
                                                    a->fn.apply(toUse.collectStream(collectorInternal)));
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
    public boolean isLazy() {
        return strict == Evaluation.LAZY;
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
    public ReactiveSeq<T> stream() {


        ReactiveSeq<T> toUse = seq.get();
        if (toUse != null) {
            return toUse;
        }
        return ReactiveSeq.fromIterable(list);
    }

    public boolean isMaterialized(){

        return seq.get()==null;
    }
    @Override
    public CollectionX<T> materialize() {
        get();
        return this;
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
