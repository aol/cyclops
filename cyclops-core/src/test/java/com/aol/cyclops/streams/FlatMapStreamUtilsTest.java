package com.aol.cyclops.streams;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.util.stream.StreamUtils;
public class FlatMapStreamUtilsTest {

	@Test
	public void flatMap(){
		assertThat(StreamUtils.flatMapStream(Stream.of(1,2,3),i->(Stream<Integer>)Stream.of(i)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCrossType(){
		assertThat(StreamUtils.flatMapOptional(Stream.of(1,2,3,null),Optional::ofNullable).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCollection(){
		assertThat(StreamUtils.flatMapCollection(Stream.of(20),i->Arrays.asList(1,2,i)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,20)));

	}
	@Test
	public void flatMap2(){
		assertThat(StreamUtils.flatMapStream(Stream.of(1,2,3),i->(Stream<Integer>)Stream.of(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
	}
	
	@Test
	public void flatMapToSeq(){
		
		assertThat(StreamUtils.flatMapStream(Stream.of(1,2,3),i->Seq.<Integer>of(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		assertThat(StreamUtils.flatMapStream(Seq.of(1,2,3),i->Stream.<Integer>of(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToCompletableFuture(){
		assertThat(StreamUtils.flatMapCompletableFuture(Seq.of(1,2,3),i->CompletableFuture.<Integer>completedFuture(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
		}
	@Test
	public void flatMapSeqToSequenceM(){
		assertThat(StreamUtils.flatMapSequenceM(Seq.of(1,2,3),i-> SequenceM.<Integer>of(i+2)).collect(Collectors.toList()),equalTo(Arrays.asList(3,4,5)));
	}
	
	
}
