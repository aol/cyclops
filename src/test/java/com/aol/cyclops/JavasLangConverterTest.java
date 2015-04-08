package com.aol.cyclops;

import static com.aol.cyclops.JavasLangConverter.*;
import static com.aol.cyclops.JavasLangConverter.FromJDK.λ2;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import javaslang.Tuple;

import org.junit.Test;

import com.aol.cyclops.JavasLangConverter.FromTotallyLazy;

public class JavasLangConverterTest {

	@Test
	public void testJDKλ() {
		assertThat(JavasLangConverter.FromJDK.λ((Integer a)->a*100).apply(2),is(200));
		
	}
	@Test
	public void testJDKλ2(){
		assertThat(λ2((Integer a,Integer b)->a*b).curried().apply(100).apply(5),is(500));
	}
	@Test
	public void testJDKOption(){
		assertThat(JavasLangConverter.FromJDK.option(Optional.of(1)).get(),is(1));
	}
	@Test
	public void testJDKOptionNull(){
		assertThat(JavasLangConverter.FromJDK.option(Optional.ofNullable(null)).orElse(100),is(100));
	}
	
	@Test
	public void testGuavaλ() {
		assertThat(JavasLangConverter.FromGuava.λ((Integer a)->a*100).apply(2),is(200));
		
	}
	@Test
	public void testGuavaOption(){
		assertThat(JavasLangConverter.FromGuava.option(com.google.common.base.Optional.of(1)).get(),is(1));
	}
	@Test
	public void testJDKGuavaNull(){
		assertThat(JavasLangConverter.FromGuava.option(com.google.common.base.Optional.fromNullable(null)).orElse(100),is(100));
	}



	
	


}
