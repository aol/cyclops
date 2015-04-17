package com.aol.cyclops.matcher.builders;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import com.aol.cyclops.matcher.PatternMatcher;

public class Matching {



	
	public static final <T,X> MatchingInstance<T,X> streamCase(Consumer<Case> consumer){
		StreamCase cse = new StreamCase(new PatternMatcher());
		consumer.accept(cse);
		return new MatchingInstance<T,X>(cse);
	}
	public static final<T,X> MatchingInstance<T,X> simpleCase(Consumer<AggregatedCase> consumer){
		AggregatedCase cse = new AggregatedCase(new PatternMatcher());
		consumer.accept(cse);
		return new MatchingInstance<T,X>(cse);
	}
	public static final<T,X> MatchingInstance<T,X> atomisedCase(Consumer<AtomisedCase> consumer){
		AtomisedCase cse = new AtomisedCase(new PatternMatcher());
		consumer.accept(cse);
		return new MatchingInstance<T,X>(cse);
	}

	
}
