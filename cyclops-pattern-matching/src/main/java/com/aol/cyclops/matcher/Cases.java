package com.aol.cyclops.matcher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.lambda.api.Decomposable;

/**
 * Represents an ordered list of pattern matching cases.
 * 
 * @author johnmcclean
 *
 * @param <T>  Input type for predicate and function (action)
 * @param <R>  Return type for function (action) which is executed if the predicate tests positive
 * @param <X> Type of Function - cyclops pattern matching builders use ActionWithReturn which is serialisable and retains type info
 */
@AllArgsConstructor
public class Cases<T,R,X extends Function<T,R>> implements Function<T,Optional<R>>{
	@Wither
	private final PStack<Case<T,R,X>> cases;
	@Wither(AccessLevel.PRIVATE)
	private final boolean sequential;

	 Cases() {
		cases = ConsPStack.empty();
		sequential =true;
	}
	 
	 public PStack<Case<T,R,X>> get(){
		 return cases;
	 }
	 
	 /**
	  * Construct a Cases instance from a persistent stack of Pattern Matching Cases
	  * Will execute sequentially when Match is called.
	  * 
	 * @param cases Persistent Stack of cases to build Cases from
	 * @return  New Cases instance (sequential)
	*/
	public static <T,R,X extends Function<T,R>>  Cases<T,R,X> ofPStack(PStack<Case<T,R,X>> cases){
		 return new Cases(cases,true);
	}
	 
		 /**
		  * Construct a Cases instance from a list of Pattern Matching Cases
		  * Will execute sequentially when Match is called.
		  * 
		 * @param cases Persistent Stack of cases to build Cases from
		 * @return  New Cases instance (sequential)
		*/
	public static <T,R,X extends Function<T,R>>  Cases<T,R,X> ofList(List<Case<T,R,X>> cases){
		 return new Cases(cases.stream().map(ConsPStack::singleton)
					.reduce(ConsPStack.empty(),(acc,next)-> acc.plus(acc.size(),next.get(0))),true);
	 }

	/**
	 * Construct a Cases instance from an array Pattern Matching Cases
	 * Will execute sequentially when Match is called.
	 * 
	 * @param cazes Array of cases to build Cases instance from 
	 * @return New Cases instance (sequential)
	 */
	public static <T,R,X extends Function<T,R>>  Cases<T,R,X> of(Case<T,R,X>... cazes){
		return ofPStack(
				Stream.of(cazes)
				.map(ConsPStack::singleton)
				.reduce(ConsPStack.empty(),(acc,next)-> acc.plus(acc.size(),next.get(0))));
			
	}
	/**
	 * Zip two Streams into pattern Matching Cases
	 * 
	 * @param predicates Stream of predicates
	 * @param functions Stream of functions
	 * @return Cases with predicates paired to functions
	 */
	public static <T,R,X extends Function<T,R>>  Cases<T,R,X> zip(Stream<Predicate<T>> predicates, Stream<X> functions){
		
		return ofPStack(Seq.seq(predicates)
			.zip(Seq.seq(functions))
			.map(Case::of)
			.map(ConsPStack::singleton)
			.reduce(ConsPStack.empty(),(acc, next)-> acc.plus(acc.size(),next.get(0))));
		
		
	}
	/**
	 * <pre>
	 * 	val cases = Cases.of(Case.of(input->true,input->"hello"));
	 *	val unzipped = cases.unzip();
	 *	assertTrue(unzipped.v1.map(p->p.test(10)).allMatch(v->v));
	 *	assertTrue(unzipped.v2.map(fn->fn.apply(10)).allMatch(v->"hello".equals(v)));
	 * </pre>
	 * 
	 * 
	 * @return unzipped Cases, with Predicates in one Stream and Functions in the other.
	 */
	public Tuple2<Stream<Predicate<T>>,Stream<X>> unzip(){
		return Tuple.<Stream<Predicate<T>>,Stream<X>>tuple(cases.stream().map(c-> c.getPredicate()),cases.stream().map(c->c.getAction()));
	}
	
	
	/**
	 * Iterate over each case
	 * 
	 * @param consumer accept each case in turn
	 */
	public void forEach(Consumer<Case<T,R,X>> consumer){
		cases.stream().forEach(consumer);
	}
	
