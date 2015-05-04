package com.aol.cyclops.comprehensions;

import java.util.function.Function;

import com.aol.cyclops.comprehensions.notype.LessTypingForComprehension1;
import com.aol.cyclops.comprehensions.notype.LessTypingForComprehension2;
import com.aol.cyclops.comprehensions.notype.LessTypingForComprehension3;

public class ForComprehensions {

	
	public static <T,R> R foreach1(Function<LessTypingForComprehension1.Step1<T,R>,R> fn){
		return LessTypingForComprehension1.foreach(fn);
	}
	public static <T,R> R foreach2(Function<LessTypingForComprehension2.Step1<T,R>,R> fn){
		return LessTypingForComprehension2.foreach(fn);
	}
	public static <T,R> R foreach3(Function<LessTypingForComprehension3.Step1<T,R>,R> fn){
		return LessTypingForComprehension3.foreach(fn);
	}
}
