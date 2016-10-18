package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.types.extensability.MonadicConverter;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;

public class PublisherToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean accept(final Object o) {
        return o instanceof Publisher;
    }

    @Override
    public Stream convertToMonadicForm(final Object f) {

        final Publisher p = (Publisher) f;
        final SeqSubscriber sub = SeqSubscriber.subscriber();
        p.subscribe(sub);
        return sub.stream();

    }

}
