package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.UntypedDo;

import fj.data.Either;
import fj.data.Validation;
public class MixedDoTest {

	@Test
	public void mixedCompletableFutureStream(){
		
		
		CompletableFuture<String> future = CompletableFuture.supplyAsync(this::loadData);
		
		
		
		CompletableFuture<String> results1 = UntypedDo.add(future)
									 				.add(()->Stream.of("first","second"))
									 				.yield((String loadedData) -> (String local)-> loadedData + ":" + local );
		
	
		
		
		
		
		CompletableFuture<List<String>> results2 = CompletableFuture.supplyAsync(this::loadData)
																  .<List<String>>thenApply(loadedData -> 
																  				Stream.of("first","second")
																  					  .map(local -> loadedData + ":" + local)
																  					  .collect(Collectors.toList())
																		  	);
		
		
		
		assertThat(results2.join(),equalTo(Arrays.asList("loaded:first", "loaded:second")));
		assertThat(results1.join(),equalTo("loaded:first"));
	
		
	}
	@Test
	public void mixedStreamCompletableFuture(){
		
		
		
		Stream<String> results1 = UntypedDo.add(Stream.of("first","second"))
									 				.add(CompletableFuture.supplyAsync(this::loadData))
									 				.yield((String local) -> (String loadedData)-> loadedData + ":" + local );
		
	
		
		
		
		
		Stream<String> results2 = Stream.of("first","second")
										.map(local -> CompletableFuture.supplyAsync(this::loadData).join() + ":" + local);
		
		
		
		
		
		assertThat(results1.collect(Collectors.toList()),equalTo(results2.collect(Collectors.toList())));
	
		
	}
	
	private String loadData(){
		return "loaded";
	}

	@Test
	public void mixedOptionalEither(){
		/**
		fj.data.Stream<Integer> stream = fj.data.Stream.range(1, 10);
		
		Optional<String> opt = Optional.of("not null");
		
		
		Validation<RuntimeException,String> results = Do.add(stream)
											.add(opt)
											.yield((Integer right) -> (String optVal) -> "" + right + ":" + optVal);
		
		System.out.println(results);**/
	}
}
