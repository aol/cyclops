package com.aol.cyclops.internal.comprehensions.converters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Stream;

import com.aol.cyclops.types.extensability.MonadicConverter;
import com.aol.cyclops.util.ExceptionSoftener;

import lombok.val;

public class URLToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof URL;
    }

    @Override
    public Stream convertToMonadicForm(final Object f) {
        val url = (URL) f;
        try {
            final BufferedReader in = new BufferedReader(
                                                         new InputStreamReader(
                                                                               url.openStream()));

            return in.lines();

        } catch (final IOException e) {
            ExceptionSoftener.throwSoftenedException(e);
            return null;
        }

    }

}
