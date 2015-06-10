package com.aol.simple.react.capacity.monitor;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

public class SamplingMonitorTest {
	SamplingMonitor monitor;
	int recieved;
	@Before
	public void setup(){
		recieved = 0;
		monitor = new SamplingMonitor(it->recieved++);
	}
	@Test
	public void testSamplingMonitorConsumerOfCompletableFuture() {
		monitor = new SamplingMonitor(it->recieved=recieved+8);
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(272));
	}

	@Test
	public void testAccept() {
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(34));
	}

	@Test
	public void testBuilder() {
		monitor = new SamplingMonitor(1,0,it->recieved++);
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(100));
	}

	@Test
	public void testSamplingMonitorIntIntConsumerOfCompletableFuture() {
		monitor = new SamplingMonitor(5,0,it->recieved++);
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(20));
	}

	@Test
	public void testWithSampleRate() {
		monitor = monitor.withSampleRate(5);
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(20));
	}

	@Test
	public void testWithCount() {
		monitor = monitor.withCount(80);
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(33));
	}

	@Test
	public void testWithMonitor() {
		monitor = monitor.withMonitor(it-> {recieved= recieved+3;});
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(102));
	}

}
