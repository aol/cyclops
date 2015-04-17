package com.aol.cyclops.matcher;
import static com.aol.cyclops.matcher.Predicates.p;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.matcher.builders.Matching;
public class MatchingDataStructuresTest {
	@Test
	public void inCaseOfManySingle() {
		
		assertThat(
				Matching.isTrue((Person p) -> p.isTall())
							.isType(list -> list.get(0).getName() + " is tall")
						
						
						
						.match(new Person("bob")).get(), is("bob is tall"));
	}
	@Test
	public void inCaseOfManyDouble() {
		Predicate<Person> isTall = (Person p) -> p.isTall();
		assertThat(
				Matching.isTrue(isTall).isType(list -> list.get(0).getName() + " is tall")
						.match(new Person("bob"),new Person("bob")).get(), is("bob is tall"));
	}
	@Test
	public void inCaseOfManyList() {
		Predicate<Person> isTall = (Person p) -> p.isTall();
		assertThat(
				Matching.isTrue(isTall).isType(list -> list.get(0).getName() + " is tall")
						.apply(asList(new Person("bob"))).get(), is("bob is tall"));
	}
	

	@Test
	public void  inMatchOfManyList() {
		assertThat(
				Matching.matches(Matchers.<Person> samePropertyValuesAs(new Person("bob")))
											.isType(list -> list.get(0).getName())
						.apply(asList(new Person("bob"))).get(), is("bob"));
	}
	@Test
	public void  inMatchOfManySingle() {
		assertThat(
				Matching.matches(Matchers.<Person> samePropertyValuesAs(new Person("bob")))
												.isType(list -> list.get(0).getName())
						.apply(
						new Person("bob")).get(), is("bob"));
	}
	@Test
	public void  inMatchOfManyDouble() {
		assertThat(
				Matching.matches(Matchers.<Person> samePropertyValuesAs(new Person("bob")))
										.isType(list -> list.get(0).getName())
						.match(
						new Person("bob"), new Person("two")).get(), is("bob"));
	}
	

	@Test
	public void  inMatchOfMatchers() {

		assertThat(
				Matching.inMatchOfTuple(tuple(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything()),
												bob->bob.getName(), Extractors.<Person>first())
												.apply(Tuple.tuple(new Person("bob"),"boo hoo!")).get(),is("bob"));
	}
	@Test
	public void  inMatchOfMatchersSingle() {

		assertThat(
				Matching.inMatchOfTuple(tuple(samePropertyValuesAs(new Person("bob")),any(String.class)),
												bob->bob.getName(), 
												Extractors.<Person>first())
						.match(new Person("bob"),"boo hoo!").get(),is("bob"));
	}

	@Test
	public void  inCaseOfPredicates() {
		
		String result = Matching.inCaseOf( (Person p) -> p.isTall(),p->true, (String p)->p.getName() + " is tall", Extractors.<Person>first())
								.apply(new Person("bob")).get();
		assertThat(result,is("bob is tall"));
	}
	@Test
	public void  inCaseOfPredicatesMultiple() {
		
		String result = Matching.inCaseOfPredicates(tuple(p((Person p) -> p.isTall()),p->true),
													p->p.getName() + " is tall", 
													Extractors.<Person>first())
													
								.match(new Person("bob"),"test").get();
		assertThat(result,is("bob is tall"));
	}
	
