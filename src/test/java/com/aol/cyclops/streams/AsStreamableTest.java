package com.aol.cyclops.streams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.util.stream.AsStreamable;
import com.aol.cyclops.util.stream.Streamable;
public class AsStreamableTest {

	@Test
	public void testAsStreamableT() {
		
		val result = AsStreamable.<Integer>fromIterable(Arrays.asList(1,2,3)).stream().map(i->i+2).collect(Collectors.toList());
		
		assertThat(result,equalTo(Arrays.asList(3,4,5)));
	}

	@Test
	public void testAsStreamableStreamOfT() {
		Stream<Integer> stream = Stream.of(1,2,3,4,5);
		val streamable = AsStreamable.<Integer>fromStream(stream);
		val result1 = streamable.stream().map(i->i+2).collect(Collectors.toList());
		val result2 = streamable.stream().map(i->i+2).collect(Collectors.toList());
		val result3 = streamable.stream().map(i->i+2).collect(Collectors.toList());
		
		assertThat(result1,equalTo(Arrays.asList(3,4,5,6,7)));
		assertThat(result1,equalTo(result2));
		assertThat(result1,equalTo(result3));
	}
	volatile boolean failed=false;
	@Test
	public void concurrentLazy() throws InterruptedException{
		Streamable<Integer> streamable =   AsStreamable.synchronizedFromStream(IntStream.range(0,1000).boxed());

		for(int i=0;i<100;i++){
			CountDownLatch init = new CountDownLatch(4);
			
			Thread t1 = new Thread( ()->{
				init.countDown();
				try {
					init.await();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try{
					streamable.stream().forEach(System.out::println);
				}catch(Exception e){
					failed=true;
					fail(e.getMessage());
				}
				});
			Thread t2 = new Thread( ()->{
			init.countDown();
			try {
				init.await();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try{
				streamable.stream().forEach(System.out::println);
			}catch(Exception e){
				failed=true;
				fail(e.getMessage());
			}
				});
			Thread t3 = new Thread( ()->{
			init.countDown();
			try {
				init.await();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try{
				streamable.stream().forEach(System.out::println);
			}catch(Exception e){
				failed=true;
				fail(e.getMessage());
			}
			});
			
			t1.start();
			t2.start();
			t3.start();
			init.countDown();
			t1.join();
			t2.join();
			t3.join();
		}
		assertFalse(failed);
	}

}
