package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Extractors.at;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;



public class PresentationTest {

	 @Test
	    public void patternMatch() {

	    	
	        String result =  Matching.inCaseOfType((FileNotFoundException e) -> "file not found")
	        		.inCaseOfType((Exception e) -> "general exception")
	        		.inCaseOfType((Integer i)->"hello")
	                .match(new FileNotFoundException("test"))
	                .orElse("ok");


	        System.out.println("matched " + result);

	    }
	    @Test
	    public void patternMatch2() {

	    	
	        String result = new PatternMatcher()
	        		.inCaseOfValue(5,at(0),r-> "found "+r)
	        		.inCaseOfValue(10,at(0),r-> "found 10")
	        		.inCaseOfType(at(1),(FileNotFoundException e) -> "file not found")
	        		.inCaseOf(at(2),(Integer value)->value>1000,value -> "larger than 1000")
	        		.caseOf(at(2),(Integer value)->value>1000,System.out::println)
	                .<String>match(ImmutableList.of(10,Optional.empty(),999))
	                .orElse("ok");


	        System.out.println("matched " + result);

	    }
}
