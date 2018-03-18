package com.oath.cyclops.anym.internal.monads;

import java.util.Objects;


import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.anym.extensability.MonadAdapter;
import cyclops.monads.WitnessType;

public class AnyMValueImpl<W extends WitnessType<W>,T> extends BaseAnyMImpl<W,T>implements AnyMValue<W,T> {

    public AnyMValueImpl(final Object monad,MonadAdapter<W> adapter) {
        super(monad,adapter);

    }

    @Override
    public <T> T unwrap() {
        return super.unwrap();
    }

    @Override
    public String toString() {
        return mkString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unwrap());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof AnyMValue))
            return false;
        final AnyMValue v2 = (AnyMValue) obj;
        return unwrap().equals(v2.unwrap());

    }

}
