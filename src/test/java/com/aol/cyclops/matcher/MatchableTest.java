package com.aol.cyclops.matcher;

import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;
import static com.aol.cyclops.control.Matchable.whenGuard;
import static com.aol.cyclops.control.Maybe.just;
import static com.aol.cyclops.util.function.Predicates.__;
import static com.aol.cyclops.util.function.Predicates.any;
import static com.aol.cyclops.util.function.Predicates.decons;
import static com.aol.cyclops.util.function.Predicates.eq;
import static com.aol.cyclops.util.function.Predicates.greaterThan;
import static com.aol.cyclops.util.function.Predicates.has;
import static com.aol.cyclops.util.function.Predicates.in;
import static com.aol.cyclops.util.function.Predicates.lessThan;
import static com.aol.cyclops.util.function.Predicates.lessThanOrEquals;
import static com.aol.cyclops.util.function.Predicates.not;
import static com.aol.cyclops.util.function.Predicates.some;
import static com.aol.cyclops.util.function.Predicates.type;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;
import com.aol.cyclops.control.Matchable.MatchSelf;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.mixins.Printable;
import com.aol.cyclops.util.function.Predicates;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

public class MatchableTest implements Printable{
    
    Eval<Long> fibonacci(int i){
        return fibonacci(i,1,0);
    }
    Eval<Long> fibonacci(int n, long a, long b) {
        return n == 0 ? Eval.now(b) : Eval.later( ()->fibonacci(n-1, a+b, a)).flatMap(i->i);
    }
    
    
    
    @Test
    public void odd(){
        System.out.println(even(Eval.now(200000)).get());
    }
    public Eval<String> odd(Eval<Integer> n )  {
       
       return n.flatMap(x->even(Eval.now(x-1)));
    }
    public Eval<String> even(Eval<Integer> n )  {
        return n.flatMap(x->{
            return Matchable.of(x)
                            .matches(c->c.is(when(lessThanOrEquals(0)), then(()->"done")), 
                                                    odd(Eval.now(x-1)));
        });
     }
   
    
    private String doA(){
        return "hello";
    }
    private String doB(){
        return "world";
    }
    static class A{}
    static class B{}
    @Test
    public void matchTypeAB(){
        
       
            Object o = new A();
            assertThat(Matchable.of(o)
                     .matches(c->c.is(when(Predicates.type(A.class).anyValues()), then(this::doA))
                                  .is(when(Predicates.type(B.class).anyValues()),then(this::doB))
                                , otherwise("missing")),equalTo(Eval.now("hello")));
            
            assertThat( Matchable.of(o)
                     .matches(c->c.is(when(Predicates.instanceOf(A.class)), then(this::doA))
                                  .is(when(Predicates.instanceOf(B.class)), then(this::doB))
                                  ,otherwise("missing")),equalTo(Eval.now("hello")));
                    
    }
    @Test
    public void recursiveStructuralMatching(){
       
       assertTrue( Predicates.decons(when(10,"hello","my city")).test(new Address(10,"hello","my city").match()));
       
      String result =  new Customer("test",new Address(10,"hello","my city"))
                                .match()
                                .on$_2()
                                .matches(c->c.is(when(decons(when(10,"hello","my city"))),then("hello")), otherwise("miss")).get();
      
      assertThat(result,equalTo("hello"));
    }

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
		
		
		new Address(10,"hello","world").match().matches(c->c.is(when(this::isValidHouse, this::isValidStreet,this::isValidCity),then("ok")),otherwise("hoo!"));
			
