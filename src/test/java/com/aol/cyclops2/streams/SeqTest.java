package com.aol.cyclops2.streams;

import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;

public class SeqTest {

	@Test
	public void testUnzipWithLimits() {
		
		Supplier<Stream<Tuple2<Integer, String>>> s = () -> ReactiveSeq.of(
				tuple(1, "a"),tuple(2, "b"),tuple(3, "c"));

		Tuple2<Seq<Integer>, Seq<String>> u1 = Seq.unzip(s
				.get());

		assertTrue(u1.v1.limit(2).toList().containsAll(Arrays.asList(1, 2)));

		assertTrue(u1.v2.toList().containsAll(Arrays.asList("a", "b", "c")));

	}
}
