package com.aol.cyclops.internal.comprehensions.converters;

import java.util.Optional;

import com.aol.cyclops.types.extensability.MonadicConverter;

public class NullToOptionalConverter implements MonadicConverter<Optional> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o == null;
    }

    @Override
    public Optional convertToMonadicForm(final Object f) {
        return Optional.ofNullable(f);
    }

}
