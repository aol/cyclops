package com.aol.cyclops.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;
import com.aol.cyclops.matcher.ScalaParserExample.X;

public class Builder {

	public static Cases newCase(){
		return new Cases(new ArrayList<>());
	}
	@Wither
	@AllArgsConstructor
	public static class Cases{
		List<CaseBuilder> cases;
		public<T> PredicateBuilder<T> of(Predicate<T> p){
			CaseBuilder next = new CaseBuilder(null,null,null,null);
			
			return new PredicateBuilder<T>(next.withPredicate(p),this);
		}
		public <R> Optional<R> match(Object t){
			return new PatternMatcher(cases).match(t);
		}
	}
	
	@Wither
	@AllArgsConstructor
	public static class CaseBuilder<T>{
		@Getter(AccessLevel.PACKAGE)
		Predicate predicate;
		Extractor preExtractor;
		ActionWithReturn action;
		Extractor postExtractor;
		
		
	}
	@Wither
	@AllArgsConstructor
	public static class PredicateBuilder<T>{
		CaseBuilder<T> builder;
		Cases cases;
		
		public PredicateBuilder<T> and(Predicate<T> p){
			Predicate<T> newPredicate = v-> builder.getPredicate().test(v) &&  p.test(v);
			return this.withBuilder(builder.withPredicate(newPredicate));
		}
		public PredicateBuilder<T> or(Predicate<T> p){
			Predicate<T> newPredicate = v-> builder.getPredicate().test(v) ||  p.test(v);
			return this.withBuilder(builder.withPredicate(newPredicate));
		}
		public PredicateBuilder<T> andMatches(Matcher<T> p){
			Predicate<T> newPredicate = v-> builder.getPredicate().test(v) &&  p.matches(v);
			return this.withBuilder(builder.withPredicate(newPredicate));
		}
		public PredicateBuilder<T> orMatches(Matcher<T> p){
			Predicate<T> newPredicate = v-> builder.getPredicate().test(v) ||  p.matches(v);
			return this.withBuilder(builder.withPredicate(newPredicate));
		}
		public PredicateBuilder<T> andValue(T p){
			Predicate<T> newPredicate = v-> builder.getPredicate().test(v) && Objects.equals(p, v);
			return this.withBuilder(builder.withPredicate(newPredicate));
		}
		public PredicateBuilder<T> orValue(T p){
			Predicate<T> newPredicate = v-> builder.getPredicate().test(v) ||  Objects.equals(p, v);
			return this.withBuilder(builder.withPredicate(newPredicate));
		}
		
		public <R> ActionBuilder<R> extract(Extractor<T,R> postExtractor){
			return new ActionBuilder(builder.withPostExtractor(postExtractor));
		}
	}
	
	@Wither
	@AllArgsConstructor
	public static class ActionBuilder<T>{
		CaseBuilder<T> builder;
		Cases cases;
		public <X> Cases thenApply(ActionWithReturn<T,X> action){
			cases.cases.add(builder);
			builder.withAction(action);
			return cases;
		}
	}
	
	
	
}
