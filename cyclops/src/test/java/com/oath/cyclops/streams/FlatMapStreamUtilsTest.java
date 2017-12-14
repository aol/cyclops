package com.oath.cyclops.streams;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.companion.Streams;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

public class FlatMapStreamUtilsTest {

	@Test
	public void flatMap(){
		assertThat(Streams.flatMapStream(Stream.of(1,2,3), i->(Stream<Integer>)Stream.of(i)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCrossType(){
		assertThat(Streams.flatMapOptional(Stream.of(1,2,3,null),Optional::ofNullable).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCollection(){
		assertThat(Streams.concatMapterable(Stream.of(20), i->Arrays.asList(1,2,i)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,20)));

	}
	@Test
	public void flatMap2(){
		assertThat(Streams.flatMapStream(Stream.of(1,2,3), i->(Stream<Integer>)Stream.of(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
	}

	@Test
	public void flatMapToSeq(){

		assertThat(Streams.flatMapStream(Stream.of(1,2,3), i-> ReactiveSeq.<Integer>of(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		assertThat(Streams.flatMapStream(ReactiveSeq.of(1,2,3), i->Stream.<Integer>of(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToCompletableFuture(){
		assertThat(Streams.flatMapCompletableFuture(ReactiveSeq.of(1,2,3), i->CompletableFuture.<Integer>completedFuture(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
		}



}
