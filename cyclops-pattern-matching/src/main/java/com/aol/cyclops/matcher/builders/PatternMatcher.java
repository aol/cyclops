package com.aol.cyclops.matcher.builders;

import static com.aol.cyclops.matcher.builders.SeqUtils.seq;

import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import lombok.experimental.Wither;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.matcher.Action;
import com.aol.cyclops.matcher.ActionWithReturn;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.ChainOfResponsibility;
import com.aol.cyclops.matcher.Decomposable;
import com.aol.cyclops.matcher.Extractor;
import com.aol.cyclops.matcher.Extractors;



/**
 * PatternMatcher supports advanced pattern matching for Java 8
 * 
 * This is an API for creating Case instances and allows new type definitions to be supplied for each Case
 * 
 * Features include
 * 
 * -cases match by value
 * -cases match by type
 * -cases using predicates
 *  		inCaseOfXXX
 *  		caseOfXXX
 * -cases using hamcrest Matchers
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
@AllArgsConstructor
public class PatternMatcher implements Function{
	
	@Wither @Getter
	private final Cases cases;
	
	public PatternMatcher(){
		cases = Cases.of();
	}
	/**
	 * @return Pattern Matcher as function that will return the 'unwrapped' result when apply is called.
	 *  i.e. Optional#get will be called.
	 * 
	 */
	public <T,X> Function<T,X> asUnwrappedFunction(){
		return cases.asUnwrappedFunction();
	}
	/**
	 * @return Pattern Matcher as a function that will return a Stream of results
	 */
	public <T,X> Function<T,Stream<X>> asStreamFunction(){
		
		return	cases.asStreamFunction();
	}
	
	
	
	
	/* 
	 *	@param t Object to match against
	 *	@return Value from matched case if present
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	public Optional<Object> apply(Object t){
		return match(t);
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
		return cases.matchMany(t);
		
	}
	
	/**
	 * Each input element can generated a single matched value
	 * 
	 * @param s Stream of data to match against (input to matcher)
	 * @return Stream of matched values, one case per input value can match
	 */
	public <R> Stream<R> matchFromStream(Stream s){
		
		return cases.matchFromStream(s);
	}
	/**
	 * Aggregates supplied objects into a List for matching against
	 * 
	 * 
	 * @param t Array to match on
	 * @return Matched value wrapped in Optional
	 */
	public <R> Optional<R> match(Object... t){
		return cases.match(t);
	}
	/**
	 * Decomposes the supplied input via it's unapply method
	 * Provides a List to the Matcher of values to match on
	 * 
	 * @param t Object to decompose and match on
	 * @return Matched result wrapped in an Optional
	 */
	public <R> Optional<R> unapply(Decomposable t){
		return cases.unapply(t);
	}
	/**
	 * @param t Object to match against supplied cases
	 * @return Value returned from matched case (if present) otherwise Optional.empty()
	 */
	public <R> Optional<R> match(Object t){
			
		return cases.match(t);
		
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
	public <R,T,X,V> PatternMatcher caseOfType( Extractor<T,R> ext,Action<V> a){
		val extractor = Extractors.memoised(ext);
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		Predicate predicate = extractorPredicate(extractor,it -> it.getClass().isAssignableFrom(clazz));
		return this.withCases(cases.append( index(),Case.of(predicate,extractorAction(extractor,new ActionWithReturnWrapper(a)))));
		
	}
	
	Object extractIfType(Object t,Extractor extractor){
		try{
			MethodType type = extractor.getType();
			if(type.parameterCount()==0)
				return t; //can't get parameter types for MethodReferences
			return type.parameterType(type.parameterCount() - 1).isAssignableFrom(t.getClass()) ? extractor.apply(t) : t;
	
		}catch(ClassCastException e){ // MethodReferences will result in ClassCastExceptions

		}
		return t;
	}
		
	Predicate extractorPredicate(Extractor extractor, Predicate p){
		if(extractor ==null)
			return p;
		
			
		return t -> p.test(extractIfType(t,extractor));
	}
	ActionWithReturn extractorAction(Extractor extractor, ActionWithReturn action){
		if(extractor==null)
			return action;
		return input -> action.apply(extractor.apply(input));
	}
	/**
	 * Match by specified value against the extracted value from user input. Data will only be extracted from user input if
	 * user input is of a type acceptable to the extractor
	 * 
	 * <pre>
	 * new PatternMatcher.caseOfValue(100, Person::getAge, (Integer i) -&gt; value = i)
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
	
	/**
	 * Match by specified value and then execute supplied action.
	 * 
	 * <pre>
	 *  new PatternMatcher.caseOfValue(100, (Person p) -&gt; value = p.getAge())
			   .match(new Person(100));
	 * </pre>
	 * 
	 * @param value to compare against, if Objects.equals(value,matching-input) is true, action is triggered
	 * @param a Action to be consumed (no return value)
	 * @return new PatternMatcher
	 */
	public <V,X> PatternMatcher caseOfValue(V value,Action<V> a){
		
		return caseOfThenExtract(it -> Objects.equals(it, value), a, null);
		
	}
	/**
	 * Match against an iterable using supplied predicates. Each predicate will be tested a against a different sequential element in the user
	 * supplied iterable. e.g. 
	 * 
	 * <pre>
	 * 	new PatternMatcher()
	 *		.caseOfMany((List&lt;String&gt; list) -&gt; language  = list.get(1) ,
	 *							v -&gt; v.equals(&quot;-l&quot;) || v.equals(&quot;---lang&quot;),v-&gt;true)
	 *		.match(asList(&quot;-l&quot;,&quot;java&quot;));
	 *	
	 *	assertThat(language,is(&quot;java&quot;));
	 * </pre>
	 * 
	 * @param a Action to execute if case passes
	 * @param predicates To match against each sequential element in the iterable
	 * @return New PatternMatcher
	 */
	@SafeVarargs
	public final <V> PatternMatcher caseOfMany(Action<List<V>> a,Predicate<V>... predicates){
		
		Seq<Predicate<V>> pred = Seq.of(predicates);
		
		
		return caseOfThenExtract(it -> seq(it).zip(pred)
				.map(t -> t.v2.test((V)t.v1)).allMatch(v->v==true), a, this::wrapInList);
		
	}
	/**
	 * Match against an iterable using supplied hamcrest matchers. Each matcher will be tested a against a different sequential element in the user
	 * supplied iterable. e.g.
	 * 
	 * <pre>
	 * new PatternMatcher()
	 *	.matchOfMany( (List&lt;String&gt; list) -&gt; language  = list.get(1) ,
	 *						equalTo(&quot;-l&quot;),any(String.class))
	 *		.match(asList(&quot;-l&quot;,&quot;java&quot;));
	 *	
	 *	assertThat(language,is(&quot;java&quot;));
	 * 
	 * </pre>
	 * 
	 * @param a Action to execute if case passes
	 * @param predicates To match against each sequential element in the iterable
	 * @return New PatternMatcher
	 */
	@SafeVarargs
	public final <V> PatternMatcher matchOfMany(Action<List<V>> a,Matcher<V>... predicates){
		
		Seq<Matcher<V>> pred = Seq.of(predicates);
		
		
		return matchOfThenExtract(new BaseMatcher(){

			@Override
			public boolean matches(Object item) {
				return seq(item).zip(pred)
						.map(t -> t.v2.matches((V)t.v1)).allMatch(v->v==true);
			}

			@Override
			public void describeTo(Description description) {
			
				
			}
			
		}, a, this::wrapInList);
		
	}
	/**
	 * Run both matchers in the supplied tuple against the first two elements of a supplied iterable for matching
	 * <pre>
	 * new PatternMatcher()
	 *		.matchOfMatchers(tuple( equalTo(&quot;-l&quot;),
	 *								 anything()),
	 *							lang -&gt; language  = lang,Extractors.&lt;String&gt;at(1) )
	 *		.match(tuple(&quot;-l&quot;,&quot;java&quot;));
	 *	
	 *	assertThat(language,is(&quot;java&quot;));
	 * </pre>
	 * 
	 * 
	 * @param predicates Tuple of 2 hamcrest matchers
	 * @param a Action to be triggered on successful match, will receive data via the extractor
	 * @param extractor Extractor to extract data out of incoming iterable after matchers have matched
	 * @return New Pattern Matcher
	 */
	public <T,R,V,V1>  PatternMatcher matchOfMatchers(Tuple2<Matcher<V>,Matcher<V1>> predicates,
				Action<R> a,Extractor<T,R> extractor){
			
			Seq<Object> pred = Seq.seq(predicates);
			
			return matchOfThenExtract(new BaseMatcher(){

				@Override
				public boolean matches(Object item) {
					return seq(item).zip(pred).map(t -> ((Matcher)t.v2).matches(t.v1)).allMatch(v->v==true);
				}

				@Override
				public void describeTo(Description description) {
				
					
				}
				
			}, a, extractor);
			
	}
	/**
	 * Run both predicates in the supplied tuple against the first two elements of a supplied iterable for matching
	 * <pre>
	 * new PatternMatcher()
	 *		.caseOfPredicates(tuple( v -&gt;  v.equals(&quot;-l&quot;) ||  v.equals(&quot;---lang&quot;),
	 *								 v-&gt;true),
	 *							lang -&gt; language  =lang,Extractors.&lt;String&gt;at(1) )
	 *		.match(tuple(&quot;-l&quot;,&quot;java&quot;));
	 *	
	 *	assertThat(language,is(&quot;java&quot;));
	 * 
	 * </pre>
	 * 
	 * @param predicates Tuple of 2 predicates
	 * @param a Action to be triggered on successful match, will receive data via the extractor
	 * @param extractor Extractor to extract data out of incoming iterable after predicates have matched
	 * @return New Pattern Matcher
	 */
	public <T,R,V,V1> PatternMatcher caseOfPredicates(Tuple2<Predicate<V>,Predicate<V1>> predicates,
							Action<R> a,Extractor<T,R> extractor){
		
		Seq<Object> pred = Seq.seq(predicates);
		
		return caseOfThenExtract(it -> seq(it).zip(pred).map(t -> ((Predicate)t.v2).test(t.v1)).allMatch(v->v==true), a, extractor);
		
	}
	/**
	 * Match against a tuple of predicates (or prototype values, or hamcrest matchers). Each predicate will match against an element in an iterable. 
	 * 
	 * @param predicates Predicates to match with
	 * @param a Action triggered if predicates hold
	 * @param extractor
	 * @return
	 */
	public <T,R> PatternMatcher caseOfTuple(Tuple predicates, Action<R> a,Extractor<T,R> extractor){

				Seq<Object> pred = Seq.seq(predicates);
				return caseOfThenExtract(it -> seq(it).zip(pred).map(t -> (convertToPredicate(t.v2)).test(t.v1)).allMatch(v->v==true), a, extractor);
				
	}
	private Predicate convertToPredicate(Object o){
		if(o instanceof Predicate)
			return (Predicate)o;
		if(o instanceof Matcher)
			return test -> ((Matcher)o).matches(test);
			
		return test -> Objects.equals(test,o);
	}
	
	public <T,R> PatternMatcher matchOfTuple(Tuple predicates, Action<R> a,Extractor<T,R> extractor){

		Seq<Object> pred = Seq.seq(predicates);
		return matchOfThenExtract(new BaseMatcher(){

			@Override
			public boolean matches(Object item) {
				return seq(item).zip(pred).map(t -> ((Matcher)t.v2).matches(t.v1)).allMatch(v->v==true);
			}

			@Override
			public void describeTo(Description description) {
			
				
			}
			
		}, a, extractor);
		
	}
	
	
	
	public <V,X> PatternMatcher selectFromChain(Stream<? extends ChainOfResponsibility<V,X>> stream){
		return selectFrom(stream.map(n->new Tuple2(n,n)));
		
	}
	public <V,X> PatternMatcher selectFrom(Stream<Tuple2<Predicate<V>,Function<V,X>>> stream){
		PatternMatcher[] matcher = {this};
		stream.forEach(t -> matcher[0] = matcher[0].inCaseOf(t.v1,a->t.v2.apply(a)));
		return matcher[0];
	}
	
	 public <T,V,X> PatternMatcher inCaseOfManyType(Predicate master,ActionWithReturn<T,X> a,
    		 Predicate<V>... predicates){
		
		Seq<Predicate<V>> pred = Seq.of(predicates);
		
		
		return inCaseOf(it -> master.test(it) && seq(Extractors.decompose().apply(it)).zip(pred)
				.map(t -> t.v2.test((V)t.v1)).allMatch(v->v==true), a);
		
	}
	 
	
     public <V,X> PatternMatcher inCaseOfMany(ActionWithReturn<List<V>,X> a,
    		 Predicate<V>... predicates){
		
		Seq<Predicate<V>> pred = Seq.of(predicates);
		
		
		return inCaseOfThenExtract(it -> seq(it).zip(pred)
				.map(t -> t.v2.test((V)t.v1)).allMatch(v->v==true), a, e-> wrapInList(e));
		
	}
	private List wrapInList(Object a) {
		if(a instanceof List)
			return (List)a;
		else
			return Arrays.asList(a);
	}

	public <V,X> PatternMatcher inMatchOfMany(ActionWithReturn<List<V>,X> a,
			Matcher<V>... predicates){
		
		Seq<Matcher<V>> pred = (Seq<Matcher<V>>) Seq.of(predicates);
		
		
		return inMatchOfThenExtract(new BaseMatcher(){

			@Override
			public boolean matches(Object item) {
				return seq(item).zip(pred)
						.map(t -> t.v2.matches((V)t.v1)).allMatch(v->v==true);
			}

			@Override
			public void describeTo(Description description) {
			
				
			}
			
		}, a, this::wrapInList);
		
	}
	public <T,R,V,V1,X>  PatternMatcher inMatchOfMatchers(Tuple2<Matcher<V>,Matcher<V1>> predicates,
				ActionWithReturn<R,X> a,Extractor<T,R> extractor){
			
			Seq<Object> pred = Seq.seq(predicates);
			
			return inMatchOfThenExtract(new BaseMatcher(){

				@Override
				public boolean matches(Object item) {
					return seq(item).zip(pred).map(t -> ((Matcher)t.v2).matches(t.v1)).allMatch(v->v==true);
				}

				@Override
				public void describeTo(Description description) {
				
					
				}
				
			}, a, extractor);
			
	}
	public <T,R,V,V1,X> PatternMatcher inCaseOfPredicates(Tuple2<Predicate<V>,Predicate<V1>> predicates,
							ActionWithReturn<R,X> a,Extractor<T,R> extractor){
		
		Seq<Object> pred = Seq.seq(predicates);
		
		return inCaseOfThenExtract(it -> seq(it).zip(pred).map(t -> ((Predicate)t.v2).test(t.v1)).allMatch(v->v==true), a, extractor);
		
	}
	
	
	public <T,R,X> PatternMatcher inCaseOfSeq(Seq<Predicate> predicates, ActionWithReturn<R,X> a,Extractor<T,R> extractor){

		Seq<Object> pred = (Seq)predicates;
		return inCaseOfThenExtract(it -> seq(it).zip(pred).map(t -> ((Predicate)t.v2).test(t.v1)).allMatch(v->v==true), a, extractor);
		
	}
	
	public <T,R,X> PatternMatcher inMatchOfSeq(Seq<Matcher> predicates, ActionWithReturn<R,X> a,Extractor<T,R> extractor){

		Seq<Object> pred = (Seq)(predicates);
		return inMatchOfThenExtract(new BaseMatcher(){

			@Override
			public boolean matches(Object item) {
				return seq(item).zip(pred).map(t -> ((Matcher)t.v2).matches(t.v1)).allMatch(v->v==true);
			}

			@Override
			public void describeTo(Description description) {
			
				
			}
			
		}, a, extractor);
		
}
	
	public <V,X> PatternMatcher caseOfType(Action<V> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		return caseOfThenExtract(it -> it.getClass().isAssignableFrom(clazz), a, null);
		
	}
	public <V> PatternMatcher matchOf(Matcher<V> match,Action<V> a){
		return inCaseOfThenExtract(it->match.matches(it), new ActionWithReturnWrapper(a), null);
		
	}
	public <V> PatternMatcher caseOf(Predicate<V> match,Action<V> a){
		return inCaseOfThenExtract(match, new ActionWithReturnWrapper(a), null);
		
	}
	public <R,V,T> PatternMatcher caseOfThenExtract(Predicate<V> match,Action<R> a, Extractor<T,R> extractor){
		
		return withCases(cases.append(index(),Case.of(match, extractorAction(extractor,new ActionWithReturnWrapper(a)))));
		
	}
	public <R,V,T> PatternMatcher matchOfThenExtract(Matcher<V> match,Action<V> a, Extractor<T,R> extractor){
		Predicate<V> predicate = it->match.matches(it);
		return withCases(cases.append(index(),Case.of(predicate, extractorAction(extractor,new ActionWithReturnWrapper(a)))));
		
	}
	public <R,V,T> PatternMatcher caseOf( Extractor<T,R> ext,Predicate<R> match,Action<V> a){
		val extractor = Extractors.memoised(ext);
		return withCases(cases.append(index(),Case.of(extractorPredicate(extractor,match),extractorAction(extractor,new ActionWithReturnWrapper(a)))));
		
	}
	public <R,V,T> PatternMatcher matchOf( Extractor<T,R> ext,Matcher<R> match,Action<V> a){
		val extractor = Extractors.memoised(ext);
		Predicate<V> predicate = it->match.matches(it);
		return withCases(cases.append(index(),Case.of(extractorPredicate(extractor,predicate),extractorAction(extractor,new ActionWithReturnWrapper(a)))));
		
	}
	public <V,X> PatternMatcher inCaseOfValue(V value,ActionWithReturn<V,X> a){
		
		return inCaseOfThenExtract(it -> Objects.equals(it, value), a, null);
		
	}
	public <V,X> PatternMatcher inCaseOfType(ActionWithReturn<V,X> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		return inCaseOfThenExtract(it -> it.getClass().isAssignableFrom(clazz), a, null);
		
	}
	public <V,X> PatternMatcher inCaseOf(Predicate<V> match,ActionWithReturn<V,X> a){
		return inCaseOfThenExtract(match, a, null);
		
	}
	public <R,T,X> PatternMatcher inCaseOfThenExtract(Predicate<T> match,ActionWithReturn<R,X> a, Extractor<T,R> extractor){
		
		return withCases(cases.append(index(),Case.of(match,extractorAction(extractor,a))));
		
	}
	
	
	
	public <R,V,T,X> PatternMatcher inCaseOf( Extractor<T,R> ext,Predicate<V> match,ActionWithReturn<V,X> a){
		val extractor = Extractors.memoised(ext);
		return withCases(cases.append(index(),Case.of(extractorPredicate(extractor,match),extractorAction(extractor,a))));
		
	}
	
	public <R,V,T,X> PatternMatcher inCaseOfType( Extractor<T,R> ext,ActionWithReturn<V,X> a){
		val extractor = Extractors.memoised(ext);
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		Predicate predicate = it -> it.getClass().isAssignableFrom(clazz);
		return withCases(cases.append(index(),Case.of(extractorPredicate(extractor,predicate),extractorAction(extractor,a))));
		
	}
	public <R,V,T,X> PatternMatcher inCaseOfValue(V value, Extractor<T,R> ext,ActionWithReturn<V,X> a){
		val extractor = Extractors.memoised(ext);
		Predicate predicate = it -> Objects.equals(it, value);
		return withCases(cases.append(index(),Case.of(extractorPredicate(extractor,predicate),extractorAction(extractor,a))));
		
	}
	
	
	/**hamcrest **/
	public <V,X> PatternMatcher inMatchOf(Matcher<V> match,ActionWithReturn<V,X> a){
		Predicate<V> predicate = it->match.matches(it);
		return inCaseOfThenExtract(predicate, a, null);
	}
	public <R,T,X> PatternMatcher inMatchOfThenExtract(Matcher<T> match,ActionWithReturn<R,X> a, Extractor<T,R> extractor){
		Predicate<T> predicate = it->match.matches(it);
		return withCases(cases.append(index(),Case.of(predicate,
				extractorAction(extractor,a))));
		
	}
	
	
	public <R,V,T,X> PatternMatcher inMatchOf( Extractor<T,R> ext,Matcher<V> match,ActionWithReturn<V,X> a){
		val extractor = Extractors.memoised(ext);
		Predicate<V> predicate = it->match.matches(it);
		return withCases(cases.append(index(),Case.of(extractorPredicate(extractor,predicate),extractorAction(extractor,a))));
		
	}
	

	
	private int index() {
		return cases.size();
	}



	
	private final static Object NO_VALUE = new Object();
	public static class ActionWithReturnWrapper<T,X> implements ActionWithReturn<T,X>{
		private final Action<T> action;
		public ActionWithReturnWrapper(Action<T> action){
			this.action = action;
		}
		
		public X apply(T t){
			action.accept(t);
			return (X)NO_VALUE;
		}
	}
	
	
	
	
	
	
	
	
}
