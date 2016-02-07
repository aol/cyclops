package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Predicates.__;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.aol.cyclops.matcher.recursive.Matchable;


public class MatchableTest {

	@Test
	public void testMatch(){
		
		Matchable.of(new NestedCase(1,2,new NestedCase(3,4,null)))
		 			.matches(c->c.hasValues(1,__,Predicates.hasValues(3,4,__))
				 	.then(i->"2"));
		Matchable.of(Arrays.asList(1,2,3))
		 		 .matches(c->c.hasValues(1,__,3)
		 				 	.then(i->"2"));
		Matchable.of(Arrays.asList(1,2,3))
		 .matches(c->c.hasValuesWhere(t->t.equals(1),Predicates.__,t->t.equals(3))
				 	.then(i->"2"));
		
		Matchable.of(Arrays.asList(1,2,3))
					.matches(c->c.hasValuesMatching(equalTo(1),any(Integer.class),equalTo(4))
							.then(i->"2"));
		assertThat(new MyCase<Integer>(4,5,6).matches(c ->
								c.isType((MyCase<Integer> ce) -> "hello").anyValues()
							) ,
				  equalTo("hello"));
		
		
	}
	@Value
	static class MyCase<R>  implements Matchable<MyCase<R>>{
		int a;
		int b;
		int c;
	}
	@Value
	static class NestedCase  implements Matchable<MyCase<R>>{
		int a;
		int b;
		NestedCase c;
	}
	@Test
	public void singleCase(){
		
		
		int result = Matchable.of(Optional.of(1))
								.matches(c->c.hasValues(1).then(i->2));
		
		assertThat(result,equalTo(2));
	}
	@Test(expected=NoSuchElementException.class)
	public void singleCaseFail(){
		 Matchable.of(Optional.of(2))
								.matches(c->c.hasValues(1).then(i->2));
		
		fail("exception expected");
	}
	@Test
	public void cases2(){
		int result = Matchable.listOfValues(1,2)
								.matches(c->c.hasValues(1,3).then(i->2),
										c->c.hasValues(1,2).then(i->3));
		
		assertThat(result,equalTo(3));
	}
	@Test 
	public void matchable(){
		Optional<Integer> result = Matchable.of(Optional.of(1))
											.mayMatch(c->c.hasValues(2).then(i->2));
		assertThat(Matchable.of(result)
				 .matches(c->c.isEmpty().then(i->"hello")),equalTo("hello"));
		
	}
	@Test 
	public void optionalMatch(){
		Integer result2 = Matchable.of(Optional.of(1))
									.matches(c->c.hasValues(1).then(i->2));
		
		assertThat(result2,equalTo(2));
	}
	@Test 
	public void emptyList(){
		
		assertThat(Matchable.of(Arrays.asList()).matches(c->c.isEmpty().then(i->"hello")),equalTo("hello"));
	}
	@Test 
	public void emptyStream(){
		
		assertThat(Matchable.of(Stream.of()).matches(c->c.isEmpty().then(i->"hello")),equalTo("hello"));
	}
	@Test 
	public void emptyOptional(){
		
		assertThat(Matchable.of(Optional.empty()).matches(c->c.isEmpty().then(i->"hello")),equalTo("hello"));
	}
	@Test
	public void emptyOptionalMultiple2(){
		assertThat(Matchable.of(Optional.empty())
				            .matches(
				            			o-> o.isEmpty().then(i->"hello"),
				            			o-> o.hasValues(1).then(i->""+2)
				            		)
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyOptionalMultiple3(){
		assertThat(Matchable.of(Optional.empty())
				            .matches(
				            			o-> o.isEmpty().then(i->"hello"),
				            			o-> o.hasValues(1).then(i->""+2),
				            			o-> o.hasValues(2).then(i->""+3)
				            		)
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyOptionalMultiple4(){
		assertThat(Matchable.of(Optional.of(3))
				            .matches(
				            			o-> o.isEmpty().then(i->"hello"),
				            			o-> o.hasValues(1).then(i->""+2),
				            			o-> o.hasValues(2).then(i->""+3),
				            			o-> o.hasValues(3).then(i->""+4)
				            		)
				            		,equalTo("4"));
		
		
	}
	@Test
	public void emptyOptionalMultiple5(){
		assertThat(Matchable.of(Optional.of(4))
				            .matches(
				            			o-> o.isEmpty().then(i->"hello"),
				            			o-> o.hasValues(1).then(i->""+2),
				            			o-> o.hasValues(2).then(i->""+3),
				            			o-> o.hasValues(3).then(i->""+4),
				            			o-> o.hasValues(4).then(i->""+5)
				            		)
				            		,equalTo("5"));
		
		
	}
	@Test 
	public void emptyOptionalMaybe(){
		
		assertThat(Matchable.of(Optional.empty()).mayMatch(c->c.isEmpty().then(i->"hello")).get(),equalTo("hello"));
	}
	@Test
	public void emptyOptionalMultiple2Maybe(){
		assertThat(Matchable.of(Optional.empty())
				            .mayMatch(
				            			o-> o.isEmpty().then(i->"hello"),
				            			o-> o.hasValues(1).then(i->""+2)
				            		).get()
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyOptionalMultiple3Maybe(){
		assertThat(Matchable.of(Optional.empty())
				            .mayMatch(
				            			o-> o.isEmpty().then(i->"hello"),
				            			o-> o.hasValues(1).then(i->""+2),
				            			o-> o.hasValues(2).then(i->""+3)
				            		).get()
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyOptionalMultiple4Maybe(){
		assertThat(Matchable.of(Optional.of(3))
				            .mayMatch(
				            			o-> o.isEmpty().then(i->"hello"),
				            			o-> o.hasValues(1).then(i->""+2),
				            			o-> o.hasValues(2).then(i->""+3),
				            			o-> o.hasValues(3).then(i->""+4)
				            		).get()
				            		,equalTo("4"));
		
		
	}
	@Test
	public void emptyOptionalMultiple5Maybe(){
		assertThat(Matchable.of(Optional.of(4))
				            .mayMatch(
				            			o-> o.isEmpty().then(i->"hello"),
				            			o-> o.hasValues(1).then(i->""+2),
				            			o-> o.hasValues(2).then(i->""+3),
				            			o-> o.hasValues(3).then(i->""+4),
				            			o-> o.hasValues(4).then(i->""+5)
				            		).get()
				            		,equalTo("5"));
		
		
	}
	public void matchByType(){
		
		assertThat(Matchable.of(1)
				                    .matches(c->c.isType((Integer it)->"hello").anyValues()),
				                    equalTo("hello"));
	}
	public void matchListOfValues(){
		assertThat(Matchable.listOfValues(1,2,3)
							        .matches(c->c.hasValuesWhere((Object i)->(i instanceof Integer)).then(i->2)),
							        equalTo(2));
		
	}
	@Test
	public void recursive(){
		String result = Matchable.listOfValues(1,new MyCase(4,5,6))
				 				.matches(c->c.hasValues(Predicates.__,Predicates.hasValues(4,5,6))
				 				.then(i->"rec"));
		
		assertThat(result,equalTo("rec"));
	}
	
	@Test
	public void matchType(){
		
		int result = Matchable.of(new Child(10,20)).matches(
									c-> c.isType( (Child child) -> child.val).hasValues(10,20)
									);
		
		assertThat(result,equalTo(10));
	}
	
	@Value
	static class Child extends Parent{
		int nextVal;
		public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
	}
	@AllArgsConstructor(access=AccessLevel.PACKAGE)
	static class Parent{
		int val;
	}
	
}
