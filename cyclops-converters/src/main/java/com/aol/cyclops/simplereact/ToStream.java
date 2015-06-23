package com.aol.cyclops.simplereact;

import java.util.stream.Stream;

import javax.sound.midi.Sequence;

import org.jooq.lambda.Seq;

import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.nurkiewicz.lazyseq.LazySeq;

public class ToStream {
	/**
	public static <T> Sequence<T> toTotallyLazy(FutureStream<T> s) {
		return Sequences.sequence(s);
	}**/
	public static <T> FluentIterable<T> toFluentIterable(FutureStream<T> s){
		return FluentIterable.from(s);
	}

	public static <T> Stream<T> toStream(FutureStream<T> s) {
		return Seq.seq(s.iterator());
	}

	public static <T> Seq<T> toJooλ(FutureStream<T> s) {
		return Seq.seq(s.iterator());
	}

	public static <T> LazyFutureStream<T> toFutureStream(FutureStream<T> s) {
		return LazyFutureStream.of(s.iterator());
	}

	public static <T> javaslang.collection.Stream<T> toJavasLang(
			FutureStream<T> s) {
		return javaslang.collection.Stream.of(s);
	}

	public static <T> LazySeq<T> toLazySeq(FutureStream<T> s) {
		return LazySeq.of(s.iterator());
	}

	public static <T> fj.data.Stream<T> toFunctionalJavaStream(FutureStream<T> s) {
		return fj.data.Stream.iterableStream(s);
	}

}
