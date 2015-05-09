package com.aol.cyclops.lambda.mixins;

import static org.junit.Assert.fail;

import java.util.Arrays;

import lombok.Value;

import org.junit.Test;

import com.aol.cyclops.lambda.api.CoerceToDecomposable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
public class CoerceToDecomposableTest {

	@Test
	public void test() {
		assertThat(CoerceToDecomposable.coerceToDecomposable(new MyCase("key",10)).unapply(),equalTo(Arrays.asList("key",10)));
	}
	
	
	@Value
	static class MyCase { String key; int value;}

}
