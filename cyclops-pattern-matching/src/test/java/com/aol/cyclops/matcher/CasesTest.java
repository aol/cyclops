package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;
import org.pcollections.ConsPStack;

public class CasesTest {

	
	@Test
	public void ofPStack() {
		val cases = Cases.of(ConsPStack.singleton(Case.of(input->true,input->"hello")));
		assertThat(cases.size(),is(1));
	}

	@Test
	public void ofVarargs() {
		val cases = Cases.of(Case.of(input->true,input->"hello"));
		assertThat(cases.size(),is(1));
	}

	@Test
	public void testZip() {
		Stream<Predicate<Integer>> predicates = Stream.of(i->true,i->false);
		Stream<Function<Integer,Integer>> functions = Stream.of(i->i+2,i->i*100);
		
		val cases = Cases.zip(predicates, functions);
		assertThat(cases.size(),is(2));
		assertThat(cases.match(100).get(),is(102));
	}

	@Test
	public void testUnzip() {
		val cases = Cases.of(Case.of(input->true,input->"hello"));
		
		val unzipped = cases.unzip();
		assertTrue(unzipped.v1.map(p->p.test(10)).allMatch(v->v));
		assertTrue(unzipped.v2.map(fn->fn.apply(10)).allMatch(v->"hello".equals(v)));
	}

	int found;
	@Test
	public void testForEach() {
		found = 0;
		Cases.of(Case.of(input->true,input->"hello")).forEach( cse -> found++);
		assertTrue(found==1);
	}

	@Test
	public void testSequential() {
		Set<Long> threads = new HashSet<>();
		val case1 = Case.of(input->true,input->{ threads.add(Thread.currentThread().getId());return "hello";});
		Cases.of(case1,case1,case1,case1).sequential().match(10);
		assertThat(threads.size(),is(1));
	}

	@Test
	public void testParallel() {
		Set<Long> threads = new HashSet<>();
		val case1 = Case.of(input->true,input->{ threads.add(Thread.currentThread().getId());return "hello";});
		Cases.of(case1,case1,case1,case1).parallel().match(10);
		assertThat(threads.size(),greaterThan(1));
	}

	@Test
	public void testMerge() {
		val cases1 = Cases.of(Case.of(input->true,input->"hello"));
		val cases2 = Cases.of(Case.of(input->true,input->"hello"));
		
		val cases3 = cases1.merge(cases2);
		
		assertThat(cases3.size(),is(cases1.size()+cases2.size()));
	}

	@Test
	public void testFilter() {
		val cases = Cases.of(Case.of(input->true,input->"hello"),Case.of(input->false,input->"second"))
									.filter(p-> p.getPredicate().test(10));
		assertThat(cases.size(),is(1));
	}

	@Test
	public void testFilterPredicate() {
		val cases = Cases.of(Case.of(input->true,input->"hello"),Case.of(input->false,input->"second"))
				.filterPredicate(p-> p.test(10));
		assertThat(cases.size(),is(1));
	}

	@Test
	public void testFilterFunction() {
		val cases = Cases.of(Case.of(input->true,input->"hello"),Case.of(input->false,input->"second"))
				.filterFunction(fn-> fn.apply(10).equals("second"));
		assertThat(cases.size(),is(1));
	}

	@Test
	public void testMapPredicate() {
		List results = Cases.of(Case.of(input->true,input->"hello"),Case.of(input->false,input->"second"))
						.mapPredicate(p->input->true).matchMany(10).collect(Collectors.toList());
		
		assertThat(results.size(),is(2));
	}

	@Test
	public void testMapFunction() {
		List<String> results = Cases.of(Case.of(input->true,input->"hello"),Case.of(input->true,input->"second"))
				.mapFunction(fn->input->"prefix_"+fn.apply(input)).<String>matchMany(10).collect(Collectors.toList());
		
		assertThat(results.size(),is(2));
		assertTrue(results.stream().allMatch(s->s.startsWith("prefix_")));
		assertTrue(results.stream().anyMatch(s->s.startsWith("prefix_hello")));
		assertTrue(results.stream().anyMatch(s->s.startsWith("prefix_second")));
	}

	@Test
	public void testMap() {
		List<String> results = Cases.of(Case.of(input->true,input->"hello"),Case.of(input->false,input->"second"))
				.map(cse->Case.of(t->true,input->"prefix_"+cse.getAction().apply(input))).<String>matchMany(10).collect(Collectors.toList());
		
		assertThat(results.size(),is(2));
		assertTrue(results.stream().allMatch(s->s.startsWith("prefix_")));
		assertTrue(results.stream().anyMatch(s->s.startsWith("prefix_hello")));
		assertTrue(results.stream().anyMatch(s->s.startsWith("prefix_second")));
	}

