package com.aol.cyclops.streams.anyM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.util.stream.StreamUtils;
import com.aol.cyclops.control.AnyM;

public class UnwrapTest {

	@Test
	public void unwrap(){
		Stream<String> stream = AnyM.streamOf("hello","world").stream().stream();
		assertThat(stream.collect(Collectors.toList()),equalTo(Arrays.asList("hello","world")));
	}
	
	@Test
	public void unwrapOptional(){
		Optional<ListX<String>> stream = AnyM.streamOf("hello","world")
											.stream()
											.toOptional();
		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void unwrapOptionalEmpty(){
		Optional<ListX<String>> opt = AnyM.fromOptional(Optional.of(Optional.empty()))
		                                    .<String>toSequence()
											.toOptional();
		System.out.println(opt);
		assertFalse(opt.isPresent());
	}
	@Test
	public void unwrapOptionalList(){
		Optional<ListX<String>> stream =AnyM.fromOptional(Optional.of(Arrays.asList("hello","world")))
												.<String>toSequence()
												.toOptional();
		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void unwrapCompletableFuture(){
		CompletableFuture<ListX<String>> cf = AnyM.streamOf("hello","world")
											.stream()
											.toCompletableFuture();
		assertThat(cf.join(),equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void unwrapCompletableFutureList(){
		CompletableFuture<ListX<String>> cf = AnyM.fromCompletableFuture(CompletableFuture.completedFuture(Arrays.asList("hello","world")))
												.<String>toSequence()
												.toCompletableFuture();
		assertThat(cf.join(),equalTo(Arrays.asList("hello","world")));
	}
}
