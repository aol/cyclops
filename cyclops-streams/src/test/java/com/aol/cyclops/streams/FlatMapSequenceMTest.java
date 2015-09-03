package com.aol.cyclops.streams;
import static com.aol.cyclops.lambda.api.AsAnyM.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;

import com.nurkiewicz.lazyseq.LazySeq;
public class FlatMapSequenceMTest {

	@Test
	public void flatMap(){
		assertThat(anyM(Stream.of(1,2,3)).asSequence().flatMapStream(i->Stream.of(i)).toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCrossType(){
		anyM(Optional.of(Arrays.asList(1,2,3)))
		.asSequence().forEach(System.out::println);
	/**	System.out.println(anyM(Optional.of(Arrays.asList(1,2,3)))
								.asSequence()
							.peek(System.out::println)
							.flatMapStream(i->Stream.of(i.size())).toList());*/
		assertThat(anyM(Optional.of(Arrays.asList(1,2,3))).asSequence().flatMapStream(i->Stream.of(i.size())).toList(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void flatMapCollection(){
	assertThat(	anyM(Optional.of(20)).asSequence().flatMapCollection(i->Arrays.asList(1,2,i) ).toList(),equalTo(Arrays.asList(1,2,20)));
	}
	@Test
	public void flatMap2(){
		
		assertThat(anyM(LazySeq.of(1,2,3)).asSequence().flatMapStream(i-> Stream.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapToLazySeq(){
		assertThat(anyM(Stream.of(1,2,3)).asSequence().flatMapLazySeq(i-> LazySeq.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapToSeq(){
		
		assertThat(anyM(Stream.of(1,2,3)).asSequence().flatMapStream(i-> Seq.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		
		assertThat(anyM(Seq.of(1,2,3)).asSequence().flatMapStream(i-> Stream.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToCompletableFuture(){
		
		assertThat(anyM(Seq.of(1,2,3)).asSequence().flatMapCompletableFuture(i-> CompletableFuture.completedFuture(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToSequenceM(){
		
		assertThat(anyM(Seq.of(1,2,3)).asSequence().flatMapAnyM(i-> anyM(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	
	
}
