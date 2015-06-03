package com.aol.cyclops.matcher;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;



import com.aol.cyclops.lambda.utils.LazyImmutable;
import com.aol.cyclops.matcher.builders.ADTPredicateBuilder;
/**
 * An interface / trait for building functionally compositional pattern matching cases
 * 
 * Consists of a Predicate and a Function
 * When match is called, if the predicate holds the function is executed
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> Input type for predicate and function (action)
 * @param <R> Return type for function (action) which is executed if the predicate tests positive
 * @param <X> Type of Function - cyclops pattern matching builders use ActionWithReturn which is serialisable and retains type info
 */
public interface Case<T,R,X extends Function<T,R>>  {
	/**
	 * @return true is this an 'empty' case
	 */
	public boolean isEmpty();
	/**
	 * @return Pattern for this case
	 */
	public Two<Predicate<T>,X> get();
	
	/**
	 * @return Predicate for this case
	 */
	default Predicate<T> getPredicate(){
		return get().v1;
	}
	/**
	 * @return Action (Function) for this case
	 */
	default X getAction(){
		return get().v2;
	}
	
	/**
	 * @return A new case with the predicate negated
	 * 
	 * {@code
	 *     Case.of((Integer num) -> num >100,(Integer num) -> num * 10).negate();
	 *     
	 * }
	 * 
	 * Results in a case that will multiply numbers 100 or less by 10
	 * 
	 */
	default Case<T,R,X> negate(){
		return map(t2-> Two.tuple(t2.v1.negate(),t2.v2));
	}
	
	/**
	 * @param action New action to replace current one
	 * @return A news case that inverts the predicate and replaces the current action with the supplied one
	 * 
	 * <pre>
	 *     Case.of((Integer num) -&gt; num &gt;100,(Integer num) -&gt; num * 10).negate(num -&gt; num * 5);
	 *     
	 * </pre>
	 * 
	 * Results in a case that will multiply numbers 100 or less by 5
	 */
	default Case<T,R,X> negate(X action){
		return map(t2-> Two.tuple(t2.v1.negate(),action));
	}
	
