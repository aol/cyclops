package com.aol.cyclops.internal.comprehensions.converters;

import java.util.Optional;
import java.util.OptionalInt;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class OptionalIntToOptionalConverter implements MonadicConverter<Optional> {
    public static int priority = 5;

    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(Object o) {
        return (o instanceof OptionalInt);
    }

    @Override
    public Optional convertToMonadicForm(Object f) {
        val optional = (OptionalInt) f;
        if (optional.isPresent())
            return Optional.of(optional.getAsInt());
        return Optional.empty();
    }

}
