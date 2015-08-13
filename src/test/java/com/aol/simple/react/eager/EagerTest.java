package com.aol.simple.react.eager;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.stream.IntStream;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerReact;

public class EagerTest {

	int slow(){
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 3;
	}
	
	@Test
	public void convertToLazy(){
		
		
		
		assertThat(EagerReact.parallelCommonBuilder()
						.react(()->slow(),()->1,()->2)
						.peek(System.out::println)
						.convertToLazyStream()
						.zipWithIndex()
						.block().size(),is(3));
						
	}

	@Test
	public void convertToLazyAndBack(){
		
		
		
		
		assertThat(EagerReact.parallelCommonBuilder()
						.react(()->slow(),()->1,()->2)
						.peek(System.out::println)
						.convertToLazyStream()
						.zipWithIndex()
						.peek(System.out::println)
						.convertToEagerStream()
						.map(it->slow())
						.peek(System.out::println)
						.block().size(),is(3));
						
	}
	
	@Test
	public void testPrimitiveStream(){
		EagerReact.parallelCommonBuilder()
		.of(IntStream.range(0, 1000))
		.forEach(System.out::println);
	}
	@Test
	public void jitter(){
		EagerReact.parallelCommonBuilder()
						.of(IntStream.range(0, 100))
						.map(it -> it*100)
						.jitter(10l)
						.peek(System.out::println)
						.block();
	}
	@Test 
	public void jitterSequential(){
		EagerReact.sequentialCommonBuilder()
						.of(IntStream.range(0, 100))
						.map(it -> it*100)
						.jitter(100000l)
						.peek(System.out::println)
						.block();
	}
	@Test
	public void doOnEach(){
		String[] found = {""};
		String res = EagerReact.sequentialBuilder().react(()->"hello").doOnEach(it->{ found[0]=it;return "world";}).map(it->it+"!").first();
		while("".equals(found[0])){
			Thread.yield();
		}
		assertThat(found[0],is("hello"));
		assertThat(res,is("hello!"));
	}
	
	@Test
	public void eagerReactStream(){
		EagerReact.sequentialBuilder()
			.react( ()-> 1 )
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void eagerParallel(){
		EagerReact.parallelBuilder()
			.react( ()-> 1 )
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void eagerReactStreamList(){
		EagerReact.sequentialBuilder()
			.react( asList(()-> 1 ))
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void eagerParallelList(){
		EagerReact.parallelBuilder()
			.react( asList(()-> 1 ))
			.map(list -> 1+2)
			.block();
	}
}
