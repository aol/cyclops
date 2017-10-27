package com.oath.cyclops.comprehensions;

import cyclops.companion.Streams;
import cyclops.companion.CompletableFutures;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
public class MixedDoTest {

	@Test
	public void mixedCompletableFutureStream(){


		CompletableFuture<String> future = CompletableFuture.supplyAsync(this::loadData);



		CompletableFuture<String> results1 = CompletableFutures.forEach2(future,
																			a->CompletableFuture.completedFuture("takeOne"),
																		(String loadedData,String local)-> loadedData + ":" + local );






		CompletableFuture<List<String>> results2 = CompletableFuture.supplyAsync(this::loadData)
																  .<List<String>>thenApply(loadedData ->
																  				Stream.of("takeOne","second")
																  					  .map(local -> loadedData + ":" + local)
																  					  .collect(Collectors.toList())
																		  	);



		assertThat(results2.join(),equalTo(Arrays.asList("loaded:takeOne", "loaded:second")));
		assertThat(results1.join(),equalTo("loaded:takeOne"));


	}
	@Test
	public void mixedStreamCompletableFuture(){



		Stream<String> results1 = Streams.forEach2(Stream.of("takeOne","second"),
		                                               a->Stream.of(loadData()),
									 				   (String local,String loadedData)-> loadedData + ":" + local );






		Stream<String> results2 = Stream.of("takeOne","second")
										.map(local -> CompletableFuture.supplyAsync(this::loadData).join() + ":" + local);





		assertThat(results1.collect(Collectors.toList()),equalTo(results2.collect(Collectors.toList())));


	}

	private String loadData(){
		return "loaded";
	}


}
