package com.aol.cyclops.javaslang;

import java.util.Iterator;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.nurkiewicz.lazyseq.LazySeq;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStream {

    public static <T> FluentIterable<T> toFluentIterable(javaslang.collection.Stream<T> s){
        return FluentIterable.from(s);
    }
    public static <T> Stream<T> toStream(javaslang.collection.Stream<T> s){
        return Seq.seq(()->s.iterator());
    }
    public static <T> Seq<T> toJooqLambda(javaslang.collection.Stream<T> s){
        return Seq.seq(()->s.iterator());
    }
    public static <T> LazyFutureStream<T> toFutureStream(javaslang.collection.Stream<T> s){
        return LazyFutureStream.lazyFutureStream(s.iterator());
    }
    public static <T> SequenceM<T> toSequenceM(javaslang.collection.Stream<T> s){
        return SequenceM.fromIterable(()->s.iterator());
    }

    public static <T> LazySeq<T> toLazySeq(javaslang.collection.Stream<T> s){
        return LazySeq.of((Iterator)s.iterator());
    }

    public static <T> fj.data.Stream<T> toFunctionalJavaStream(javaslang.collection.Stream<T> s){
        return fj.data.Stream.iterableStream(s);
    }


}
