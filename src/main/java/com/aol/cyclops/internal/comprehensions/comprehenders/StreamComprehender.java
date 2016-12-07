package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.anyM.Witness.stream;
import com.aol.cyclops.types.extensability.AbstractFunctionalAdapter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class StreamComprehender extends AbstractFunctionalAdapter<Witness.stream> {
    
    public final static StreamComprehender stream = new StreamComprehender();
    @Override
    public <T> Iterable<T> toIterable(AnyM<stream, T> t) {
        return Witness.reactiveSeq(t);
    }
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.extensability.Comprehender#filter(com.aol.cyclops.control.AnyM, java.util.function.Predicate)
     */
    @Override
    public <T> AnyM<stream, T> filter(AnyM<stream, T> t, Predicate<? super T> fn) {
        return AnyM.fromStream(Witness.stream(t).filter(fn));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.extensability.Comprehender#empty()
     */
    @Override
    public <T> AnyM<stream, T> empty() {
        return AnyM.fromStream(ReactiveSeq.of());
    }


    @Override
    public <T, R> AnyM<stream, R> ap(AnyM<stream, Function<T, R>> fn, AnyM<stream, T> apply) {
         return AnyM.fromStream(Witness.reactiveSeq(apply).zip(fn).map(t->t.v2.apply(t.v1)));
    }

    @Override
    public <T, R> AnyM<stream, R> flatMap(AnyM<stream, T> t,
            Function<? super T, ? extends AnyM<stream, ? extends R>> fn) {
        return AnyM.fromStream(Witness.stream(t).flatMap(fn.andThen(Witness::stream)));
    }

    @Override
    public <T> AnyM<stream, T> unitIterator(Iterator<T> it) {
       return AnyM.fromStream(ReactiveSeq.fromIterator(it));
    }

   
   
}
