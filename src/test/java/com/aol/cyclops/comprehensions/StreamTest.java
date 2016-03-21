package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.control.For;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.ReactiveSeq;

import lombok.val;

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
		
		List<String> res = For.stream(Stream.of("hello world","hello")) 
							.yield( v1->  v1 + "1")
							.stream().toList();
		List<String> expected = Arrays.asList("hello world1", "hello1");
		
		
		
		assertThat(expected, equalTo( res));
	}
	@Test
	public void stringStream() {
		
		List<String> res = For.stream("hello world".chars().boxed().map(i->Character.toChars(i)[0])) 
							.yield( v-> ""+ v + "1").<String>stream().toList();
		List<String> expected = Arrays.asList("h1", "e1", "l1", "l1", "o1",  " 1", "w1", "o1", "r1", 
				"l1", "d1");
		
		
		
		assertThat(expected,equalTo( res));
	}
	@Test
	public void stringStreamWithNull() {
		
		Stream<String> res =  For.stream(  "hello world".chars().boxed().map(i->Character.toChars(i)[0])) 
							.iterable(i->(Iterable<String>)null)
							.yield( v1-> v2-> ""+ v1 + v2)
							.unwrap();
		List<String> expected = Arrays.asList();
		
		
		
		assertThat(expected, equalTo( res.collect(Collectors.toList())));
	}
	
	@Test
	public void iterableStream() {
		
		ReactiveSeq<String> res = For.iterable(  new MyIterable()) 
							.yield( v->  v + "*").unwrap();
		List<String> expected = Arrays.asList("hello*","world*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void mapStream() {
		
		Map<String,Integer> m = new HashMap<>();
		m.put("hello",10);
		List<String> res = For.stream(m.entrySet().stream()) 
								.yield( v-> ""+ v + "*")
								.stream()
								.toList();
		List<String> expected = Arrays.asList("hello=10*");
		
		
		
		
		assertThat(expected, equalTo( res));
	}
	
	static enum MyEnum{FIRST, SECOND, THIRD}
	
	static class MyIterable implements Iterable<String>{

		@Override
		public Iterator iterator() {
			return Arrays.asList("hello","world").iterator();
		}
		
	}
}
