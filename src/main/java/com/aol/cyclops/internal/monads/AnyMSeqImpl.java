package com.aol.cyclops.internal.monads;

import cyclops.stream.ReactiveSeq;
import com.aol.cyclops.types.anyM.AnyMSeq;
import cyclops.monads.WitnessType;
import com.aol.cyclops.types.extensability.FunctionalAdapter;

import java.util.Objects;

public class AnyMSeqImpl<W extends WitnessType<W>,T> extends BaseAnyMImpl<W,T>implements AnyMSeq<W,T> {

    public AnyMSeqImpl(final Object monad,  FunctionalAdapter<W> adapter) {
        super(monad,adapter);

    }
    @Override
    public ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(this);
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