	/**
	 * Filter this case with the supplied predicate
	 * 
	 * Some options here are to filter based on
	 *      success of predicate execution 
	 *      return values from function execution
	 *      if using ActionWithReturn for function instances on Generic parameter types
	 *      Custom function types with additional info can be used with Case
	 * 
	 * <pre>
	 * 
	 *  empty = Case.of(t-&amp;gt;true,input-&amp;gt;10).filter(p-&amp;gt;false);
	 *	assertThat(empty,instanceOf(EmptyCase.class));
	 *	
	 *  ActionWithReturn&amp;lt;String,Integer&amp;gt; act = hello -&amp;gt;10;
	 *	caze = Case.of(t-&gt;true, act);
	 *	     
 	 *
	 *	</pre>
	 * 
	 * 
	 * 
	 * @param predicate
	 * @return
	 */
	default  Case<T,R,X> filter(Predicate<Two<Predicate<T>,X>> predicate){
		return predicate.test(get()) ? this : empty();
	}
	/**
	 * Allows the predicate to be replaced with one returned from the supplied function
	 * 
	 * E.g. switching off a predicate
	 * <pre>
	 * case1 =Case.of(input-&gt;true,input-&gt;input+10);
	 * assertThat(case1.mapPredicate(p-&gt; t-&gt;false).match(100).isPresent(),is(false));
	 * </pre>
	 * 
	 * @param mapper Function that supplies the new predicate
	 * @return A new Case instance with a new Predicate
	 */
	default  Case<T,R,X> mapPredicate(Function<Predicate<T>,Predicate<T>> mapper){
		return this.map(t2-> Two.tuple(mapper.apply(t2.v1),t2.v2));
	}
	/**
	 * Allows the current function to be replaced by another
	 * <pre>
	 *  case1 =Case.of(input-&gt;true,input-&gt;input+10);
	 * assertThat(case1.mapFunction(fn-&gt; input-&gt;input+20).match(100).get(),is(120));
	 * </pre>
	 * Returns 100+20 rather than 100+10
	 * 
	 * @param mapper Function that supplies the new function
	 * @return  A new Case instance with a new function
	 */
	default  <R1,X1 extends Function<T,R1>> Case<T,R1,X1> mapFunction(Function<Function<T,R>,X1> mapper){
		return this.<T,R1,X1>map(t2-> Two.tuple(t2.v1,mapper.apply(t2.v2)));
	}
	/**
	 * Allows both the predicate and function in the current case to be replaced in a new Case
	 * <pre>
	 *  case1 =Case.of(input-&gt;false,input-&gt;input+10);
	 * Tuple2&lt;Predicate&lt;Integer&gt;,Function&lt;Integer,Integer&gt;&gt; tuple = Tuple.tuple( t-&gt;true,(Integer input)-&gt;input+20);
		assertThat(case1.map(tuple2 -&gt; tuple).match(100).get(),is(120));
	 * </pre>
	 * 
	 * @param mapper Function that generates the new predicate and action (function) 
	 * @return New case with a (potentially) new predicate and action
	 */
	default <T1,R1,X1 extends Function<T1,R1>> Case<T1,R1,X1> map(Function<Two<Predicate<T>,X>,Two<Predicate<T1>,X1>> mapper){
		return Case.of(mapper.apply(get()));	
	}
	/**
	 * Create a new Case from the supplied Function
	 * 
	 * <pre>
	 * case1 =Case.of(input-&gt;false,input-&gt;input+10);
	 * assertThat(case1.flatMap(tuple2 -&gt; Case.of(tuple2.v1,(Integer input)-&gt;input+20)).match(100).get(),is(120));
	 * </pre>
	 * 
	 * @param mapper Function that creates a new Case
	 * @return new Case instance created
	 */
	default <T1,R1,X1 extends Function<T1,R1>> Case<T1,R1,X1> flatMap(Function<Two<Predicate<T>,X>,Case<T1,R1,X1>> mapper){
		return mapper.apply(get());	
	}
	
	
	/**
	 * Provide a Case that will be run only if this one matches successfully
	 * The result of the current case will be the input into the supplied case
	 * Both cases have to be successful to return a result from match
	 * 
	 * <pre>
	 * case1 =Case.of(input-&gt;false,input-&gt;input+10);
	 * assertThat(case1.andThen(Case.of(input-&gt;true,input-&gt;input+1)).match(100).get(),is(111));
	 * </pre>
	 * 
	 * @param after Case that will be run after this one, if it matches successfully
	 * @return New Case which chains current case and the supplied one
	 */
	default <T1> Case<T,T1,Function<T,T1>> andThen(Case<R,T1,? extends Function<R,T1>> after){
		return after.compose(this);
	}
	
	
	
	/**
	 * Add a set of Cases that will be run if this case matches successfull
	 * 
	 * @param after Cases to run if this case matches
	 * @return New Case which chains current case and the supplied cases
	 */
	default <T1>  Case<T,T1,Function<T,T1>> andThen(Cases<R,T1,? extends Function<R,T1>> after){
		final LazyImmutable<Optional<T1>> var = new LazyImmutable<>();
		return andThen(Case.of(t-> var.setOnce(after.match(t)).get().isPresent(),  t-> var.get().get()));
	}
	
