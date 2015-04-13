package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Extractors.at;
import static com.aol.cyclops.matcher.Extractors.get;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
public class MatchingTest {

	
	Object value;
	
	@Before
	public void setup(){
		value = null;
	}
	
	@Test
	public void testCaseOfTypeWithExtractorAndAction() {
		
		Matching.caseOfType(Person::getAge, (Integer i) -> value = i)
				.match(new Person(100));
		
		assertThat(value,is(100));
		
	}
	@Test
	public void testCaseOfTypeWithExtractorAndActionFalse() {
		
		Matching.caseOfType(Person::getAge, (Long i) -> value = i)
				.match(new Person(100));
		
		assertThat(value,is(nullValue()));
		
	}

	@Test
	public void testCaseOfValueExtractorAction() {
		Matching.caseOfValue(100, Person::getAge, (Integer i) -> value = i)
			.match(new Person(100));
		assertThat(value,is(100));
	}
	@Test
	public void testCaseOfValueExtractorActionFalse() {
		
		Matching.caseOfValue(200, Person::getAge, (Integer i) -> value = i)
			.match(new Person(100));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfValueVActionOfV() {
		Matching.caseOfValue("hello", s -> value=s).match("hello");
		assertThat(value,is("hello"));
	}
	@Test
	public void testCaseOfValueVActionOfVFalse() {
		Matching.caseOfValue("hello", s -> value=s).match("hello1");
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfTypeActionOfV() {
		Matching.caseOfType((String s)-> value=s).match("hello");
		assertThat(value,is("hello"));
	}
	@Test
	public void testCaseOfTypeActionOfVFalse() {
		Matching.caseOfType((String s)-> value=s).match(new Date());
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfMatcherOfVActionOfV() {
		Matching.matchOf(hasItem("hello world"), l -> value=l)
					.match(Arrays.asList("hello world"));
		
		assertThat(value,is(Arrays.asList("hello world")));
	}
	@Test
	public void testCaseOfMatcherOfVActionOfVFalse() {
		Matching.matchOf(hasItem("hello world2"), l -> value=l)
					.match(Arrays.asList("hello world"));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfPredicateOfVActionOfV() {
		Person person = new Person(42);
		Matching.caseOf(v->v==person, p->value=p)
				.match(person);
		
		assertThat(value,is(person));
	}
	@Test
	public void testCaseOfPredicateOfVActionOfVFalse() {
		Person person = new Person(42);
		Matching.caseOf(v->v==person, p->value=p)
				.match(new Person(42));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfThenExtractPredicateOfVActionOfVExtractorOfTR() {
		Matching.caseOfThenExtract(it-> it instanceof List, it->value=it , at(0))
				.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(true));
	}
	@Test
	public void testCaseOfThenExtractPredicateOfVActionOfVExtractorOfTRFalse() {
		Matching.caseOfThenExtract(it-> it instanceof Map, it->value=it , at(0))
				.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfThenExtractMatcherOfVActionOfVExtractorOfTR() {
		Matching.matchOfThenExtract(is(not(empty())), it->value=it, get(1))
						.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(false));
	}
	@Test
	public void testCaseOfThenExtractMatcherOfVActionOfVExtractorOfTRFalse() {
		Matching.matchOfThenExtract(is(not(empty())), it->value=it, get(1))
						.match(Arrays.asList());
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfExtractorOfTRPredicateOfVActionOfV() {
		Matching.caseOf(at(0), name-> "bob".equals(name), it->value=it)
					.match(new Person("bob",22));
		
		assertThat(value,is("bob"));
	}
	@Test
	public void testCaseOfExtractorOfTRPredicateOfVActionOfVFalse() {
		Matching.caseOf(at(0), name-> "bob".equals(name), it->value=it)
					.match(new Person("rosie",22));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfExtractorOfTRMatcherOfVActionOfV() {
		Matching.matchOf(at(1), greaterThan(21), it->value=it)
					.match(new Person("rosie",22));
		
		assertThat(value,is(22));
	}

	@Test
	public void testCaseOfExtractorOfTRMatcherOfVActionOfVFalse() {
		Matching.matchOf(at(1), greaterThan(21), it->value=it)
					.match(new Person("rosie",20));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testInCaseOfValueVActionWithReturnOfVX() {
		assertThat(Matching.inCaseOfValue(100,v-> v+100)
					.match(100).orElse(100),is(200));
	}
	@Test
	public void testInCaseOfValueVActionWithReturnOfVXFalse() {
		assertThat(Matching.inCaseOfValue(100,v-> v+100)
					.match(500).orElse(100),is(100));
	}

	@Test
	public void testInCaseOfTypeActionWithReturnOfTX() {
		assertThat(Matching.inCaseOfType((Integer i) -> i-50)
					.match(100).get(),is(50));
	}
	@Test
	public void testInCaseOfTypeActionWithReturnOfTXWithMap() {
		assertThat(Matching.inCaseOfType((Integer i) -> i-50)
					.match(100)
					.map(x->x*100).get(),
					is(5000));
	}
	@Test
	public void testInCaseOfTypeActionWithReturnOfTXFalse() {
		assertThat(Matching.inCaseOfType((Integer i) -> i-50)
					.match(100l)
					.map(x->x*100),
					is(Optional.empty()));
	}

	@Test
	public void testInCaseOfPredicateOfVActionWithReturnOfVX() {
		assertThat(Matching.inCaseOf((Integer a)-> a>100, x->x*10)
				.apply(101),is(1010));
	}
	@Test
	public void testInCaseOfPredicateOfVActionWithReturnOfVXFalse() {
		assertThat(Matching.inCaseOf((Integer a)-> a>100, x->x*10)
				.match(99),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfThenExtractPredicateOfTActionWithReturnOfRXExtractorOfTR() {
		assertThat(Matching.inCaseOfThenExtract((Person person)->person.getAge()>18,
							name-> name + " is an adult", 
							Person::getName)
							.match(new Person("rosie",39)).get(),is("rosie is an adult"));
	}
	@Test
	public void testInCaseOfThenExtractPredicateOfTActionWithReturnOfRXExtractorOfTRFalse() {
		assertThat(Matching.inCaseOfThenExtract((Person person)->person.getAge()>18,
							name-> name + " is an adult", 
							Person::getName)
							.match(new Person("rosie",9)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfExtractorOfTRPredicateOfVActionWithReturnOfVX() {
		assertThat(Matching.inCaseOf(Person::getName, 
					(String name)->name.length()>5, 
					name->name+" is too long")
					.match(new Person("long name",9)).get(),is("long name is too long"));
	}
	@Test
	public void testInCaseOfExtractorOfTRPredicateOfVActionFalse() {
		assertThat(Matching.inCaseOf(Person::getName, 
					(String name)->name.length()>5, 
					name->name+" is too long")
					.match(new Person("short",9)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfMatcherOfVActionWithReturnOfVX() {
		assertThat(Matching.inMatchOf(hasItem("hello"), hello->"world")
				.apply(Arrays.asList("hello")),is("world"));
	}
	@Test
	public void testInCaseOfMatcherOfVActionWithReturnOfVXFalse() {
		assertThat(Matching.inMatchOf(hasItem("hello"),  hello-> "world")
				.match(Arrays.asList("hello2")),is(Optional.empty()));
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testInCaseOfThenExtractMatcherOfTActionWithReturnOfRXExtractorOfTR() {
		assertThat(Matching.inMatchOfThenExtract((Matcher)hasItem("hello"), 
				value -> "second value is " + value, at(1))
						.match(Arrays.asList("hello","world")).get(),is("second value is world"));
	}
	public void testInCaseOfThenExtractMatcherOfTActionWithReturnOfRXExtractorOfTRFalse() {
		assertThat(Matching.inMatchOfThenExtract((Matcher)hasItem("hello2"), 
				value -> "second value is " + value, at(1))
						.match(Arrays.asList("hello","world")), is(Optional.empty()));
	}

	@Test
	public void testInCaseOfExtractorOfTRMatcherOfVActionWithReturnOfVX() {
		assertThat(Matching.inMatchOf(Person::getName, is("bob"), name -> name + " wins!")
				.apply(new Person("bob",65)),is("bob wins!"));
	}
	@Test
	public void testInCaseOfExtractorOfTRMatcherOfVActionWithReturnOfVXFalse() {
		assertThat(Matching.inMatchOf(Person::getName, is("bob2"), name -> name + " wins!")
				.match(new Person("bob",65)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfTypeExtractorOfTRActionWithReturnOfVX() {
		assertThat(Matching.inCaseOfType(at(0), (Person person)->"age is " + person.getAge()).match(Arrays.asList(new Person("amy",22))).get(),is("age is 22"));
	}
	@Test
	public void testInCaseOfTypeExtractorOfTRActionWithReturnOfVXFalse() {
		assertThat(Matching.inCaseOfType(at(0), (Person person)->"age is " + person.getAge()).match(Arrays.asList(new Address(10, "street", "city", "country"),
				new Person("amy",22))),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfValueRExtractorOfTRActionWithReturnOfVX() {
		assertThat(Matching.inCaseOfValue("hello", Person::getName, name -> name + " world").match(new Person("hello",40)).get(),is("hello world"));
	}
	@Test
	public void testInCaseOfValueRExtractorOfTRActionWithReturnOfVXFalse() {
		assertThat(Matching.inCaseOfValue("hello", Person::getName, name -> name + " world").match(new Person("hello2",40)),is(Optional.empty()));
	}
	
	
	@AllArgsConstructor
	static class Person implements Iterable{
		@Getter
		String name;
		@Getter
		int age;
		
		public Person(int age){
			name = null;
			this.age = age;
		}

		@Override
		public Iterator iterator() {
			return Arrays.asList(name,age).iterator();
		}
	}
	@AllArgsConstructor
	static class Address implements Iterable{
		@Getter
		int number;
		@Getter
		String street;
		@Getter
		String city;
		@Getter
		String country;
		
		
		@Override
		public Iterator iterator() {
			return Arrays.asList(number,street,city,country).iterator();
		}
	}

}
