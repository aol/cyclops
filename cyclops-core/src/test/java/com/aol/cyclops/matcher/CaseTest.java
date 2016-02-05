package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Predicates.__;
import static com.aol.cyclops.matcher.Predicates.hasValues;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Value;
import lombok.val;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.objects.Decomposable;

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
		TypedFunction<String,Integer> act = hello ->10;
		 Case<String, Integer, TypedFunction<String, Integer>> caze = Case.of(t->true, act)
				 														.filter(t -> t.v2.getType()!=null);
		Case<String, Object, Function<String, Object>> x = caze.mapFunction(fn -> input ->20);
		assertThat(x.match("hello").get(),is(20));
	}
	@Test
	public void testChainingFilterFails(){
		TypedFunction<String,Integer> act = hello ->10;
		 Case<String, Integer, TypedFunction<String, Integer>> caze = Case.of(t->true, act)
				 															.filter(t -> t.v2.getType()==null);
		 Case<String, Object, Function<String, Object>> x = caze.mapFunction(fn -> input ->20);
		assertThat(x.match("hello").isPresent(),is(false));
	}
	
	
	
	@Test
	public void testfilterReturnsEmpty(){
		Case empty = Case.of(t->true,input->10);
		empty=empty.filter(p->false);
		assertThat(empty,instanceOf(EmptyCase.class));
	}
	
	@Test
	public void testAnd(){
		assertThat(case1.and(p->false).match(100).isPresent(),is(false));
	}
	@Test
	public void testAndOfType(){
		assertThat(case1.andOfType(Integer.class).match(100).isPresent(),is(true));
	}
	@Test
	public void testAndOfTypeNegative(){
		assertThat(((Case)case1).andOfType(String.class).match(100).isPresent(),is(false));
	}
	@Test
	public void testAndOfValue(){
		assertThat(case1.andOfValue(100).match(100).isPresent(),is(true));
	}
	@Test
	public void testAndOfValueNegative(){
		assertThat(((Case)case1).andOfValue(5).match(100).isPresent(),is(false));
	}
	@Value static final class Person implements Decomposable{ String name; int age; Address address; }
	@Value static final  class Address implements Decomposable { int number; String city; String country;}
	
	@Test
	public void testAndWithValues(){
		System.out.println(Object.class.isAssignableFrom(Person.class));
		
		Case<Person, Object, Function<Person, Object>> case2 = Case.of((Person p)->p.age>18,p->p.name + " can vote");
		assertThat(case2.andWithValues(__,__,Predicates.hasValues(__,__,"Ireland")).match(new Person("bob",19,new Address(10,"dublin","Ireland"))).isPresent(),is(true));
	}
	@Test
	public void testAndWithValuesNegative(){
		 Case<Person, Object, Function<Person, Object>> case2 = Case.of((Person p)->p.age>18,p->p.name + " can vote");
		assertThat(case2.andWithValues(__,__,hasValues(__,__,"Ireland")).match(new Person("bob",17,new Address(10,"dublin","Ireland"))).isPresent(),is(false));
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
		Case<Integer, Object, Function<Integer, Object>> x = case1.mapFunction(fn-> input->input+20);
		assertThat(x.match(100).get(),is(120));
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
	//	assertThat(case1.flatMap(tuple2 -> Case.of(tuple2.v1,(Integer input)->input+20)).match(100).get(),is(120));
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