	@Test
	public void testFlatMap() {
		val cases = Cases.of(Case.of(input->true,input->"hello"),Case.of(input->false,input->"second"))
						.flatMap(input-> Cases.of(input.plus(Case.of(in->true,in->"new"))));
		
		assertThat(cases.size(),is(3));
	}

	@Test
	public void testAppend() {
		Cases<Integer,String,Function<Integer,String>> cases1 = Cases.of(Case.of((Integer input)->10==input,input->"hello"),Case.of(input->11==input,input->"world"));
		Case<Integer,String,Function<Integer,String>> caze = Case.of((Integer input)->11==input,input->"hello");
		
		val cases3 = cases1.append(1,caze);
		
		assertThat(cases3.size(),is(3));
		
		assertThat(cases3.match(11).get(),is("hello"));
	}

	

	@Test
	public void testAsUnwrappedFunction() {
		assertThat(Cases.of(Case.of(input->true,input->"hello")).asUnwrappedFunction().apply(10),is("hello"));
	}

	@Test
	public void testAsStreamFunction() {
		assertThat(Cases.of(Case.of(input->true,input->"hello")).asStreamFunction().apply(10).findFirst().get(),is("hello"));
	}

	@Test
	public void testApply() {
		assertThat(Cases.of(Case.of(input->true,input->"hello")).apply(10).get(),is("hello"));
	}

	@Test
	public void testMatchManyFromStream() {
		List<String> results = Cases.of(Case.of((Integer input)->10==input,input->"hello"),
											Case.of(input->11==input,input->"world"))
										.<String>matchManyFromStream(Stream.of(1,10,11))
										.toList();
		
		assertThat(results.size(),is(2));
		assertThat(results,hasItem("hello"));
		assertThat(results,hasItem("world"));
		
	}

	@Test
	public void testMatchManyFromStreamAsync() {
		List<String> results = Cases.of(Case.of((Integer input)->10==input,input->"hello"),
				Case.of(input->11==input,input->"world"))
			.<String>matchManyFromStreamAsync(ForkJoinPool.commonPool(),Stream.of(1,10,11))
			.join()
			.toList();

		assertThat(results.size(),is(2));
		assertThat(results,hasItem("hello"));
		assertThat(results,hasItem("world"));
	}

	@Test
	public void testMatchMany() {
		List<String> results =  Cases.of(Case.of((Integer input)->10==input,input->"hello"),
				Case.of(input->11==input,input->"world"),
				Case.of(input->10==input,input->"woo!"))
			.<String>matchMany(10).toList();
		
		assertThat(results.size(),is(2));
		assertThat(results,hasItem("hello"));
		assertThat(results,hasItem("woo!"));
	}

	@Test
	public void testMatchManyAsync() {
		List<String> results =  Cases.of(Case.of((Integer input)->10==input,input->"hello"),
				Case.of(input->11==input,input->"world"),
				Case.of(input->10==input,input->"woo!"))
			.<String>matchManyAsync(ForkJoinPool.commonPool(),10).join().toList();
		
		assertThat(results.size(),is(2));
		assertThat(results,hasItem("hello"));
		assertThat(results,hasItem("woo!"));
	}

	@Test
	public void testMatchFromStream() {
		List<String> results = Cases
				.of(Case.of((Integer input) -> 10 == input, input -> "hello"),
						Case.of((Integer input) -> 10 == input, input -> "ignored"),
						Case.of(input -> 11 == input, input -> "world"))
				.<String> matchFromStream(Stream.of(1, 11, 10)).toList();

		assertThat(results.size(), is(2));
		assertThat(results, hasItem("hello"));
		assertThat(results, hasItem("world"));
	}

	@Test
	public void testMatchFromStreamAsync() {
		List<String> results = Cases
				.of(Case.of((Integer input) -> 10 == input, input -> "hello"),
						Case.of((Integer input) -> 10 == input, input -> "ignored"),
						Case.of(input -> 11 == input, input -> "world"))
				.<String> matchFromStreamAsync(ForkJoinPool.commonPool(),Stream.of(1, 11, 10)).join().toList();

		assertThat(results.size(), is(2));
		assertThat(results, hasItem("hello"));
		assertThat(results, hasItem("world"));
	}

	@Test
	public void testMatchObjectArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchAsync() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnapply() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchT() {
		fail("Not yet implemented");
	}

	@Test
	public void testWithCases() {
		fail("Not yet implemented");
	}

	@Test
	public void testCasesPStackOfCaseOfTRXBoolean() {
		fail("Not yet implemented");
	}

}
