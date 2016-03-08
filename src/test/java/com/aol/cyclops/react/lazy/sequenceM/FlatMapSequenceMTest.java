package com.aol.cyclops.react.lazy.sequenceM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
public class FlatMapSequenceMTest {

	@Test
	public void flatMap(){
		assertThat(LazyFutureStream.of(1,2,3).flatMapStream(i->Stream.of(i)).toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCrossType(){
		AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3)))
		.stream().forEach(System.out::println);
	
		assertThat(LazyFutureStream.of(Arrays.asList(1,2,3)).flatMapStream(i->Stream.of(i.size())).toList(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void flatMapCollection(){
	    assertThat(	LazyFutureStream.of(20).flatMapIterable(i->Arrays.asList(1,2,i) ).toList(),equalTo(Arrays.asList(1,2,20)));
	}
	@Test
	public void flatMapCollectionAnyM(){
	    assertThat(	LazyFutureStream.of(20).flatMapIterable(i->Arrays.asList(1,2,i) ).toList(),equalTo(Arrays.asList(1,2,20)));
	}
	@Test
	public void flatMapToSeq(){
		
		assertThat(LazyFutureStream.of(1,2,3).flatMapStream(i-> Seq.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		
		assertThat(LazyFutureStream.of(1,2,3).flatMapStream(i-> Stream.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToCompletableFuture(){
		
		assertThat(LazyFutureStream.of(1,2,3).flatMapCompletableFuture(i-> CompletableFuture.completedFuture(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToSequenceM(){
		
		assertThat(LazyFutureStream.of(1,2,3).flatMapAnyM(i-> AnyM.fromCompletableFuture(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	
	
}
