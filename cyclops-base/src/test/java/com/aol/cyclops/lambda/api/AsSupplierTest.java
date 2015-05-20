package com.aol.cyclops.lambda.api;

import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
public class AsSupplierTest {

	@Test
	public void testAsSupplierObject() {
		assertThat(AsSupplier.asSupplier(Optional.of("hello")).get(),equalTo("hello"));
	}

	@Test
	public void testAsSupplierObjectString() {
		assertThat(AsSupplier.asSupplier(new Duck(),"quack").get(),equalTo("quack"));
	}

	static class Duck{
		
		public String quack(){
			return  "quack";
		}
	}
}
