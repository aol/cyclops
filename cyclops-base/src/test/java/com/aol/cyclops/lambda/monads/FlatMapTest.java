package com.aol.cyclops.lambda.monads;
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
public class FlatMapTest {


	@Test
	public void flatMap(){
		assertThat(anyM(Stream.of(1,2,3)).flatMapStream(i->Stream.of(i)).asSequence().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCrossType(){
		assertThat(anyM(Optional.of(Arrays.asList(1,2,3))).flatMapStream(i->Stream.of(i.size())).toSequence().toList(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void flatMapCrossTypeNotCollection(){
		assertThat(anyM(Optional.of(1)).flatMapStream(i->Stream.of(i+2)).toSequence().toList(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void flatMapCrossTypeNotCollectionUnwrap(){

		assertThat(anyM(Optional.of(1)).flatMapStream(i->Stream.of(i+2)).unwrap(),equalTo(Optional.of(Arrays.asList(3))));
	}
	@Test
	public void flatMapCollection(){
		
	assertThat(	anyM(Optional.of(20)).flatMapCollection(i->Arrays.asList(1,2,i) ).toSequence().toList(),equalTo(Arrays.asList(1,2,20)));
	}
	@Test
	public void flatMap2(){
		
		assertThat(anyM(LazySeq.of(1,2,3)).flatMapStream(i-> Stream.of(i+2)).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapToLazySeq(){
		assertThat(anyM(Stream.of(1,2,3)).flatMapLazySeq(i-> LazySeq.of(i+2)).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapToSeq(){
		
		assertThat(anyM(Stream.of(1,2,3)).flatMapStream(i-> Seq.of(i+2)).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		
		assertThat(anyM(Seq.of(1,2,3)).flatMapStream(i-> Stream.of(i+2)).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToCompletableFuture(){
		
		assertThat(anyM(Seq.of(1,2,3)).flatMapCompletableFuture(i-> CompletableFuture.completedFuture(i+2)).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToSequenceM(){
		
		assertThat(anyM(Seq.of(1,2,3)).flatMapSequenceM(i-> anyM(CompletableFuture.completedFuture(i+2)).asSequence()).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
}
