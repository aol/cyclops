package com.aol.cyclops.internal.comprehensions.converters;

import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.types.extensability.MonadicConverter;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;

public class PublisherToStreamConverter implements MonadicConverter<Stream> {

    public static int priority = 5;
    public int priority(){
        return priority;
    }
    @Override
    public boolean accept(Object o) {
        return o instanceof Publisher;
    }

    @Override
    public Stream convertToMonadicForm(Object f) {
        
        Publisher p = (Publisher)f;
        SeqSubscriber sub = SeqSubscriber.subscriber();
        p.subscribe(sub);
        return sub.stream();
   
    }

}
