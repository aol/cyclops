package com.aol.simple.react.capacity.monitor;

import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.aol.simple.react.config.MaxActive;

public class LimitingMonitorTest {

	 LimitingMonitor waiter;
	 @Before
	 public void setup(){
		 waiter = new  LimitingMonitor();
	 }
	@Test
	public void testAccept() {
		for(int i=0;i<1000;i++){
			waiter.accept(CompletableFuture.completedFuture(10l));
		}
	}
	@Test
	public void testAcceptMock() {
		CompletableFuture cf = mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,atLeastOnce()).isDone();
	}
	@Test
	public void testAcceptMock495() {
		waiter = new LimitingMonitor(new MaxActive(500,5));
		CompletableFuture cf = mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}
	@Test
	public void testAcceptMock50() {
		waiter = new LimitingMonitor(new MaxActive(500,450));
		CompletableFuture cf = mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}

	@Test
	public void testBuilder() {
		waiter = LimitingMonitor.builder().maxActive(new MaxActive(2,1)).build();
		CompletableFuture cf = Mockito.mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(999)).isDone();
	}

	@Test
	public void testWithMaxActive() {
		waiter = waiter.withMaxActive(new MaxActive(10000,5));
		CompletableFuture cf = Mockito.mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(0)).isDone();
	}

	@Test
	public void testActiveSpinWaiterMaxActive() {
		waiter = new LimitingMonitor(new MaxActive(10,5));
		CompletableFuture cf = Mockito.mock(CompletableFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(990)).isDone();
	}

}
