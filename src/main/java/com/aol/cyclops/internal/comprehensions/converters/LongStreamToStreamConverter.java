package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class LongStreamToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof IntStream;

    }

    @Override
    public Stream convertToMonadicForm(final Object f) {
        val s = (IntStream) f;
        return s.boxed();
    }

}
