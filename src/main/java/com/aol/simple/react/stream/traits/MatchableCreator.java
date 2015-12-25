package com.aol.simple.react.stream.traits;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.matcher.recursive.Matchable;

public class MatchableCreator {

	public static <U> BiFunction<LazyFutureStream<U>,U,Matchable> matchable(){
		
		return (lfs,u)->{ 
			System.out.println(lfs.isDecomposePatternMatching()); 
		return  lfs.isDecomposePatternMatching()? Matchable.of(u):Matchable.listOfValues(u);
				};
	}
}
