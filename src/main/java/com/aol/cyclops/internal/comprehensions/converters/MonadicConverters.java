package com.aol.cyclops.internal.comprehensions.converters;

import java.util.ServiceLoader;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.pcollections.PStack;

import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.extensability.MonadicConverter;

import lombok.Getter;
import lombok.val;

public class MonadicConverters {

    private final StreamUpscaler upscaler = stream -> ReactiveSeq.fromStream(stream);

    @Getter
    private final static PStack<MonadicConverter> converters;

    static {
        val loader = ServiceLoader.load(MonadicConverter.class);
        converters = Reducers.<MonadicConverter> toPStackReversed()
                             .mapReduce(StreamSupport.stream(Spliterators.spliteratorUnknownSize(loader.iterator(), Spliterator.ORDERED), false)
                                                     .sorted((a, b) -> b.priority() - a.priority()));

    }

    public Object convertToMonadicForm(final Object o) {
        return upscaler.upscaleIfStream(converters.stream()
                                                  .filter(t -> t.accept(o))
                                                  .map(m -> m.convertToMonadicForm(o))
                                                  .findFirst()
                                                  .orElse(o));
    }

}
