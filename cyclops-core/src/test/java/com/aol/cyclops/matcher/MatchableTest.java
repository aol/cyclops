package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.matcher.MatchableTest.Child;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.matcher.recursive.Matchable.MatchableTuple2;
import com.aol.cyclops.matcher.recursive.Matchable.MatchableTuple3;
import com.aol.cyclops.matcher.recursive.Matchable.MyCase;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;


public class MatchableTest {

	private String concat(String a,String b){
		return a+","+b;
	}
	private boolean isValidStreet(String street1){
		return true;
	}
	private boolean isValidHouse(int house){
		return true;
	}
	@Test
	public void matchTestStructuralAndGuards(){
		String v  =new Address(10,"hello","my city").match()
							   			 .on$12_()
							   			 .when((house,street)-> 
							   			 	house.<String>mayMatch(c->c.hasValuesWhere(this::isValidHouse).then(i->"valid house"))
							   		            	 .recover("incorrectly configured house")
							   		            	 .ap2(this::concat)
							   		            	 .ap(
							   		      				street.<String>mayMatch(c->c.hasValuesWhere(this::isValidStreet).then(s->"valid street"))
							   		      				.recover("incorrectly configured steet")
							   		            	 )
							   		).get();
							   		
	}
	@Test
	public void matchTestStructuralOnly(){
		String v =new Address(10,"hello","my city").match()
							   			 .on$12_()
							   			 .when((house,street)-> 
							   					house.filter(this::isValidHouse).map(i->"valid house").recover("incorrectly configured house")
							   		                 .<String,String>ap2(this::concat)
							   		            	 .ap(street.filter(this::isValidStreet).map(s->"valid street").recover("incorrectly configured steet")).get());
							   		
	}
	@Test
	public void matchTestNestedStructural(){
		String v = new Customer("test",new Address(10,"hello","my city"))
										 .match()
							   			 .on$_2()
							   			 .when(address ->
							   			   		 address.on$12_()
							   					   		.when((house,street)-> 
							   									house.filter(this::isValidHouse).map(i->"valid house").recover("incorrectly configured house")
							   									 	 .ap2(this::concat)
							   									 	 .ap(street.filter(this::isValidStreet).map(s->"valid street").recover("incorrectly configured steet"))
							   									 	 .get())
							   			 , ()->"no address configured");
							   	
	}
	@AllArgsConstructor
	static class Address{
		int house;
		String street;
		String city;
		
		public MatchableTuple3<Integer,String,String> match(){
			return Matchable.from(()->house,()->street,()->city);
		}
	}
	@AllArgsConstructor
	static class Customer{
		String name;
		Address address;
		public MatchableTuple2<String,MatchableTuple3<Integer,String,String>> match(){
			return Matchable.from(()->name,()->Maybe.ofNullable(address).map(a->a.match()).orElseGet(()->null));
		}
	}
	@Test
	public void testMatch(){
		Matchable example with Predicates.type(type)
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
	static class NestedCase <R> implements Matchable<MyCase<R>>{
		int a;
		int b;
		NestedCase<R> c;
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
		Maybe<Integer> result = Matchable.of(Optional.of(1))
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
