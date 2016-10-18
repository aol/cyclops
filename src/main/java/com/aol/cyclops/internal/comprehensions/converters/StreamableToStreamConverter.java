package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.Stream;

import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class StreamableToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof Streamable;

    }

    @Override
    public Stream convertToMonadicForm(final Object f) {
        val s = (Streamable) f;
        return s.stream();
    }

}
