package com.aol.cyclops.internal.matcher2;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.StreamUtils;

public class SeqUtils {

    private static Object nonNull(final Object in) {
        if (in == null)
            return EMPTY;
        return in;
    }

    public final static class EMPTY {
    }

    public static final EMPTY EMPTY = new EMPTY();

    public static ReactiveSeq<Object> seq(final Object t) {
        return ReactiveSeq.fromStream(stream(t));
    }

    public static Stream<Object> stream(final Object t) {

        if (t instanceof Iterable) {
            return Stream.concat(StreamUtils.stream((Iterable) t)
                                            .map(SeqUtils::nonNull),
                                 StreamUtils.cycle(Stream.of(EMPTY)));
        }
        if (t instanceof Stream) {
            return Stream.concat(((Stream) t).map(SeqUtils::nonNull), StreamUtils.cycle(Stream.of(EMPTY)));
        }
        if (t instanceof Iterator) {
            return Stream.concat(StreamUtils.stream((Iterator) t)
                                            .map(SeqUtils::nonNull),
                                 StreamUtils.cycle(Stream.of(EMPTY)));
        }
        if (t instanceof Map) {
            return Stream.concat(StreamUtils.stream((Map) t)
                                            .map(SeqUtils::nonNull),
                                 StreamUtils.cycle(Stream.of(EMPTY)));
        }
        return Stream.concat(Stream.of(t)
                                   .map(SeqUtils::nonNull),
                             StreamUtils.cycle(Stream.of(EMPTY)));
    }

}
