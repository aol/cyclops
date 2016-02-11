package com.aol.cyclops.matcher.collections;
import static com.aol.cyclops.matcher2.Predicates.ANY;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher2.CollectionMatcher;
import com.aol.cyclops.matcher2.Extractors;

import lombok.AllArgsConstructor;
import lombok.Getter;
public class MatchingDataStructuresTest {
	
	
	@Test
	public void allValues(){
	
		
		assertThat(CollectionMatcher.whenIterable().allValues(1,ANY(),2).thenApply(l->"case1")
			.whenIterable().allValues(1,3,2).thenApply(l->"case2")
			.whenIterable().bothTrue((Integer i)->i==1,(String s)->s.length()>0)
					.thenExtract(Extractors.<Integer,String>of(0,1))
					.thenApply(t->t.v1+t.v2)
			.match(1,"hello",2).get(),is("case1"));
		
		
	}
	@Test
	public void inCaseOfManySingle() {
		
		assertThat(
				CollectionMatcher.whenIterable().allTrue((Person p) -> p.isTall())
											.thenApply(list -> list.get(0).getName() + " is tall")
						.whenIterable().allTrue((Person p) -> p.isTall())
											.thenApply(list -> list.get(0).getName() + " is tall")
						
						
						.match(new Person("bob")).get(), is("bob is tall"));
	}
	@Test
	public void inCaseOfManyDouble() {
		Predicate<Person> isTall = (Person p) -> p.isTall();
		assertThat(
				CollectionMatcher.whenIterable().allTrue(isTall)
										.thenApply(list -> list.get(0).getName() + " is tall")
						.match(new Person("bob"),new Person("bob")).get(), is("bob is tall"));
	}
	@Test
	public void inCaseOfManyList() {
		Predicate<Person> isTall = (Person p) -> p.isTall();
		assertThat(
				CollectionMatcher.whenIterable().allTrue(isTall)
										.thenApply(list -> list.get(0).getName() + " is tall")
										
						.apply(asList(new Person("bob"))).get(), is("bob is tall"));
	}
	

	@Test
	public void  inMatchOfManyList() {
		assertThat(
				CollectionMatcher.whenIterable().allMatch(samePropertyValuesAs(new Person("bob")))
											.thenApply(list -> list.get(0).getName())
						.apply(asList(new Person("bob"))).get(), is("bob"));
	}
	@Test
	public void  inMatchOfManySingle() {
		assertThat(
				CollectionMatcher.whenIterable().allMatch(samePropertyValuesAs(new Person("bob")))
												.thenApply(list -> list.get(0).getName())
						.apply(new Person("bob")).get(), is("bob"));
	}
	@Test
	public void  inMatchOfManyDouble() {
		assertThat(
				CollectionMatcher.whenIterable().allMatch(samePropertyValuesAs(new Person("bob")))
										.thenApply(list -> list.get(0).getName())
						.match(new Person("bob"), new Person("two")).get(), is("bob"));
	}
	

	@Test
	public void  inMatchOfMatchers() {

		assertThat(
				CollectionMatcher.whenIterable().bothMatch(samePropertyValuesAs(new Person("bob")),anything())
											.thenExtract(Extractors.<Person>first())
											.thenApply(bob->bob.getName())
											.apply(Tuple.tuple(new Person("bob"),"boo hoo!")).get(),is("bob"));
	}
	@Test
	public void  inMatchOfMatchersSingle() {

		assertThat(
				CollectionMatcher.whenIterable().bothMatch(samePropertyValuesAs(new Person("bob")),any(String.class))
											.thenExtract(Extractors.<Person>first())
											.thenApply(bob -> bob.getName())
						.match(new Person("bob"), "boo hoo!").get(),is("bob"));
	}