	/**
	 * This is the default behaviour, cases are matched sequentially when a matching function is called
	 * @return New Cases object that will match sequentially 
	 * 
	 */
	public Cases<T,R,X> sequential(){
		return this.withSequential(true);
	}
	/**
	 * Default behaviour is to match sequentially
	 * @return A new cases object that will match in parallel
	 */
	public Cases<T,R,X> parallel(){
		return this.withSequential(false);
	}
	
	
	/**
	 * Merge two cases, with supplied Cases added after current Cases
	 * @see #append
	 * 
	 * @param patterns Cases to merge to this set
	 * @return New merged Cases
	 */
	public Cases<T,R,X> merge(Cases<T, R,X> patterns){
		return this.withCases(cases.plusAll(size(), patterns.cases));
	}
	
	
	/**
	 * Filter the Cases with the supplied predicate
	 * 
	 * <pre>
	 * cases = Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;),Case.of(input-&gt;false,input-&gt;&quot;second&quot;))
									.filter(p-&gt; p.getPredicate().test(10));
		assertThat(cases.size(),is(1));
	 * 
	 * </pre>
	 * 
	 * 
	 * @param predicate to filter out cases
	 * @return New Filtered Cases
	 */
	public Cases<T,R,X> filter(Predicate<Case<T,R,X>> predicate) {
		return withCases(cases.stream().filter(data -> predicate.test(data)).map(ConsPStack::singleton)
				.reduce(ConsPStack.empty(),(acc, next)-> acc.plus(acc.size(),next.get(0))));
	}

	/**
	 * Filter the Cases with the supplied predicate
	 * 
	 * <pre>
	 *  Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;),Case.of(input-&gt;false,input-&gt;&quot;second&quot;))
				.filterPredicate(p-&gt; p.test(10));
		assertThat(cases.size(),is(1));
	 * 
	 * </pre>
	 * 
	 * 
	 * @param predicate to filter out cases
	 * @return New Filtered Cases
	 */
	public Cases<T,R,X> filterPredicate(Predicate<Predicate<T>> predicate) {
		return withCases(cases.stream()
				.filter(data -> predicate.test(data.getPredicate())).map(ConsPStack::singleton)
				.reduce(ConsPStack.empty(),(acc, next)-> acc.plus(acc.size(),next.get(0))));
	}

	/**
	 * Filter the Cases with the supplied predicate
	 * 
	 * <pre>
	 * 
	 * cases = Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;),Case.of(input-&gt;false,input-&gt;&quot;second&quot;))
	 *			.filterFunction(fn-&gt; fn.apply(10).equals(&quot;second&quot;));
	 *	assertThat(cases.size(),is(1));
	 * 
	 * </pre>
	 * 
	 * 
	 * @param predicate to filter out cases
	 * @return New Filtered Cases
	 */
	public Cases<T,R,X> filterFunction(Predicate<Function<T,R>> predicate) {
		return withCases(cases.stream()
				.filter(data -> predicate.test(data.getAction())).map(ConsPStack::singleton)
				.reduce(ConsPStack.empty(),(acc, next)-> acc.plus(acc.size(),next.get(0))));
	}

	/**
	 * Map predicates for all Cases with the supplied function
	 * 
	 * <pre>
	 * 
	 * List results = Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;),Case.of(input-&gt;false,input-&gt;&quot;second&quot;))
	 *					.mapPredicate(p-&gt;input-&gt;true).matchMany(10).collect(Collectors.toList());
	 *	
	 *	assertThat(results.size(),is(2));
	 * 
	 * </pre>
	 * 
	 * 
	 * @param predicateMapper Function to map the predicates
	 * @return New Cases with mapped predicates
	 */
	public Cases<T,R,X> mapPredicate(Function<Predicate<T>, Predicate<T>> predicateMapper) {
		return map(caseData -> {
			return Case.of(predicateMapper.apply(caseData.getPredicate()),
					caseData.getAction());
		});
	}

