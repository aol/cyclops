package com.aol.cyclops.streams.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
public class FlatMapTest {


	@Test
	public void flatMap(){
		assertThat(AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->AnyM.fromStream(Stream.of(i))).stream().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	
	@Test
	public void flatMapToSeq(){
		
		assertThat(AnyM.fromStream(Stream.of(1,2,3)).flatMap(i-> AnyM.fromStream(Seq.of(i+2))).stream().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){
		
		assertThat(AnyM.fromStream(Seq.of(1,2,3)).flatMap(i-> AnyM.fromStream(Stream.of(i+2))).stream().toList(),equalTo(Arrays.asList(3,4,5)));
	}



}
