package com.aol.cyclops.matcher;

import static com.aol.cyclops.control.Matchable.whenGuard;
import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;
import static com.aol.cyclops.util.function.Predicates.__;
import static com.aol.cyclops.util.function.Predicates.eq;
import static com.aol.cyclops.util.function.Predicates.has;
import static com.aol.cyclops.util.function.Predicates.type;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;
import com.aol.cyclops.control.Matchable.MatchSelf;
import com.aol.cyclops.control.Matchable.MatchableTuple2;
import com.aol.cyclops.control.Matchable.MatchableTuple3;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.util.function.Predicates;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;


public class MatchableTest {

	private String concat(String a,String b){
		return a+","+b;
	}
	private boolean isValidCity(String city){
		return true;
	}
	private boolean isValidStreet(String street1){
		return true;
	}
	private boolean isValidHouse(int house){
		return true;
	}
	@Test
	public void pojoTypeSafe(){
		/**
		new Customer("test",new Address(10,"hello","my city")).match().mayMatch( 
				
				c->c.isWhere(t3->"ok",(String s)->s=="test",(MatchableTuple3<Integer,String,String> a)->a==null)
				);
		//Matchable.of(10).mayMatch(c->c.)
		**/
		new Address(10,"hello","world").match().mayMatch(c->c.is(when(this::isValidHouse, this::isValidStreet,this::isValidCity),then("ok")))
																	 .orElse("hello");
			
		new Address(10,"hello","world").match().mayMatch(c->c.is(when(10,"hello","world"),then("ok"))
															 .is(when(6,"something","oops!"),then("res"))
															 .isEmpty(then(()->"empty")));
	
				
	}
	@Test
	public void tuple2(){
		Eval<String> result = Matchable.from(()->"hello",()->2) 
										.matches(c->c.is(when("hello",5), (t2)-> "hello"),otherwise("hello"));
		
		result.get();
	}
	@Test
	public void tuple2Predicates(){
		 Matchable.from(()->"hello",()->2)
 			.mayMatch(c->c.is(when(s->s=="hello",t->(int)t>5),then(()->"hello")));
	}
	@Test
	public void matchTestStructuralAndGuards(){
	
		
	    
	   
	    
		Matchable.of("hello").<String>mayMatch(c->c.is(when("hello"),then("world")));
		//assertThat(Matchable.of("hello").<String>mayMatch(c->c.is(i->"world", "hello"))),equalTo(Maybe.of("world"));
	//	assertThat(Matchable.of("hello").mayMatch(c->c.is(in -> "world", "hello"))),equalTo(Maybe.of("world")));
		
	 Matchable.from(()->"hello",()->ListX.of(1,2,3))
	 			.visit((num,list)-> list.orElse(ListX.empty())
	 											.visit((x,xs)-> xs.toList()));
	 			     
		String v  =new Address(10,"hello","my city").match()
							   			 .on$12_()
							   			 .visit((house,street)-> 
							   			 	house.<String>mayMatch(c->c.is(when(this::isValidHouse),in->"valid house"))
							   		            	 .recover("incorrectly configured house")
							   		            	 .ap2(this::concat)
							   		            	 .ap(
							   		      				street
							   		      						.<String>mayMatch(c->c.has(when(this::isValidStreet),then(()->"valid street")))
							   		      						
							   		      				.recover("incorrectly configured steet")
							   		            	 )
							   		).get();
		
		assertThat(v,equalTo("valid house,valid street"));
		
							   		
	}
	@Test
	public void matchTestStructuralOnly(){
		String v =new Address(10,"hello","my city")
										 .match()
							   			 .on$12_()
							   			 .visit((house,street)-> 
							   					house.filter(this::isValidHouse)
							   						 .map(i->"valid house")
							   						 .recover("incorrectly configured house")
							   		                 .ap2(this::concat)
							   		            	 .ap(street.filter(this::isValidStreet)
							   		            			   .map(s->"valid street")
							   		            			   .recover("incorrectly configured steet"))
							   		            	 .get());
		
		
		assertThat(v,equalTo("valid house,valid street"));
							   		
	}
	
	@Test
	public void matchTestNestedStructural(){
		String v = new Customer("test",new Address(10,"hello","my city"))
										 .match()
							   			 .on$_2()
							   			 .visit(address ->
							   			   		 address.on$12_()
							   					   		.visit((house,street)-> 
							   									house.filter(this::isValidHouse)
							   										 .map(i->"valid house")
							   										 .recover("incorrectly configured house")
							   									 	 .ap2(this::concat)
							   									 	 .ap(street.filter(this::isValidStreet)
							   									 			   .map(s->"valid street")
							   									 			   .recover("incorrectly configured steet"))
							   									 	 .get())
							   			 , ()->"no address configured");
		
		assertThat(v,equalTo("valid house,valid street"));
							   	
	}
	@AllArgsConstructor
	static class Address{
		int house;
		String street;
		String city;
		