	/**
	 * Map all the functions to another function
	 * 
	 * <pre>
	 * 
	 * List&lt;String&gt; results = Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;),Case.of(input-&gt;true,input-&gt;&quot;second&quot;))
	 *			.mapFunction(fn-&gt;input-&gt;&quot;prefix_&quot;+fn.apply(input)).&lt;String&gt;matchMany(10).collect(Collectors.toList());
	 *	
	 *	assertThat(results.size(),is(2));
	 *	assertTrue(results.stream().allMatch(s-&gt;s.startsWith(&quot;prefix_&quot;)));
	 *	assertTrue(results.stream().anyMatch(s-&gt;s.startsWith(&quot;prefix_hello&quot;)));
	 *	assertTrue(results.stream().anyMatch(s-&gt;s.startsWith(&quot;prefix_second&quot;)));
	 * 
	 * </pre>
	 * @param actionMapper Function to apply mapping
	 * @return New Cases with all functions mapped
	 */
	public <R1> Cases<T,R,X> mapFunction(
			Function<Function<T,R>, Function<T,R1>> actionMapper) {
		return map(caseData -> {
			return Case.of(caseData.getPredicate(),
					actionMapper.apply(caseData.getAction()));
		});
	}

	/**
	 * Map all Case instances present to a different value
	 * 
	 * <pre>
	 * 		List&lt;String&gt; results = Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;),Case.of(input-&gt;false,input-&gt;&quot;second&quot;))
	 *			.map(cse-&gt;Case.of(t-&gt;true,input-&gt;&quot;prefix_&quot;+cse.getAction().apply(input))).&lt;String&gt;matchMany(10).collect(Collectors.toList());
	 *	
	 *	assertThat(results.size(),is(2));
	 *	assertTrue(results.stream().allMatch(s-&gt;s.startsWith(&quot;prefix_&quot;)));
	 *	assertTrue(results.stream().anyMatch(s-&gt;s.startsWith(&quot;prefix_hello&quot;)));
	 *	assertTrue(results.stream().anyMatch(s-&gt;s.startsWith(&quot;prefix_second&quot;)));
	 * 
	 * 
	 * </pre>
	 * 
	 * 
	 * @param mapper Function to map case instances
	 * @return New Cases with new Case instances
	 */
	public <T1,R1,X1 extends Function<T1,R1>> Cases<T,R,X> map(Function<Case<T,R,X>, Case<T1,R1,X1>> mapper) {

		return this.withCases((PStack)cases.stream().map(mapper).map(ConsPStack::singleton)
				.reduce(ConsPStack.empty(),(acc, next)-> acc.plus(acc.size(),next.get(0))));

	}
	/**
	 * Expand each Case into a Cases object allowing 1:Many expansion of Cases
	 * 
	 * <pre>
	 * 		Case&lt;Object,Integer,Function&lt;Object,Integer&gt;&gt; cse = Case.of(input-&gt; input instanceof Person, input -&gt; ((Person)input).getAge());
	 *
	 *	 
	 *	assertThat(Cases.of(cse).flatMap(c -&gt; Cases.of(c.andThen(Case.of( age-&gt; age&lt;18,s-&gt;&quot;minor&quot;)),
	 *									c.andThen(Case.of( age-&gt;age&gt;=18,s-&gt;&quot;adult&quot;)))).match(new Person(&quot;bob&quot;,21)).get(),is(&quot;adult&quot;)); 
     *
	 * 
	 * </pre>
	 * 
	 * @param mapper Function to map Case instances to Cases instances
	 * @return New Cases aggregated from new mapped Cases instances
	 */
	public <T1,R1,X1 extends Function<T1,R1>> Cases<T,R,X> flatMap(Function<Case<T,R,X>, Cases<T1,R1,X1>> mapper) {

		return this.withCases((PStack)cases.stream()
											.map(mapper)
											.flatMap(Cases::sequentialStream)
											.map(ConsPStack::singleton)
											.reduce(ConsPStack.empty(),(acc, next)-> acc.plus(acc.size(),next.get(0))));

	}

