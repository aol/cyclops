package com.aol.cyclops.streams.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
public class FlatMapTest {


	@Test
	public void flatMap(){
		assertThat(AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->AnyM.fromStream(Stream.of(i))).asSequence().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCrossType(){
		assertThat(AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3))).flatMap(i->AnyM.fromStream(Stream.of(i.size()))).toSequence().toList(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void flatMapCrossTypeNotCollection(){
		assertThat(AnyM.fromOptional(Optional.of(1)).flatMap(i->AnyM.fromStream(Stream.of(i+2))).toSequence().toList(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void flatMapCrossTypeNotCollectionUnwrap(){

		assertThat(AnyM.fromOptional(Optional.of(1)).flatMap(i->AnyM.fromStream(Stream.of(i+2))).unwrap(),equalTo(Optional.of(3)));
	}
	@Test
	public void flatMapCollection(){
		
	assertThat(	AnyM.fromOptional(Optional.of(20)).flatMap(i->AnyM.fromIterable(Arrays.asList(1,2,i)) ).toSequence().toList(),equalTo(Arrays.asList(1,2,20)));
	}
	
	@Test
	public void flatMapToSeq(){
		
		assertThat(AnyM.fromStream(Stream.of(1,2,3)).flatMap(i-> AnyM.fromStream(Seq.of(i+2))).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		
		assertThat(AnyM.fromStream(Seq.of(1,2,3)).flatMap(i-> AnyM.fromStream(Stream.of(i+2))).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToCompletableFuture(){
		
		assertThat(AnyM.fromStream(Seq.of(1,2,3)).flatMap(i-> AnyM.fromCompletableFuture(CompletableFuture.completedFuture(i+2))).asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToSequenceM(){
		
		assertThat(AnyM.fromStream(Seq.of(1,2,3))
						.flatMap(i-> AnyM.fromCompletableFuture(CompletableFuture.completedFuture(i+2)))
						.asSequence().toList(),equalTo(Arrays.asList(3,4,5)));
	}
}