	@Test
	public void  inCaseOfPredicates() {
		CollectionMatcher.whenIterable().allTrueNoType((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first())
								.thenApply(p -> p.getName() + " is tall").apply(new Person("bob")).get();
		String result = CollectionMatcher.<String>whenIterable().allTrueNoType((Person p) -> p.isTall(),p->true)
												.thenExtract(Extractors.<Person>first())
												.thenApply(p -> p.getName() + " is tall")
						.apply(new Person("bob")).get();
		assertThat(result,is("bob is tall"));
	}
	@Test
	public void  inCaseOfPredicatesMultiple() {
		
		String result = CollectionMatcher.<String>whenIterable().allTrueNoType( (Person p) -> p.isTall(), p->true)
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

		String result = CollectionMatcher.whenIterable()
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

		String result = CollectionMatcher.whenIterable().threeTrue((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("Ireland"),p->true)
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
		
		String result = CollectionMatcher.whenIterable().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything())
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

		CollectionMatcher.whenIterable().allTrue(isTall)
					.thenConsume(list -> value = list.get(0).getName() + " is tall")
				.whenIterable().allTrue(isTall)
					.thenConsume(list -> value = list.get(0).getName() + " is tall")	
				.match(asList(new Person("bob")));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void  caseOfManySingle(){
		
		

		CollectionMatcher.whenIterable().allTrue((Person p) -> p.isTall()).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(new Person("bob"));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void  caseOfManyDouble(){
		
		

		CollectionMatcher.whenIterable().allTrue((Person p) -> p.isTall()).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(new Person("bob"),new Person("two"));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void   matchOfManyList(){
		CollectionMatcher.whenIterable().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.apply(
				asList(new Person("bob")));
		assertThat(value, is("bob"));
		
	}
	@Test
	public void   matchOfManySingle(){
		CollectionMatcher.whenIterable().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.apply(
				new Person("bob"));
		assertThat(value, is("bob"));
		
	}
	@Test
	public void   matchOfManyDouble(){
		CollectionMatcher.whenIterable().allMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.match(
				new Person("bob"),new Person("two"));
		assertThat(value, is("bob"));
		
	}
	
	
	@Test
	public void  matchOfMatchers(){

		CollectionMatcher.whenIterable().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything())
																.thenExtract(Extractors.<Person>first())
																.thenApply(bob-> value =bob.getName())
									.whenIterable().bothMatch(Matchers.<Person> samePropertyValuesAs(new Person("bob2")),anything())
																.thenExtract(Extractors.<Person>first())
																.thenApply(bob-> value =bob.getName())
											.apply(Tuple.tuple(new Person("bob"),"boo hoo!"));
				assertThat(value, is("bob"));
		
	}
	
	@Test
	public void  caseOfPredicates(){
		Stream.of(1,2,3,4).map(Matching.when().isValue(1).thenApply(v->"found 1")
									 .when().isTrue((Integer i)-> i%2==0).thenApply(v->"found even"))
									.filter(Maybe::isPresent).map(Maybe::get).collect(Collectors.toList());
		
		
		
		CollectionMatcher.whenIterable().bothTrue((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first()).thenApply( p->value = p.getName() + " is tall" )
				.whenIterable().bothTrue((Person p) -> p.isTall(),p->true).thenExtract(Extractors.<Person>first()).thenApply( p->value = p.getName() + " is tall" )
								.apply(new Person("bob"));
		
		assertThat(value,is("bob is tall"));
		
	}
			
	@Test
	public void  caseOfTuple(){
/** not compling in Eclipse MARS
		CollectionMatcher.whenIterable()
						.allTrueNoType((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("Ireland"),  p->true)
								.thenExtract(Extractors.<Person,Address>of(0,1))
								.thenApply(t->value = t.v1.getName() + " is tall and lives in " + t.v2.getCity())
				.whenIterable()
									.allTrueNoType((Person p) -> p.isTall(),(Address a)->a.getCountry().equals("France"), p->true)
									.thenExtract(Extractors.<Person,Address>of(0,1))
									.thenApply(t-> value = t.v1.getName() + " is tall and lives in " + t.v2.getCity())
						.apply(Arrays.asList(new Person("bob"),new Address(), new Job()));
		
		assertThat(value,is("bob is tall and lives in Dublin"));
		**/
	}
			
	@Test
	public void matchOfTuple(){
		CollectionMatcher.whenIterable().bothMatch(samePropertyValuesAs(new Person("bob")),anything())
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
