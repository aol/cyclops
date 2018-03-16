package com.oath.cyclops.anym.internal.monads;

import com.oath.cyclops.anym.AnyMSeq;
import com.oath.cyclops.anym.extensability.MonadAdapter;
import cyclops.reactive.ReactiveSeq;

import cyclops.monads.WitnessType;

import java.util.Objects;
import java.util.stream.Stream;

public class AnyMSeqImpl<W extends WitnessType<W>,T> extends BaseAnyMImpl<W,T>implements AnyMSeq<W,T> {

    public AnyMSeqImpl(final Object monad,  MonadAdapter<W> adapter) {
        super(monad,adapter);

    }
    @Override
    public ReactiveSeq<T> stream(){


        if(unwrap() instanceof Stream){
            return ReactiveSeq.fromStream((Stream<T>)unwrap());

        }
        return adapter.toStream(this);

    }
    @Override
    public String toString() {
        return String.format("AnyMSeq[%s]", monad);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(monad);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof AnyMSeq))
            return false;
        return unwrap().equals(((AnyMSeq) o).unwrap());
    }

}
