package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class DoubleStreamToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof DoubleStream;

    }

    @Override
    public Stream convertToMonadicForm(final Object f) {
        val s = (DoubleStream) f;
        return s.boxed();
    }

}
