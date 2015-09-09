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

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.matcher.builders.Matching;
public class MatchingTest {

	
	Object value;
	
	@Before
	public void setup(){
		value = null;
	}
	
	@Test
	public void testCaseOfTypeWithExtractorAndAction() {
		
		Matching.when().extract(Person::getAge).isType((Integer i) -> value=i)
				.when().extract(Person::getAge).isType((Integer i) -> value=i)
				.match(new Person(100));
		
		
		assertThat(value,is(100));
	}
	
	@Test(expected=Exception.class)
	public void testCaseOfTypeWithExtractorAndActionBadCase() {
		Matching.when().extract(Person::getName).isType((Integer i) -> value = i)
				.when().extract(Person::getName).isType((Integer i) -> value = i)
					.match(new Person(100));
	

		assertThat(value,is(100));

	}
	@Test
	public void testCaseOfTypeMethodReference() {
		Matching.when().extract(Person::getAge).isType((Integer i) -> value = i)
				.match("hello");
		
		assertThat(value,is(nullValue()));

	}
	@Test
	public void testCaseOfTypeWithExtractorAndActionFalse() {
		Matching.when().extract(Person::getAge).isType((Long i) -> value = i)
					.match(new Person(100));
	
		assertThat(value,is(nullValue()));
		
	}

