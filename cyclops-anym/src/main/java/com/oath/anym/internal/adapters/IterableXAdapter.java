package com.oath.anym.internal.adapters;

import static cyclops.monads.AnyM.fromIterableX;
import static cyclops.monads.Witness.collectionX;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.monads.AnyM;

import cyclops.monads.Witness;
import com.oath.anym.extensability.AbstractFunctionalAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class IterableXAdapter<W extends Witness.IterableXWitness<W>> extends AbstractFunctionalAdapter<W> {

    private final Supplier<IterableX<?>> empty;
    private final Function<?,IterableX<?>> unit;
    private final Function<Iterator<?>,IterableX<?>> unitIterator;
    private final W witness;


    private <U> Supplier<IterableX<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,IterableX<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,IterableX<U>>  getUnitIterator(){
        return (Function)unitIterator;
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<W, T> t) {
        return collectionX(t);
    }


    @Override
    public <T> AnyM<W, T> filter(AnyM<W, T> t, Predicate<? super T> fn) {
        return fromIterableX(collectionX(t).filter(fn),witness);

    }


    @Override
    public <T> AnyM<W, T> empty() {
        return fromIterableX(this.<T>getEmpty().get(),witness);

    }

  @Override
  public <T, T2, R> AnyM<W, R> zip(AnyM<W, ? extends T> t, AnyM<W, ? extends T2> t2, BiFunction<? super T, ? super T2, ? extends R> fn) {
    return fromIterableX(collectionX(t).zip(t2, fn),witness);
  }

  @Override
    public <T, R> AnyM<W, R> ap(AnyM<W, ? extends Function<? super T,? extends R>> fn, AnyM<W, T> apply) {
         return fromIterableX(collectionX(apply).zip(collectionX(fn),(a,b)->b.apply(a)),witness);

    }

    @Override
    public <T, R> AnyM<W, R> flatMap(AnyM<W, T> t,
            Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        return fromIterableX(collectionX(t).concatMap(fn.andThen(Witness::collectionX)),witness);
    }

    @Override
    public <T> AnyM<W, T> unitIterable(Iterable<T> it)  {
       return fromIterableX(this.<T>getUnitIterator().apply(it.iterator()),witness);
    }

    @Override
    public <T> AnyM<W, T> unit(T o) {
        return fromIterableX(this.<T>getUnit().apply(o),witness);
    }



}
