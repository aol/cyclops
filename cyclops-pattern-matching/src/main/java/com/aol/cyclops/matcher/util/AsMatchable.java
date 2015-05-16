package com.aol.cyclops.matcher.util;

import java.util.Objects;
import java.util.function.Function;

import lombok.AllArgsConstructor;

import com.aol.cyclops.matcher.Matchable;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher.builders.SimplestCase;
import com.aol.cyclops.matcher.builders._Case;

public class AsMatchable {
	public static <T> Matchable asMatchable(T toCoerce){
		return new CoercedMatchable<T>(toCoerce);
	}
	@AllArgsConstructor
	public static class CoercedMatchable<T> implements Matchable{
		private final T coerced;
		
		

		/**
		 * Match against this matchable using simple matching interface
		 * 
		 * {@code 
		 * return match(c -> 
							c.caseOf( (Put p) -> new Put(p.key,p.value,(Action)fn.apply(p.next)))
							 .caseOf((Delete d) -> new Delete(d.key,(Action)fn.apply(d.next)))
							 .caseOf((Get g) -> new Get(g.key,(Function)g.next.andThen(fn)))
						);
		 * }
		 * 
		 * @param fn Function to build the matching expression
		 * @return Matching result
		 */
		@Override
		public <R> R match(Function<SimplestCase<? super R>,SimplestCase<? super R>> fn){
			return new MatchingInstance<Object,R>(fn.apply( new SimplestCase<>( new PatternMatcher()))).match(coerced).get();
		} 
		/**
		 * Match against this matchable using simple matching interface
		 * 
		 * {@code 
		 * return match(c -> 
							 c.caseOf( (Put p) -> new Put(p.key,p.value,(Action)fn.apply(p.next)))
							 .caseOf((Delete d) -> new Delete(d.key,(Action)fn.apply(d.next)))
							 .caseOf((Get g) -> new Get(g.key,(Function)g.next.andThen(fn)))
							 noOperation()
						);
		 * }
		 * 
		 * @param fn Function to build the matching expression
		 * @param defaultValue Default value if matching expression does not match
		 * @return Matching result
		 */
		@Override
		public <R> R match(Function<SimplestCase<? super R>,SimplestCase<? super R>> fn,R defaultValue){
			return new MatchingInstance<Object,R>(fn.apply( new SimplestCase<>( new PatternMatcher()))).match(coerced).orElse(defaultValue);
		}
		/**
		 * Match against this matchable using algebraic matching interface (each field can
		 * be matched individually).
		 * 
		 * 
		 * @param fn Function to build the matching expression
		 * @return Matching result
		 */
		@Override
		public<R,I> R _match(Function<_Case<I>,MatchingInstance> fn){
			return (R)fn.apply( Matching._case()).match(coerced).get();
		}
		/**
		 * Match against this matchable using algebraic matching interface (each field can
		 * be matched individually).
		 * 
		 * 
		 * @param fn Function to build the matching expression
		 * @param defaultValue Default value if matching expression does not match
		 * @return Matching result
		 */
		public <R,I> R _match(Function<_Case<I>,MatchingInstance> fn,R defaultValue){
			return (R)fn.apply( Matching._case()).match(coerced).orElse(defaultValue);
		}
		@Override
		public int hashCode() {
			return coerced.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return Objects.equals(coerced, obj);
			
		}
		public T unwrap(){
			return coerced;
		}

	
	}
}
