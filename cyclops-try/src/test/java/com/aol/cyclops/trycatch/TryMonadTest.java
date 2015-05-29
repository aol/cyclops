package com.aol.cyclops.trycatch;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.dynamic.As;



public class TryMonadTest {

	@Test
	public void tryTest(){
		assertThat(As.asMonad(Try.withCatch(()->"hello world")).map(o-> "2" + o).toList(),equalTo(Arrays.asList("2hello world")));
	}
	
	@Test
	public void tryFailInStream(){
	
		List<Integer> list = As.<Stream<Integer>,Integer>asMonad(Stream.of(1,2,3))
									.bind(i -> Try.withCatch( ()-> { if(i==1) { throw new RuntimeException();} else{ return i+2; } }) )
									.toList();
		
		
		assertThat(list,equalTo(Arrays.asList(4,5)));
	}
}
