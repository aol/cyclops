package com.aol.cyclops.matcher.collections;
import static com.aol.cyclops.matcher.Predicates.ANY;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.*;
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

import com.aol.cyclops.matcher.CollectionMatching;
import com.aol.cyclops.matcher.Extractors;
import com.aol.cyclops.matcher.Two;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.collections.MatchingDataStructuresTest.Person;
public class MatchingDataStructuresTest {
	
	
	@Test
	public void allValues(){
		assertThat(CollectionMatching.whenIterable().allValues(1,ANY(),2).thenApply(l->"case1")
			.whenIterable().allValues(1,3,2).thenApply(l->"case2")
			.whenIterable().bothTrue((Integer i)->i==1,(String s)->s.length()>0)
					.thenExtract(Extractors.<Integer,String>of(0,1))
					.thenApply(t->t.v1+t.v2)
			.match(1,"hello",2).get(),is("case1"));
		
		
	}
	@Test
	public void inCaseOfManySingle() {
		
		assertThat(
				CollectionMatching.whenIterable().allTrue((Person p) -> p.isTall())
											.thenApply(list -> list.get(0).getName() + " is tall")
						.whenIterable().allTrue((Person p) -> p.isTall())
											.thenApply(list -> list.get(0).getName() + " is tall")
						
						
						.match(new Person("bob")).get(), is("bob is tall"));
	}
	@Test
	public void inCaseOfManyDouble() {
		Predicate<Person> isTall = (Person p) -> p.isTall();
		assertThat(
				CollectionMatching.whenIterable().allTrue(isTall)
										.thenApply(list -> list.get(0).getName() + " is tall")
						.match(new Person("bob"),new Person("bob")).get(), is("bob is tall"));
	}
	@Test
	public void inCaseOfManyList() {
		Predicate<Person> isTall = (Person p) -> p.isTall();
		assertThat(
				CollectionMatching.whenIterable().allTrue(isTall)
										.thenApply(list -> list.get(0).getName() + " is tall")
										
						.apply(asList(new Person("bob"))).get(), is("bob is tall"));
	}
	

	@Test
	public void  inMatchOfManyList() {
		assertThat(
				CollectionMatching.whenIterable().allMatch(samePropertyValuesAs(new Person("bob")))
											.thenApply(list -> list.get(0).getName())
						.apply(asList(new Person("bob"))).get(), is("bob"));
	}
	@Test
	public void  inMatchOfManySingle() {
		assertThat(
				CollectionMatching.whenIterable().allMatch(samePropertyValuesAs(new Person("bob")))
												.thenApply(list -> list.get(0).getName())
						.apply(new Person("bob")).get(), is("bob"));
	}
	@Test
	public void  inMatchOfManyDouble() {
		assertThat(
				CollectionMatching.whenIterable().allMatch(samePropertyValuesAs(new Person("bob")))
										.thenApply(list -> list.get(0).getName())
						.match(new Person("bob"), new Person("two")).get(), is("bob"));
	}
	

	@Test
	public void  inMatchOfMatchers() {

		assertThat(
				CollectionMatching.whenIterable().bothMatch(samePropertyValuesAs(new Person("bob")),anything())
											.thenExtract(Extractors.<Person>first())
											.thenApply(bob->bob.getName())
											.apply(Two.tuple(new Person("bob"),"boo hoo!")).get(),is("bob"));
	}
	@Test
	public void  inMatchOfMatchersSingle() {

		assertThat(
				CollectionMatching.whenIterable().bothMatch(samePropertyValuesAs(new Person("bob")),any(String.class))
											.thenExtract(Extractors.<Person>first())
											.thenApply(bob -> bob.getName())
						.match(new Person("bob"), "boo hoo!").get(),is("bob"));
	}

