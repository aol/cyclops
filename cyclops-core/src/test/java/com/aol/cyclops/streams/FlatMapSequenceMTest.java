package com.aol.cyclops.streams;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
public class FlatMapSequenceMTest {

	@Test
	public void flatMap(){
		assertThat(AnyM.fromStream(Stream.of(1,2,3)).asSequence().flatMapStream(i->Stream.of(i)).toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCrossType(){
		AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3)))
		.asSequence().forEach(System.out::println);
	
		assertThat(AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3))).asSequence().flatMapStream(i->Stream.of(i.size())).toList(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void flatMapCollection(){
	assertThat(	AnyM.fromOptional(Optional.of(20)).asSequence().flatMapCollection(i->Arrays.asList(1,2,i) ).toList(),equalTo(Arrays.asList(1,2,20)));
	}
	@Test
	public void flatMapCollectionAnyM(){
	assertThat(	AnyM.fromOptional(Optional.of(20)).flatMapCollection(i->Arrays.asList(1,2,i) ).toSequence().toList(),equalTo(Arrays.asList(1,2,20)));
	}
	@Test
	public void flatMapToSeq(){
		
		assertThat(AnyM.fromStream(Stream.of(1,2,3)).asSequence().flatMapStream(i-> Seq.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		
		assertThat(AnyM.fromStream(Seq.of(1,2,3)).asSequence().flatMapStream(i-> Stream.of(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToCompletableFuture(){
		
		assertThat(AnyM.fromStream(Seq.of(1,2,3)).asSequence().flatMapCompletableFuture(i-> CompletableFuture.completedFuture(i+2)).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToSequenceM(){
		
		assertThat(AnyM.fromStream(Seq.of(1,2,3)).asSequence().flatMapAnyM(i-> AnyM.fromCompletableFuture(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));
	}
	
	
}
