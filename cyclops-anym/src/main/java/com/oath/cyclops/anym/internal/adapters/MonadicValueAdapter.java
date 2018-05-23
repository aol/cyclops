package com.oath.cyclops.anym.internal.adapters;

import static cyclops.monads.AnyM.fromMonadicValue;
import static cyclops.monads.Witness.monadicValue;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.types.MonadicValue;

import com.oath.cyclops.anym.extensability.AbstractMonadAdapter;
import com.oath.cyclops.anym.extensability.MonadAdapter;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;

import com.oath.cyclops.anym.extensability.ValueAdapter;
import cyclops.typeclasses.monad.Monad;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MonadicValueAdapter<W extends Witness.MonadicValueWitness<W>> extends AbstractMonadAdapter<W> implements ValueAdapter<W> {

    private final Supplier<MonadicValue<?>> empty;
    private final Function<?,MonadicValue<?>> unit;
    private final Function<MonadicValue<?>,MonadicValue<?>> convert;

    private final boolean filter;
    private final W witness;


    @Override
    public boolean isFilterable(){
        return filter;
    }


    private <U> Supplier<MonadicValue<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,MonadicValue<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,MonadicValue<U>>  getUnitIterator(){
        return  it->it.hasNext() ? this.<U>getUnit().apply(it.next()) : this.<U>getEmpty().get();
    }
    public <T> Option<T> get(AnyMValue<W,T> t){
        return ((MonadicValue<T>)t.unwrap()).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<W, T> t) {
        return monadicValue(t);
    }

    public <R> R visit(Function<? super MonadAdapter<W>,? extends R> fn1, Function<? super ValueAdapter<W>, ? extends R> fn2){
        return fn2.apply(this);
    }

  @Override
  public <T, T2, R> AnyM<W, R> zip(AnyM<W, ? extends T> t, AnyM<W, ? extends T2> t2, BiFunction<? super T, ? super T2, ? extends R> fn) {
    return fromMonadicValue(zippableValue(monadicValue(t)).zip(monadicValue(t2),fn),witness);
  }
    static interface ZippableValue<T> {
        <T2,R> MonadicValue<R> zip(MonadicValue<? extends T2> mv, BiFunction<? super T,? super T2, ? extends R> fn);
    }

    public static <T> ZippableValue<T> zippableValue(MonadicValue<T> ma){
        return new ZippableValue<T>() {
            @Override
            public <T2, R> MonadicValue<R> zip(MonadicValue<? extends T2> mv, BiFunction<? super T, ? super T2, ? extends R> fn) {
                return ma.flatMap(a->mv.map(b->fn.apply(a,b)));
            }
        };
    }
  @Override
    public <T> AnyM<W, T> filter(AnyM<W, T> t, Predicate<? super T> fn) {
        if(filter)
            return fromMonadicValue(monadicValue(t).filter(fn),witness);
        return super.filter(t, fn);
    }


    @Override
    public <T> AnyM<W, T> empty() {
        return fromMonadicValue(this.<T>getEmpty().get(),witness);

    }

    @Override
    public <T, R> AnyM<W, R> ap(AnyM<W,? extends Function<? super T, ? extends R>> fn, AnyM<W, T> apply) {
         return fromMonadicValue(zippableValue(monadicValue(apply))
                                                    .zip(monadicValue(fn),
                                                      (a,b)->b.apply(a)),witness);

    }


    @Override
    public <T, R> AnyM<W, R> flatMap(AnyM<W, T> t,
            Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        return fromMonadicValue(monadicValue(t).flatMap(fn.andThen(Witness::monadicValue)),witness);
    }

    @Override
    public <T, R> AnyM<W, R> map(AnyM<W, T> t, Function<? super T, ? extends R> fn) {
        return fromMonadicValue(monadicValue(t).map(fn),witness);
    }

    @Override
    public <T> AnyM<W, T> unitIterable(Iterable<T> it) {
        if(it instanceof MonadicValue){
            fromMonadicValue(convert.apply((MonadicValue)it),witness);
        }
       return fromMonadicValue(this.<T>getUnitIterator().apply(it.iterator()),witness);
    }

    @Override
    public <T> AnyM<W, T> unit(T o) {
        return fromMonadicValue(this.<T>getUnit().apply(o),witness);
    }



}
