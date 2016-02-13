package com.aol.cyclops.javaslang;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import javaslang.Tuple;

import org.junit.Test;

public class TotallyLazyTest {
/**	@Test
	public void testFromTotallyLazyλ() {
		assertThat(FromTotallyLazy.λ((Integer a)->a*100).apply(2),is(200));
		
	}
	
	@Test
	public void testFromTotallyLazyλ2(){
		assertThat(FromTotallyLazy.λ2((Integer a,Integer b)->a*b).curried().apply(100).apply(5),is(500));
	}
	@Test
	public void testFromTotallyLazyOption(){
		assertThat(FromTotallyLazy.option(com.googlecode.totallylazy.Option.option(1)).get(),is(1));
	}
	@Test
	public void testFromTotallyLazyOptionNull(){
		assertThat(FromTotallyLazy.option(com.googlecode.totallylazy.Option.none()).orElse(100),is(100));
	}
	@Test
	public void testFromTotallyLazyEitherLeft(){
		assertThat(FromTotallyLazy.either(com.googlecode.totallylazy.Either.left(1)).left().get(),is(1));
	}
	@Test
	public void testFromTotallyLazyEitherRight(){
		assertThat(FromTotallyLazy.either(com.googlecode.totallylazy.Either.right(1)).right().get(),is(1));
	}
	@Test
	public void testFromTotallyLazyTuple2(){
		assertThat(FromTotallyLazy.tuple(com.googlecode.totallylazy.Pair.pair(1, 2)),is(Tuple.of(1,2)));
	}
	@Test
	public void testFromTotallyLazyTuple3(){
		assertThat(FromTotallyLazy.tuple(com.googlecode.totallylazy.Triple.triple(1, 2,3)),is(Tuple.of(1,2,3)));
	}
	@Test
	public void testFromTotallyLazyTuple4(){
		assertThat(FromTotallyLazy.tuple(com.googlecode.totallylazy.Quadruple.quadruple(1, 2, 3, 4)),is(Tuple.of(1,2,3,4)));
	}
	@Test
	public void testFromTotallyLazyTuple5(){
		assertThat(FromTotallyLazy.tuple(com.googlecode.totallylazy.Quintuple.quintuple(1, 2, 3, 4,5)),is(Tuple.of(1,2,3,4,5)));
	}**/
}
