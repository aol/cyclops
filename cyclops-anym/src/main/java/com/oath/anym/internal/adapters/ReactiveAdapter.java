package com.oath.anym.internal.adapters;

import com.oath.cyclops.internal.stream.ReactiveStreamX;
import com.oath.anym.extensability.AbstractFunctionalAdapter;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.StreamWitness;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static cyclops.companion.Streams.zipSequence;
import static cyclops2.monads.AnyM.fromStream;

@AllArgsConstructor
public class ReactiveAdapter<W extends StreamWitness<W>> extends  AbstractFunctionalAdapter<W> {

    private final Supplier<Stream<?>> empty;
    private final Function<?,Stream<?>> unit;
    private final Function<Iterator<?>,Stream<?>> unitIterator;
    private final W witness;


    public final static ReactiveAdapter reactiveSeq = new ReactiveAdapter(()->Spouts.of(), t->Spouts.of(t), it->(Stream)ReactiveSeq.fromIterator((Iterator)it),Witness.reactiveSeq.REACTIVE);

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

        if(it instanceof ReactiveSeq){
            W witnessToUse = it instanceof ReactiveStreamX ? witness : (W)Witness.reactiveSeq.CO_REACTIVE;
            return fromStream((ReactiveSeq<T>)it,witnessToUse);
        }
        if(it instanceof Publisher){
            return fromStream(Spouts.from((Publisher)it),witness);
        }
       return fromStream(this.<T>getUnitIterator().apply(it.iterator()),witness);
    }

    @Override
    public <T> AnyM<W, T> unit(T o) {
        return fromStream(this.<T>getUnit().apply(o),witness);
    }

    @Override
    public <T, R> AnyM<W, R> map(AnyM<W, T> t, Function<? super T, ? extends R> fn) {
        return fromStream(((Stream)t.unwrap()).map(fn),witness);
    }
}
