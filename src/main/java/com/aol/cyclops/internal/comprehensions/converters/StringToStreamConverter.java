package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

public class StringToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof CharSequence;
    }

    @Override
    public Stream convertToMonadicForm(final Object f) {
        return ((CharSequence) f).chars()
                                 .boxed()
                                 .map(i -> Character.toChars(i)[0]);
    }

}
