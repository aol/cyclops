package com.aol.cyclops2.react.lazy.sequence;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import cyclops.async.LazyReact;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import org.junit.Test;

import cyclops.monads.AnyM;

public class FlatMapSequenceMTest {

	@Test
	public void flatMap(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3).flatMapStream(i->Stream.of(i)).toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCrossType(){
		AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3)))
		.stream().forEach(System.out::println);
	
		assertThat(LazyReact.sequentialBuilder().of(Arrays.asList(1,2,3)).flatMapStream(i->Stream.of(i.size())).toList(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void flatMapCollection(){
	    assertThat(	LazyReact.sequentialBuilder().of(20).flatMapI(i->Arrays.asList(1,2,i) ).toList(),equalTo(Arrays.asList(1,2,20)));
	}
	@Test
	public void flatMapCollectionAnyM(){
	    assertThat(	LazyReact.sequentialBuilder().of(20).flatMapI(i->Arrays.asList(1,2,i) ).toList(),equalTo(Arrays.asList(1,2,20)));
	}
	@Test
	public void flatMapToSeq(){
		
		assertThat(LazyReact.sequentialBuilder().of(1,2,3).flatMapStream(i-> ReactiveSeq.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		
		assertThat(LazyReact.sequentialBuilder().of(1,2,3).flatMapStream(i-> Stream.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToCompletableFuture(){
		
		assertThat(LazyReact.sequentialBuilder().of(1,2,3).flatMapCompletableFuture(i-> CompletableFuture.completedFuture(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToSequenceM(){
		
		assertThat(LazyReact.sequentialBuilder().of(1,2,3)
		                           .flatMapAnyM(i-> AnyM.fromStream(Stream.of(i+2)))
		                           .toList(),
		                           equalTo(Arrays.asList(3,4,5)));
	}
	
	
}
