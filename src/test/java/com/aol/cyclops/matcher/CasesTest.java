package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.internal.matcher2.Case;
import com.aol.cyclops.internal.matcher2.Cases;
import com.aol.cyclops.types.Decomposable;


public class CasesTest {

	
	@Test
	public void ofPStack() {
		Cases cases = Cases.ofList(Arrays.asList(Case.of(input->true,input->"hello")));
		assertThat(cases.size(),is(1));
	}

	@Test
	public void ofVarargs() {
		val cases = Cases.of(Case.of(input->true,input->"hello"));
		assertThat(cases.size(),is(1));
	}

	
	int found;
	

	

	
	@AllArgsConstructor
	@Getter
	static class Person{
		String name;
		int age;
	}
	
	@Test
	public void testAppend() {
		Cases<Integer,String> cases1 = Cases.of(Case.of((Integer input)->10==input,input->"hello"),Case.of(input->11==input,input->"world"));
		Case<Integer,String> caze = Case.of((Integer input)->11==input,input->"hello");
		
		val cases3 = cases1.append(1,caze);
		
		assertThat(cases3.size(),is(3));
		
		assertThat(cases3.match(11).get(),is("hello"));
	}

	

	@Test
	public void testAsUnwrappedFunction() {
		assertThat(Cases.of(Case.of(input->true,input->"hello")).asUnwrappedFunction().apply(10),is("hello"));
	}

	@Test
	public void testApply() {
		assertThat(Cases.of(Case.of(input->true,input->"hello")).apply(10).get(),is("hello"));
	}


	@Test
	public void testMatchFromStream() {
		List<String> results = Cases
				.of(Case.of((Integer input) -> 10 == input, input -> "hello"),
						Case.of((Integer input) -> 10 == input, input -> "ignored"),
						Case.of(input -> 11 == input, input -> "world"))
				.<String> matchFromStream(Stream.of(1, 11, 10)).collect(Collectors.toList());

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
				.<String> matchFromStreamAsync(ForkJoinPool.commonPool(),Stream.of(1, 11, 10)).join().collect(Collectors.toList());

		assertThat(results.size(), is(2));
		assertThat(results, hasItem("hello"));
		assertThat(results, hasItem("world"));
	}

	@Test
	public void testMatchObjectArray() {
		assertThat(Cases.of(Case.of((List<Integer> input) -> input.size()==3, input -> "hello"),
				Case.of((List<Integer> input) -> input.size()==2, input -> "ignored"),
				Case.of((List<Integer> input) -> input.size()==1, input -> "world")).match(1,2,3).get(),is("hello"));
	}

	@Test
	public void testMatchAsync() {
		assertThat(Cases.of(Case.of((List<Integer> input) -> input.size()==3, input -> "hello"),
				Case.of((List<Integer> input) -> input.size()==2, input -> "ignored"),
				Case.of((List<Integer> input) -> input.size()==1, input -> "world"))
				.matchAsync(ForkJoinPool.commonPool(),1,2,3).join().get(),is("hello"));

	}

	@Test
	public void testUnapply() {
		assertThat(Cases.of(Case.of((List input) -> input.size()==3, input -> "hello"),
				Case.of((List input) -> input.size()==2, input -> "ignored"),
				Case.of((List input) -> input.size()==1, input -> "world"))
				.unapply(new MyClass(1,"hello")).get(),is("ignored"));
	}
	@AllArgsConstructor
	static class MyClass implements Decomposable{
		int value;
		String name;
	}
	
	

	

}
