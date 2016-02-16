package com.aol.cyclops.react.collectors.lazy;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.react.async.future.FastFuture;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class BatchingCollectorTest {

	BatchingCollector collector;
	 @Before
	 public void setup(){
		 collector = new BatchingCollector(MaxActive.IO,LazyReact.sequentialBuilder().of(1)).withResults(new ArrayList());
	 }
	@Test
	public void testAccept() {
		for(int i=0;i<1000;i++){
			collector.accept(FastFuture.completedFuture(10l));
		}
	}
	@Test
	public void testAcceptMock() {
		FastFuture cf = mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,atLeastOnce()).isDone();
	}
	@Test
	public void testAcceptMock495() {
		collector = new BatchingCollector(new MaxActive(500,5),LazyFutureStream.of(1)).withResults(new ArrayList<>());
		FastFuture cf = mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}
	@Test
	public void testAcceptMock50() {
		collector = new BatchingCollector(new MaxActive(500,450),LazyFutureStream.of(1)).withResults(new ArrayList<>());
		FastFuture cf = mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}

	@Test
	public void testBuilder() {
		collector = BatchingCollector.builder().blocking(LazyFutureStream.of(1)).maxActive(new MaxActive(2,1)).results(new ArrayList<>()).build();
		FastFuture cf = Mockito.mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(999)).isDone();
	}

	@Test
	public void testWithMaxActive() {
		collector = collector.withMaxActive(new MaxActive(10000,5));
		FastFuture cf = Mockito.mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(0)).isDone();
	}

	@Test
	public void testBatchingCollectorMaxActive() {
		collector = new BatchingCollector(new MaxActive(10,5),LazyFutureStream.of(1)).withResults(new HashSet<>());
		FastFuture cf = Mockito.mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(990)).isDone();
	}


}
