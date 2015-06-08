package com.aol.simple.react.collectors.lazy;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.capacity.monitor.LimitingMonitor;
import com.aol.simple.react.config.MaxActive;

public class EmptyCollectorTest {

	EmptyCollector collector;
	@Before
	public void setup(){
		collector = new EmptyCollector();
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
		verify(cf,atLeastOnce()).isDone();
	}
	@Test
	public void testAcceptMock495() {
		collector = new EmptyCollector(new MaxActive(500,5,1000));
		CompletableFuture cf = mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}
	@Test
	public void testAcceptMock50() {
		collector = new EmptyCollector(new MaxActive(500,450,1000));
		CompletableFuture cf = mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}

	@Test
	public void testWithResults() {
		
		collector = collector.withMaxActive(new MaxActive(4,3,1000));
		assertThat(collector.withResults(null).getMaxActive().getMaxActive(),is(4));
	}

	@Test
	public void testGetResults() {
		assertTrue(collector.getResults().isEmpty());
	}

	@Test
	public void testGetMaxActive() {
		assertThat(collector.getMaxActive().getMaxActive(),is(MaxActive.defaultValue.factory.getInstance().getMaxActive()));
	}

	
	

}