	@Test
	public void testCaseOfValueExtractorAction() {
		Matching.when().extract(Person::getAge).isValue(100).thenConsume((Integer i) -> value = i)
			.match(new Person(100));
		assertThat(value,is(100));
	}
	@Test
	public void testCaseOfValueExtractorActionFalse() {
		Matching.when().extract(Person::getAge).isValue(200).thenConsume( (Integer i) -> value = i)
			.match(new Person(100));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfValueVActionOfV() {
		Matching.when().isValue("hello").thenConsume(s -> value=s)
					.match("hello");
		assertThat(value,is("hello"));
	}
	@Test
	public void testCaseOfValueVActionOfVFalse() {
		Matching.when().isValue("hello").thenConsume(s -> value=s)
			.match("hello1");
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfTypeActionOfV() {
		Matching.when().isType((String s)-> value=s)
			.match("hello");
		assertThat(value,is("hello"));
	}
	@Test
	public void testCaseOfTypeActionOfVFalse() {
		Matching.when().isType((String s)-> value=s)
					.match(new Date());
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfMatcherOfVActionOfV() {
		Matching.when().isMatch(hasItem("hello world")).thenConsume( l -> value=l)
					.match(Arrays.asList("hello world"));
		
		assertThat(value,is(Arrays.asList("hello world")));
	}
	@Test
	public void testCaseOfMatcherOfVActionOfVFalse() {
		Matching.when().isMatch(hasItem("hello world2")).thenConsume( l -> value=l)
				.match(Arrays.asList("hello world"));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfPredicateOfVActionOfV() {
		Person person = new Person(42);
		
		Matching.when().isTrue(v->v==person).thenConsume(p->value=p)
				.match(person);
		
		assertThat(value,is(person));
	}
	@Test
	public void testCaseOfPredicateOfVActionOfVFalse() {
		Person person = new Person(42);
		Matching.when().isTrue(v->v==person).thenConsume(p->value=p)
			.match(new Person(42));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfThenExtractPredicateOfVActionOfVExtractorOfTR() {
		Matching.when().isTrue(it-> it instanceof List).thenExtract(get(0))
							.thenConsume(it->value=it)
				.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(true));
	}
	@Test
	public void testCaseOfThenExtractPredicateOfVActionOfVExtractorOfTRFalseSize() {
		Matching.when().isTrue((List<Object> it)-> it.size()>3)
							.thenExtract(at(0))
							.thenConsume( it->value=it)
				.when().isTrue((List<Object> it)-> it.size()>3).thenExtract(at(0)).thenConsume( it->value=it)
				.when().isTrue(it->it instanceof Map).thenExtract(at(0)).thenConsume(it->value=it)
				.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(nullValue()));
	}
	

	@Test
	public void testCaseOfThenExtractMatcherOfVActionOfVExtractorOfTR() {
		
		Matching.when().isMatch(is(not(empty())))
							.thenExtract(get(1))
							.thenConsume(it->value=it)
				.match(Arrays.asList(true,false,"hello"));
		
		//Matching.matchOfThenExtract(is(not(empty())), it->value=it, )
		//				.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(false));
	}
	@Test
	public void testCaseOfThenExtractMatcherOfVActionOfVExtractorOfTRFalse() {
		Matching.when().isMatch(is(not(empty())))
							.thenExtract(get(1))
							.thenConsume(it->value=it)
							.match(Arrays.asList());
//		Matching.matches(is(not(empty()))).thenExtract(get(1)).isType( it->value=it)
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfExtractorOfTRPredicateOfVActionOfV() {
		Matching.when().extract(at(0))
								.isTrue(name-> "bob".equals(name))
								.thenConsume(it->value=it)
							.match(new Person("bob",22));
	
		assertThat(value,is("bob"));
	}
	@Test
	public void testCaseOfExtractorOfTRPredicateOfVActionOfVFalse() {
		Matching.when().extract(at(0)).isTrue(name-> "bob".equals(name))
							.thenConsume(it->value=it)
	
					.match(new Person("rosie",22));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfExtractorOfTRMatcherOfVActionOfV() {
		Matching.when().extract(at(1))
						.isMatch(greaterThan(21))
						.thenConsume( it->value=it)
					.match(new Person("rosie",22));
		
		assertThat(value,is(22));
	}

	@Test
	public void testCaseOfExtractorOfTRMatcherOfVActionOfVFalse() {
		
		Matching.when().extract(at(1)).isMatch(greaterThan(21)).thenApply(it->value=it)
					.match(new Person("rosie",20));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testInCaseOfValueVFunctionOfVX() {
		
		assertThat(Matching.when().isValue(100).thenApply(v-> v+100)
					.match(100).orElse(100),is(200));
	}
	@Test
	public void testInCaseOfValueVFunctionOfVXFalse() {
		assertThat(Matching.when().isValue(100).thenApply(v-> v+100)
					.match(500).orElse(100),is(100));
	}
	@Test
	public void matchMany() {
		assertThat(Matching.when().isValue(100).thenApply(v-> v+100)
					.when().isType((Integer i) -> i)
					.matchMany(100).collect(Collectors.toList()),is(Arrays.asList(200,100)));
	}

	@Test
	public void testInCaseOfTypeFunctionOfTX() {
		assertThat(Matching.when().isType((Integer i) -> i-50)
					.match(100).get(),is(50));
	}
	@Test
	public void testInCaseOfTypeFunctionOfTXWithMap() {
		assertThat(Matching.when().isType((Integer i) -> i-50)
					.match(100)
					.map(x->x*100).get(),
					is(5000));
	}
	@Test
	public void testInCaseOfTypeFunctionOfTXFalse() {
		assertThat(Matching.when().isType((Integer i) -> i-50)
					.match(100l)
					.map(x->x*100),
					is(Optional.empty()));
	}

	@Test
	public void testInCaseOfPredicateOfVFunctionOfVX() {
		assertThat(Matching.when()
							.isTrue((Integer a)-> a>100)
							.thenApply(x->x*10)
				.apply(101).get(),is(1010));
	}
	@Test
	public void testInCaseOfPredicateOfVFunctionOfVXFalse() {
		assertThat(Matching.when().isTrue((Integer a)-> a>100).thenApply(x->x*10)
				.match(99),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfThenExtractPredicateOfTFunctionOfRXExtractorOfTR() {
		assertThat(Matching.when().isTrue((Person person)->person.getAge()>18)
								.thenExtract(Person::getName)
								.thenApply(name-> name + " is an adult")
							.match(new Person("rosie",39)).get(),is("rosie is an adult"));
	}
	@Test
	public void testInCaseOfThenExtractPredicateOfTFunctionOfRXExtractorOfTRFalse() {
		
		assertThat(Matching.when().isTrue((Person person)->person.getAge()>18).thenExtract(Person::getName).thenApply(name-> name + " is an adult")
							.match(new Person("rosie",9)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfExtractorOfTRPredicateOfVFunctionOfVX() {
		assertThat(Matching.when().extract(Person::getName).isTrue((String name)->name.length()>5). 
											thenApply(name->name+" is too long")
					.match(new Person("long name",9)).get(),is("long name is too long"));
	}
	@Test
	public void testInCaseOfExtractorOfTRPredicateOfVActionFalse() {
		assertThat(Matching.when().extract(Person::getName)
							.isTrue((String name)->name.length()>5)
							.thenApply(name->name+" is too long")
					.match(new Person("short",9)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfMatcherOfVFunctionOfVX() {
		
		assertThat(Matching.when()
							.isMatch(hasItem("hello"))
							.thenApply(hello->"world")
				.apply(Arrays.asList("hello")).get(),is("world"));
	}
	@Test
	public void testInCaseOfMatcherOfVFunctionOfVXFalse() {
		assertThat(Matching.when().isMatch(hasItem("hello"))
									.thenApply(  hello-> "world")
				.match(Arrays.asList("hello2")),is(Optional.empty()));
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testInCaseOfThenExtractMatcherOfTFunctionOfRXExtractorOfTR() {
		
		assertThat(Matching.when().isMatch(hasItem("hello")).thenExtract(at(1)).thenApply(value -> "second value is " + value)
						.match(Arrays.asList("hello","world")).get(),is("second value is world"));
	}
	public void testInCaseOfThenExtractMatcherOfTFunctionOfRXExtractorOfTRFalse() {
		;
		assertThat(  Matching.when().isMatch(hasItem("hello2"))
							.thenExtract(at(1))
							.thenApply(value -> "second value is " + value)
							.match(Arrays.asList("hello","world")), is(Optional.empty()));
	}

	@Test
	public void testInCaseOfExtractorOfTRMatcherOfVFunctionOfVX() {
		
		assertThat(Matching.when().extract(Person::getName)
									.isMatch(is("bob"))
									.thenApply(name -> name + " wins!")
								.apply(new Person("bob",65)).get(),is("bob wins!"));
	}
	@Test
	public void testInCaseOfExtractorOfTRMatcherOfVFunctionOfVXFalse() {
		assertThat(Matching.when().extract(Person::getName)
							.isMatch(is("bob2"))
							.thenApply( name -> name + " wins!")
				.match(new Person("bob",65)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfTypeExtractorOfTRFunctionOfVX() {
		assertThat(Matching.when().extract(at(0))
										.isType((Person person)->"age is " + person.getAge())
							.match(Arrays.asList(new Person("amy",22))).get(),is("age is 22"));
	}
	@Test
	public void testInCaseOfTypeExtractorOfTRFunctionOfVXFalse() {
		assertThat(Matching.when().extract(at(0))
										.isType((Person person)->"age is " + person.getAge())
							.match(Arrays.asList(new Address(10, "street", "city", "country"),
				new Person("amy",22))),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfValueRExtractorOfTRFunctionOfVX() {
		assertThat(Matching.when().extract(Person::getName)
										.isValue("hello")
										.thenApply(name -> name + " world")
							.match(new Person("hello",40)).get(),is("hello world"));
	}
	@Test
	public void testInCaseOfValueRExtractorOfTRFunctionOfVXFalse() {
		assertThat(Matching.when().extract(Person::getName)
								.isValue("hello")
								.thenApply(name -> name + " world")
							.match(new Person("hello2",40)),is(Optional.empty()));
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