	/**
	 * Map to a new Cases instance via a function that is provided all current Cases
	 * 
	 * <pre>
	 * 
	 *  cases = Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;),Case.of(input-&gt;false,input-&gt;&quot;second&quot;))
						.flatMapAll(input-&gt; Cases.of(input.plus(Case.of(in-&gt;true,in-&gt;&quot;new&quot;))));
		
		assertThat(cases.size(),is(3));
	 *  
	 *  </pre>
	 * 
	 * 
	 * 
	 * @param mapper Function to map to a new Cases instance
	 * @return new Cases instance
	 */
	public <T1,R1,X1 extends Function<T1,R1>> Cases<T1,R1,X1> flatMapAll(Function<PStack<Case<T,R,X>>, Cases<T1,R1,X1>> mapper) {

		return mapper.apply(cases);
	}

	/**
	 * Append an individual case with supplied Cases inserted at index
	 * @see #merge
	 * @param index to insert supplied cases in new Case instance
	 * @param pattern Cases to append / insert
	 * @return New Cases with current and supplied cases
	 */
	public Cases<T,R,X> append(int index, Case<T,R,X> pattern) {
		return this.withCases(cases.plus(index, pattern));
	}

	/**
	 * @return number of cases
	 */
	public int size() {
		return cases.size();
	}

	/**
	 * 
	 * <pre>
	 * 
	 * 
	 * assertThat(Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;)).asUnwrappedFunction().apply(10),is(&quot;hello&quot;));
	 * 
	 * </pre>
	 * 
	 * @return A function that when applied will return the 'unwrapped' result
	 * of matching. I.e. Optional#get will have been called.
	 */
	public <T1, X> Function<T1, X> asUnwrappedFunction() {
		return (T1 t) -> (X) apply((T)t).get();
	}

	/**
	 * 
	 * <pre>
	 * 
	 * assertThat(Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;)).asStreamFunction().apply(10).findFirst().get(),is(&quot;hello&quot;));
	 * 
	 * </pre>
	 * 
	 * 
	 * @return A function that returns the result of matching as a Stream
	 */
	public <T1, X> Function<T1, Stream<X>> asStreamFunction() {

		return (T1 t) -> (Stream<X>) Stream.of(t).map(input-> this.apply((T)input))
				.filter(Optional::isPresent).map(Optional::get);
	}

	/*
	 * 
	 * <pre>
	 * assertThat(Cases.of(Case.of(input-&gt;true,input-&gt;&quot;hello&quot;)).apply(10).get(),is(&quot;hello&quot;));
	 * </pre>
	 * 
	 * @param t Object to match against
	 * 
	 * @return Value from matched case if present
	 * 
	 * @see java.util.function.Function#apply(java.lang.Object)
	 * 
	 */
	public Optional<R> apply(T t) {
		return match(t);
	}

	/**
	 * Each input element in the stream can generate multiple matched values
	 * 
	 * <pre>
	 * 
	 * List&lt;String&gt; results = Cases.of(Case.of((Integer input)-&gt;10==input,input-&gt;&quot;hello&quot;),
	 *										Case.of(input-&gt;11==input,input-&gt;&quot;world&quot;))
	 *									.&lt;String&gt;matchManyFromStream(Stream.of(1,10,11))
	 *									.toList();
	 *	
	 *	assertThat(results.size(),is(2));
	 *	assertThat(results,hasItem(&quot;hello&quot;));
	 *	assertThat(results,hasItem(&quot;world&quot;));
	 * 
	 * </pre>
	 * 
	 * 
	 * @param s
	 *            Stream of data to match against (input to matcher)
	 * @return Stream of values from matched cases
	 */
	public <R> Seq<R> matchManyFromStream(Stream<T> s) {
		return Seq.seq(s.flatMap(this::matchMany));
	}
	/**
	 * Asynchronously match against a Stream of data, on the supplied executor
	 * Each input element in the stream can generate multiple matched values
	 * 
	 * <pre>
	 * 
	 * List&lt;String&gt; results = Cases.of(Case.of((Integer input)-&gt;10==input,input-&gt;&quot;hello&quot;),
	 *			Case.of(input-&gt;11==input,input-&gt;&quot;world&quot;))
	 *		.&lt;String&gt;matchManyFromStreamAsync(ForkJoinPool.commonPool(),Stream.of(1,10,11))
	 *		.join()
	 *		.toList(); 
     *
	 *	assertThat(results.size(),is(2));
	 *	assertThat(results,hasItem(&quot;hello&quot;));
	 *	assertThat(results,hasItem(&quot;world&quot;));
	 * 
	 * </pre>	 
	 * @param executor used to perform async task
	 * @param s
	 *            Stream of data to match against (input to matcher)
	 * @return Stream of values from matched cases
	 */
	public <R> CompletableFuture<Seq<R>> matchManyFromStreamAsync(Executor executor, Stream s){
		return CompletableFuture.supplyAsync(()->matchManyFromStream(s), executor);
	}

