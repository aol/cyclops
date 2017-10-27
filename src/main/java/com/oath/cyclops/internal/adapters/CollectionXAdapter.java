package com.oath.cyclops.internal.adapters;

import static cyclops.monads.AnyM.fromCollectionX;
import static cyclops.monads.Witness.collectionX;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import cyclops.monads.AnyM;
import com.oath.cyclops.data.collections.extensions.CollectionX;
import cyclops.monads.Witness;
import com.oath.cyclops.types.extensability.AbstractFunctionalAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CollectionXAdapter<W extends Witness.CollectionXWitness<W>> extends AbstractFunctionalAdapter<W> {

    private final Supplier<CollectionX<?>> empty;
    private final Function<?,CollectionX<?>> unit;
    private final Function<Iterator<?>,CollectionX<?>> unitIterator;
    private final W witness;


    private <U> Supplier<CollectionX<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,CollectionX<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,CollectionX<U>>  getUnitIterator(){
        return (Function)unitIterator;
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<W, T> t) {
        return collectionX(t);
    }


    @Override
    public <T> AnyM<W, T> filter(AnyM<W, T> t, Predicate<? super T> fn) {
        return fromCollectionX(collectionX(t).filter(fn),witness);

    }


    @Override
    public <T> AnyM<W, T> empty() {
        return fromCollectionX(this.<T>getEmpty().get(),witness);

    }

    @Override
    public <T, R> AnyM<W, R> ap(AnyM<W, ? extends Function<? super T,? extends R>> fn, AnyM<W, T> apply) {
         return fromCollectionX(collectionX(apply).zip(collectionX(fn),(a,b)->b.apply(a)),witness);

    }

    @Override
    public <T, R> AnyM<W, R> flatMap(AnyM<W, T> t,
            Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        return fromCollectionX(collectionX(t).flatMap(fn.andThen(Witness::collectionX)),witness);
    }

    @Override
    public <T> AnyM<W, T> unitIterable(Iterable<T> it)  {
       return fromCollectionX(this.<T>getUnitIterator().apply(it.iterator()),witness);
    }

    @Override
    public <T> AnyM<W, T> unit(T o) {
        return fromCollectionX(this.<T>getUnit().apply(o),witness);
    }



}