	/**
	 * Provide a Case that will be executed before the current one. The current case will only be attempted
	 * if the supplied case matches.
	 * 
	 * {@code
	 *  case1 =Case.of(input-&gt;false,input-&gt;input+10);
	 * assertThat(case1.compose(Case.of((Integer input)->true,input->input*2)).match(100).get(),is(210))
	 * }
	 * 
	 * (100*2)+10=210
	 * 
	 * @param before Case to be run before this one
	 * @return New Case which chains the supplied case and the current one
	 */
	default <T1> Case<T1,R,Function<T1,R>> compose(Case<T1,T,? extends Function<T1,T>> before){
		
		final LazyImmutable<T> value = new LazyImmutable<>();
		Predicate<T1> predicate =t-> {
			final boolean passed;
			if(before.get().v1.test(t)){
				passed=true;
				value.setOnce(before.get().v2.apply(t));
			}else
				passed= false;
			return  passed && get().v1.test(value.get());
		};
		
		return Case.<T1,R,Function<T1,R>>of(predicate,  input -> get().v2.apply(value.get()));
		
	}
	/**
	 * Provide a Case that will be executed before the current one. The function from the supplied Case will be executed
	 * first and it;s output will be provided to the function of the current case - if either predicate holds.
	 * 
	 * <pre>
	 * case1 =Case.of(input-&gt;false,input-&gt;input+10);
	 * assertThat(case1.composeOr(Case.of((Integer input)-&gt;false,input-&gt;input*2)).match(100).get(),is(210));
	 * </pre>
	 * 
	 * 
	 * @param before Case to be run before this one
	 * @return New Case which chains the supplied case and the current one
	 */
	default <T1 > Case<T1,R,Function<T1,R>> composeOr(Case<T1,T,? extends Function<T1,T>> before){
		
		
		return Case.<T1,R,Function<T1,R>>of(t->   before.get().v1.test(t) || get().v1.test(before.get().v2.apply(t)),
				input -> get().v2.apply(before.get().v2.apply(input)));
		
	}
	/**
	 * Creates a new Case that will execute the supplied function before current function if current predicate holds.
	 * The output of this function will be provided to the function in the current case.
	 * 
	 * <pre>
	 * case1 =Case.of(input-&amp;gt;false,input-&amp;gt;input+10);
	 * assertThat(case1.composeFunction((Integer input)-&gt;input*2).match(100).get(),is(210));
	 * </pre>
	 * 
	 * 
	 * @param before Function to execute before current function
	 * @return New case which chains the supplied function before the current function
	 */
	default <T1> Case<T1,R,Function<T1,R>> composeFunction(Function<T1,T> before){
		return this.compose(Case.<T1,T,Function<T1,T>>of(t->true,before));
	}
	/**
	 * Creates a new Case that will execute the supplied function after current function if current predicate holds.
	 * The output of the current function will be provided as input to the supplied function.
	 * 
	 * <pre>
	 * case1 =Case.of(input-&amp;gt;false,input-&amp;gt;input+10);
	 * assertThat(case1.andThenFunction(input-&gt;input*2).match(100).get(),is(220));
	 * </pre>
	 * 
	 * @param after Function to execute after the current function
	 * @return New case which chains the supplied function after the current function
	 */
	default  Case<T,T,Function<T,T>> andThenFunction(Function<R,T>  after){
		return this.andThen(Case.<R,T,Function<R,T>>of(r->true,after));
	}
	/**
	 * Create a new Case that will loosen the current predicate.
	 * Either predicate can hold in order for the Case to pass.
	 * 
	 * <pre>
	 * case1 =Case.of(input-&gt;true,input-&gt;input+10);
 	 * offCase = case1.mapPredicate(p-&gt; p.negate());
 	 * assertThat(offCase.or(t-&gt;true).match(100).get(),is(110));
	 * </pre>
	 * 
	 * 
	 * @param or Predicate that will be or'd with the current predicate
	 * @return New Case where the predicate is the supplied predicate or the current predicate.
	 */
	default Case<T,R,Function<T,R>> or(Predicate<T> or){
		return composeOr(Case.of(or,Function.identity()));
		
	}
	/**
	 * Syntax sugar for composeOr
	 * @see #composeOr
	 * 
	 * Provide a Case that will be executed before the current one. The function from the supplied Case will be executed
	 * first and it;s output will be provided to the function of the current case - if either predicate holds.
	 * 
	 * <pre>
	 * case1 =Case.of(input-&gt;false,input-&gt;input+10);
	 * assertThat(case1.composeOr(Case.of((Integer input)-&gt;false,input-&gt;input*2)).match(100).get(),is(210));
	 * </pre>
	 * 
	 * @param or Predicate for  before case
	 * @param fn Function for before case
	 * @return New Case which chains the supplied case and the current one
	 */
	default<T1> Case<T1,R,Function<T1,R>> or(Predicate<T1> or, Function<T1,T> fn){
		return composeOr(Case.of(or,fn));
		
	}
	/**
	 * Create a new case which ands the supplied predicate with the current predicate.
	 * The supplied predicate will be tested before existing predicate
	 * 
	 * {@code
	 * assertThat(case1.and(p->false).match(100).isPresent(),is(false));
	 *
	 * assertThat(case1.and(p->true).match(100).isPresent(),is(true));
	 * 
	 * }
	 * 
	 * @param and New Predicate to be and'd with current
	 * 
	 * @return New case instance which and's current predicate with supplied
	 */
	default Case<T,R,Function<T,R>> and(Predicate<T> and){
		return compose((Case)Case.of(and,Function.identity()));
	}
	
