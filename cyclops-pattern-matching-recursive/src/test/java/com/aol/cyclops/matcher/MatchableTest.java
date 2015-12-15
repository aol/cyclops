package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;

import lombok.Value;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.matcher.recursive.RecursiveMatcher;

public class MatchableTest {

	@Test
	public void testMatch(){
		
		assertThat(new MyCase(4,5,6).matchType(c ->c.isType((MyCase ce) -> "hello")) ,
				  equalTo("hello"));
		
		
	}
	@Value
	static class MyCase<R>  implements Matchable{
		int a;
		int b;
		int c;
	}
	
	@Test 
	public void matchable(){
		Optional<Integer> result = Matchable.listOfValues(Optional.of(1))
											.mayMatch(c->c.hasValues(2).then(i->2));
		assertThat(Matchable.of(result)
				 .matches(c->c.isEmpty().then(i->"hello")),equalTo("hello"));
		
		Integer result2 = Matchable.of(Optional.of(1))
									.matches(c->c.hasValues(1).then(i->2));
		
		System.out.println(Matchable.of(Optional.of(1)).matches(c->c.hasValues(1).then(i->2)));
		System.out.println(Matchable.of(Optional.empty()).matches(c->c.isEmpty().then(i->"hello")));
		System.out.println(Matchable.of(Optional.empty()).matches(
				 o->o.isEmpty().then(i->"hello"),
				 o->o.hasValues(1).then(i->2)));
		
		System.out.println(Matchable.of(1)
				                    .matchType(c->c.isType((Integer it)->"hello")));
		System.out.println(Matchable.listOfValues(1,2,3)
							        .matches(c->c.hasValuesWhere((Object i)->(i instanceof Integer)).then(i->2)));
		
	}

}
