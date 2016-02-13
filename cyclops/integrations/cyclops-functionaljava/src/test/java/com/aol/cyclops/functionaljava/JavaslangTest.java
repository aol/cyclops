package com.aol.cyclops.functionaljava;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import javaslang.Tuple;
import javaslang.control.Left;
import javaslang.control.Option;
import javaslang.control.Right;

import org.junit.Test;

import fj.P;



public class JavaslangTest {

	@Test
	public void testFromJavaslangλ() {
		assertThat(FromJavaslang.f1((Integer a)->a*100).f(2),is(200));
		
	}
	@Test
	public void testFromJavaslangλ2(){
		assertThat(FromJavaslang.f2((Integer a,Integer b)->a*b).f(100,5),is(500));
	}
	@Test
	public void testFromJavaslangOption(){
		assertThat(FromJavaslang.option(Option.of(1)).some(),is(1));
	}
	@Test
	public void testFromJavaslangOptionNull(){
		assertThat(FromJavaslang.option(Option.none()).orSome(100),is(100));
	}
	@Test
	public void testFromJavaslangEitherLeft(){
		assertThat(FromJavaslang.either(new Left(1)).left().value(),is(1));
	}
	@Test
	public void testFromJavaslangEitherRight(){
		assertThat(FromJavaslang.either(new Right(1)).right().value(),is(1));
	}
	@Test
	public void testFromJavaslangTuple1(){
		assertThat(FromJavaslang.tuple(Tuple.of(1)),is(P.p(1)));
	}
	@Test
	public void testFromJavaslangTuple2(){
		assertThat(FromJavaslang.tuple(Tuple.of(1, 2)),is(P.p(1,2)));
	}
	@Test
	public void testFromJavaslangTuple3(){
		assertThat(FromJavaslang.tuple(Tuple.of(1,2,3)),is(P.p(1,2,3)));
	}
	@Test
	public void testFromJavaslangTuple4(){
		assertThat(FromJavaslang.tuple(Tuple.of(1,2,3,4)),is(P.p(1,2,3,4)));
	}
	@Test
	public void testFromJavaslangTuple5(){
		assertThat(FromJavaslang.tuple(Tuple.of(1,2,3,4,5)),is(P.p(1,2,3,4,5)));
	}
	@Test
	public void testFromJavaslangTuple6(){
		assertThat(FromJavaslang.tuple(Tuple.of(1,2,3,4,5,6)),is(P.p(1,2,3,4,5,6)));
	}
	@Test
	public void testFromJavaslangTuple7(){
		assertThat(FromJavaslang.tuple(Tuple.of(1,2,3,4,5,6,7)),is(P.p(1,2,3,4,5,6,7)));
	}
	
	@Test
	public void testFromJavaslangTuple8(){
		assertThat(FromJavaslang.tuple(Tuple.of(1,2,3,4,5,6,7,8)),is(P.p(1,2,3,4,5,6,7,8)));
	}
	
	
	
	
}
