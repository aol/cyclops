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
	default <T,R> R  matches(Function<CheckValues<T,R>,CheckValues<T,R>> fn1){
		return (R) new MatchingInstance(new _Simpler_Case( fn1.apply( (CheckValues)
				new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R matches(Function<CheckValues<T,R>,CheckValues<T,R>> fn1,Function<CheckValues<T,R>,CheckValues<T,R>> fn2){
		
		return  (R) new MatchingInstance(new _Simpler_Case( fn1.compose(fn2).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R  matches(Function<CheckValues<T,R>,CheckValues<T,R>> fn1,Function<CheckValues<T,R>,
							CheckValues<T,R>> fn2,Function<CheckValues<T,R>,CheckValues<T,R>> fn3){
		
		return  (R)new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3)).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R  matches(Function<CheckValues<T,R>,CheckValues<T,R>> fn1,Function<CheckValues<T,R>,CheckValues<T,R>> fn2,
											Function<CheckValues<T,R>,CheckValues<T,R>> fn3,
											Function<CheckValues<T,R>,CheckValues<T,R>> fn4){
		
		return (R) new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3).compose(fn4)).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> R  matches(Function<CheckValues<T,R>,CheckValues<T,R>> fn1,Function<CheckValues<T,R>,CheckValues<T,R>> fn2,Function<CheckValues<T,R>,CheckValues<T,R>> fn3,
			Function<CheckValues<T,R>,CheckValues<T,R>> fn4,Function<CheckValues<T,R>,CheckValues<T,R>> fn5){

			return (R)new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3).compose(fn4).compose(fn5)).apply( (CheckValues)
			new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
			.match(getMatchable()).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R>  mayMatch(Function<CheckValues<T,R>,CheckValues<T,R>> fn1){
		return  new MatchingInstance(new _Simpler_Case( fn1.apply( (CheckValues)
				new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R> mayMatch(Function<CheckValues<T,R>,CheckValues<T,R>> fn1,Function<CheckValues<T,R>,CheckValues<T,R>> fn2){
		
		return  new MatchingInstance(new _Simpler_Case( fn1.compose(fn2).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R>  mayMatch(Function<CheckValues<T,R>,CheckValues<T,R>> fn1,Function<CheckValues<T,R>,
							CheckValues<T,R>> fn2,Function<CheckValues<T,R>,CheckValues<T,R>> fn3){
		
		return  new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3)).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R>  mayMatch(Function<CheckValues<T,R>,CheckValues<T,R>> fn1,Function<CheckValues<T,R>,CheckValues<T,R>> fn2,
											Function<CheckValues<T,R>,CheckValues<T,R>> fn3,
											Function<CheckValues<T,R>,CheckValues<T,R>> fn4){
		
		return  new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3).compose(fn4)).apply( (CheckValues)
					new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
						.match(getMatchable());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T,R> Optional<R>  mayMatch(Function<CheckValues<T,R>,CheckValues<T,R>> fn1,Function<CheckValues<T,R>,CheckValues<T,R>> fn2,Function<CheckValues<T,R>,CheckValues<T,R>> fn3,
			Function<CheckValues<T,R>,CheckValues<T,R>> fn4,Function<CheckValues<T,R>,CheckValues<T,R>> fn5){

			return new MatchingInstance(new _Simpler_Case( fn1.compose(fn2.compose(fn3).compose(fn4).compose(fn5)).apply( (CheckValues)
			new _Simpler_Case(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
			.match(getMatchable());
	}
	
	/**
	 * Match against this matchable using simple matching interface
	 * 
	 * <pre>{@code 
	 * return match(c -> 
						c.caseOf( (Put p) -> new Put(p.key,p.value,(Action)fn.apply(p.next)))
						 .caseOf((Delete d) -> new Delete(d.key,(Action)fn.apply(d.next)))
						 .caseOf((Get g) -> new Get(g.key,(Function)g.next.andThen(fn)))
					);
	 * }</pre>
	 * 
	 * @param fn Function to build the matching expression
	 * @return Matching result
	 */
	default <R> R matchType(Function<CheckType<? super R>,CheckType<? super R>> fn){
		return new MatchingInstance<Object,R>(fn.apply( new CheckType<>( new PatternMatcher()))).match(getMatchable()).get();
	} 
	/**
	 * Match against this matchable using simple matching interface
	 * 
	 * <pre>{@code 
	 * return match(c -> 
						 c.caseOf( (Put p) -> new Put(p.key,p.value,(Action)fn.apply(p.next)))
						 .caseOf((Delete d) -> new Delete(d.key,(Action)fn.apply(d.next)))
						 .caseOf((Get g) -> new Get(g.key,(Function)g.next.andThen(fn)))
						 noOperation()
					);
	 * }</pre>
	 * 
	 * @param fn Function to build the matching expression
	 * @param defaultValue Default value if matching expression does not match
	 * @return Matching result
	 */
	default <R> R matchType(Function<CheckType<? super R>,CheckType<? super R>> fn,R defaultValue){
		return new MatchingInstance<Object,R>(fn.apply( new CheckType<>( new PatternMatcher()))).match(getMatchable()).orElse(defaultValue);
	}
	
	
	
	
	/**
	 * Create a new matchable that will match on the fields of the provided Object
	 * 
	 * @param o Object to match on it's fields
	 * @return new Matchable
	 */
	public static Matchable of(Object o){
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
