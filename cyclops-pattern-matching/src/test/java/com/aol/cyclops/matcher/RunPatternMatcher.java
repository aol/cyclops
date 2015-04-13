package com.aol.cyclops.matcher;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static com.aol.cyclops.matcher.Extractors.at;
import static com.aol.cyclops.matcher.Predicates.rangeChecker;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class RunPatternMatcher {

	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		PatternMatcher  m = new PatternMatcher();
	
		m.matchOf( instanceOf(String.class),
				(String t) -> System.out.println("Value "+ t));
		
		m.caseOf( rangeChecker(1,10),
				(Integer t) -> System.out.println("Value in range "+ t));
		
		m.matchOfThenExtract(instanceOf(List.class), (head)-> System.out.println("head is " + head),
				Extractors.at(0));
		
		m.caseOf(Extractors.at(0),rangeChecker(1,10), (head)-> System.out.println("within range " + head));
		
		m.inCaseOf(Extractors.at(0),rangeChecker(100,200), (Integer head)-> head +100);
		m.caseOf(at(3),rangeChecker(100,200), System.out::println);
		
		m.match("hello world");
		m.match(String.class);
		m.match(100);
		m.match(5);
		m.match(ImmutableList.of(100,2,3,4));
		m.match(ImmutableList.of(5,2,3,4));
		System.out.println("*");
		m.match(ImmutableList.of(1,2,3,150,160));
		System.out.println("*");
		
		
		System.out.println ("Got (+100)" + m.match(ImmutableList.of(120,200,420)).get());
	}
	
	@Test
	public void testPostExtract(){
		PatternMatcher  m = new PatternMatcher();
		m.matchOfThenExtract(instanceOf(List.class), (head)-> System.out.println("head is " + head),
				Extractors.at(0));
		m.match(ImmutableList.of(120,200,420));
		
	}
	@Test
	public void testPostExtractWithReturn(){
		PatternMatcher  m = new PatternMatcher();
		m.inMatchOfThenExtract(instanceOf(List.class), (head)-> head,
				Extractors.at(0));
		assertThat(120,is(m.match(ImmutableList.of(120,200,420)).get()));
	}
}
