package com.aol.cyclops.internal.comprehensions.converters;

import java.util.Optional;
import java.util.OptionalDouble;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class OptionalLongToOptionalConverter implements MonadicConverter<Optional> {

    public static int priority = 5;

    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(Object o) {
        return (o instanceof OptionalDouble);
    }

    @Override
    public Optional convertToMonadicForm(Object f) {
        val optional = (OptionalDouble) f;
        if (optional.isPresent())
            return Optional.of(optional.getAsDouble());
        return Optional.empty();
    }

}