	@Test
	public void inCaseOfTuple() {

		String result = Matching.inCaseOfTuple(tuple(p((Person p) -> p.isTall()),p((Address a)->a.getCountry().equals("Ireland")), p( p->true)),
													t->t.v1.getName() + " is tall and lives in " + t.v2.getCity(), Extractors.<Person,Address>of(0,1))
						
								.inCaseOfTuple(tuple(p((Person p) -> p.isTall()),p((Address a)->a.getCountry().equals("France")), p( p->true)),
											t->t.v1.getName() + " is tall and lives in " + t.v2.getCity(), Extractors.<Person,Address>of(0,1))
						
								.apply(tuple(new Person("bob"),new Address(), new Job())).get();
		
		assertThat(result,is("bob is tall and lives in Dublin"));
	}
	@Test
	public void inCaseOfTupleUnsuitableObject() {

		String result = Matching.inCaseOfTuple(			//condition
													tuple(p((Person p) -> p.isTall()),p((Address a)->a.getCountry().equals("Ireland")), p( p->true)),
														//action
													t-> t.v1.getName() + " is tall and lives in " + t.v2.getCity(), 
														//values
													Extractors.<Person,Address>of(0,1))
						
								.inCaseOfTuple(		//condition
												tuple(p((Person p) -> p.isTall()),p((Address a)->a.getCountry().equals("France")), p( p->true)),
													//action
												t->t.v1.getName() + " is tall and lives in " + t.v2.getCity(), 
													//values
												Extractors.<Person,Address>of(0,1))
						
								.match(new Person("bob"),new Address()).get();
		
		assertThat(result,is("bob is tall and lives in Dublin"));
	}
	
	@Test
	public void inMatchOfTuple() {
		
		String result = Matching.inMatchOfTuple(tuple(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything()), 
										t->t.v1.getName() + " is tall and lives in " + t.v2.getCity(), 
										Extractors.<Person,Address>of(0,1))
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

		Matching.isTrue(isTall).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(asList(new Person("bob")));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void  caseOfManySingle(){
		
		

		Matching.isTrue((Person p) -> p.isTall()).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(new Person("bob"));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void  caseOfManyDouble(){
		
		

		Matching.isTrue((Person p) -> p.isTall()).thenConsume(list -> value = list.get(0).getName() + " is tall")
				.match(new Person("bob"),new Person("two"));

		assertThat(value, is("bob is tall"));
	}
	@Test
	public void   matchOfManyList(){
		Matching.matches(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.apply(
				asList(new Person("bob")));
		assertThat(value, is("bob"));
		
	}
	@Test
	public void   matchOfManySingle(){
		Matching.matches(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.apply(
				new Person("bob"));
		assertThat(value, is("bob"));
		
	}
	@Test
	public void   matchOfManyDouble(){
		Matching.matches(Matchers.<Person> samePropertyValuesAs(new Person("bob"))).thenConsume(list -> value = list.get(0).getName())
					.match(
				new Person("bob"),new Person("two"));
		assertThat(value, is("bob"));
		
	}
	
	
	@Test
	public void  matchOfMatchers(){

				Matching.matchOfMatchers(tuple(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything()),
												bob-> value =bob.getName(), Extractors.<Person>first())
												.apply(Tuple.tuple(new Person("bob"),"boo hoo!"));
				assertThat(value, is("bob"));
		
	}
	
	@Test
	public void  caseOfPredicates(){
		Stream.of(1,2,3,4).map(Matching.isValue(1).isType(v->"found 1").inCaseOf((Integer i)-> i%2==0,v->"found even"))
									.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		
		
		
		Matching.caseOfPredicates(tuple(p((Person p) -> p.isTall()),p->true), p->value = p.getName() + " is tall", Extractors.<Person>first())
								.apply(new Person("bob"));
		
		assertThat(value,is("bob is tall"));
		
	}
			
	@Test
	public void  caseOfTuple(){
		Matching.caseOfTuple(tuple(p((Person p) -> p.isTall()),p((Address a)->a.getCountry().equals("Ireland")), p( p->true)),
				t->value = t.v1.getName() + " is tall and lives in " + t.v2.getCity(), Extractors.<Person,Address>of(0,1))

				.caseOfTuple(tuple(p((Person p) -> p.isTall()),p((Address a)->a.getCountry().equals("France")), p( p->true)),
						t-> value = t.v1.getName() + " is tall and lives in " + t.v2.getCity(), Extractors.<Person,Address>of(0,1))

						.apply(tuple(new Person("bob"),new Address(), new Job()));
		
		assertThat(value,is("bob is tall and lives in Dublin"));
		
	}
			
	@Test
	public void matchOfTuple(){
		Matching.matchOfTuple(tuple(Matchers.<Person> samePropertyValuesAs(new Person("bob")),anything()), 
				t->value = t.v1.getName() + " is tall and lives in " + t.v2.getCity(), 
				Extractors.<Person,Address>of(0,1))
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