	/**
	 * 
	 * <pre>
	 * List&lt;String&gt; results =  Cases.of(Case.of((Integer input)-&gt;10==input,input-&gt;&quot;hello&quot;),
	 *			Case.of(input-&gt;11==input,input-&gt;&quot;world&quot;),
	 *			Case.of(input-&gt;10==input,input-&gt;&quot;woo!&quot;))
	 *		.&lt;String&gt;matchMany(10).toList();
	 *	
	 *	assertThat(results.size(),is(2));
	 *	assertThat(results,hasItem(&quot;hello&quot;));
	 *	assertThat(results,hasItem(&quot;woo!&quot;));
	 *	
	 *	</pre>
	 * 
	 * 
	 * @param t
	 *            input to match against - can generate multiple values
	 * @return Stream of values from matched cases for the input
	 */
	public <R> Seq<R> matchMany(T t) {
		return Seq.seq((Stream) stream().map(pattern -> pattern.match(t))
				.filter(Optional::isPresent).map(Optional::get));

	}
	/**
	 * Match many asynchronously
	 * <pre>
	 * List&lt;String&gt; results =  Cases.of(Case.of((Integer input)-&gt;10==input,input-&gt;&quot;hello&quot;),
	 *			Case.of(input-&gt;11==input,input-&gt;&quot;world&quot;),
	 *			Case.of(input-&gt;10==input,input-&gt;&quot;woo!&quot;))
	 *		.&lt;String&gt;matchManyAsync(ForkJoinPool.commonPool(),10).join().toList();
	 *	
	 *	assertThat(results.size(),is(2));
	 *	assertThat(results,hasItem(&quot;hello&quot;));
	 *	assertThat(results,hasItem(&quot;woo!&quot;));
	 * 
	 * 
	 * </pre>
	 * 
	 * 
	 * @param executor Executor to execute task asynchronously
	 * @param t  input to match against - can generate multiple values
	 * @return Stream of values from matched cases for the input wrapped in a  CompletableFuture
	 */
	public <R> CompletableFuture<Seq<R>> matchManyAsync(Executor executor, T t){
		return CompletableFuture.supplyAsync(()->matchMany(t), executor);
	}
	/**
	 * Each input element can generated a single matched value
	 * 
	 * <pre>
	 * 
	 * 		List&lt;String&gt; results = Cases
	 *			.of(Case.of((Integer input) -&gt; 10 == input, input -&gt; &quot;hello&quot;),
	 *					Case.of((Integer input) -&gt; 10 == input, input -&gt; &quot;ignored&quot;),
	 *					Case.of(input -&gt; 11 == input, input -&gt; &quot;world&quot;))
	 *			.&lt;String&gt; matchFromStream(Stream.of(1, 11, 10)).toList(); 
     *
	 *	assertThat(results.size(), is(2));
	 *	assertThat(results, hasItem(&quot;hello&quot;));
	 *	assertThat(results, hasItem(&quot;world&quot;));
	 * 
	 * </pre>
	 * 
	 * @param s
	 *            Stream of data to match against (input to matcher)
	 * @return Stream of matched values, one case per input value can match
	 */
	public <R> Seq<R> matchFromStream(Stream<T> s) {

		Stream<Optional<R>> results = s.<Optional<R>> map(this::match);
		return Seq.seq(results.filter(Optional::isPresent).map(Optional::get));
	}
	/**
	 * Execute matchFromStream asynchronously
	 * @see #matchFromStream
	 * <pre>
	 * List&lt;String&gt; results = Cases
	 *			.of(Case.of((Integer input) -&gt; 10 == input, input -&gt; &quot;hello&quot;),
	 *					Case.of((Integer input) -&gt; 10 == input, input -&gt; &quot;ignored&quot;),
	 *					Case.of(input -&gt; 11 == input, input -&gt; &quot;world&quot;))
	 *			.&lt;String&gt; matchFromStreamAsync(ForkJoinPool.commonPool(),Stream.of(1, 11, 10)).join().toList(); 
     *  
	 *	assertThat(results.size(), is(2));
	 *	assertThat(results, hasItem(&quot;hello&quot;));
	 *	assertThat(results, hasItem(&quot;world&quot;));
	 * 
	 * </pre>
	 * 
	 * @param executor executor Executor to execute task asynchronously
	 * @param s Stream of data
	 * @return Results
	 */
	public <R> CompletableFuture<Seq<R>> matchFromStreamAsync(Executor executor, Stream<T> s){
		return CompletableFuture.supplyAsync(()->matchFromStream(s), executor);
	}
	
