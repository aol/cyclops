package com.aol.simple.react.capacity.monitor;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.aol.simple.react.async.future.FastFuture;
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
			waiter.accept(FastFuture.completedFuture(10l));
		}
	}
	@Test
	public void testAcceptMock() {
		FastFuture cf = mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,atLeastOnce()).isDone();
	}
	@Test
	public void testAcceptMock495() {
		waiter = new LimitingMonitor(new MaxActive(500,5));
		FastFuture cf = mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}
	@Test
	public void testAcceptMock50() {
		waiter = new LimitingMonitor(new MaxActive(500,450));
		FastFuture cf = mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(501)).isDone();
	}

	

	@Test
	public void testWithMaxActive() {
		waiter = waiter.withMaxActive(new MaxActive(10000,5));
		FastFuture cf = Mockito.mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(0)).isDone();
	}

	@Test
	public void testActiveSpinWaiterMaxActive() {
		waiter = new LimitingMonitor(new MaxActive(10,5));
		FastFuture cf = Mockito.mock(FastFuture.class);
		given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			waiter.accept(cf);
		}
		verify(cf,times(990)).isDone();
	}

}
