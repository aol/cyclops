package com.aol.cyclops2.react.lazy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import cyclops.stream.FutureStream;
import org.junit.Test;

public class ToOptionalCompletableFutureTest {

	@Test
	public void toCompletableFuture(){
		assertThat(FutureStream.of(1,2,3,4)
						.toCompletableFuture()
						.join(),equalTo(Arrays.asList(1,2,3,4)));
	  
	}
	@Test
	public void toOptional(){
		assertThat(FutureStream.of(1,2,3,4)
						.toOptional()
						.get(),equalTo(Arrays.asList(1,2,3,4)));
	  
	}
	@Test
	public void toOptionalEmpty(){
		assertFalse(FutureStream.of()
						.toOptional().isPresent());
	  
	}
}
