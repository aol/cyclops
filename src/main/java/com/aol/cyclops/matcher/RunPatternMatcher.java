package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Extractors._;
import static com.aol.cyclops.matcher.Matchers.rangeChecker;
import static com.aol.cyclops.matcher.Matchers.typeMatcher;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class RunPatternMatcher {

	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		PatternMatcher  m = new PatternMatcher();
	
		m.caseOf( typeMatcher(String.class),
				(String t) -> System.out.println("Value "+ t));
		
		m.caseOf( rangeChecker(1,10),
				(Integer t) -> System.out.println("Value in range "+ t));
		
		m.caseOfThenExtract(typeMatcher(List.class), (head)-> System.out.println("head is " + head),
				Extractors.collectionHead);
		
		m.caseOf(Extractors.collectionHead,rangeChecker(1,10), (head)-> System.out.println("within range " + head));
		
		m.inCaseOf(Extractors.collectionHead,rangeChecker(100,200), (Integer head)-> head +100);
		m.caseOf(_(3),rangeChecker(100,200), System.out::println);
		
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
}
