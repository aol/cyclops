package com.oath.anym.internal.monads;

import com.oath.cyclops.types.anyM.AnyMValue2;
import com.oath.anym.extensability.FunctionalAdapter;
import cyclops.monads.WitnessType;

import java.util.Objects;

public class AnyMValue2Impl<W extends WitnessType<W>,T2,T> extends BaseAnyMImpl<W,T>implements AnyMValue2<W,T2,T> {

    public AnyMValue2Impl(final Object monad, FunctionalAdapter<W> adapter) {
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
        if (!(obj instanceof AnyMValue2))
            return false;
        final AnyMValue2 v2 = (AnyMValue2) obj;
        return unwrap().equals(v2.unwrap());

    }

}
