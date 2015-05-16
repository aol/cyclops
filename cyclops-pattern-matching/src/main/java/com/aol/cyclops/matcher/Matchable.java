package com.aol.cyclops.matcher;

import java.util.function.Function;

import com.aol.cyclops.matcher.builders.ElementCase;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher.builders.SimplestCase;
import com.aol.cyclops.matcher.builders._Case;


/**
 * Decomposable that is also matchable
 * 
 * @author johnmcclean
 *
 */
public interface Matchable{
	
	/**
	 * Match against this matchable using simple matching interface
	 * 
	 * {@code 
	 * return match(c -> 
							c.isType( (Put p) -> putRecord(p.key,p.value))
							.newCase().isType((Delete d) -> deleteRecord(d.key))
							.newCase().isType((Get g) -> getRecord(g.key))
					);
	 * }
	 * 
	 * @param fn Function to build the matching expression
	 * @return Matching result
	 */
	default <R,I> R match(Function<SimplestCase<I>,SimplestCase> fn){
		return (R)new MatchingInstance(fn.apply( new SimplestCase( new PatternMatcher()))).match(this).get();
	} //desired api for this should be this.match ( c-> caseOf( (Return r)-> doSomething(r)
		//										.caseOf( (Suspend s)-> doSomething(s));
	/**
	 * Match against this matchable using simple matching interface
	 * 
	 * {@code 
	 * return match(c -> 
							c.isType( (Put p) -> putRecord(p.key,p.value))
							.newCase().isType((Delete d) -> deleteRecord(d.key))
							.newCase().isType((Get g) -> getRecord(g.key)),
							noOperation()
					);
	 * }
	 * 
	 * @param fn Function to build the matching expression
	 * @param defaultValue Default value if matching expression does not match
	 * @return Matching result
	 */
	default <R,I> R match(Function<ElementCase<I>,MatchingInstance> fn,R defaultValue){
		return (R)fn.apply( Matching.newCase()).match(this).orElse(defaultValue);
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
		return (R)fn.apply( Matching._case()).match(this).get();
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
		return (R)fn.apply( Matching._case()).match(this).orElse(defaultValue);
	}
}
