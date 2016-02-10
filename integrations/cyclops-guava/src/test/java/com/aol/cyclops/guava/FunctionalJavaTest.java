package com.aol.cyclops.guava;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class FunctionalJavaTest {

	@Test
	public void testFromFunctionalJavaf() {
		assertThat(FromFunctionalJava.f1((Integer a)->a*100).apply(2),is(200));
		
	}
	
	@Test
	public void testFromFunctionalJavaOption(){
		assertThat(FromFunctionalJava.option(fj.data.Option.some(1)).get(),is(1));
	}
	@Test
	public void testFromFunctionalJavaOptionNull(){
		assertThat(FromFunctionalJava.option(fj.data.Option.none()).or(100),is(100));
	}
	
	
}
