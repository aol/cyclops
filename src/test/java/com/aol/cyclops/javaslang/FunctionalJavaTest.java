package com.aol.cyclops.javaslang;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import javaslang.Tuple;

import org.junit.Test;

import com.aol.cyclops.javaslang.FromFunctionalJava;

public class FunctionalJavaTest {

	@Test
	public void testFromFunctionalJavaλ() {
		assertThat(FromFunctionalJava.λ((Integer a)->a*100).apply(2),is(200));
		
	}
	@Test
	public void testFromFunctionalJavaλ2(){
		assertThat(FromFunctionalJava.λ2((Integer a,Integer b)->a*b).curried().apply(100).apply(5),is(500));
	}
	@Test
	public void testFromFunctionalJavaOption(){
		assertThat(FromFunctionalJava.option(fj.data.Option.some(1)).get(),is(1));
	}
	@Test
	public void testFromFunctionalJavaOptionNull(){
		assertThat(FromFunctionalJava.option(fj.data.Option.none()).orElse(100),is(100));
	}
	@Test
	public void testFromFunctionalJavaEitherLeft(){
		assertThat(FromFunctionalJava.either(fj.data.Either.left(1)).left().get(),is(1));
	}
	@Test
	public void testFromFunctionalJavaEitherRight(){
		assertThat(FromFunctionalJava.either(fj.data.Either.right(1)).right().get(),is(1));
	}
	@Test
	public void testFromFunctionalJavaTuple1(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1)),is(Tuple.of(1)));
	}
	@Test
	public void testFromFunctionalJavaTuple2(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1, 2)),is(new Tuple.Tuple2(1,2)));
	}
	@Test
	public void testFromFunctionalJavaTuple3(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3)),is(Tuple.of(1,2,3)));
	}
	@Test
	public void testFromFunctionalJavaTuple4(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4)),is(Tuple.of(1,2,3,4)));
	}
	@Test
	public void testFromFunctionalJavaTuple5(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5)),is(Tuple.of(1,2,3,4,5)));
	}
	@Test
	public void testFromFunctionalJavaTuple6(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5,6)),is(Tuple.of(1,2,3,4,5,6)));
	}
	@Test
	public void testFromFunctionalJavaTuple7(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5,6,7)),is(Tuple.of(1,2,3,4,5,6,7)));
	}
	

	
	@Test
	public void testFromFunctionalJavaTuple8(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5,6,7,8)),is(Tuple.of(1,2,3,4,5,6,7,8)));
	}
	/**
	@Test
	public void testFromFunctionalJavaTuple9(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5,6,7,8,9)),is(Tuple.of(1,2,3,4,5,6,7,8,9)));
	}
	
	@Test
	public void testFromFunctionalJavaTuple10(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5,6,7,8,9,10)),is(Tuple.of(1,2,3,4,5,6,7,8,9,10)));
	}
	
	@Test
	public void testFromFunctionalJavaTuple11(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5,6,7,8,9,10,11)),is(Tuple.of(1,2,3,4,5,6,7,8,9,10,11)));
	}
	
	@Test
	public void testFromFunctionalJavaTuple12(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5,6,7,8,9,10,11,12)),is(Tuple.of(1,2,3,4,5,6,7,8,9,10,11,12)));
	}
	
	@Test
	public void testFromFunctionalJavaTuple13(){
		assertThat(FromFunctionalJava.tuple(fj.P.p(1,2,3,4,5,6,7,8,9,10,11,12,13)),is(Tuple.of(1,2,3,4,5,6,7,8,9,10,11,12,13)));
	}
	**/
	
	
	
}
