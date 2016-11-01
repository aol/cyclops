package com.aol.cyclops.types.higherkindedtypes.type.constructors;

import java.util.function.Function;
import java.util.stream.Stream;

import org.derive4j.hkt.Higher;
import org.derive4j.hkt.__;
import org.jooq.lambda.Seq;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Simulates Higher Kinded Types for Stream's
 * 
 * StreamType is a Stream and a Higher Kinded Type (StreamType.µ,T)
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Stream
 */

public interface StreamType<T> extends Higher<StreamType.µ, T>, Stream<T> {
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class µ {
    }

    /**
     * Convert a Stream to a simulated HigherKindedType that captures Stream nature
     * and Stream element data type separately. Recover via @see StreamType#narrow
     * 
     * If the supplied Stream implements StreamType it is returned already, otherwise it
     * is wrapped into a Stream implementation that does implement StreamType
     * 
     * @param Stream Stream to widen to a StreamType
     * @return StreamType encoding HKT info about Streams
     */
    public static <T> StreamType<T> widen(final Stream<T> stream) {
        if (stream instanceof StreamType)
            return (StreamType<T>) stream;
        return new Box<>(stream);
    }

    /**
     * Convert the HigherKindedType definition for a Stream into
     * 
     * @param Stream Type Constructor to convert back into narrowed type
     * @return StreamX from Higher Kinded Type
     */
    public static <T> Stream<T> narrow(final Higher<StreamType.µ, T> stream) {
        if (stream instanceof Stream)
            return (Stream)stream;
        final Box<T> type = (Box<T>) stream;
        return type.narrow();
    }
    
    public static <T> Stream<T> cast(final Higher<StreamType.µ, ?> stream) {
        return narrow( (Higher)stream);
    }
    
    public static <T> ReactiveSeq<T> narrowReactiveSeq(final Higher<StreamType.µ, T> stream) {
        if (stream instanceof Stream)
            return ReactiveSeq.fromStream((Stream) stream);
        final Box<T> type = (Box<T>) stream;
        return ReactiveSeq.fromStream(type.narrow());
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Box<T> implements StreamType<T> {

        @Delegate
        private final Stream<T> boxed;

        /**
         * @return This back as a StreamX
         */
        public Stream<T> narrow() {
            return boxed;
        }

        
       

    }
    
    public static <T> Function<Stream<T>,Seq<T>> toSeq(){
        return Seq::seq;
    }
    public static  <T> Function<Stream<T>,ReactiveSeq<T>> toReactiveSeq(){
        return ReactiveSeq::fromStream;
    }
    public static  <T> Function<Stream<T>,ReactiveSeq<T>> toFutureStream(LazyReact react){
        return s->react.fromStream(s);
    }
    public static <T> Function<Stream<T>,ReactiveSeq<T>> toFutureStream(){
        return LazyFutureStream::lazyFutureStream;
    }
}
