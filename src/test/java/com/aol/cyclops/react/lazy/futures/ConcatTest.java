package com.aol.cyclops.react.lazy.futures;

import static com.aol.cyclops.types.futurestream.LazyFutureStream.of;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class ConcatTest {
	@Test
	public void concatStreamsJDK(){
	List<String> result = 	of(1,2,3).actOnFutures().concat(Stream.of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,hasItems("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	@Test
	public void concatStreams(){
	List<String> result = 	of(1,2,3).actOnFutures().concat(of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	@Test
	public void concatStreamsEager(){
	List<String> result = 	of(1,2,3).actOnFutures().concat(Stream.of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	@Test
	public void concat(){
	List<String> result = 	of(1,2,3).actOnFutures().concat(100,200,300)
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	@Test
	public void concatSingle(){
	List<String> result = 	of(1,2,3).actOnFutures().concat(100)
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","3!!","100!!"));
	}
	@Test
	public void concatFutures(){
	List<String> result = 	of(1,2,3).actOnFutures().concatFutures(CompletableFuture.completedFuture(100),CompletableFuture.completedFuture(200),CompletableFuture.completedFuture(300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	@Test
	public void concatFutureStream(){
	List<String> result = 	of(1,2,3).actOnFutures().concatStreamFutures(Stream.of(CompletableFuture.completedFuture(100),CompletableFuture.completedFuture(200),CompletableFuture.completedFuture(300)))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
}
