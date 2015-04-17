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
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hamcrest.Matcher;
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
		Matching.newCase(c -> c.extract(Person::getAge).isType((Integer i) -> value=i))
				.match(new Person(100));
		
		
		assertThat(value,is(100));
		
	}
	@Test(expected=Exception.class)
	public void testCaseOfTypeWithExtractorAndActionBadCase() {
		Matching.newCase(c->c.extract(Person::getName).isType((Integer i) -> value = i))
					.match(new Person(100));
	

		assertThat(value,is(100));

	}
	@Test
	public void testCaseOfTypeMethodReference() {
		Matching.newCase(c->c.extract(Person::getAge).isType((Integer i) -> value = i))
				.match("hello");
		
		assertThat(value,is(nullValue()));

	}
	@Test
	public void testCaseOfTypeWithExtractorAndActionFalse() {
		Matching.newCase(c->c.extract(Person::getAge).isType((Long i) -> value = i))
					.match(new Person(100));
	
		assertThat(value,is(nullValue()));
		
	}

	@Test
	public void testCaseOfValueExtractorAction() {
		Matching.newCase(c->c.extract(Person::getAge).isValue(100).thenConsume((Integer i) -> value = i))
			.match(new Person(100));
		assertThat(value,is(100));
	}
	@Test
	public void testCaseOfValueExtractorActionFalse() {
		Matching.newCase(c->c.extract(Person::getAge).isValue(200).thenConsume( (Integer i) -> value = i))
			.match(new Person(100));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfValueVActionOfV() {
		Matching.newCase(c->c.isValue("hello").thenConsume(s -> value=s))
					.match("hello");
		assertThat(value,is("hello"));
	}
	@Test
	public void testCaseOfValueVActionOfVFalse() {
		Matching.newCase(c->c.isValue("hello").thenConsume(s -> value=s))
			.match("hello1");
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfTypeActionOfV() {
		Matching.newCase(c->c.isType((String s)-> value=s))
			.match("hello");
		assertThat(value,is("hello"));
	}
	@Test
	public void testCaseOfTypeActionOfVFalse() {
		Matching.newCase(c->c.isType((String s)-> value=s))
					.match(new Date());
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfMatcherOfVActionOfV() {
		Matching.newCase(c-> c.isMatch(hasItem("hello world")).thenConsume( l -> value=l))
					.match(Arrays.asList("hello world"));
		
		assertThat(value,is(Arrays.asList("hello world")));
	}
	@Test
	public void testCaseOfMatcherOfVActionOfVFalse() {
		Matching.newCase(c->c.isMatch(hasItem("hello world2")).thenConsume( l -> value=l))
				.match(Arrays.asList("hello world"));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfPredicateOfVActionOfV() {
		Person person = new Person(42);
		
		Matching.newCase(c->c.isTrue(v->v==person).thenConsume(p->value=p))
		
		
				.match(person);
		
		assertThat(value,is(person));
	}
	@Test
	public void testCaseOfPredicateOfVActionOfVFalse() {
		Person person = new Person(42);
		Matching.newCase(c->c.isTrue(v->v==person).thenConsume(p->value=p))
			.match(new Person(42));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfThenExtractPredicateOfVActionOfVExtractorOfTR() {
		Matching.newCase(c->c.isTrue(it-> it instanceof List).thenExtract(get(0))
							.thenConsume(it->value=it))
				.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(true));
	}
	@Test
	public void testCaseOfThenExtractPredicateOfVActionOfVExtractorOfTRFalse() {
		Matching.newCase(c->c.isTrue((List<Object> it)-> it.size()>2)
							.thenExtract(at(0))
							.thenConsume( it->value=it))
				.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfThenExtractMatcherOfVActionOfVExtractorOfTR() {
		
		Matching.newCase(c->c.isMatch(is(not(empty())))
							.thenExtract(get(1))
							.thenConsume(it->value=it))
				.match(Arrays.asList(true,false,"hello"));
		
		//Matching.matchOfThenExtract(is(not(empty())), it->value=it, )
		//				.match(Arrays.asList(true,false,"hello"));
		
		assertThat(value,is(false));
	}
	@Test
	public void testCaseOfThenExtractMatcherOfVActionOfVExtractorOfTRFalse() {
		Matching.newCase(c->c.isMatch(is(not(empty())))
							.thenExtract(get(1))
							.thenConsume(it->value=it))
							.match(Arrays.asList());
//		Matching.matches(is(not(empty()))).thenExtract(get(1)).isType( it->value=it)
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfExtractorOfTRPredicateOfVActionOfV() {
		Matching.newCase(c->c.extract(at(0))
								.isTrue(name-> "bob".equals(name))
								.thenConsume(it->value=it))
							.match(new Person("bob",22));
	
		assertThat(value,is("bob"));
	}
	@Test
	public void testCaseOfExtractorOfTRPredicateOfVActionOfVFalse() {
		Matching.newCase().extract(at(0)).isTrue(name-> "bob".equals(name))
							.thenConsume(it->value=it)
	
					.match(new Person("rosie",22));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testCaseOfExtractorOfTRMatcherOfVActionOfV() {
		Matching.newCase().extract(at(1))
						.isMatch(greaterThan(21))
						.thenConsume( it->value=it)
					.match(new Person("rosie",22));
		
		assertThat(value,is(22));
	}

	@Test
	public void testCaseOfExtractorOfTRMatcherOfVActionOfVFalse() {
		
		Matching.newCase().extract(at(1)).isMatch(greaterThan(21)).thenApply(it->value=it)
					.match(new Person("rosie",20));
		
		assertThat(value,is(nullValue()));
	}

	@Test
	public void testInCaseOfValueVActionWithReturnOfVX() {
		
		assertThat(Matching.newCase().isValue(100).thenApply(v-> v+100)
					.match(100).orElse(100),is(200));
	}
	@Test
	public void testInCaseOfValueVActionWithReturnOfVXFalse() {
		assertThat(Matching.newCase().isValue(100).thenApply(v-> v+100)
					.match(500).orElse(100),is(100));
	}

	@Test
	public void testInCaseOfTypeActionWithReturnOfTX() {
		assertThat(Matching.newCase().isType((Integer i) -> i-50)
					.match(100).get(),is(50));
	}
	@Test
	public void testInCaseOfTypeActionWithReturnOfTXWithMap() {
		assertThat(Matching.newCase().isType((Integer i) -> i-50)
					.match(100)
					.map(x->x*100).get(),
					is(5000));
	}
	@Test
	public void testInCaseOfTypeActionWithReturnOfTXFalse() {
		assertThat(Matching.newCase().isType((Integer i) -> i-50)
					.match(100l)
					.map(x->x*100),
					is(Optional.empty()));
	}

	@Test
	public void testInCaseOfPredicateOfVActionWithReturnOfVX() {
		assertThat(Matching.newCase()
							.isTrue((Integer a)-> a>100)
							.thenApply(x->x*10)
				.apply(101).get(),is(1010));
	}
	@Test
	public void testInCaseOfPredicateOfVActionWithReturnOfVXFalse() {
		assertThat(Matching.newCase().isTrue((Integer a)-> a>100).thenApply(x->x*10)
				.match(99),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfThenExtractPredicateOfTActionWithReturnOfRXExtractorOfTR() {
		assertThat(Matching.newCase().isTrue((Person person)->person.getAge()>18)
								.thenExtract(Person::getName)
								.thenApply(name-> name + " is an adult")
							.match(new Person("rosie",39)).get(),is("rosie is an adult"));
	}
	@Test
	public void testInCaseOfThenExtractPredicateOfTActionWithReturnOfRXExtractorOfTRFalse() {
		
		assertThat(Matching.newCase().isTrue((Person person)->person.getAge()>18).thenExtract(Person::getName).thenApply(name-> name + " is an adult")
							.match(new Person("rosie",9)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfExtractorOfTRPredicateOfVActionWithReturnOfVX() {
		assertThat(Matching.newCase(c->c.extract(Person::getName).isTrue((String name)->name.length()>5). 
											thenApply(name->name+" is too long"))
					.match(new Person("long name",9)).get(),is("long name is too long"));
	}
	@Test
	public void testInCaseOfExtractorOfTRPredicateOfVActionFalse() {
		assertThat(Matching.newCase().extract(Person::getName)
							.isTrue((String name)->name.length()>5)
							.thenApply(name->name+" is too long")
					.match(new Person("short",9)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfMatcherOfVActionWithReturnOfVX() {

		assertThat(Matching.newCase()
							.isMatch(hasItem("hello"))
							.thenApply(hello->"world")
				.apply(Arrays.asList("hello")).get(),is("world"));
	}
	@Test
	public void testInCaseOfMatcherOfVActionWithReturnOfVXFalse() {
		assertThat(Matching.newCase().isMatch(hasItem("hello"))
									.thenApply(  hello-> "world")
				.match(Arrays.asList("hello2")),is(Optional.empty()));
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testInCaseOfThenExtractMatcherOfTActionWithReturnOfRXExtractorOfTR() {
		
		assertThat(Matching.newCase().isMatch(hasItem("hello")).thenExtract(at(1)).thenApply(value -> "second value is " + value)
						.match(Arrays.asList("hello","world")).get(),is("second value is world"));
	}
	public void testInCaseOfThenExtractMatcherOfTActionWithReturnOfRXExtractorOfTRFalse() {
		;
		assertThat(  Matching.newCase().isMatch(hasItem("hello2"))
							.thenExtract(at(1))
							.thenApply(value -> "second value is " + value)
							.match(Arrays.asList("hello","world")), is(Optional.empty()));
	}

	@Test
	public void testInCaseOfExtractorOfTRMatcherOfVActionWithReturnOfVX() {
		
		assertThat(Matching.newCase().extract(Person::getName)
									.isMatch(is("bob"))
									.thenApply(name -> name + " wins!")
								.apply(new Person("bob",65)).get(),is("bob wins!"));
	}
	@Test
	public void testInCaseOfExtractorOfTRMatcherOfVActionWithReturnOfVXFalse() {
		assertThat(Matching.newCase(c->c.extract(Person::getName)
							.isMatch(is("bob2"))
							.thenApply( name -> name + " wins!"))
				.match(new Person("bob",65)),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfTypeExtractorOfTRActionWithReturnOfVX() {
		assertThat(Matching.newCase(c->c.extract(at(0))
										.isType((Person person)->"age is " + person.getAge()))
							.match(Arrays.asList(new Person("amy",22))).get(),is("age is 22"));
	}
	@Test
	public void testInCaseOfTypeExtractorOfTRActionWithReturnOfVXFalse() {
		assertThat(Matching.newCase(c->c.extract(at(0))
										.isType((Person person)->"age is " + person.getAge()))
							.match(Arrays.asList(new Address(10, "street", "city", "country"),
				new Person("amy",22))),is(Optional.empty()));
	}

	@Test
	public void testInCaseOfValueRExtractorOfTRActionWithReturnOfVX() {
		assertThat(Matching.newCase(c->c.extract(Person::getName)
										.isValue("hello")
										.thenApply(name -> name + " world"))
							.match(new Person("hello",40)).get(),is("hello world"));
	}
	@Test
	public void testInCaseOfValueRExtractorOfTRActionWithReturnOfVXFalse() {
		assertThat(Matching.newCase(c->c.extract(Person::getName)
								.isValue("hello")
								.thenApply(name -> name + " world"))
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
