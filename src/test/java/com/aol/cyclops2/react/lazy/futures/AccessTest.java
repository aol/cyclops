package com.aol.cyclops2.react.lazy.futures;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import cyclops.stream.FutureStream;
import org.junit.Test;

public class AccessTest {
	@Test
	public void get0(){
		assertThat(FutureStream.of(1).actOnFutures().get(0).v1,equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(FutureStream.of(1,2,3,4,5).actOnFutures().get(2).v1,equalTo(3));
	}
	@Test
	public void elementAt0(){
		assertTrue(FutureStream.of(1).actOnFutures().elementAt(0).isPresent());
	}
	@Test
	public void elementAtMultple(){
		assertThat(FutureStream.of(1,2,3,4,5).actOnFutures().elementAt(2).get(),equalTo(3));
	}
	@Test
	public void elementAt1(){
		assertFalse(FutureStream.of(1).actOnFutures().elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(FutureStream.of().actOnFutures().elementAt(0).isPresent());
	}
	
	
}
