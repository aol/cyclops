package com.aol.simple.react.collectors.lazy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;

import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.capacity.monitor.SamplingMonitor;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.collectors.lazy.SamplingCollector;
import com.aol.simple.react.config.MaxActive;

public class SamplingCollectorTest {

	SamplingCollector monitor;
	int recieved;
	@Before
	public void setup(){
		recieved = 0;
		monitor = new SamplingCollector(3,new MyConsumer(it->recieved++));
	}
	@Test
	public void testSamplingMonitorConsumerOfCompletableFuture() {
		monitor = new SamplingCollector(3,new MyConsumer(it->recieved=recieved+8));
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
		monitor = SamplingCollector.builder().sampleRate(1).consumer(new MyConsumer(it->recieved++)).count(0).build();
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(100));
	}

	@Test
	public void testSamplingMonitorIntIntConsumerOfCompletableFuture() {
		monitor = new SamplingCollector(5,0,new MyConsumer(it->recieved++));
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
		monitor = monitor.withConsumer(new MyConsumer(it->recieved=recieved+3));
		for(int i=0;i<100;i++)
			monitor.accept(CompletableFuture.completedFuture(10));
		assertThat(recieved, is(102));
	}

	@AllArgsConstructor
	static class MyConsumer implements LazyResultConsumer{

		Consumer c;
		@Override
		public void accept(Object t) {
			c.accept(t);
			
		}

		@Override
		public LazyResultConsumer withResults(Collection t) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection getResults() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MaxActive getMaxActive() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
