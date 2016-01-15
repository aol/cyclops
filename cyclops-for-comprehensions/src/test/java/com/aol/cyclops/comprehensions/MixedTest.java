package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;

public class MixedTest {
	@Test
	public void mixedListOptional(){
		val strs = Arrays.asList("hello","world");
		val opt = Optional.of("cool");
		
		
		List<String> results = Do.add(strs)
									.add(opt)
									.yield(v1->v2 -> v1 + v2).unwrap();
										 
		
		val list = results.stream().collect(Collectors.toList());
		assertThat(list,hasItem("hellocool"));
		assertThat(list,hasItem("worldcool"));
	}
	@Test
	public void mixedStreamOptional(){
		val strs = Stream.of("hello","world");
		val opt = Optional.of("cool");
		
		
		Stream<String> results = Do.addStream(strs)
									.add(opt)
									.yield(v1->v2 -> v1 + v2).unwrap();
										 
		
		val list = results.collect(Collectors.toList());
		assertThat(list,hasItem("hellocool"));
		assertThat(list,hasItem("worldcool"));
	}
	@Test
	public void mixedStreamOptionalEmpty(){
		val strs = Arrays.asList("hello","world");
		val opt = Optional.empty();
		
		
		List<String> results = Do.add(strs)
								.add(opt)
								.yield( v1->v2 -> v1+ v2)
								.unwrap();
								
		
		
		
		System.out.println(results);
		assertThat(results.size(),equalTo(0));
		
	}
	@Test
	public void mixedOptionalStream(){
		val strs = Arrays.asList("hello","world");
		val opt = Optional.of("cool");
		
		
		Optional<String> results = Do.add(opt)
											.add(strs)
											.yield(v1->v2 -> v1+ v2)
											.unwrap();
		
		assertThat(results.get(),equalTo("coolhello"));
		
	}
	@Test
	public void mixedOptionalEmptyStream(){
		val strs = Arrays.asList("hello","world");
		val opt = Optional.empty();
		
		
		Optional<List<String>> results = Do.add(opt)
											.add(strs)
											.yield(v1->v2 -> v1 + v2)
											.unwrap();
		
		assertFalse(results.isPresent());
	}
	
	@Test
	public void mixedOptionalCompletableFuture(){
		val str = CompletableFuture.completedFuture("hello");
		val opt = Optional.of("cool");
		
		
		Optional<String> results = Do.add(opt)
										 .add(str)
										 .yield(v1->v2 -> v1+v2)
										 .unwrap();
		
		assertThat(results.get(),equalTo("coolhello"));
		
	}
	
	
}
