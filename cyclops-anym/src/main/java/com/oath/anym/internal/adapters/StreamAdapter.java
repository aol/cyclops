package com.oath.anym.internal.adapters;

import static cyclops.monads.AnyM.fromStream;
import static cyclops.companion.Streams.zipSequence;
import static cyclops.monads.Witness.stream;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import cyclops.monads.AnyM;
import cyclops.reactive.FutureStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.companion.Streams;
import cyclops.monads.Witness;
import com.oath.anym.extensability.AbstractFunctionalAdapter;


public class StreamAdapter<W extends Witness.StreamWitness<W>> extends  AbstractFunctionalAdapter<W> {

    private final Supplier<Stream<?>> empty;
    private final Function<?,Stream<?>> unit;
    private final Function<Iterator<?>,Stream<?>> unitIterator;
    private final W witness;


    public StreamAdapter(Supplier<Stream<?>> empty, Function<?, Stream<?>> unit, Function<Iterator<?>, Stream<?>> unitIterator, W witness) {
        this.empty = empty;
        this.unit = unit;
        this.unitIterator = unitIterator;
        this.witness = witness;
    }

    public final static StreamAdapter stream = new StreamAdapter( ()->Stream.of(), t->Stream.of(t), it-> (Stream)Streams.stream(()->(Iterator)it),Witness.stream.INSTANCE);

    public final static StreamAdapter reactiveSeq = new StreamAdapter(()->ReactiveSeq.of(),t->ReactiveSeq.of(t),it->(Stream)ReactiveSeq.fromIterator((Iterator)it),Witness.reactiveSeq.CO_REACTIVE);

    public final static StreamAdapter futureStream = new StreamAdapter(()->FutureStream.builder().of(),t->FutureStream.builder().of(t),it->(Stream)FutureStream.builder().from((Iterator)it),Witness.futureStream.INSTANCE);

    private <U> Supplier<Stream<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,Stream<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,Stream<U>>  getUnitIterator(){
        return (Function)unitIterator;
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<W, T> t) {
        return ()->stream(t).iterator();
    }


    @Override
    public <T> AnyM<W, T> filter(AnyM<W, T> t, Predicate<? super T> fn) {
        return fromStream(stream(t).filter(fn),witness);
    }

    <T> Stream<T> stream(AnyM<W,T> anyM){
        return anyM.unwrap();
    }

    @Override
    public <T> AnyM<W, T> empty() {
        return fromStream(this.<T>getEmpty().get(),witness);
    }


  @Override
  public <T, T2, R> AnyM<W, R> zip(AnyM<W, ? extends T> t, AnyM<W, ? extends T2> t2, BiFunction<? super T, ? super T2, ? extends R> fn) {
    return AnyM.fromStream(Streams.zipSequence(stream(t),ReactiveSeq.fromIterable(t2),fn),witness);
  }

  @Override
    public <T, R> AnyM<W, R> ap(AnyM<W,? extends Function<? super T,? extends R>> fn, AnyM<W, T> apply) {
         return fromStream(zipSequence(stream(apply), stream(fn),(a,b)->b.apply(a)),witness);
    }

    @Override
    public <T, R> AnyM<W, R> flatMap(AnyM<W, T> t,
            Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        return fromStream(((Stream)t.unwrap()).flatMap(fn.andThen(a-> (Stream)a.unwrap())),witness);
    }

    @Override
    public <T> AnyM<W, T> unitIterable(Iterable<T> it)  {
       return fromStream(this.<T>getUnitIterator().apply(it.iterator()),witness);
    }

    @Override
    public <T> AnyM<W, T> unit(T o) {
        return fromStream(this.<T>getUnit().apply(o),witness);
    }


    @Override
    public <T, R> AnyM<W, R> map(AnyM<W, T> t, Function<? super T, ? extends R> fn) {

        return fromStream(stream(t).map(fn),witness);
    }

    @Override
    public <T> ReactiveSeq<T> toStream(AnyM<W, T> t) {
        return ReactiveSeq.fromStream(stream(t));
    }
}
