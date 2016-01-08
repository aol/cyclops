package com.aol.cyclops.javaslang.streams;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javaslang.collection.List;
import javaslang.collection.Stream;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.aol.cyclops.sequence.SequenceM;

public class SchedulingTest {

	ScheduledExecutorService ex =Executors.newScheduledThreadPool(1);
	AtomicInteger count = new AtomicInteger(0);
	
	@Test
	public void cronDebounceTest() throws InterruptedException{
		assertThat(StreamUtils.debounce(StreamUtils.schedule(Stream.ofAll(1,2,3,4)
				.peek(i->count.incrementAndGet())
				.peek(System.out::println)
				,"* * * * * ?", ex)
				.connect()
				,1,TimeUnit.DAYS)
				.peek(System.out::println)
				.toList(),equalTo(List.of(1)));
		
		
	}
	@Test
	public void fixedRateTest() throws InterruptedException{
		assertThat(StreamUtils.debounce(StreamUtils.scheduleFixedRate(Stream.ofAll(1,2,3,4)
				.peek(i->count.incrementAndGet())
				.peek(System.out::println)
				,1000, ex)
				.connect()
				,1,TimeUnit.DAYS)
				.peek(System.out::println)
				.toList(),equalTo(List.of(1)));
		
		
		
	}
	@Test
	public void fixedRateDelay() throws InterruptedException{
		
		
		assertThat(StreamUtils.debounce(StreamUtils.scheduleFixedDelay(Stream.ofAll(1,2,3,4)
				.peek(i->count.incrementAndGet())
				.peek(System.out::println)
				,1000, ex)
				.connect()
				,1,TimeUnit.DAYS)
				.peek(System.out::println)
				.toList(),equalTo(List.of(1)));
		
		
		
	}
}
