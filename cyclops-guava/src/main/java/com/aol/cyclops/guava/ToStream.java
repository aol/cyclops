package com.aol.cyclops.guava;

import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.nurkiewicz.lazyseq.LazySeq;

import org.jooq.lambda.Seq;

import java.util.stream.Stream;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStream {

    public static <T> fj.data.Stream<T> toFJStream(FluentIterable<T> s){
        return fj.data.Stream.stream(s);
    }
    public static <T> Stream<T> toStream(FluentIterable<T> s){
        return Seq.seq(s.iterator());
    }
    public static <T> Seq<T> toJooqLambda(FluentIterable<T> s){
        return Seq.seq(s.iterator());
    }
    public static <T> LazyFutureStream<T> toFutureStream(FluentIterable<T> s){
        return LazyFutureStream.of(s.iterator());
    }

    public static <T> LazySeq<T> toLazySeq(FluentIterable<T> s){
        return LazySeq.of(s.iterator());
    }



}
