package com.aol.cyclops;

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

	@Test
	public void testTotallyLazyλ() {
		assertThat(JavasLangConverter.FromTotallyLazy.λ((Integer a)->a*100).apply(2),is(200));
		
	}
	@Test
	public void testFromTotallyLazyλ2(){
		assertThat(FromTotallyLazy.λ2((Integer a,Integer b)->a*b).curried().apply(100).apply(5),is(500));
	}
	@Test
	public void testFromTotallyLazyOption(){
		assertThat(JavasLangConverter.FromTotallyLazy.option(com.googlecode.totallylazy.Option.option(1)).get(),is(1));
	}
	@Test
	public void testFromTotallyLazyOptionNull(){
		assertThat(JavasLangConverter.FromTotallyLazy.option(com.googlecode.totallylazy.Option.none()).orElse(100),is(100));
	}
	@Test
	public void testFromTotallyLazyEitherLeft(){
		assertThat(JavasLangConverter.FromTotallyLazy.either(com.googlecode.totallylazy.Either.left(1)).left().get(),is(1));
	}
	@Test
	public void testFromTotallyLazyEitherRight(){
		assertThat(JavasLangConverter.FromTotallyLazy.either(com.googlecode.totallylazy.Either.right(1)).right().get(),is(1));
	}
	@Test
	public void testFromTotallyLazyTuple2(){
		assertThat(JavasLangConverter.FromTotallyLazy.tuple(com.googlecode.totallylazy.Pair.pair(1, 2)),is(new Tuple.Tuple2(1,2)));
	}
	@Test
	public void testFromTotallyLazyTuple3(){
		assertThat(JavasLangConverter.FromTotallyLazy.tuple(com.googlecode.totallylazy.Triple.triple(1, 2,3)),is(new Tuple.Tuple3(1,2,3)));
	}
	@Test
	public void testFromTotallyLazyTuple4(){
		assertThat(JavasLangConverter.FromTotallyLazy.tuple(com.googlecode.totallylazy.Quadruple.quadruple(1, 2, 3, 4)),is(new Tuple.Tuple4(1,2,3,4)));
	}
	@Test
	public void testFromTotallyLazyTuple5(){
		assertThat(JavasLangConverter.FromTotallyLazy.tuple(com.googlecode.totallylazy.Quintuple.quintuple(1, 2, 3, 4,5)),is(new Tuple.Tuple5(1,2,3,4,5)));
	}
	
}