	/**
	 * Aggregates supplied objects into a List for matching against
	 * 
	 * <pre>
 	 * assertThat(Cases.of(Case.of((List&lt;Integer&gt; input) -&gt; input.size()==3, input -&gt; &quot;hello&quot;),
	 *			Case.of((List&lt;Integer&gt; input) -&gt; input.size()==2, input -&gt; &quot;ignored&quot;),
	 *			Case.of((List&lt;Integer&gt; input) -&gt; input.size()==1, input -&gt; &quot;world&quot;)).match(1,2,3).get(),is(&quot;hello&quot;));
     *
	 * </pre>
	 * 
	 * @param t Array to match on
	 * @return Matched value wrapped in Optional
	 */
	public <R> Optional<R> match(Object... t) {
		return match((T)Arrays.asList(t));
	}
	/**
	 * Aggregates supplied objects into a List for matching asynchronously against
	 * 
	 * <pre>
	 * assertThat(Cases.of(Case.of((List&lt;Integer&gt; input) -&gt; input.size()==3, input -&gt; &quot;hello&quot;),
	 *			Case.of((List&lt;Integer&gt; input) -&gt; input.size()==2, input -&gt; &quot;ignored&quot;),
	 *			Case.of((List&lt;Integer&gt; input) -&gt; input.size()==1, input -&gt; &quot;world&quot;))
	 *			.matchAsync(ForkJoinPool.commonPool(),1,2,3).join().get(),is(&quot;hello&quot;));	 
	 *
	 * </pre>
	 * @param executor Executor to perform the async task
	 * @param t Array to match on
	 * @return Matched value wrapped in CompletableFuture & Optional
	 */
	public <R> CompletableFuture<Optional<R>> matchAsync(Executor executor, Object... t){
		return CompletableFuture.supplyAsync(()->match(t), executor);
	}
	/**
	 * Decomposes the supplied input via it's unapply method
	 * Provides a List to the Matcher of values to match on
	 * 
	 * <pre>
	 * assertThat(Cases.of(Case.of((List input) -&gt; input.size()==3, input -&gt; &quot;hello&quot;),
	 *			Case.of((List input) -&gt; input.size()==2, input -&gt; &quot;ignored&quot;),
	 *			Case.of((List input) -&gt; input.size()==1, input -&gt; &quot;world&quot;))
	 *			.unapply(new MyClass(1,&quot;hello&quot;)).get(),is(&quot;ignored&quot;));
	 *					
	 *	@Value static class MyClass implements Decomposable{ int value; String name; }		
	 * </pre>
	 * @param t Object to decompose and match on
	 * @return Matched result wrapped in an Optional
	 */
	public <R> Optional<R> unapply(Decomposable t) {
		return match((T)t.unapply());
	}

	/**
	 * @param t
	 *            Object to match against supplied cases
	 * @return Value returned from matched case (if present) otherwise
	 *         Optional.empty()
	 */
	public <R> Optional<R> match(T t) {

		return (Optional) stream().map(pattern -> pattern.match(t))
				.filter(Optional::isPresent).map(Optional::get).findFirst();

	}
	private Stream<Case<T,R,X>> sequentialStream(){
		
			return cases.stream();
		
	}
	
	private Stream<Case<T,R,X>> stream(){
		if(this.sequential)
			return sequentialStream();
		return cases.parallelStream();
	}

}
