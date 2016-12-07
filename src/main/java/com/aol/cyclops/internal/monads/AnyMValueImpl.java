package com.aol.cyclops.internal.monads;

import java.util.Objects;

import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.extensability.Comprehender;

public class AnyMValueImpl<W extends WitnessType,T> extends BaseAnyMImpl<W,T>implements AnyMValue<W,T> {

    public AnyMValueImpl(final Object monad,Comprehender<W> adapter) {
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
