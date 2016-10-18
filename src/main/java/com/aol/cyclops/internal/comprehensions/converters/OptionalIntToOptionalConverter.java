package com.aol.cyclops.internal.comprehensions.converters;

import java.util.Optional;
import java.util.OptionalInt;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class OptionalIntToOptionalConverter implements MonadicConverter<Optional> {
    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof OptionalInt;
    }

    @Override
    public Optional convertToMonadicForm(final Object f) {
        val optional = (OptionalInt) f;
        if (optional.isPresent())
            return Optional.of(optional.getAsInt());
        return Optional.empty();
    }

}
