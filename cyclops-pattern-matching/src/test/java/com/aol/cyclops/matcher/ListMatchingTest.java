package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Two.tuple;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import com.aol.cyclops.lambda.api.Printable;
import com.aol.cyclops.matcher.builders.PatternMatcher;
public class ListMatchingTest implements Printable{

	
	String language = null;
	@Test
	public void multiMatch(){
		new PatternMatcher()
			.caseOfMany((List<String> list) -> language  = list.get(1) ,
								v -> v.equals("-l") || v.equals("---lang"),v->true)
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
			.matchOfMany( (List<String> list) -> language  = list.get(1) ,
							equalTo("-l"),any(String.class))
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
