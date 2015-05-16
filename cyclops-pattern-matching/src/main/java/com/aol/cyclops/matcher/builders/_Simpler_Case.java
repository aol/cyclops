package com.aol.cyclops.matcher.builders;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import org.jooq.lambda.Seq;

import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.Predicates;
@AllArgsConstructor
public class _Simpler_Case<X> extends CaseBeingBuilt {
	@Getter(AccessLevel.PACKAGE)
	@Wither(AccessLevel.PACKAGE)
	private final PatternMatcher patternMatcher;
	
	public  <T> _MembersMatchBuilder<X,T> withType(Class<T> type){
		return new _MembersMatchBuilder(type,this);
		
		
	}
	
	
	
}
