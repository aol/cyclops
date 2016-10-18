package com.aol.cyclops.internal.comprehensions.converters;

import java.io.BufferedReader;
import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.val;

public class BufferedReaderToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof BufferedReader;
    }

    @Override
    public Stream convertToMonadicForm(final Object f) {
        val reader = (BufferedReader) f;
        return reader.lines();
    }

}
