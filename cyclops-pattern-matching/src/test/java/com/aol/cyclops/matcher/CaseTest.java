package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.val;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

public class CaseTest {
	Case<Integer,Integer,Function<Integer,Integer>> case1;
	Case<Integer,Integer,Function<Integer,Integer>> offCase;
	@Before
	public void setup(){
		case1 =Case.of(input->true,input->input+10);
		offCase = case1.mapPredicate(p-> p.negate());
	}
	
	@Test
	public void testNegate(){
		assertThat(case1.match(100).isPresent(),is(true));
		assertThat(case1.negate().match(100).isPresent(),is(false));
	}
	@Test
	public void testNegateAction(){
		
		assertThat(case1.negate().negate(input->input*100).match(100).get(),is(10000));
	}
	
	@Test
	public void testChaining(){
		ActionWithReturn<String,Integer> act = hello ->10;
		val caze = Case.of(t->true, act);
		
		assertThat(caze.filter(t -> t.v2.getType()!=null).mapFunction(fn -> input ->20).match("hello").get(),is(20));
	}
	@Test
	public void testChainingFilterFails(){
		ActionWithReturn<String,Integer> act = hello ->10;
		val caze = Case.of(t->true, act);
		
		assertThat(caze.filter(t -> t.v2.getType()==null).mapFunction(fn -> input ->20).match("hello").isPresent(),is(false));
	}
	
	@Test
	public void testfilterReturnsEmpty(){
		val empty = Case.of(t->true,input->10).filter(p->false);
		assertThat(empty,instanceOf(EmptyCase.class));
	}
	
	@Test
	public void testAnd(){
		assertThat(case1.and(p->false).match(100).isPresent(),is(false));
	}
	@Test
	public void testAndTrue(){
		assertThat(case1.and(p->true).match(100).isPresent(),is(true));
	}
	@Test
	public void testAndComposeFn(){
		assertThat(case1.composeAnd(p->false,(Integer input)->input*2).match(100).isPresent(),is(false));
	}
	@Test
	public void testAndComposeFnTrue(){
		assertThat(case1.composeAnd(p->true,(Integer input)->input*2).match(100).get(),is(210));
	}
	
	@Test
	public void mapPredicate(){
		assertThat(case1.mapPredicate(p-> t->false).match(100).isPresent(),is(false));
	}
	
	@Test
	public void mapFunction(){
		assertThat(case1.mapFunction(fn-> input->input+20).match(100).get(),is(120));
	}
	
	@Test
	public void map(){
		Tuple2<Predicate<Integer>,Function<Integer,Integer>> tuple = Tuple.tuple( t->false,(Integer input)->input+20);
		assertThat(case1.map(tuple2 -> tuple).match(100).isPresent(),is(false));
	}
	@Test
	public void mapTrue(){
		Tuple2<Predicate<Integer>,Function<Integer,Integer>> tuple = Tuple.tuple( t->true,(Integer input)->input+20);
		assertThat(case1.map(tuple2 -> tuple).match(100).get(),is(120));
	}
	
	@Test
	public void flatMap(){
		assertThat(case1.flatMap(tuple2 -> Case.of(tuple2.v1,(Integer input)->input+20)).match(100).get(),is(120));
	}
	
	@Test
	public void andThen(){
		assertThat(case1.andThen(Case.of(input->true,input->input*2)).match(100).get(),is(220));
	}
	@Test
	public void compose(){
		assertThat(case1.compose(Case.of((Integer input)->true,input->input*2)).match(100).get(),is(210));
	}
	@Test
	public void composeOr(){
		assertThat(case1.composeOr(Case.of((Integer input)->false,input->input*2)).match(100).get(),is(210));
	}
	@Test
	public void orFunction(){
		assertThat(case1.or((Integer input)->false,input->input*2).match(100).get(),is(210));
	}
	@Test
	public void composeFunction(){
		assertThat(case1.composeFunction((Integer input)->input*2).match(100).get(),is(210));
	}
	
	@Test
	public void andThenFunction(){
		assertThat(case1.andThenFunction(input->input*2).match(100).get(),is(220));
	}
	
	@Test
	public void or(){
		assertThat(offCase.or(t->true).match(100).get(),is(110));
	}
	@Test
	public void matchAsync(){
		assertThat(offCase.or(t->true).matchAsync(ForkJoinPool.commonPool(),100).join().get(),is(110));
	}
}
