package com.aol.cyclops.guava;

import static com.aol.simple.react.stream.traits.LazyFutureStream.lazyFutureStream;
import static org.jooq.lambda.Seq.seq;

import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.nurkiewicz.lazyseq.LazySeq;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStream {

    public static <T> fj.data.Stream<T> toFJStream(FluentIterable<T> s){
        return fj.data.Stream.stream(s);
    }
    public static <T> Stream<T> toStream(FluentIterable<T> s){
        return seq(s.iterator());
    }
    public static <T> Seq<T> toJooqLambda(FluentIterable<T> s){
        return seq(s.iterator());
    }
    public static <T> LazyFutureStream<T> toFutureStream(FluentIterable<T> s){
        return lazyFutureStream(s.iterator());
    }
    public static <T> ReactiveSeq<T> toSequenceM(FluentIterable<T> s){
        return ReactiveSeq.fromIterable(()->s.iterator());
    }
    public static <T> LazySeq<T> toLazySeq(FluentIterable<T> s){
        return LazySeq.of(s.iterator());
    }



}
