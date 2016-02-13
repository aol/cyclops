package com.aol.cyclops.react.lazy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class ToOptionalCompletableFutureTest {

	@Test
	public void toCompletableFuture(){
		assertThat(LazyFutureStream.of(1,2,3,4)
						.toCompletableFuture()
						.join(),equalTo(Arrays.asList(1,2,3,4)));
	  
	}
	@Test
	public void toOptional(){
		assertThat(LazyFutureStream.of(1,2,3,4)
						.toOptional()
						.get(),equalTo(Arrays.asList(1,2,3,4)));
	  
	}
	@Test
	public void toOptionalEmpty(){
		assertFalse(LazyFutureStream.of()
						.toOptional().isPresent());
	  
	}
}
