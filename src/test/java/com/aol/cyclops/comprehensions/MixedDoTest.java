package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.For;
public class MixedDoTest {

	@Test
	public void mixedCompletableFutureStream(){
		
		
		CompletableFuture<String> future = CompletableFuture.supplyAsync(this::loadData);
		
		
		
		CompletableFuture<String> results1 = For.future(future)
									 				.stream(a->Stream.of("first","second"))
									 				.yield((String loadedData) -> (String local)-> loadedData + ":" + local )
									 				.unwrap();
		
	
		
		
		
		
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
		
		
		
		Stream<String> results1 = For.stream(Stream.of("first","second"))
									 				.future(a->CompletableFuture.supplyAsync(this::loadData))
									 				.yield((String local) -> (String loadedData)-> loadedData + ":" + local )
									 				.unwrap();
		
	
		
		
		
		
		Stream<String> results2 = Stream.of("first","second")
										.map(local -> CompletableFuture.supplyAsync(this::loadData).join() + ":" + local);
		
		
		
		
		
		assertThat(results1.collect(Collectors.toList()),equalTo(results2.collect(Collectors.toList())));
	
		
	}
	
	private String loadData(){
		return "loaded";
	}

	
}
