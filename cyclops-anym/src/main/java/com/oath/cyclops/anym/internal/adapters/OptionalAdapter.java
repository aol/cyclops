package com.oath.cyclops.anym.internal.adapters;

import static cyclops.monads.AnyM.fromOptional;
import static cyclops.monads.Witness.optional;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.anym.extensability.AbstractMonadAdapter;
import com.oath.cyclops.anym.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.control.Maybe;
import cyclops.monads.Witness;
import cyclops.companion.Optionals;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OptionalAdapter extends AbstractMonadAdapter<optional> implements ValueAdapter<optional> {

    private final Supplier<Optional<?>> empty;
    private final Function<?,Optional<?>> unit;


    public final static OptionalAdapter optional = new OptionalAdapter(()->Optional.empty(),t->Optional.of(t));



  private <U> Supplier<Optional<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,Optional<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,Optional<U>>  getUnitIterator(){
        return  it->it.hasNext() ? this.<U>getUnit().apply(it.next()) : this.<U>getEmpty().get();
    }
    public <T> Option<T> get(AnyMValue<optional,T> t){
        return Option.fromOptional((Optional<T>)t.unwrap());
    }

    @Override
    public <T> Iterable<T> toIterable(AnyM<optional, T> t) {
        return Maybe.fromOptional(optional(t));
    }


    @Override
    public <T> AnyM<optional, T> filter(AnyM<optional, T> t, Predicate<? super T> fn) {
        return fromOptional(optional(t).filter(fn));
    }


    @Override
    public <T> AnyM<optional, T> empty() {
        return fromOptional(this.<T>getEmpty().get());
    }


    @Override
    public <T, R> AnyM<optional, R> ap(AnyM<optional, ? extends Function<? super T,? extends R>> fn, AnyM<optional, T> apply) {
         return fromOptional(Optionals.zip(optional(apply), optional(fn),(a,b)->b.apply(a)));
    }

    @Override
    public <T, R> AnyM<optional, R> flatMap(AnyM<optional, T> t,
            Function<? super T, ? extends AnyM<optional, ? extends R>> fn) {
        return fromOptional(optional(t).<R>flatMap(fn.andThen(Witness::optional).andThen(Optionals::narrow)));
    }

    @Override
    public <T> AnyM<optional, T> unitIterable(Iterable<T> it)  {
       return fromOptional(this.<T>getUnitIterator().apply(it.iterator()));
    }

    @Override
    public <T> AnyM<optional, T> unit(T o) {
        return fromOptional(this.<T>getUnit().apply(o));
    }

    @Override
    public <T, R> AnyM<optional, R> map(AnyM<optional, T> t, Function<? super T, ? extends R> fn) {
        return fromOptional(optional(t).<R>map(fn));
    }
}
