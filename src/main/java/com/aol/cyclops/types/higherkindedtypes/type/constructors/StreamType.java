package com.aol.cyclops.types.higherkindedtypes.type.constructors;




import java.util.function.Function;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.higherkindedtypes.Higher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;




/**
 * Simulates Higher Kinded Types for Stream's
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Stream
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public final class StreamType<T> implements  Higher<StreamType.stream,T>{
    
    /**
     * Convert a Stream to a simulated HigherKindedType that captures Stream nature
     * and Stream element data type separately. Recover via @see StreamType#narrow
     * 
     * @param Stream
     * @return
     */
    public static <T> StreamType<T> widen(Stream<T> stream){
        return new StreamType<>(stream);
    }
    /**
     * Convert the HigherKindedType definition for a Stream into
     * @param stream Type Constructor to convert back into narrowed type
     * @return Stream from Higher Kinded Type
     */
    public static <T> Stream<T> narrow(Higher<StreamType.stream, T> stream){
        return ((StreamType<T>)stream).narrow();
    }
    
    
    private final Stream<T> boxed;
    
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class stream{}
    
    /**
     * @return This back as a Stream
     */
    public Stream<T> narrow(){
        return boxed;
    }
    public static final <T> Function<Stream<T>,Seq<T>> toSeq(){
        return Seq::seq;
    }
    public static final <T> Function<Stream<T>,ReactiveSeq<T>> toReactiveSeq(){
        return ReactiveSeq::fromStream;
    }
    public static final <T> Function<Stream<T>,ReactiveSeq<T>> toFutureStream(LazyReact react){
        return s->react.fromStream(s);
    }
    public static final <T> Function<Stream<T>,ReactiveSeq<T>> toFutureStream(){
        return LazyFutureStream::lazyFutureStream;
    }
    
}

