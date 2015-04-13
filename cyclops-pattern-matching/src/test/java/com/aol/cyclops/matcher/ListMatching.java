package com.aol.cyclops.matcher;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.function.Predicate;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;
public class ListMatching {

	
	String language = null;
	@Test
	public void multiMatch(){
		new PatternMatcher()
			.caseOfPredicates(asList( v -> v.equals("-l") || v.equals("---lang"),v->true),
								(List<String> list) -> language  = list.get(1) )
			.match(asList("-l","java"));
		
		assertThat(language,is("java"));
	}
	@Test
	public void tupleMatch(){
		new PatternMatcher()
			.caseOfPredicates(tuple(p( v -> v.equals("-l") || v.equals("---lang")),p(v->true)),
								 tuple -> language  = ((Tuple2<String,String>)tuple).v2 )
			.match(tuple("-l","java"));
		
		assertThat(language,is("java"));
	}

	public Predicate p(Predicate p){
		return p;
	}
	
}
