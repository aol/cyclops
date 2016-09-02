package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;

public class StringToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(Object o) {
        return o instanceof CharSequence;
    }

    @Override
    public Stream convertToMonadicForm(Object f) {
        return (((CharSequence) f).chars()
                                  .boxed()).map(i -> Character.toChars(i)[0]);
    }

}
