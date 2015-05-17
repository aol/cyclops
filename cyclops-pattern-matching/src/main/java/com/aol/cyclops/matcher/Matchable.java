package com.aol.cyclops.matcher;

import java.util.function.Function;

import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher.builders.SimplestCase;
import com.aol.cyclops.matcher.builders._Case;


/**
 * Matchable
 * 
 * todo - add AsMatchable.asMatchable
 * 
 * @author johnmcclean
 *
 */
public interface Matchable{
	
	
	/**
	 * @return matchable
	 */
	default Object getMatchable(){
		return this;
	}
	
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
	default <R> R match(Function<SimplestCase<? super R>,SimplestCase<? super R>> fn){
		return new MatchingInstance<Object,R>(fn.apply( new SimplestCase<>( new PatternMatcher()))).match(getMatchable()).get();
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
	default <R> R match(Function<SimplestCase<? super R>,SimplestCase<? super R>> fn,R defaultValue){
		return new MatchingInstance<Object,R>(fn.apply( new SimplestCase<>( new PatternMatcher()))).match(getMatchable()).orElse(defaultValue);
	}
	/**
	 * Match against this matchable using algebraic matching interface (each field can
	 * be matched individually).
	 * 
	 * 
	 * @param fn Function to build the matching expression
	 * @return Matching result
	 */
	default <R,I> R _match(Function<_Case<I>,MatchingInstance> fn){
		return (R)fn.apply( Matching._case()).match(getMatchable()).get();
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
	default <R,I> R _match(Function<_Case<I>,MatchingInstance> fn,R defaultValue){
		return (R)fn.apply( Matching._case()).match(getMatchable()).orElse(defaultValue);
	}
}
