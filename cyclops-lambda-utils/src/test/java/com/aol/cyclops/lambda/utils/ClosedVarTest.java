package com.aol.cyclops.lambda.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
public class ClosedVarTest {

	

	@Test
	public void testSet() {
		assertThat(new ClosedVar().set("hello").get(),is("hello"));
	}

	@Test
	public void testClosedVar() {
		assertThat(new ClosedVar(10).get(),equalTo(10));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new ClosedVar(10),equalTo(new ClosedVar(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new ClosedVar(10),not(equalTo(new ClosedVar(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new ClosedVar(10).hashCode(),equalTo(new ClosedVar(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new ClosedVar(10).hashCode(),not(equalTo(new ClosedVar(20).hashCode())));
	}
}
