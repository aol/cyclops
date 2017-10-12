package com.aol.cyclops2.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.companion.Streams;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import cyclops.control.lazy.Trampoline;

public class StreamTest {
	@Test
	public void trampoline2Test(){
		ReactiveSeq.of(10,20,30,40)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println);
	}
	@Test
	public void trampolineTest(){
		ReactiveSeq.of(10_000,200_000,3_000_000,40_000_000)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println);
	}
	Trampoline<Long> fibonacci(int i){
		return fibonacci(i,1,0);
	}
	Trampoline<Long> fibonacci(int n, long a, long b) {
	    return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
	}

	@Test
	public void arrayStream() {
		
		List<String> res =  Streams.forEach2(Stream.of("hello world","hello"),
												a->Stream.of("boo!"),
										(v1,v2)->  v1 + "1" + v2).collect(Collectors.toList());
		List<String> expected = Arrays.asList("hello world1boo!", "hello1boo!");
		
		
		
		assertThat(expected, equalTo( res));
	}
	@Test
	public void stringStream() {
		
		List<String> res = Streams.forEach2("hello world".chars()
															  .boxed()
																.map(i->Character.toChars(i)[0]),
													i->Stream.of(i),
													(a,b)-> ""+ a + "1").collect(Collectors.toList());
		List<String> expected = Arrays.asList("h1", "e1", "l1", "l1", "o1",  " 1", "w1", "o1", "r1", 
				"l1", "d1");
		
		
		
		assertThat(expected,equalTo( res));
	}


}
