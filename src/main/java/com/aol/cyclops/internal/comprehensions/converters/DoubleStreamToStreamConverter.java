package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class DoubleStreamToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(Object o) {
        return (o instanceof DoubleStream);

    }

    @Override
    public Stream convertToMonadicForm(Object f) {
        val s = (DoubleStream) f;
        return s.boxed();
    }

}
