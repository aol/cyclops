package com.aol.cyclops.matcher;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.val;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;



/**
 * PatternMatcher supports advanced pattern matching for Java 8
 * 
 * Features include
 * 
 * -cases match by value
 * -cases match by type
 * -cases using predicates
 *  		inCaseOfXXX
 *  		caseOfXXX
 * -cases using hamcrete Matchers
 * 			inMatchOfXXX
 * 			matchOfXXX
 * -cases as expressions (return value) - inCaseOfXXX, inMatchOfXXX
 * -cases as statements (no return value) - caseOfXXX, matchOfXXX
 * -pre & post variable extraction via Extractor (@see com.aol.cyclops.matcher.Extractors)
 * -match using iterables of predicates or hamcrest Matchers
 * 		- see caseOfIterable, matchOfIterable, inCaseOfIterable, matchOfIterable
 * -match using tuples of predicates or hamcreate Matchers
 * 	 	- see caseOfTuple, matchOfTuple, inCaseOfTuple, inMatchOfTuple
 * 
 * - single match (match method)
 * - match many (matchMany)
 * - match against a stream (single match, match many)
 * 
 * @author johnmcclean
 *
 */
@SuppressWarnings("unchecked")
public class PatternMatcher implements Function{
	
	private final Map<Pair<Predicate,Optional<Extractor>>,Pair<ActionWithReturn,Optional<Extractor>>> cases = new LinkedHashMap<>();

	/* 
	 *	@param t Object to match against
	 *	@return Value from matched case if present
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	public Object apply(Object t){
		return match(t).get();
	}
	
	/**
	 * Each input element can generated multiple matched values
	 * 
	 * @param s  Stream of data to match against (input to matcher)
	 * @return Stream of values from matched cases
	 */
	public<R> Stream<R> matchManyFromStream(Stream s){
		return s.flatMap(this::matchMany);
	}
	
	/**
	 * 
	 * @param t input to match against - can generate multiple values
	 * @return Stream of values from matched cases for the input
	 */
	public<R> Stream<R> matchMany(Object t) {
		return cases.entrySet().stream().flatMap(entry -> mapper(t, entry));
	}
	
	/**
	 * Each input element can generated a single matched value
	 * 
	 * @param s Stream of data to match against (input to matcher)
	 * @return Stream of matched values, one case per input value can match
	 */
	public <R> Stream<R> matchFromStream(Stream s){
		
		Stream<Optional<R>> results = s.<Optional<R>>map(this::match);
		return results.filter(Optional::isPresent).map(Optional::get);
	}
	
