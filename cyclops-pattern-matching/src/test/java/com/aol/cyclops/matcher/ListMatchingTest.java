package com.aol.cyclops.matcher;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
public class ListMatchingTest {

	
	String language = null;
	@Test
	public void multiMatch(){
		new PatternMatcher()
			.caseOfIterable(asList( v -> v.equals("-l") || v.equals("---lang"),v->true),
								(List<String> list) -> language  = list.get(1) )
			.match(asList("-l","java"));
		
		assertThat(language,is("java"));
	}
	@Test
	public void tupleMatch(){
		new PatternMatcher()
			.caseOfPredicates(tuple( v ->  v.equals("-l") ||  v.equals("---lang"),
									 v->true),
								lang -> language  =lang,Extractors.<String>at(1) )
			.match(tuple("-l","java"));
		
		assertThat(language,is("java"));
	}
	@Test
	public void generalTuple(){
		new PatternMatcher()
			.caseOfTuple(tuple(p( v ->  v.equals("-l") ||  v.equals("---lang")),
									 p(v->true)),
								lang -> language  =lang,Extractors.<String>at(1) )
			.match(tuple("-l","java"));
		
		assertThat(language,is("java"));
	}
	@Test
	public void generalTuple2(){
		new PatternMatcher()
			.caseOfTuple(tuple(p( v ->  v.equals("-l") ||  v.equals("---lang")),
									 p(v->true)),
								t ->language =t.v2,
								Extractors.<String,String>of(0,1) )
			.match(tuple("-l","java"));
		
		assertThat(language,is("java"));
	}
	private Predicate p(Predicate p) {
		return p;
	}
	@Test
	public void tupleMatchers(){
		new PatternMatcher()
			.matchOfMatchers(tuple( equalTo("-l"),
									 anything()),
								lang -> language  = lang,Extractors.<String>at(1) )
			.match(tuple("-l","java"));
		
		assertThat(language,is("java"));
	}
	@Test
	public void multiMatchOfIterable(){
		new PatternMatcher()
			.matchOfIterable(asList( equalTo("-l"),any(String.class)),
								(List<String> list) -> language  = list.get(1) )
			.match(asList("-l","java"));
		
		assertThat(language,is("java"));
	}
	
	@Test
	public void generalMatchTuple(){
		new PatternMatcher()
			.matchOfTuple(tuple(equalTo("-l"),anything()),
								lang -> language  =lang,Extractors.<String>at(1) )
			.match(tuple("-l","java"));
		
		assertThat(language,is("java"));
	}

	
	
}