		public MTuple3<Integer,String,String> match(){
			return Matchable.from(()->house,()->street,()->city);
		}
	}
	@AllArgsConstructor
	static class Customer{
		String name;
		Address address;
		public MTuple2<String,MTuple3<Integer,String,String>> match(){
			return Matchable.from(()->name,()->Maybe.ofNullable(address).map(a->a.match()).orElseGet(()->null));
		}
	}
	@Test
	public void testMatch(){
		
		Matchable.of(new NestedCase(1,2,new NestedCase(3,4,null))) //relaxed type operators on CheckValues again (is)
		 			.matches(c->c.is(whenGuard(1,__,has(3,4,__)),in->"2"),otherwise(4))
		 			.get();
		Matchable.of(new NestedCase(1,2,new NestedCase(3,4,null)))
			.matches(c->c.is(whenGuard(1,__,type(NestedCase.class).has(3,4,__)),in->"2"),otherwise(-1))
			.get();
		Matchable.of(Arrays.asList(1,2,3))
					.matches(c->c.is(whenGuard(1,__,3),in->"2"),otherwise(2));
		Matchable.of(Arrays.asList(1,2,3))
					
					.matches(c->c.is(whenGuard(t->t.equals(1),Predicates.__,t->t.equals(3)),in->"2"),otherwise(-2));
		
		Matchable.of(Arrays.asList(1,2,3))
					.matches(c->c.is(when(equalTo(1),any(Integer.class),equalTo(4)),in->"2"),otherwise(45));
		
		
		
	}
	@Value
	static class MyCase<R>  implements MatchSelf<MyCase<R>>, Decomposable{
		int a;
		int b;
		int c;
	}
	@Value
	static class NestedCase <R> implements MatchSelf<MyCase<R>>, Decomposable{
		int a;
		int b;
		NestedCase<R> c;
	}
	@Test
	public void singleCase(){
		Eval<Integer> result = Matchable.of(Optional.of(1))
										.matches(c->c.is(when(1),then(2)),otherwise(3));
		
		assertThat(result,equalTo(Eval.now(2)));
	}
	@Test(expected=NoSuchElementException.class)
	public void singleCaseFail(){
		 Matchable.of(Optional.of(2))
				   .matches(c->c.is(when(1),in->2),otherwise(1)).get();
		
		fail("exception expected");
	}
	@Test
	public void cases2(){
		Eval<Integer> result = Matchable.listOfValues(1,2)
										.matches(c->c.has(when(1,3),in->2)
											 .has(when(1,2),in->3),otherwise(8));
		
		assertThat(result,equalTo(Eval.now(3)));
	}
	@Test 
	public void matchable(){
		Maybe<Integer> result = Matchable.of(Optional.of(1))
											.mayMatch(c->c.is(when(2),in->2));
	
		assertThat(result
						 .matches(c->c.isEmpty( then("hello")),otherwise("none")),equalTo(Eval.now("hello")));
		
	}
	@Test 
	public void optionalMatch(){
		Eval<Integer> result2 = Matchable.of(Optional.of(1))
									
									.matches(c->c.values(in->2,1));
		
		assertThat(result2,equalTo(Eval.now(2)));
	}
	@Test 
	public void emptyList(){
		
		assertThat(Matchable.of(Arrays.asList()).matches(c->c.isEmpty(in->"hello")),equalTo(Eval.now("hello")));
	}
	@Test 
	public void emptyStream(){
		
		assertThat(Matchable.of(Stream.of()).matches(c->c.isEmpty(in->"hello")),equalTo(Eval.now("hello")));
	}
	@Test 
	public void emptyOptional(){
		
		assertThat(Matchable.of(Optional.empty()).matches(c->c.isEmpty(in->"hello")),equalTo(Eval.now("hello")));
	}
	@Test
	public void emptyOptionalMultiple2(){
		assertThat(Matchable.of(Optional.empty())
				            .matches(
				            			o-> o.isEmpty(in->"hello")
				            			     .values(in->"2",1)
				            		)
				            		,equalTo(Eval.now("hello")));
		
		
	}
	@Test
	public void emptyOptionalMultiple3(){
		assertThat(Matchable.of(Optional.empty())
							
				            .matches(
				            			o-> o.isEmpty(in->"hello")
				            			     .values(i->""+2,1)
				            			     .has(i->""+3,2)
				            		).get()
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyMaybeMultiple3(){
		assertThat(Maybe.none()
						.matches(
				            			o-> o.isEmpty(in->"hello")
				            			      .values(in->"2",1)
				            			      .has(in->"3",2)
				            		)
				            		,equalTo(Eval.now("hello")));
		
		
	}
	@Test
	public void emptyOptionalMultiple4(){
		assertThat(Matchable.of(Optional.of(3))
							
				            .matches(
				            			o-> o.isEmpty(i->"hello")
				            			     .values(i->""+2,1)
				            			     .has(i->""+3,2)
				            			     .has(i->""+4,3)
				            		)
				            		,equalTo(Eval.now("4")));
		
		
	}
	@Test
	public void emptyOptionalMultiple5(){
		assertThat(Matchable.of(Optional.of(4))
								
								.matches(
				            			o-> o.isEmpty(i->"hello")
				            			.values(i->""+2,1)
				            			.has(i->""+3,2)
				            			.has(i->""+4,3)
				            			.has(i->""+5,4)
				            		)
				            		,equalTo(Eval.now("5")));
		
		
	}
	@Test 
	public void emptyOptionalMaybe(){
		
		assertThat(Matchable.of(Optional.empty()).mayMatch(c->c.isEmpty(i->"hello")).get(),equalTo("hello"));
	}
	@Test
	public void emptyOptionalMultiple2Maybe(){
		assertThat(Matchable.of(Optional.empty())
				            .mayMatch(
				            			o-> o.isEmpty(i->"hello")
				            			     .has(i->""+2)
				            		).get()
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyOptionalMultiple3Maybe(){
		assertThat(Matchable.of(Optional.empty())
				            .mayMatch(
				            			o-> o.isEmpty(i->"hello")
				            			     .has(i->""+2)
				            			     .has(i->""+3)
				            		).get()
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyOptionalMultiple4Maybe(){
		assertThat(Matchable.of(Optional.of(3))
				            .mayMatch(
				            			o-> o.isEmpty(i->"hello")
				            			     .values(i->""+2,1)
				            			     .has(i->""+3,2)
				            			     .has(i->""+4,3)
				            		).get()
				            		,equalTo("4"));
		
		
	}
	@Test
	public void emptyOptionalMultiple5Maybe(){
		assertThat(Matchable.of(Optional.of(4))
				            .mayMatch(
				            			o-> o.isEmpty(i->"hello")
				            			     .values(i->""+2,1)
				            			     .has(i->""+3,2)
				            			     .has(i->""+4,3)
				            			     .has(i->""+5,4)
				            		).get()
				            		,equalTo("5"));
		
		
	}
	public void matchByType(){
		
		assertThat(Matchable.of(1)
				                    .matches(c->c.justMatch(in->"hello",instanceOf(Integer.class))),
				                    equalTo("hello"));
	}
	public void matchListOfValues(){
		assertThat(Matchable.listOfValues(1,2,3)
							        .matches(c->c.where(i->2,(Object i)->(i instanceof Integer))),
							        equalTo(2));
		
	}
	@Test
	public void recursive(){
		Eval<String> result = Matchable.listOfValues(1,new MyCase(4,5,6))
				 				.matches(c->c.values(i->"rec",Predicates.__,Predicates.has(4,5,6)));
		
		assertThat(result.get(),equalTo("rec"));
	}
	
	@Test
	public void matchType(){
		
		Eval<Integer> result = Matchable.of(new Child(10,20)).matches(
									c-> c.justWhere(in->10, Predicates.type(Child.class).has()));
		
		assertThat(result.get(),equalTo(10));
	}
	@Test
	public void matchTypeBreakdown(){
		
		Eval<Integer> result = Matchable.of(new Child(10,20)).matches(
									c-> c.justWhere(in->10, Predicates.type(Child.class).has(10,20)));
		
		assertThat(result.get(),equalTo(10));
	}
	@Test
	public void matchTypeBreakdownJust(){
		
		Maybe<Integer> result = Matchable.of(new Child(10,20)).mayMatch(
									c-> c.justWhere(in->10, Predicates.type(Child.class).is(10)));
		
		assertThat(result,equalTo(Maybe.none()));
	}
	@Test
	public void matchTypeBreakdownJust2(){
		
		Maybe<Integer> result = Matchable.of(new Child(10,20)).mayMatch(
									c-> c.justWhere(in->10, Predicates.type(Child.class).is(10,20)));
		
		assertThat(result,equalTo(Maybe.of(10)));
	}
	@Test
	public void matchTypeBreakdownJustWhere(){
		
		Maybe<Integer> result = Matchable.of(new Child(10,20)).mayMatch(
									c-> c.justWhere(in->10, Predicates.type(Child.class).isWhere(i->(int)i==10,i->(int)i==20)));
		
		assertThat(result,equalTo(Maybe.of(10)));
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