		new Address(10,"hello","world").match().matches(c->c.is(when(10,"hello","world"),then("ok"))
															 .is(when(6,"something","oops!"),then("res"))
															 .isEmpty(then(()->"empty")),otherwise("boo"));
	
				
	}
	@Test
	public void tuple2(){
		Eval<String> result = Matchables.supplier2(()->"hello",()->2) 
										.matches(c->c.is(when("hello",5), then("hello")),otherwise("hello"));
		
		result.get();
	}
	@Test
	public void tuple2Predicates(){
		 Matchables.supplier2(()->"hello",()->2)
 			.matches(c->c.is(when(s->s=="hello",t->(int)t>5),then(()->"hello")),otherwise("world"));
	}
	@Test
	public void matchTestStructuralAndGuards(){
		    
		Matchable.of("hello")
		         .matches(c->c.is(when("hello"),then("world")),otherwise("boo!"));
		
		Matchables.supplier2(()->"hello",()->ListX.of(1,2,3))
	 			    .visit((num,list)-> just(list).orElse(ListX.empty())
	 											  .visit((x,xs)-> xs.toList(),()->ListX.empty()));
	 			     
		String v  =new Address(10,"hello","my city").match()
							   			 .on$12_()
							   			 .visit((house,street)-> 
							   			 	    just(house).matches(c->c.is(when(this::isValidHouse),then("valid house")),
							   			 	                        otherwise("incorrectly configured house"))
							   			 	               .applyFunctions()
							   			 	               .ap2(this::concat)
							   			 	               .ap(just(street)
							   		      						.matches(c->c.is(when(this::isValidStreet),then(()->"valid street")),
							   		      						        otherwise("incorrectly configured steet"))
							   			 	                ).get());
		
		assertThat(v,equalTo("valid house,valid street"));
		
							   		
	}
	@Test
	public void matchTestStructuralOnly(){
	    
		String v =new Address(10,"hello","my city")
										 .match()
							   			 .on$12_()
							   			 .visit((house,street)-> 
							   					just(house).filter(this::isValidHouse)
							   						 .map(i->"valid house")
							   						 .recover("incorrectly configured house")
							   						 .applyFunctions()
							   		                 .ap2(this::concat)
							   		            	 .ap(just(street)
							   		            	 .filter(this::isValidStreet)
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
							   									just(house).filter(this::isValidHouse)
							   										 .map(i->"valid house")
							   										 .recover("incorrectly configured house")
							   										 .applyFunctions()
							   									 	 .ap2(this::concat)
							   									 	 .ap(just(street).filter(this::isValidStreet)
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
			return Matchables.supplier3(()->house,()->street,()->city);
		}
	}
	@AllArgsConstructor
	static class Customer{
		String name;
		Address address;
		public MTuple2<String,MTuple3<Integer,String,String>> match(){
			return Matchables.supplier2(()->name,()->Maybe.ofNullable(address).map(a->a.match()).orElseGet(()->null));
		}
	}
	@Test
	public void testMatch(){
		
		Matchable.of(new NestedCase(1,2,new NestedCase(3,4,null))) 
		 			.matches(cases->cases.is(whenGuard(1,__,has(3,4,__)),then("2")),otherwise("4"))
		 			.get();
		Matchable.of(new NestedCase(1,2,new NestedCase(3,4,null)))
			.matches(c->c.is(whenGuard(1,__,type(NestedCase.class).hasGuard(3,4,__)),then("2")),otherwise("-1"))
			.get();
		Matchable.of(Arrays.asList(1,2,3))
					.matches(c->c.is(whenGuard(1,__,3),then("2")),otherwise("2"));
		Matchable.of(Arrays.asList(1,2,3))
					
					.matches(c->c.is(whenGuard(t->t.equals(1),Predicates.__,t->t.equals(3)),then("2")),otherwise("-2"));
		
		Matchable.of(Arrays.asList(1,2,3))
					.matches(c->c.is(whenGuard(eq(1),any(Integer.class),eq(4)),then("2")),otherwise("45"));
		
		
		
	}
	@Value
	static class MyCase  implements MatchSelf<MyCase>, Decomposable{
		int a;
		int b;
		int c;
	}
	@Value
	static class NestedCase implements MatchSelf<NestedCase>, Decomposable{
		int a;
		int b;
		NestedCase c;
	}
	@Test
	public void singleCase(){
		Eval<Integer> result = Matchables.optional(Optional.of(1))
										.matches(c->c.is(when(1),then(2)),otherwise(3));
		
		assertThat(result,equalTo(Eval.now(2)));
	}
	@Test
	public void singleCaseFail(){
		Eval<Integer> result =  Matchables.optional(Optional.of(1))
				   						 .matches(c->c.is(when(2),then(2)),otherwise(1));
		assertThat(result, equalTo(Eval.always(()->1)));
		
		
	}
	@Test
	public void cases2(){
		Eval<String> result = Matchables.listOfValues(1,2)
										.matches(c->c.has(when(1,3),then("2"))
											         .has(when(1,2),then("3")),otherwise("8"));
		
		assertThat(result,equalTo(Eval.now("3")));
	}
	@Test 
	public void matchable(){
		Eval<Integer> result = Matchables.optional(Optional.of(1))
											.matches(c->c.is(when(2),then(2)),otherwise(3));
	
		assertThat(result
						 .matches(c->c.is(when(10), then("hello")),otherwise("none")),equalTo(Eval.now("none")));
		
	}
	@Test 
	public void optionalMatch(){
		Eval<Integer> result2 = Matchable.of(Optional.of(1)).matches(c->c.is(when(not(in(2,3,4))),then(3)),otherwise(2));
		assertThat(result2,equalTo(Eval.now(3)));
		
		assertThat(Matchable.of(Optional.of(1)).visit(i->"some", ()->"none"),equalTo("some"));
	}
	@Test 
	public void emptyList(){
		
		assertThat(Matchables.iterable(Arrays.asList()).matches(c->c.isEmpty(then("hello")),otherwise("world")),equalTo(Eval.now("hello")));
	}
	@Test 
	public void emptyStream(){
		
		assertThat(Matchable.of(Stream.of()).matches(c->c.isEmpty(then("hello")),otherwise("boo")),equalTo(Eval.now("hello")));
	}
	@Test 
	public void emptyOptional(){
		
		assertThat(Matchables.optional(Optional.empty()).matches(c->c.isEmpty(then("hello")),otherwise("n/a")),equalTo(Eval.now("hello")));
	}
	@Test
	public void emptyOptionalMultiple2(){
		assertThat(Matchables.optional(Optional.empty())
				             .matches(
				            			o-> 
				            			     o.is(when(1),then("2")),otherwise("world")
				            		)
				            		,equalTo(Eval.now("world")));
		
		
	}
	@Test
	public void emptyOptionalMultiple3(){
		assertThat(Matchables.optional(Optional.empty())
							 .matches(
				            			o-> o
				            			     .is(when(1),then(""+2))
				            			     .is(when(2),then(""+3))
				            			     ,otherwise("hello")
				            		).get()
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyMaybeMultiple3(){
		assertThat(Maybe.none()
						.matches(
				            			o-> o
				            			      .is(when(1),then("2"))
				            			      .is(when(2),then("3")),otherwise("hello")
				            		)
				            		,equalTo(Eval.now("hello")));
		
		
	}
	@Test
	public void emptyOptionalMultiple4(){
	    Matchables.optional(Optional.of(3)).matches(o-> o.isEmpty(then("none"))
                .is(when(1),then("one"))
                .is(when(2),then("two"))
                .is(when(lessThan(0)), then("negative")),otherwise("many"));
		assertThat(Matchables.optional(Optional.of(3))
							 .matches(
				            		o-> o.is(when(1),then("2"))
		            			      	 .is(when(2),then("3"))
		            			      	 .is(when(3), then("4")),otherwise("boo!")
				            		)
				            		,equalTo(Eval.now("4")));
		
		
	}
	
	@Test 
	public void emptyOptionalMaybe(){
		
		assertThat(Matchables.optional(Optional.empty())
		                        .matches(c->c.is(when(some()),then("goodbye")),
		                                otherwise("hello")).get(),equalTo("goodbye"));
		assertThat(Matchables.optional(Optional.empty()).visit(i->"some", ()->"none"),equalTo("none"));
	}
	@Test
	public void emptyOptionalMultiple2Maybe(){
		assertThat(Matchables.optional(Optional.empty())
				            .matches(
				            			o->o.is(when(Optional.of(2)),then("3")),otherwise("hello")
				            		).get()
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyOptionalMultiple3Maybe(){
		assertThat(Matchables.optional(Optional.empty())
				            .matches(
				            			o-> o.isEmpty(then("hello"))
				            			      .is(when(1),then("2"))
				            			      .is(when(2),then("3")),otherwise("miss")
				            		).get()
				            		,equalTo("hello"));
		
		
	}
	@Test
	public void emptyOptionalMultiple4Maybe(){
		assertThat(Matchables.optional(Optional.of(3))
				            .matches(
				            			o-> o.is(when(1),then("2"))
				            			     .is(when(2),then("3"))
				            			     .is(when(3),then("4")),
				            			     otherwise("miss")
				            			    
				            		).get()
				            		,equalTo("4"));
		
		
	}
	public void matchGreaterThan(){
	    Matchable.matchable(100)
	             .matches(c->c.is(when(greaterThan(50)), ()->"large"), ()->"small");
	}
	public void matchByType(){
		
		assertThat(Matchable.of(1)
				                    .matches(c->c.is(when(Predicates.instanceOf(Integer.class)),then("hello")),otherwise("world")),
				                    equalTo("hello"));
	}
	public void matchListOfValues(){
		assertThat(Matchables.listOfValues(1,2,3)
							        .matches(c->c.is(when((Object i)->(i instanceof Integer)),then(2)),otherwise(-1)),
							        equalTo(2));
		
	}
	@Test
	public void recursive(){
		Eval<String> result = Matchables.listOfValues(1,new MyCase(4,5,6))
				 				.matches(c->c.is(when(__,Predicates.has(4,5,6)),then("rec")),otherwise("n/a"));
		
		assertThat(result.get(),equalTo("rec"));
	}
	
	@Test
	public void matchType(){
		
		Eval<Integer> result = Matchable.of(new Child(10,20)).matches(
									c-> c.is(when(Predicates.type(Child.class).hasGuard()),then(()->10)),otherwise(-1));
		
		assertThat(result.get(),equalTo(10));
	}
	@Test
	public void matchTypeBreakdown(){
		
		Eval<Integer> result = Matchable.of(new Child(10,20)).matches(
									c-> c.is(when(Predicates.type(Child.class).hasGuard(10,20)),then(10)),otherwise(50));
		
		assertThat(result.get(),equalTo(10));
	}
	@Test
	public void matchTypeBreakdownJust(){
	    
		Eval<Integer> result = Matchable.of(new Child(10,20)).matches(
									c-> c.is(when(Predicates.type(Child.class).isGuard(10)),then(10)),otherwise(-1));
		
		assertThat(result,equalTo(Eval.now(-1)));
	}
	@Test
	public void matchTypeBreakdownJust2(){
		
		Eval<Integer> result = Matchable.of(new Child(10,20)).matches(
									c-> c.is(when(Predicates.type(Child.class).isGuard(10,20)),then(10)),otherwise(-1));
		
		assertThat(result,equalTo(Eval.now(10)));
	}
	@Test
	public void matchTypeBreakdownJustWhere(){
		
		Eval<Integer> result = Matchable.of(new Child(10,20)).matches(
									c-> c.is(when(Predicates.type(Child.class).isGuard(eq(10),eq(20))),then(10)),otherwise(-1));
		
		assertThat(result,equalTo(Eval.now(10)));
	}
	
	@Value
	@EqualsAndHashCode(callSuper=false)
	static class Child extends Parent{
		int nextVal;
		public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
	}
	@AllArgsConstructor(access=AccessLevel.PACKAGE)
	static class Parent{
		int val;
	}
	
}
