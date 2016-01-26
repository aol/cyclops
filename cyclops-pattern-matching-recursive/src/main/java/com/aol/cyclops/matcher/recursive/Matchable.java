package com.aol.cyclops.matcher.recursive;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.matcher.builders.CheckType;
import com.aol.cyclops.matcher.builders.CheckTypeAndValues;
import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher.builders.RecursiveMatcherInstance;
import com.aol.cyclops.matcher.builders._Simpler_Case;
import com.aol.cyclops.objects.Decomposable;


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
	 * Match against the values inside the matchable with a single case
	 * 
	 * <pre>
	 * {@code
	 * int result = Matchable.of(Optional.of(1))
								.matches(c->c.hasValues(1).then(i->2));
		//2						
	 * }</pre>
	 * 
	 * Note, it is possible to continue to chain cases within a single case, but cleaner
	 * to use the appropriate overloaded matches method that accepts two (or more) cases.
	 * 
	 * @param fn1 Describes the matching case
	 * @return Result - this method requires a match or an NoSuchElement exception is thrown
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R  matches(Function<CheckValues<? super T,R>,CheckValues<? super T, R>> fn1){
		return (R) new MatchingInstance(new _Simpler_Case( fn1.apply( (CheckValues)
				new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable()).get();
	}
	/**
	 * Match against the values inside the matchable with two cases
	 * 
	 * <pre>
	 * {@code 
	 * int result = Matchable.listOfValues(1,2)
								.matches(c->c.hasValues(1,3).then(i->2),
										c->c.hasValues(1,2).then(i->3));
										
		//3								
	 * 
	 * }
	 * </pre>
	 *  Note, it is possible to continue to chain cases within a single case, but cleaner
	 * to use the appropriate overloaded matches method that accepts three (or more) cases.
	 * 
	 * @param fn1 Describes a case
	 * @param fn2 Describes a case
	 * @return Result - this method requires a match or an NoSuchElement exception is thrown
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R matches(Function<CheckValues<? super T,R>,CheckValues<? super T, R>> fn1,
								Function<CheckValues<? super T,R>,CheckValues<? super T, R>> fn2){
		
		return  (R) new MatchingInstance(new _Simpler_Case( fn1.compose(fn2).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R  matches(Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1,Function<CheckValues<? super T,R>,
							CheckValues<? super T,R>> fn2,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3){
		
		return  (R)new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3)).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R  matches(Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
											Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3,
											Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn4){
		
		return (R) new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3).compose(fn4)).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R  matches(Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1,
									Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
									Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3,
									Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn4,
									Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn5){

			return (R)new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3).compose(fn4).compose(fn5)).apply( (CheckValues)
			new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
			.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R>  mayMatch(Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1){
		return  new MatchingInstance(new _Simpler_Case( fn1.apply( (CheckValues)
				new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R> mayMatch(Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1,
											Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2){
		
		return  new MatchingInstance(new _Simpler_Case( fn1.compose(fn2).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R>  mayMatch(Function<CheckValues<? super T,R>,CheckValues<? super T, R>> fn1,Function<CheckValues<? super T,R>,
							CheckValues<? super T,R>> fn2,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3){
		
		return  new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3)).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R>  mayMatch(Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1,
											Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
											Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3,
											Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn4){
		
		return  new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3).compose(fn4)).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R>  mayMatch(Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1,
										Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
										Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3,
										Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn4,
										Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn5){

			return new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3).compose(fn4).compose(fn5)).apply( (CheckValues)
			new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
			.match(getMatchable());
	}
	
	
	
	
	
	/**
	 * Create a new matchable that will match on the fields of the provided Object
	 * 
	 * @param o Object to match on it's fields
	 * @return new Matchable
	 */
	public static Matchable of(Object o){
		if(o instanceof Stream)
			return ofStream((Stream)o);
		return AsMatchable.asMatchable(o);
	}
	/**
	 * Create a new matchable that will match on the fields of the provided Stream
	 * 
	 * @param o Object to match on it's fields
	 * @return new Matchable
	 */
	public static <T> Matchable ofStream(Stream<T> o){
		return AsMatchable.asMatchable(o.collect(Collectors.toList()));
	}
	/**
	 * Create a new matchable that will match on the fields of the provided Decomposable
	 * 
	 * @param o Decomposable to match on it's fields
	 * @return new Matchable
	 */
	public static  Matchable ofDecomposable(Decomposable o){
		return AsMatchable.asMatchable(o);
	}
	
	/**
	 * Create a matchable that matches on the provided Objects
	 * 
	 * @param o Objects to match on
	 * @return new Matchable
	 */
	public static  Matchable listOfValues(Object... o){
		return AsMatchable.asMatchable(Arrays.asList(o));
	}
}
