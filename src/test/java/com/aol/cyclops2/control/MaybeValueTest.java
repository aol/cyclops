package com.aol.cyclops2.control;

import com.aol.cyclops2.types.AbstractValueTest;
import com.aol.cyclops2.types.Value;
import cyclops.control.lazy.Maybe;

public class MaybeValueTest extends AbstractValueTest {

    @Override
    public <T> Value<T> of(T element) {
        return Maybe.of(element);
    }

}
