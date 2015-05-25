package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.Value;

import org.junit.Test;

import com.aol.cyclops.matcher.builders.CheckValues;

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
}
