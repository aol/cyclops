package com.oath.cyclops.anym.internal.adapters;

import static cyclops.monads.AnyM.fromStreamable;
import static cyclops.monads.Witness.streamable;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.oath.cyclops.anym.extensability.AbstractMonadAdapter;
import cyclops.monads.AnyM;
import cyclops.reactive.Streamable;
import cyclops.monads.Witness;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StreamableAdapter extends AbstractMonadAdapter<streamable> {

    private final Supplier<Streamable<?>> empty;
    private final Function<?,Streamable<?>> unit;
    private final Function<Iterator<?>,Streamable<?>> unitIterator;
    public final static StreamableAdapter streamable = new StreamableAdapter(()->Streamable.of(),t->Streamable.of(t),it->Streamable.fromIterator(it));


    private <U> Supplier<Streamable<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,Streamable<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,Streamable<U>>  getUnitIterator(){
        return (Function)unitIterator;
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<streamable, T> t) {
        return ()->streamable(t).iterator();
    }

  @Override
  public <T, T2, R> AnyM<Witness.streamable, R> zip(AnyM<Witness.streamable, ? extends T> t, AnyM<Witness.streamable, ? extends T2> t2, BiFunction<? super T, ? super T2, ? extends R> fn) {
    return AnyM.fromStreamable(streamable(t).zip(t2,fn));
  }

  @Override
    public <T> AnyM<streamable, T> filter(AnyM<streamable, T> t, Predicate<? super T> fn) {
        return fromStreamable(streamable(t).filter(fn));
    }


    @Override
    public <T> AnyM<streamable, T> empty() {
        return fromStreamable(this.<T>getEmpty().get());
    }


    @Override
    public <T, R> AnyM<streamable, R> ap(AnyM<streamable,? extends Function<? super T,? extends  R>> fn, AnyM<streamable, T> apply) {
         return fromStreamable(streamable(apply).zip(streamable(fn),(a,b)->b.apply(a)));
    }

    @Override
    public <T, R> AnyM<streamable, R> flatMap(AnyM<streamable, T> t,
            Function<? super T, ? extends AnyM<streamable, ? extends R>> fn) {
        return fromStreamable(streamable(t).flatMap(fn.andThen(Witness::streamable)));
    }

    @Override
    public <T> AnyM<streamable, T> unitIterable(Iterable<T> it)  {
       return fromStreamable(this.<T>getUnitIterator().apply(it.iterator()));
    }

    @Override
    public <T> AnyM<streamable, T> unit(T o) {
        return fromStreamable(this.<T>getUnit().apply(o));
    }


    @Override
    public <T, R> AnyM<streamable, R> map(AnyM<streamable, T> t, Function<? super T, ? extends R> fn) {
        return fromStreamable(streamable(t).map(fn));
    }
}