	/**
	 * Add a guard that assures input is of specified type. Note that this can't change the type supported by this case,
	 * but will allow this case to be used in a mixed type Cases environment
	 * e.g.
	 * 
	 * <pre>
	 * case1 =Case.of(input-&gt;true,input-&gt;input+10);
	 * assertThat(case1.andOfType(Integer.class).match(100).isPresent(),is(true));
	 * assertThat(((Case)case1).andOfType(String.class).match(100).isPresent(),is(false));
	 * </pre>
	 * 
	 * @param and Class to check type against
	 * @return New case with Class type guard inserted before current predicate
	 */
	default Case<T,R,Function<T,R>> andOfType(Class<T> and){
		return compose((Case)Case.of(input -> input.getClass().isAssignableFrom(and),Function.identity()));
	}
	/**
	 * Add a guard that assures input is of specified type. Note that this can't change the type supported by this case,
	 * but will allow this case to be used in a mixed type Cases environment
	 * e.g.
	 * 
	 * <pre>
	 * case1 =Case.of(input-&gt;true,input-&gt;input+10);
	 * assertThat(((Case)case1).andOfValue(5).match(100).isPresent(),is(false));
	 * assertThat(case1.andOfValue(100).match(100).isPresent(),is(true));
	 * </pre>
	 * 
	 * @param and Class to check type against
	 * @return New case with Class type guard inserted before current predicate
	 */
	default Case<T,R,Function<T,R>> andOfValue(T and){
		return compose((Case)Case.of(input -> Objects.equals(input,and),Function.identity()));
	}
	
