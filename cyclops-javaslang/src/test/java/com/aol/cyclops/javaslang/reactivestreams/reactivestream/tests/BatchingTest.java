package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;
import static com.aol.cyclops.javaslang.reactivestreams.ReactiveStream.of;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javaslang.collection.List;
import javaslang.collection.Stream;
import lombok.Value;

import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.streams.SimpleTimer;
public class BatchingTest {
	@Test
	public void batchUntil(){
		assertThat(of(1,2,3,4,5,6)
				.windowUntil(i->i%3==0)
				.toList().length(),equalTo(2));
		assertThat(ReactiveStream.of(1,2,3,4,5,6)
				.windowUntil(i->i%3==0)
				.toList().get(0),equalTo(Stream.of(1,2,3)));
	}
	@Test
	public void batchWhile(){
		assertThat(ReactiveStream.of(1,2,3,4,5,6)
				.windowWhile(i->i%3!=0)
				.toList()
				.length(),equalTo(2));
		ReactiveStream.of(1,2,3,4,5,6)
		.windowWhile(i->i%3!=0)
		.toList().forEach(a->System.out.println(a.getClass()));
		assertThat(ReactiveStream.of(1,2,3,4,5,6)
				.windowWhile(i->i%3!=0)
				.toList(),equalTo(List.of(Stream.of(1,2,3),Stream.of(4,5,6))));
	}
	
	
	
	private Integer sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	@Test
	public void windowwByTime2(){
		for(int i=0;i<5;i++){
			System.out.println(i);
			assertThat(of(1,2,3,4,5, 6)
							.map(n-> n==6? sleep(1) : n)
							.windowByTime(10,TimeUnit.MICROSECONDS)
							.toList()
							.get(0).sequenceM().toList()
							,not(hasItem(6)));
		}
	}
	@Test
	public void jitter() {
		
		ReactiveStream.range(0, 1000)
				.map(it -> it * 100)
				.jitter(100l)
				.peek(System.out::println)
				.forEach(a->{});
	}

	@Test
	public void fixedDelay2() {

		ReactiveStream.range(0, 1000)
				.fixedDelay(1l, TimeUnit.MICROSECONDS).peek(System.out::println)
				.forEach(a->{});
	}
	@Test
	public void onePerSecond() {

		
		ReactiveStream.iterate(0, it -> it + 1)
				.take(100)
				.onePer(1, TimeUnit.MICROSECONDS)
				.map(seconds -> "hello!")
				.peek(System.out::println)
				.toList();

	}

	@Value
	static class Status{
		long id;
	}
	private String saveStatus(Status s) {
		//if (count++ % 2 == 0)
		//	throw new RuntimeException();

		return "Status saved:" + s.getId();
	}
	
	
	private Object nextFile() {
		return "hello";
	}
	AtomicInteger count2 =new AtomicInteger(0);
	int count3 =0;
	volatile int otherCount;
	volatile int peek =0;
	
	@Test
	public void windowByTimeFiltered() {

		for(int x=0;x<10;x++){
			count2=new AtomicInteger(0);
			java.util.List<Collection<Map>> result = new ArrayList<>();
					
			ReactiveStream.iterate("", last -> "hello")
					.take(1000)
					.peek(i->System.out.println(++otherCount))
					.windowByTime(1, TimeUnit.MICROSECONDS)
					.peek(batch -> System.out.println("batched : " + batch + ":" + (++peek)))
					.peek(batch->count3= count3+(int)batch.seq().count())
					.forEach(next -> { 
					count2.getAndAdd((int)next.seq().count());});
		
			
			System.out.println("In flight count " + count3 + " :" + otherCount);
			System.out.println(result.size());
			System.out.println(result);
			System.out.println("x" +x);
			assertThat(count2.get(),equalTo(1000));

		}
	}
	
	@Test
	public void windowByTimex() {

		
				ReactiveStream.iterate("", last -> "next")
							  .take(100)
							  .peek(next->System.out.println("Counter " +count2.incrementAndGet()))
							  .windowByTime(10, TimeUnit.MICROSECONDS)
							  .peek(batch -> System.out.println("batched : " + batch))
							  .filter(c->! (c.length()==0))
							  .forEach(System.out::println);
			

	}

	@Test
	public void batchBySize3(){
		System.out.println(of(1,2,3,4,5,6).windowBySize(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).windowBySize(3).collect(Collectors.toList()).size(),is(2));
	}

	@Test
	public void windowBySizeAndTimeSize(){
		
		assertThat(of(1,2,3,4,5,6)
						.windowBySizeAndTime(3,10,TimeUnit.SECONDS)
						.toList().get(0).seq()
						.count(),is(3l));
	}
	@Test
	public void windowBySizeAndTimeSizeEmpty(){
		
		assertThat(of()
						.windowBySizeAndTime(3,10,TimeUnit.SECONDS)
						.toList()
						.length(),is(0));
	}
	
	@Test
	public void windowBySizeAndTimeTime(){
		
		for(int i=0;i<10;i++){
			System.out.println(i);
			java.util.List<ReactiveStream<Integer>> list = of(1,2,3,4,5,6)
					.map(n-> n==6? sleep(1) : n)
					.windowBySizeAndTime(10,1,TimeUnit.MICROSECONDS)
					
					.toJavaList();
			
			assertThat(list
							.get(0)
							.sequenceM()
							.toList()
							,not(hasItem(6)));
		}
	}
	
	
	
	@Test
	public void fixedDelay(){
		SimpleTimer timer = new SimpleTimer();
		
		assertThat(of(1,2,3,4,5,6).fixedDelay(10000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
	}
	@Test
	public void judder(){
		SimpleTimer timer = new SimpleTimer();
		
		assertThat(of(1,2,3,4,5,6).jitter(10000).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(20000l));
	}
	@Test
	public void debounce(){
		SimpleTimer timer = new SimpleTimer();
		
		
		assertThat(of(1,2,3,4,5,6).debounce(1000,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
		
	}
	@Test
	public void debounceOk(){
		System.out.println(of(1,2,3,4,5,6).debounce(1,TimeUnit.NANOSECONDS).toList());
		assertThat(of(1,2,3,4,5,6).debounce(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		
	}
	@Test
	public void onePer(){
		SimpleTimer timer = new SimpleTimer();
		System.out.println(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(600l));
	}
	@Test
	public void xPer(){
		SimpleTimer timer = new SimpleTimer();
		System.out.println(of(1,2,3,4,5,6).xPer(6,1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).xPer(6,100000000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),lessThan(60000000l));
	}
	@Test
	public void batchByTime(){
		assertThat(of(1,2,3,4,5,6).windowByTime(1,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
	}
	
	@Test
	public void batchByTimeInternalSize(){
		assertThat(of(1,2,3,4,5,6).windowByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
	}
	
	@Test
	public void windowByTimeInternalSize(){
		assertThat(of(1,2,3,4,5,6).windowByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
	}

}
