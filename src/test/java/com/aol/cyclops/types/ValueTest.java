package com.aol.cyclops.types;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;

import reactor.core.publisher.Flux;

public class ValueTest {
	
	@Test
	public void testPublisherFlatMapFirst() {
		Value value = Maybe.of(100).flatMapFirstPublisher(i -> Flux.just(10, i));
		assertEquals(value.get(), 10);
	}

	@Test
	public void testIterableFlatMapFirst() {
		Value value = Maybe.of(100).flatMapFirst(i -> AnyM.fromStream(ReactiveSeq.of(i, 20, 30)));
		assertEquals(value.get(), 100);
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testIterableFlatMapFirstWithEmptyStream() {
		Value value = Maybe.of(100).flatMapFirst(i -> AnyM.fromStream(ReactiveSeq.of()));
		assertEquals(value.get(), null);
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testPublisherFlatMapFirstWithEmptyStream() {
		Value value = Maybe.of(100).flatMapFirstPublisher(i -> Flux.just());
		assertEquals(value.get(), null);
	}
}