	/**
	 * Insert a guard that decomposes input values and compares against the supplied values
	 * Recursive decomposition is possible via Predicates.type and Predicates.with methods
	 * @see Predicates#type
	 * @see Predicates#with
	 * 
	 * {@code
	 *  val case2 = Case.of((Person p)-&gt;p.age&gt;18,p-&gt;p.name + &quot; can vote&quot;);
	 *	assertThat(case2.andWithValues(__,__,Predicates.with(__,__,&quot;Ireland&quot;)).match(new Person(&quot;bob&quot;,19,new Address(10,&quot;dublin&quot;,&quot;Ireland&quot;))).isPresent(),is(true));
	 *	assertThat(case2.andWithValues(__,__,with(__,__,&quot;Ireland&quot;)).match(new Person(&quot;bob&quot;,17,new Address(10,&quot;dublin&quot;,&quot;Ireland&quot;))).isPresent(),is(false));
     *
     *
     * \@Value static final class Person implements Decomposable{ String name; int age; Address address; }
	 * \@Value static final  class Address implements Decomposable { int number; String city; String country;}
     *
	 * }
	 * 
	 * 
	 * @param with values to compare to - or predicates or hamcrest matchers
	 * @return New case with test against decomposed values inserted as a guard
	 */
	default Case<T,R,Function<T,R>> andWithValues(Object... with){
		
		return compose((Case)Case.of(new ADTPredicateBuilder(Object.class).with(with),Function.identity()));
	}
	/**
	 * Compose a new Case which executes the Predicate and function supplied before the current predicate
	 * and function.
	 * 
	 * {@code 
	 * e.g. match(100)
	 *  new Predicate passes (100) -> apply new function (100) returns 20
	 *  			-> predicate from current case recieves (20) passes
	 *  				-> apply function from current case recieves (20)
	 * } 
	 *  both predicates must pass or Optional.empty() is returned from match.
	 * 
	 * {@code
	 * case1 =Case.of(input->true,input->input+10);
	 * 
	 * assertThat(case1.composeAnd(p->false,(Integer input)->input*2).match(100).isPresent(),is(false));
	 * 
	 * assertThat(case1.composeAnd(p->true,(Integer input)->input*2).match(100).get(),is(210));
	 * 
	 * }
	 * @param and Predicate to be and'd with current predicate
	 * @param before
	 * @return
	 */
	default  <T1> Case<T1,R,Function<T1,R>>  composeAnd(Predicate<T1> and, Function<T1,T> before){
		return compose(Case.of(and,before));
	}
	/**
	 * @return true if this not an EmptyCase
	 */
	default boolean isNotEmpty(){
		return !this.isEmpty();
	}
	/**
	 * Match against the supplied value.
	 * Value will be passed into the current predicate
	 * If it passes / holds, value will be passed to the current function.
	 * The result of function application will be returned wrapped in an Optional.
	 * If the predicate does not hold, Optional.empty() is returned.
	 * 
	 * @param value To match against
	 * @return Optional.empty if doesn't match, result of the application of current function if it does wrapped in an Optional
	 */
	default Optional<R> match(T value){
		if(get().v1.test(value))
			return Optional.of(get().v2.apply(value));
		return Optional.empty();
	}
	/**
	 * Similar to Match, but executed asynchonously on supplied Executor.
	 * 
	 * @see #match
     *
	 * Match against the supplied value.
	 * Value will be passed into the current predicate
	 * If it passes / holds, value will be passed to the current function.
	 * The result of function application will be returned wrapped in an Optional.
	 * If the predicate does not hold, Optional.empty() is returned.
     *
	 * @param executor Executor to execute matching on
	 * @param value Value to match against
	 * @return A CompletableFuture that will eventual contain an Optional.empty if doesn't match, result of the application of current function if it does
	 */
	default CompletableFuture<Optional<R>> matchAsync(Executor executor, T value){
		return CompletableFuture.supplyAsync(()->match(value),executor);
	}
	/**
	 * Construct an instance of Case from supplied predicate and action
	 * 
	 * @param predicate That will be used to match
	 * @param action Function that is executed on succesful match
	 * @return New Case instance
	 */
	public static <T,R,X extends Function<T,R>> Case<T,R,X> of(Predicate<T> predicate,X action){
		return new ActiveCase<T,R,X>(Two.tuple(predicate,action));
	}
	/**
	 *  Construct an instance of Case from supplied Tuple of predicate and action
	 * 
	 * @param pattern containing the predicate that will be used to match  and the function that is executed on succesful match
	 * @return New Case instance
	 */
	public static <T,R,X extends Function<T,R>> Case<T,R,X> of(Two<Predicate<T>,X> pattern){
		return new ActiveCase<>(pattern);
	}
	
	public static final Case empty = new EmptyCase();
	/**
	 * @return EmptyCase
	 */
	public static <T,R,X extends Function<T,R>> Case<T,R,X> empty(){
		return empty;
	}
}
