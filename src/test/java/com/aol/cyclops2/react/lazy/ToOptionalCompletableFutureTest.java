package com.aol.cyclops2.react.lazy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import cyclops.async.LazyReact;
import org.junit.Test;

public class ToOptionalCompletableFutureTest {

	@Test
	public void toCompletableFuture(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4)
						.toCompletableFuture()
						.join(),equalTo(Arrays.asList(1,2,3,4)));
	  
	}
	@Test
	public void toOptional(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4).to()
						.optional()
						.get(),equalTo(Arrays.asList(1,2,3,4)));
	  
	}
	@Test
	public void toOptionalEmpty(){
		assertFalse(LazyReact.sequentialBuilder().of().to()
						.optional().isPresent());
	  
	}
}
