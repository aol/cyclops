package com.aol.cyclops.matcher.builders;

import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.matcher.PatternMatcher;

public class Matching {

	
	public static final <T,X> MatchingInstance<T,X> streamCase(Function<Case,MatchingInstance<T,X>> fn){
		StreamCase cse = new StreamCase(new PatternMatcher());
		return fn.apply(cse);
		
	}
	public static final<X> MatchingInstance<? extends Object,X> newCase(Function<AggregatedCase<X>,MatchingInstance<? extends Object,X>>fn){
		AggregatedCase<X> cse = new AggregatedCase(new PatternMatcher());
		return fn.apply(cse);
		
	}
	public static final<X> MatchingInstance<? extends Object,X> _case(Function<_Case<? extends Object>,MatchingInstance<? extends Object,X>> fn){
		_Case cse = new _Case(new PatternMatcher());
		return fn.apply(cse);
		
	}
	public static final<X> MatchingInstance<? extends Object,X> atomisedCase(Function<AtomisedCase<? extends Object>,MatchingInstance<? extends Object,X>> fn){
		AtomisedCase cse = new AtomisedCase(new PatternMatcher());
		return fn.apply(cse);
		
	}

	
	public static final  StreamCase streamCase(){
		StreamCase cse = new StreamCase(new PatternMatcher());
		return cse;
	}
	public static final<USER_VALUE> AtomisedCase<USER_VALUE> atomisedCase(){
		AtomisedCase cse = new AtomisedCase(new PatternMatcher());
		return cse;
	}
	public static final<USER_VALUE> _Case<USER_VALUE> _case(){
		_Case cse = new  _Case(new PatternMatcher());
		return cse;
	}
	public static final<X> AggregatedCase<X> newCase(){
		AggregatedCase<X> cse = new AggregatedCase<>(new PatternMatcher());
		return cse;
	}
	
}
