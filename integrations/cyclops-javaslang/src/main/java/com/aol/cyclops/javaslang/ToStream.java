package com.aol.cyclops.javaslang;

import java.util.Iterator;
import java.util.stream.Stream;

import javaslang.collection.Traversable;

import org.jooq.lambda.Seq;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.nurkiewicz.lazyseq.LazySeq;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStream {

    public static <T> FluentIterable<T> toFluentIterable(Iterable<T> s){
        return FluentIterable.from(s);
    }
    public static <T> Stream<T> toStream(Iterable<T> s){
        return Seq.seq(()->s.iterator());
    }
    public static <T> Seq<T> toJooqLambda(Iterable<T> s){
        return Seq.seq(()->s.iterator());
    }
    public static <T> LazyFutureStream<T> toFutureStream(Iterable<T> s){
        return LazyFutureStream.lazyFutureStream(s.iterator());
    }
    public static <T> LazyFutureStream<T> toFutureStreamFromTraversable(Traversable<T> s){
        return LazyFutureStream.lazyFutureStream(s.iterator());
    }
    public static <T> ReactiveSeq<T> toSequenceM(Iterable<T> s){
        return ReactiveSeq.fromIterable(s);
    }

    public static <T> LazySeq<T> toLazySeq(Iterable<T> s){
        return LazySeq.of((Iterator)s.iterator());
    }

    public static <T> fj.data.Stream<T> toFunctionalJavaStream(Iterable<T> s){
        return fj.data.Stream.iterableStream(s);
    }


}
