package com.aol.cyclops.matcher;
import static com.aol.cyclops.matcher.Predicates.ANY;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.matcher.builders.Matching;
public class MatchingDataStructuresTest {
	
	
	@Test
	public void allValues(){
		assertThat(Matching.iterableCase().allValues(1,ANY(),2).thenApply(l->"case1")
			.iterableCase().allValues(1,3,2).thenApply(l->"case2")
			.iterableCase().bothTrue((Integer i)->i==1,(String s)->s.length()>0)
					.thenExtract(Extractors.<Integer,String>of(0,1))
					.thenApply(t->t.v1+t.v2)
			.match(1,"hello",2).get(),is("case1"));
		
		
	}
	@Test
	public void inCaseOfManySingle() {
		
		assertThat(
				Matching.iterableCase(c->c.allTrue((Person p) -> p.isTall())
											.thenApply(list -> list.get(0).getName() + " is tall"))
						.iterableCase(c->c.allTrue((Person p) -> p.isTall())
											.thenApply(list -> list.get(0).getName() + " is tall"))
						
						
						.match(new Person("bob")).get(), is("bob is tall"));
	}
	@Test
	public void inCaseOfManyDouble() {
		Predicate<Person> isTall = (Person p) -> p.isTall();
		assertThat(
				Matching.iterableCase(c->c.allTrue(isTall)
										.thenApply(list -> list.get(0).getName() + " is tall"))
						.match(new Person("bob"),new Person("bob")).get(), is("bob is tall"));
	}
	@Test
	public void inCaseOfManyList() {
		Predicate<Person> isTall = (Person p) -> p.isTall();
		assertThat(
				Matching.iterableCase(c->c.allTrue(isTall)
										.thenApply(list -> list.get(0).getName() + " is tall"))
										
						.apply(asList(new Person("bob"))).get(), is("bob is tall"));
	}
	

	@Test
	public void  inMatchOfManyList() {
		assertThat(
				Matching.iterableCase(c->c.allMatch(samePropertyValuesAs(new Person("bob")))
											.thenApply(list -> list.get(0).getName()))
						.apply(asList(new Person("bob"))).get(), is("bob"));
	}
	@Test
	public void  inMatchOfManySingle() {
		assertThat(
				Matching.iterableCase(c->c.allMatch(samePropertyValuesAs(new Person("bob")))
												.thenApply(list -> list.get(0).getName()))
						.apply(new Person("bob")).get(), is("bob"));
	}
	@Test
	public void  inMatchOfManyDouble() {
		assertThat(
				Matching.iterableCase(c->c.allMatch(samePropertyValuesAs(new Person("bob")))
										.thenApply(list -> list.get(0).getName()))
						.match(new Person("bob"), new Person("two")).get(), is("bob"));
	}
	

	@Test
	public void  inMatchOfMatchers() {

		assertThat(
				Matching.iterableCase().bothMatch(samePropertyValuesAs(new Person("bob")),anything())
											.thenExtract(Extractors.<Person>first())
											.thenApply(bob->bob.getName())
											.apply(Two.tuple(new Person("bob"),"boo hoo!")).get(),is("bob"));
	}
	@Test
	public void  inMatchOfMatchersSingle() {

		assertThat(
				Matching.iterableCase(c->  c.bothMatch(samePropertyValuesAs(new Person("bob")),any(String.class))
											.thenExtract(Extractors.<Person>first())
											.thenApply(bob -> bob.getName()))
						.match(new Person("bob"), "boo hoo!").get(),is("bob"));
	}