	/**
	 * @param t Object to match against supplied cases
	 * @return Value returned from matched case (if present) otherwise Optional.empty()
	 */
	public <R> Optional<R> match(Object t){
		
		Object[] result = {null};

		cases.forEach( (match, action) -> {
			if(result[0]==null){
				
				
				Object toUse = t;
				try{
					toUse = match.getSecond().map(ex -> {
								MethodType type = ex.getType();
								if(type.parameterCount()==0)
									return ex; //can't get parameter types for MethodReferences
								return type.parameterType(type.parameterCount() - 1).isAssignableFrom(t.getClass()) ? ex : Function.identity();
							}
					).orElse(x -> x).apply(t);
				}catch(ClassCastException e){ // MethodReferences will result in ClassCastExceptions

				}
				
				if(match.getFirst().test(toUse)){
					//assume post-extractor is type safe.
					result[0] = (R)action.getFirst().apply(action.getSecond().orElse((x) -> x).apply(toUse));
				}
			}
		});
		return Optional.ofNullable((R)result[0]);
	}


	
	/**
	 * Match by type specified in Extractor as input, if user provided type via match, matches the Action (Action extends Consumer)
	 * will be executed and provided with the result of the extraction.
	 * e.g.
	 * <pre>
	 * new PatternMatcher().caseOfType(Person::getAge, (Integer i) -&gt; value = i)
				.match(new Person(100));
	 * </pre>
	 * 
	 * This case will be triggered and the action will recieve the age of the Person (100).
	 * 
	 * 
	 * @param extractor will be used to extract a value from the user input to the matcher.
	 * @param a A consumer that will accept value from the extractor if user input matches the extractor input type
	 * @return self
	 * 
	 * (type V is not R to allow matching of V against R)
	 */
	public <R,T,X,V> PatternMatcher caseOfType( Extractor<T,R> extractor,Action<V> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		Predicate predicate = it -> it.getClass().isAssignableFrom(clazz);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(new ActionWithReturnWrapper(a),Optional.empty()));
		return this;
		
		
	}
	/**
	 * Match by specified value against the extracted value from user input. Data will only be extracted from user input if
	 * user input is of a type acceptable to the extractor
	 * 
	 * <pre>
	 * Matching.caseOfValue(100, Person::getAge, (Integer i) -&gt; value = i)
			.match(new Person(100));
	 * </pre>
	 * 
	 * This case will be triggered and the users age will be extracted, it matches 100 so the action will then be triggered.
	 * 
	 * 
	 * @param value Value to match against (via equals method)
	 * @param extractor will be used to extract a value from the user input to the matcher.
	 * @param a A consumer that will accept value from the extractor if user input matches the extractor input type
	 * @return
	 */
	public <R,V,T,X> PatternMatcher caseOfValue(R value, Extractor<T,R> extractor,Action<V> a){
		
		return inCaseOfValue(value,extractor,new ActionWithReturnWrapper(a));
	}
	public <V,X> PatternMatcher caseOfValue(V value,Action<V> a){
		
		caseOfThenExtract(it -> Objects.equals(it, value), a, null);
		return this;
	}
	public <V> PatternMatcher caseOfIterable(Iterable<Predicate<V>> predicates,Action<List<V>> a){
		
		Seq<Predicate<V>> pred = Seq.seq(predicates);
		
		
		caseOfThenExtract(it -> seq(it).zip(pred)
				.map(t -> t.v2.test((V)t.v1)).allMatch(v->v==true), a, null);
		return this;
	}
	public <V> PatternMatcher matchOfIterable(Iterable<Matcher> predicates,Action<List<V>> a){
		
		Seq<Matcher> pred = Seq.seq(predicates);
		
		
		matchOfThenExtract(new BaseMatcher(){

			@Override
			public boolean matches(Object item) {
				return seq(item).zip(pred)
						.map(t -> t.v2.matches((V)t.v1)).allMatch(v->v==true);
			}

			@Override
			public void describeTo(Description description) {
			
				
			}
			
		}, a, null);
		return this;
	}
	public <T,R,V,V1>  PatternMatcher matchOfMatchers(Tuple2<Matcher<V>,Matcher<V1>> predicates,
				Action<R> a,Extractor<T,R> extractor){
			
			Seq<Object> pred = Seq.seq(predicates);
			
			matchOfThenExtract(new BaseMatcher(){

				@Override
				public boolean matches(Object item) {
					return seq(item).zip(pred).map(t -> ((Matcher)t.v2).matches(t.v1)).allMatch(v->v==true);
				}

				@Override
				public void describeTo(Description description) {
				
					
				}
				
			}, a, extractor);
			return this;
	}
	public <T,R,V,V1> PatternMatcher caseOfPredicates(Tuple2<Predicate<V>,Predicate<V1>> predicates,
							Action<R> a,Extractor<T,R> extractor){
		
		Seq<Object> pred = Seq.seq(predicates);
		
		caseOfThenExtract(it -> seq(it).zip(pred).map(t -> ((Predicate)t.v2).test(t.v1)).allMatch(v->v==true), a, extractor);
		return this;
	}
	public <T,R> PatternMatcher caseOfTuple(Tuple predicates, Action<R> a,Extractor<T,R> extractor){

				Seq<Object> pred = Seq.seq(predicates);
				caseOfThenExtract(it -> seq(it).zip(pred).map(t -> ((Predicate)t.v2).test(t.v1)).allMatch(v->v==true), a, extractor);
				return this;
	}
	public <T,R> PatternMatcher matchOfTuple(Tuple predicates, Action<R> a,Extractor<T,R> extractor){

		Seq<Object> pred = Seq.seq(predicates);
		matchOfThenExtract(new BaseMatcher(){

			@Override
			public boolean matches(Object item) {
				return seq(item).zip(pred).map(t -> ((Matcher)t.v2).matches(t.v1)).allMatch(v->v==true);
			}

			@Override
			public void describeTo(Description description) {
			
				
			}
			
		}, a, extractor);
		return this;
}
	
	
	private Seq<Object> seq(Object t){
		if(t instanceof Iterable){
			return Seq.seq((Iterable)t);
		}
		if(t instanceof Stream){
			return Seq.seq((Stream)t);
		}
		if(t instanceof Iterator){
			return Seq.seq((Iterator)t);
		}
		if(t instanceof Map){
			return Seq.seq((Map)t);
		}
		return Seq.of(t);
	}
	
     public <V,X> PatternMatcher inCaseOfIterable(Iterable<Predicate<V>> predicates,ActionWithReturn<List<V>,X> a){
		
		Seq<Predicate<V>> pred = Seq.seq(predicates);
		
		
		inCaseOfThenExtract(it -> seq(it).zip(pred)
				.map(t -> t.v2.test((V)t.v1)).allMatch(v->v==true), a, null);
		return this;
	}
	public <V,X> PatternMatcher inMatchOfIterable(Iterable<Matcher> predicates,ActionWithReturn<List<V>,X> a){
		
		Seq<Matcher> pred = Seq.seq(predicates);
		
		
		inMatchOfThenExtract(new BaseMatcher(){

			@Override
			public boolean matches(Object item) {
				return seq(item).zip(pred)
						.map(t -> t.v2.matches((V)t.v1)).allMatch(v->v==true);
			}

			@Override
			public void describeTo(Description description) {
			
				
			}
			
		}, a, null);
		return this;
	}
	public <T,R,V,V1,X>  PatternMatcher inMatchOfMatchers(Tuple2<Matcher<V>,Matcher<V1>> predicates,
				ActionWithReturn<R,X> a,Extractor<T,R> extractor){
			
			Seq<Object> pred = Seq.seq(predicates);
			
			inMatchOfThenExtract(new BaseMatcher(){

				@Override
				public boolean matches(Object item) {
					return seq(item).zip(pred).map(t -> ((Matcher)t.v2).matches(t.v1)).allMatch(v->v==true);
				}

				@Override
				public void describeTo(Description description) {
				
					
				}
				
			}, a, extractor);
			return this;
	}
	public <T,R,V,V1,X> PatternMatcher inCaseOfPredicates(Tuple2<Predicate<V>,Predicate<V1>> predicates,
							ActionWithReturn<R,X> a,Extractor<T,R> extractor){
		
		Seq<Object> pred = Seq.seq(predicates);
		
		inCaseOfThenExtract(it -> seq(it).zip(pred).map(t -> ((Predicate)t.v2).test(t.v1)).allMatch(v->v==true), a, extractor);
		return this;
	}
	public <T,R,X> PatternMatcher inCaseOfTuple(Tuple predicates, ActionWithReturn<R,X> a,Extractor<T,R> extractor){

				Seq<Object> pred = Seq.seq(predicates);
				inCaseOfThenExtract(it -> seq(it).zip(pred).map(t -> ((Predicate)t.v2).test(t.v1)).allMatch(v->v==true), a, extractor);
				return this;
	}
	public <T,R,X> PatternMatcher inMatchOfTuple(Tuple predicates, ActionWithReturn<R,X> a,Extractor<T,R> extractor){

		Seq<Object> pred = Seq.seq(predicates);
		inMatchOfThenExtract(new BaseMatcher(){

			@Override
			public boolean matches(Object item) {
				return seq(item).zip(pred).map(t -> ((Matcher)t.v2).matches(t.v1)).allMatch(v->v==true);
			}

			@Override
			public void describeTo(Description description) {
			
				
			}
			
		}, a, extractor);
		return this;
}
	
	public <V,X> PatternMatcher caseOfType(Action<V> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		caseOfThenExtract(it -> it.getClass().isAssignableFrom(clazz), a, null);
		return this;
	}
	public <V> PatternMatcher matchOf(Matcher<V> match,Action<V> a){
		inCaseOfThenExtract(it->match.matches(it), new ActionWithReturnWrapper(a), null);
		return this;
	}
	public <V> PatternMatcher caseOf(Predicate<V> match,Action<V> a){
		inCaseOfThenExtract(match, new ActionWithReturnWrapper(a), null);
		return this;
	}
	public <R,V,T> PatternMatcher caseOfThenExtract(Predicate<V> match,Action<R> a, Extractor<T,R> extractor){
		
		cases.put(new Pair(match, Optional.empty()), new Pair<ActionWithReturn, Optional<Extractor>>(new ActionWithReturnWrapper(a), Optional.ofNullable(extractor)));
		return this;
	}
	public <R,V,T> PatternMatcher matchOfThenExtract(Matcher<V> match,Action<V> a, Extractor<T,R> extractor){
		Predicate<V> predicate = it->match.matches(it);
		cases.put(new Pair(predicate, Optional.empty()), new Pair<ActionWithReturn, Optional<Extractor>>(new ActionWithReturnWrapper(a), Optional.ofNullable(extractor)));
		return this;
	}
	public <R,V,T> PatternMatcher caseOf( Extractor<T,R> extractor,Predicate<R> match,Action<V> a){
		
		cases.put(new Pair(match,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(new ActionWithReturnWrapper(a),Optional.empty()));
		return this;
	}
	public <R,V,T> PatternMatcher matchOf( Extractor<T,R> extractor,Matcher<R> match,Action<V> a){
		Predicate<V> predicate = it->match.matches(it);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(new ActionWithReturnWrapper(a),Optional.empty()));
		return this;
	}
	public <V,X> PatternMatcher inCaseOfValue(V value,ActionWithReturn<V,X> a){
		
		inCaseOfThenExtract(it -> Objects.equals(it, value), a, null);
		return this;
	}
	public <V,X> PatternMatcher inCaseOfType(ActionWithReturn<V,X> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		inCaseOfThenExtract(it -> it.getClass().isAssignableFrom(clazz), a, null);
		return this;
	}
	public <V,X> PatternMatcher inCaseOf(Predicate<V> match,ActionWithReturn<V,X> a){
		inCaseOfThenExtract(match, a, null);
		return this;
	}
	public <R,T,X> PatternMatcher inCaseOfThenExtract(Predicate<T> match,ActionWithReturn<R,X> a, Extractor<T,R> extractor){
		
		cases.put(new Pair(match,Optional.empty()),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.ofNullable(extractor)));
		return this;
	}
	
	
	public <R,V,T,X> PatternMatcher inCaseOf( Extractor<T,R> extractor,Predicate<V> match,ActionWithReturn<V,X> a){
		
		cases.put(new Pair(match,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.empty()));
		return this;
	}
	
	public <R,V,T,X> PatternMatcher inCaseOfType( Extractor<T,R> extractor,ActionWithReturn<V,X> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		Predicate predicate = it -> it.getClass().isAssignableFrom(clazz);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.empty()));
		return this;
	}
	public <R,V,T,X> PatternMatcher inCaseOfValue(R value, Extractor<T,R> extractor,ActionWithReturn<V,X> a){
		
		Predicate predicate = it -> Objects.equals(it, value);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.empty()));
		return this;
	}
	
	
	/**hamcrest **/
	public <V,X> PatternMatcher inMatchOf(Matcher<V> match,ActionWithReturn<V,X> a){
		Predicate<V> predicate = it->match.matches(it);
		inCaseOfThenExtract(predicate, a, null);
		return this;
	}
	public <R,T,X> PatternMatcher inMatchOfThenExtract(Matcher<T> match,ActionWithReturn<R,X> a, Extractor<T,R> extractor){
		Predicate<T> predicate = it->match.matches(it);
		cases.put(new Pair(predicate,Optional.empty()),
					new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.ofNullable(extractor)));
		return this;
	}
	
	
	public <R,V,T,X> PatternMatcher inMatchOf( Extractor<T,R> extractor,Matcher<V> match,ActionWithReturn<V,X> a){
		Predicate<V> predicate = it->match.matches(it);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.empty()));
		return this;
	}
	

	
	public static interface Extractor<T,R> extends Function<T,R>, Serializable {
		public R apply(T t);
		default MethodType getType(){
			return LambdaTypeExtractor.extractType(this);
		}
	}
	
	public static class ActionWithReturnWrapper<T,X> implements ActionWithReturn<T,X>{
		private final Action<T> action;
		ActionWithReturnWrapper(Action<T> action){
			this.action = action;
		}
		public X apply(T t){
			action.accept(t);
			return null;
		}
	}
	
	public static interface ActionWithReturn<T,X> extends Function<T,X>, Serializable {
		public X apply(T t);
		default MethodType getType(){
			return LambdaTypeExtractor.extractType(this);
		}
	}
	public static interface Action<T> extends Consumer<T>, Serializable {
		
		public void accept(T t);
		
		default MethodType getType(){
			return LambdaTypeExtractor.extractType(this);
		}
	}
	
	
	private <R> Stream<R> mapper(Object t,Map.Entry<Pair<Predicate,Optional<Extractor>>,Pair<ActionWithReturn,Optional<Extractor>>> entry){
		Pair<Predicate,Optional<Extractor>> match = entry.getKey();
		Pair<ActionWithReturn,Optional<Extractor>> action = entry.getValue();
		List<R> results = new ArrayList<>();
			
				
				
				Object toUse = t;
		try{
				match.getSecond().map(ex -> {
						MethodType type = ex.getType();
						if(type.parameterCount()==0)
							return ex; //can't get parameter types for MethodReferences
						return type.parameterType(type.parameterCount() - 1).isAssignableFrom(t.getClass()) ? ex : Function.identity();
					}
			).orElse(x -> x).apply(t);
		}catch(ClassCastException e){ //can't get parameter types for MethodReferences (pre-extractors)

		}
				
				if(match.getFirst().test(toUse)){
					//assume post extract is type safe
					results.add((R)action.getFirst().apply(action.getSecond().orElse((x) -> x).apply(toUse)));
				}
			
		return results.stream();
	}
	
	
}
