package com.aol.cyclops.internal.comprehensions.comprehenders;

import static com.aol.cyclops.control.AnyM.fromStream;
import static com.aol.cyclops.control.StreamUtils.zipSequence;
import static com.aol.cyclops.types.anyM.Witness.stream;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.extensability.AbstractFunctionalAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StreamComprehender extends AbstractFunctionalAdapter<Witness.stream> {
    
    private final Supplier<Stream<?>> empty;
    private final Function<?,Stream<?>> unit;
    private final Function<Iterator<?>,Stream<?>> unitIterator;
    
    public final static StreamComprehender stream = new StreamComprehender(()->Stream.of(),t->Stream.of(t),it->StreamUtils.stream(()->it));
    public final static StreamComprehender reactiveSeq = new StreamComprehender(()->ReactiveSeq.of(),t->ReactiveSeq.of(t),it->ReactiveSeq.fromIterator(it));
    
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
    public <T> Iterable<T> toIterable(AnyM<stream, T> t) {
        return ()->stream(t).iterator();
    }
    

    @Override
    public <T> AnyM<stream, T> filter(AnyM<stream, T> t, Predicate<? super T> fn) {
        return fromStream(stream(t).filter(fn));
    }


    @Override
    public <T> AnyM<stream, T> empty() {
        return fromStream(this.<T>getEmpty().get());
    }


    @Override
    public <T, R> AnyM<stream, R> ap(AnyM<stream, Function<T, R>> fn, AnyM<stream, T> apply) {
         return fromStream(zipSequence(stream(apply), stream(fn),(a,b)->b.apply(a)));
    }

    @Override
    public <T, R> AnyM<stream, R> flatMap(AnyM<stream, T> t,
            Function<? super T, ? extends AnyM<stream, ? extends R>> fn) {
        return fromStream(Witness.stream(t).flatMap(fn.andThen(Witness::stream)));
    }

    @Override
    public <T> AnyM<stream, T> unitIterator(Iterator<T> it) {
       return fromStream(this.<T>getUnitIterator().apply(it));
    }
   
    @Override
    public <T> AnyM<com.aol.cyclops.types.anyM.Witness.stream, T> unit(T o) {
        return fromStream(this.<T>getUnit().apply(o));
    }

   
   
}
