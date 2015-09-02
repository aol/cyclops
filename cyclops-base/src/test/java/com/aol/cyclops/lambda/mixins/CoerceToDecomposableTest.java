package com.aol.cyclops.lambda.mixins;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import lombok.Value;

import org.junit.Test;

import com.aol.cyclops.objects.AsDecomposable;
public class CoerceToDecomposableTest {

	@Test
	public void test() {
		assertThat(AsDecomposable.asDecomposable(new MyCase("key",10))
				.unapply(),equalTo(Arrays.asList("key",10)));
	}
	
	
	@Value
	static class MyCase { String key; int value;}

}