	@Test
	public void  inCaseOfPredicates() {
		CollectionMatching.whenIterable().allTrueNoType((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first())
								.thenApply(p -> p.getName() + " is tall").apply(new Person("bob")).get();
		String result = CollectionMatching.<String>whenIterable().allTrueNoType((Person p) -> p.isTall(),p->true)
												.thenExtract(Extractors.<Person>first())
												.thenApply(p -> p.getName() + " is tall")
						.apply(new Person("bob")).get();
		assertThat(result,is("bob is tall"));
	}
	@Test
	public void  inCaseOfPredicatesMultiple() {
		
		String result = CollectionMatching.<String>whenIterable().allTrueNoType( (Person p) -> p.isTall(), p->true)
													.thenExtract(Extractors.<Person>first())
													.thenApply(p->p.getName() + " is tall") 
								.whenIterable().allTrueNoType( (Person p) -> p.isTall(), p->true)
													.thenExtract(Extractors.<Person>first())
													.thenApply(p->p.getName() + " is tall") 
													
								.match(new Person("bob"),"test").get();
		assertThat(result,is("bob is tall"));
	}
	
	@Test
	public void inCaseOfTuple() {

		String result = CollectionMatching.whenIterable()
										.threeTrue((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("Ireland"), p->true)
										.thenExtract( Extractors.<Person,Address>of(0,1))
										.thenApply(t->t.v1.getName() + " is tall and lives in " + t.v2.getCity())
								.whenIterable()
									.threeTrue((Person p) -> !p.isTall(),(Address a)->a.getCountry().equals("Ireland"), p->true)
									.thenExtract( Extractors.<Person,Address>of(0,1))
									.thenApply(t->t.v1.getName() + " is not tall and lives in " + t.v2.getCity())
								.whenIterable()
									.threeTrue((Address a)->a.getCountry().equals("Ireland"),(Person p) -> !p.isTall(), p->true)
									.thenExtract( Extractors.<Person,Address>of(0,1))
									.thenApply(t->t.v1.getName() + " is not tall and lives in " + t.v2.getCity())
						
								.apply(Arrays.asList(new Person("bob"),new Address(), new Job())).get();
		
		assertThat(result,is("bob is tall and lives in Dublin"));
	}
	@Test
	public void inCaseOfTupleUnsuitableObject() {

		String result = CollectionMatching.whenIterable().threeTrue((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("Ireland"),p->true)
													.thenExtract(Extractors.<Person,Address>of(0,1))
													.thenApply(t-> t.v1.getName() + " is tall and lives in " + t.v2.getCity())
														
													
						
								.whenIterable().threeTrue((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("France"), p->true)
												.thenExtract(Extractors.<Person,Address>of(0,1))
												.thenApply(t->t.v1.getName() + " is tall and lives in " + t.v2.getCity())
												
								.match(new Person("bob"),new Address()).get();
		
		assertThat(result,is("bob is tall and lives in Dublin"));
	}
	
	@Test
	public void inMatchOfTuple() {
		
		String result = CollectionMatching.whenIterable().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything())
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

		CollectionMatching.whenIterable().allTrue(isTall)
					.thenConsume(list -> value = list.get(0).getName() + " is tall")
				.whenIterable().allTrue(isTall)
					.thenConsume(list -> value = list.get(0).getName() + " is tall")	
				.match(asList(new Person("bob")));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void  caseOfManySingle(){
		
		

		CollectionMatching.whenIterable().allTrue((Person p) -> p.isTall()).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(new Person("bob"));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void  caseOfManyDouble(){
		
		

		CollectionMatching.whenIterable().allTrue((Person p) -> p.isTall()).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(new Person("bob"),new Person("two"));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void   matchOfManyList(){
		CollectionMatching.whenIterable().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.apply(
				asList(new Person("bob")));
		assertThat(value, is("bob"));
		
	}
	@Test
	public void   matchOfManySingle(){
		CollectionMatching.whenIterable().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.apply(
				new Person("bob"));
		assertThat(value, is("bob"));
		
	}
	@Test
	public void   matchOfManyDouble(){
		CollectionMatching.whenIterable().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.match(
				new Person("bob"),new Person("two"));
		assertThat(value, is("bob"));
		
	}
	
	
	@Test
	public void  matchOfMatchers(){

		CollectionMatching.whenIterable().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything())
																.thenExtract(Extractors.<Person>first())
																.thenApply(bob-> value =bob.getName())
									.whenIterable().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob2")),anything())
																.thenExtract(Extractors.<Person>first())
																.thenApply(bob-> value =bob.getName())
											.apply(Two.tuple(new Person("bob"),"boo hoo!"));
				assertThat(value, is("bob"));
		
	}
	
	@Test
	public void  caseOfPredicates(){
		Stream.of(1,2,3,4).map(Matching.when().isValue(1).thenApply(v->"found 1")
									 .when().isTrue((Integer i)-> i%2==0).thenApply(v->"found even"))
									.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		
		
		
		CollectionMatching.whenIterable().bothTrue((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first()).thenApply( p->value = p.getName() + " is tall" )
				.whenIterable().bothTrue((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first()).thenApply( p->value = p.getName() + " is tall" )
								.apply(new Person("bob"));
		
		assertThat(value,is("bob is tall"));
		
	}
			
	@Test
	public void  caseOfTuple(){

		CollectionMatching.whenIterable().allTrueNoType((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("Ireland"),  p->true)
								.thenExtract(Extractors.<Person,Address>of(0,1))
								.thenApply(t->value = t.v1.getName() + " is tall and lives in " + t.v2.getCity())
				.whenIterable()
									.allTrueNoType((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("France"), p->true)
									.thenExtract(Extractors.<Person,Address>of(0,1))
									.thenApply(t-> value = t.v1.getName() + " is tall and lives in " + t.v2.getCity())
						.apply(Arrays.asList(new Person("bob"),new Address(), new Job()));
		
		assertThat(value,is("bob is tall and lives in Dublin"));
		
	}
			
	@Test
	public void matchOfTuple(){
		CollectionMatching.whenIterable().bothMatch(samePropertyValuesAs(new Person("bob")),anything())
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