	@Test
	public void  inCaseOfPredicates() {
		Matching.iterableCase(c -> c.allTrueNoType((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first()).thenApply(p -> p.getName() + " is tall")).apply(new Person("bob")).get();
		String result = Matching.<String>iterableCase(c -> c.allTrueNoType((Person p) -> p.isTall(),p->true)
												.thenExtract(Extractors.<Person>first())
												.thenApply(p -> p.getName() + " is tall"))
						.apply(new Person("bob")).get();
		assertThat(result,is("bob is tall"));
	}
	@Test
	public void  inCaseOfPredicatesMultiple() {
		
		String result = Matching.<String>iterableCase(c->c.allTrueNoType( (Person p) -> p.isTall(), p->true)
													.thenExtract(Extractors.<Person>first())
													.thenApply(p->p.getName() + " is tall") )
								.iterableCase(c->c.allTrueNoType( (Person p) -> p.isTall(), p->true)
													.thenExtract(Extractors.<Person>first())
													.thenApply(p->p.getName() + " is tall") )
													
								.match(new Person("bob"),"test").get();
		assertThat(result,is("bob is tall"));
	}
	
	@Test
	public void inCaseOfTuple() {

		String result = Matching.iterableCase()
										.threeTrue((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("Ireland"), p->true)
										.thenExtract( Extractors.<Person,Address>of(0,1))
										.thenApply(t->t.v1.getName() + " is tall and lives in " + t.v2.getCity())
								.iterableCase()
									.threeTrue((Person p) -> !p.isTall(),(Address a)->a.getCountry().equals("Ireland"), p->true)
									.thenExtract( Extractors.<Person,Address>of(0,1))
									.thenApply(t->t.v1.getName() + " is not tall and lives in " + t.v2.getCity())
								.iterableCase()
									.threeTrue((Address a)->a.getCountry().equals("Ireland"),(Person p) -> !p.isTall(), p->true)
									.thenExtract( Extractors.<Person,Address>of(0,1))
									.thenApply(t->t.v1.getName() + " is not tall and lives in " + t.v2.getCity())
						
								.apply(Arrays.asList(new Person("bob"),new Address(), new Job())).get();
		
		assertThat(result,is("bob is tall and lives in Dublin"));
	}
	@Test
	public void inCaseOfTupleUnsuitableObject() {

		String result = Matching.iterableCase().threeTrue((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("Ireland"),p->true)
													.thenExtract(Extractors.<Person,Address>of(0,1))
													.thenApply(t-> t.v1.getName() + " is tall and lives in " + t.v2.getCity())
														
													
						
								.iterableCase().threeTrue((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("France"), p->true)
												.thenExtract(Extractors.<Person,Address>of(0,1))
												.thenApply(t->t.v1.getName() + " is tall and lives in " + t.v2.getCity())
												
								.match(new Person("bob"),new Address()).get();
		
		assertThat(result,is("bob is tall and lives in Dublin"));
	}
	
	@Test
	public void inMatchOfTuple() {
		
		String result = Matching.iterableCase().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything())
											.thenExtract(Extractors.<Person,Address>of(0,1))
											.thenApply(t->t.v1.getName() + " is tall and lives in " + t.v2.getCity())
									.match(new Person("bob"),new Address()).get();
		
		
		assertThat(result,is("bob is tall and lives in Dublin"));
	}
	
	String value;
	@Before
	public void setup(){
		value = null;
	}
	@Test
	public void  caseOfManyList(){
		
		Predicate<Person> isTall = (Person p) -> p.isTall();

		Matching.iterableCase().allTrue(isTall)
					.thenConsume(list -> value = list.get(0).getName() + " is tall")
				.iterableCase().allTrue(isTall)
					.thenConsume(list -> value = list.get(0).getName() + " is tall")	
				.match(asList(new Person("bob")));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void  caseOfManySingle(){
		
		

		Matching.iterableCase().allTrue((Person p) -> p.isTall()).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(new Person("bob"));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void  caseOfManyDouble(){
		
		

		Matching.iterableCase().allTrue((Person p) -> p.isTall()).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(new Person("bob"),new Person("two"));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void   matchOfManyList(){
		Matching.iterableCase().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.apply(
				asList(new Person("bob")));
		assertThat(value, is("bob"));
		
	}
	@Test
	public void   matchOfManySingle(){
		Matching.iterableCase().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.apply(
				new Person("bob"));
		assertThat(value, is("bob"));
		
	}
	@Test
	public void   matchOfManyDouble(){
		Matching.iterableCase().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.match(
				new Person("bob"),new Person("two"));
		assertThat(value, is("bob"));
		
	}
	
	
	@Test
	public void  matchOfMatchers(){

				Matching.iterableCase().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything())
																.thenExtract(Extractors.<Person>first())
																.thenApply(bob-> value =bob.getName())
									.iterableCase().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob2")),anything())
																.thenExtract(Extractors.<Person>first())
																.thenApply(bob-> value =bob.getName())
											.apply(Two.tuple(new Person("bob"),"boo hoo!"));
				assertThat(value, is("bob"));
		
	}
	
	@Test
	public void  caseOfPredicates(){
		Stream.of(1,2,3,4).map(Matching.newCase().isValue(1).thenApply(v->"found 1")
									 .newCase().isTrue((Integer i)-> i%2==0).thenApply(v->"found even"))
									.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		
		
		
		Matching.iterableCase().bothTrue((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first()).thenApply( p->value = p.getName() + " is tall" )
				.iterableCase().bothTrue((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first()).thenApply( p->value = p.getName() + " is tall" )
								.apply(new Person("bob"));
		
		assertThat(value,is("bob is tall"));
		
	}
			
	@Test
	public void  caseOfTuple(){

		Matching.iterableCase().allTrueNoType((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("Ireland"),  p->true)
								.thenExtract(Extractors.<Person,Address>of(0,1))
								.thenApply(t->value = t.v1.getName() + " is tall and lives in " + t.v2.getCity())
				.iterableCase()
									.allTrueNoType((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("France"), p->true)
									.thenExtract(Extractors.<Person,Address>of(0,1))
									.thenApply(t-> value = t.v1.getName() + " is tall and lives in " + t.v2.getCity())
						.apply(Arrays.asList(new Person("bob"),new Address(), new Job()));
		
		assertThat(value,is("bob is tall and lives in Dublin"));
		
	}
			
	@Test
	public void matchOfTuple(){
		Matching.iterableCase().bothMatch(samePropertyValuesAs(new Person("bob")),anything())
					.thenExtract(Extractors.<Person,Address>of(0,1))
					.thenApply(t->value = t.v1.getName() + " is tall and lives in " + t.v2.getCity())
				
				.match(new Person("bob"),new Address());


		assertThat(value,is("bob is tall and lives in Dublin"));
	}

	@AllArgsConstructor
	public static class Person{
		@Getter
		String name;
		public boolean isTall(){
			return true;
		}
	}
	
	public static class Address{
		public String getCountry(){
			return "Ireland";
		}
		public String getCity(){
			return "Dublin";
		}
	}
	
	public static class Job{}
}
