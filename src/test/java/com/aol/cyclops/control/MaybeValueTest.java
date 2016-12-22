package com.aol.cyclops.control;

import com.aol.cyclops.types.AbstractValueTest;
import com.aol.cyclops.types.Value;
import cyclops.control.Maybe;

public class MaybeValueTest extends AbstractValueTest {

    @Override
    public <T> Value<T> of(T element) {
        return Maybe.of(element);
    }

}
