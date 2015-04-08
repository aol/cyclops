package com.aol.cyclops.totallylazy;

import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.googlecode.totallylazy.Sequence;
import com.nurkiewicz.lazyseq.LazySeq;
import org.jooq.lambda.Seq;

import java.util.stream.Stream;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStream {



        public static <T> Stream<T> toStream(Sequence<T> s){
            return Seq.seq(s);
        }

        public static <T> FluentIterable<T> toFluentIterable(Sequence<T> s){
            return FluentIterable.from(s);
        }
        public static <T> Seq<T> toJooλ(Sequence<T> s){
            return Seq.seq(s);
        }
        public static <T> LazyFutureStream<T> toFutureStream(Sequence<T> s){
            return LazyFutureStream.futureStream(s);
        }

        public static <T> javaslang.collection.Stream<T> toJavasLang(Sequence<T> s){
            return javaslang.collection.Stream.of(s);
        }

        public static <T> LazySeq<T> toLazySeq(Sequence<T> s){
            return LazySeq.of(s);
        }

        public static <T> fj.data.Stream<T> toFunctionalJavaStream(Sequence<T> s){
            return fj.data.Stream.iterableStream(s);
        }


}
