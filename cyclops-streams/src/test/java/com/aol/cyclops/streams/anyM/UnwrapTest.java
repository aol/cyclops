package com.aol.cyclops.streams.anyM;
import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class UnwrapTest {

	@Test
	public void unwrap(){
		Stream<String> stream = anyM("hello","world").asSequence().stream();
		assertThat(stream.collect(Collectors.toList()),equalTo(Arrays.asList("hello","world")));
	}
	
	@Test
	public void unwrapOptional(){
		Optional<List<String>> stream = anyM("hello","world")
											.asSequence()
											.unwrapOptional();
		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void unwrapOptionalList(){
		Optional<List<String>> stream = anyM(Optional.of(Arrays.asList("hello","world")))
												.<String>toSequence()
												.unwrapOptional();
		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void unwrapCompletableFuture(){
		CompletableFuture<List<String>> cf = anyM("hello","world")
											.asSequence()
											.unwrapCompletableFuture();
		assertThat(cf.join(),equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void unwrapCompletableFutureList(){
		CompletableFuture<List<String>> cf = anyM(CompletableFuture.completedFuture(Arrays.asList("hello","world")))
												.<String>toSequence()
												.unwrapCompletableFuture();
		assertThat(cf.join(),equalTo(Arrays.asList("hello","world")));
	}
}
