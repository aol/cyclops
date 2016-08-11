package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;
import com.aol.cyclops.util.stream.Streamable;

import lombok.val;

public class StreamableToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(Object o) {
        return (o instanceof Streamable);

    }

    @Override
    public Stream convertToMonadicForm(Object f) {
        val s = (Streamable) f;
        return s.stream();
    }

}
