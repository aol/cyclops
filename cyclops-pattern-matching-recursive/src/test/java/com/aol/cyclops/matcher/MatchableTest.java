package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import lombok.Value;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.matcher.recursive.RecursiveMatcher;

public class MatchableTest {
	private <I,T> CheckValues<Object, T> cases(CheckValues<I, T> c) {
		
		return c.with(1,2,3).then(i->"hello")
				.with(4,5,6).then(i->"goodbye");
	}
	@Test
	public void test(){
		assertThat(new MyCase(1,2,3).match(this::cases),equalTo("hello"));
		
	}
	@Test
	public void test2(){
		assertThat(new MyCase(4,5,6).match(this::cases ),equalTo("goodbye"));
		
	}
	@Test
	public void test3(){
		assertThat(new MyCase(4,2,3).match(this::cases,"default"   ),equalTo("default"));
		
	}

	
	@Test
	public void test_(){
	
		
		assertThat(new MyCase(4,5,6)._match(c ->c.isType( (MyCase ce)-> "hello").with(1,2,3),"goodbye") ,
				  equalTo("goodbye"));
		
	}
	@Test
	public void test_2(){
	
		
		assertThat(new MyCase(4,5,6)._match(c ->c.isType( (MyCase ce)-> "hello").with(4,5,6),"goodbye") ,
				  equalTo("hello"));
		
	}
	@Test
	public void testMatch(){
		
		assertThat(new MyCase(4,5,6).matchType(c ->c.isType((MyCase ce) -> "hello")) ,
				  equalTo("hello"));
		
		
	}
	@Value
	static class MyCase  implements Matchable{
		int a;
		int b;
		int c;
	}
	
	@Test 
	public void matchable(){
		System.out.println(Matchable.from(Optional.of(1)).matches(c->c.hasValues(1).then(i->2)));
		System.out.println(Matchable.from(Optional.empty()).matches(c->c.isEmpty().then(i->"hello")));
		System.out.println(Matchable.from(Optional.empty()).matches(
				 o->o.isEmpty().then(i->"hello"),
				 o->o.hasValues(1).then(i->2)));
		
		System.out.println(Matchable.from(1)
				                    .matchType(c->c.isType((Integer it)->"hello")));
		System.out.println(Matchable.from(1)
							        .matches(c->c.hasValuesWhere((Object i)->(i instanceof Integer)).then(i->2)));
		
	}

}
