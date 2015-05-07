package com.aol.cyclops.comprehensions;

import static com.aol.cyclops.comprehensions.ForComprehensions.foreach2;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import fj.data.Option;

public class MixedTest {

	@Test
	public void mixedStreamOptional(){
		val strs = Arrays.asList("hello","world");
		val opt = Optional.of("cool");
		
		
		Stream<String> results = foreach2( c-> c.flatMapAs$1(strs)
										 .mapAs$2(opt)
										 .yield(() -> c.<String>$1() + c.$2()));
										 
		
		assertThat(results.collect(Collectors.toList()),hasItem("hellocool"));
		assertThat(results.collect(Collectors.toList()),hasItem("worldcool"));
	}
	@Test
	public void mixedStreamOptionalEmpty(){
		val strs = Arrays.asList("hello","world");
		val opt = Optional.empty();
		
		
		List<String> results = ForComprehensions.<Stream<String>>foreach2( 
										c-> c.flatMapAs$1(strs)
											 .mapAs$2(opt)
											 .yield(() -> c.<String>$1() + c.$2()))
											 .collect(Collectors.toList());
		
		
		
		System.out.println(results);
		assertThat(results.size(),equalTo(0));
		
	}
	@Test
	public void mixedOptionalStream(){
		val strs = Arrays.asList("hello","world");
		val opt = Optional.of("cool");
		
		
		Optional<List<String>> results = foreach2( 
											c-> c.flatMapAs$1(opt)
												.mapAs$2(strs)
												.yield(() -> c.<String>$1() + c.$2()));
		
		assertThat(results.get(),hasItem("coolhello"));
		assertThat(results.get(),hasItem("coolworld"));
	}
	@Test
	public void mixedOptionalEmptyStream(){
		val strs = Arrays.asList("hello","world");
		val opt = Optional.empty();
		
		
		Optional<List<String>> results = foreach2( 
											c-> c.flatMapAs$1(opt)
												.mapAs$2(strs)
												.yield(() -> c.<String>$1() + c.$2()));
		
		assertFalse(results.isPresent());
	}
	
	@Test
	public void mixedOptionalCompletableFuture(){
		val str = CompletableFuture.completedFuture("hello");
		val opt = Optional.of("cool");
		
		
		Optional<String> results = foreach2( 
										c-> c.flatMapAs$1(opt)
										 .mapAs$2(str)
										 .yield(() -> c.<String>$1() + c.$2()));
		
		assertThat(results.get(),equalTo("coolhello"));
		
	}
	
	
}
