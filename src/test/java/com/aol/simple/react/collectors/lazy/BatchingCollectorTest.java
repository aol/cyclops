package com.aol.simple.react.collectors.lazy;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.stream.lazy.LazyFutureStream;

public class BatchingCollectorTest {

	BatchingCollector collector;
	 @Before
	 public void setup(){
		 collector = new BatchingCollector(LazyFutureStream.sequentialBuilder().of(1)).withResults(new ArrayList());
	 }
	@Test
	public void testAccept() {
		for(int i=0;i<1000;i++){
			collector.accept(CompletableFuture.completedFuture(10l));
		}
	}
	@Test
	public void testAcceptMock() {
		CompletableFuture cf = mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(986)).isDone();
	}
	@Test
	public void testAcceptMock495() {
		collector = new BatchingCollector(new MaxActive(500,5),EagerFutureStream.of(1)).withResults(new ArrayList<>());
		CompletableFuture cf = mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}
	@Test
	public void testAcceptMock50() {
		collector = new BatchingCollector(new MaxActive(500,450),EagerFutureStream.of(1)).withResults(new ArrayList<>());
		CompletableFuture cf = mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}

	@Test
	public void testBuilder() {
		collector = BatchingCollector.builder().blocking(LazyFutureStream.of(1)).maxActive(new MaxActive(2,1)).results(new ArrayList<>()).build();
		CompletableFuture cf = Mockito.mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(999)).isDone();
	}

	@Test
	public void testWithMaxActive() {
		collector = collector.withMaxActive(new MaxActive(10000,5));
		CompletableFuture cf = Mockito.mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(0)).isDone();
	}

	@Test
	public void testBatchingCollectorMaxActive() {
		collector = new BatchingCollector(new MaxActive(10,5),EagerFutureStream.of(1)).withResults(new HashSet<>());
		CompletableFuture cf = Mockito.mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(990)).isDone();
	}


}
