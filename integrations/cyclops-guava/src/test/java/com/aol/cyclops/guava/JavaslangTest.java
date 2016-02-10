package com.aol.cyclops.guava;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import javaslang.control.Option;

import org.junit.Test;



public class JavaslangTest {

	@Test
	public void testFromJavaslangλ() {
		assertThat(FromJavaslang.f1((Integer a)->a*100).apply(2),is(200));
		
	}
	
	@Test
	public void testFromJavaslangOption(){
		assertThat(FromJavaslang.option(Option.of(1)).get(),is(1));
	}
	@Test
	public void testFromJavaslangOptionNull(){
		assertThat(FromJavaslang.option(Option.none()).or(100),is(100));
	}
	
	
	
	
}
