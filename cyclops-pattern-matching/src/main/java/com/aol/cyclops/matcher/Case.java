package com.aol.cyclops.matcher;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
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
public interface Case<T,R,X extends Function<T,R>> {
	/**
	 * @return true is this an 'empty' case
	 */
	public boolean isEmpty();
	/**
	 * @return Pattern for this case
	 */
	public Tuple2<Predicate<T>,X> getPattern();
	
	/**
	 * @return Predicate for this case
	 */
	default Predicate<T> getPredicate(){
		return getPattern().v1;
	}
	/**
	 * @return Action (Function) for this case
	 */
	default X getAction(){
		return getPattern().v2;
	}
	
	/**
	 * @return A new case with the predicate negated
	 * 
	 * <pre>
	 *     Case.of((Integer num) -> num >100,(Integer num) -> num * 10).negate();
	 *     
	 * </pre>
	 * 
	 * Results in a case that will multiply numbers 100 or less by 10
	 * 
	 */
	default Case<T,R,X> negate(){
		return map(t2-> Tuple.tuple(t2.v1.negate(),t2.v2));
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
		return map(t2-> Tuple.tuple(t2.v1.negate(),action));
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
	default  Case<T,R,X> filter(Predicate<Tuple2<Predicate<T>,X>> predicate){
		return predicate.test(getPattern()) ? this : empty();
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
		return this.map(t2-> Tuple.tuple(mapper.apply(t2.v1),t2.v2));
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
		return this.<T,R1,X1>map(t2-> Tuple.tuple(t2.v1,mapper.apply(t2.v2)));
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
	default <T1,R1,X1 extends Function<T1,R1>> Case<T1,R1,X1> map(Function<Tuple2<Predicate<T>,X>,Tuple2<Predicate<T1>,X1>> mapper){
		return Case.of(mapper.apply(getPattern()));	
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
	default <T1,R1,X1 extends Function<T1,R1>> Case<T1,R1,X1> flatMap(Function<Tuple2<Predicate<T>,X>,Case<T1,R1,X1>> mapper){
		return mapper.apply(getPattern());	
	}
	
	
	default  Case<T,T,Function<T,T>> andThen(Case<R,T,? extends Function<R,T>> after){
		return after.compose(this);
	}
	
	default <T1> Case<T1,R,Function<T1,R>> compose(Case<T1,T,? extends Function<T1,T>> before){
		
		final Object[] array = {null};
		Predicate<T1> predicate =t-> {
			final boolean passed;
			if(before.getPattern().v1.test(t)){
				passed=true;
				array[0]=before.getPattern().v2.apply(t);
			}else
				passed= false;
			return  passed && getPattern().v1.test((T)array[0]);
		};
		
		return Case.<T1,R,Function<T1,R>>of(predicate,  input -> getPattern().v2.apply((T)array[0]));
		
	}
	default <T1 > Case<T1,R,Function<T1,R>> composeOr(Case<T1,T,? extends Function<T1,T>> before){
		
		
		return Case.<T1,R,Function<T1,R>>of(t->   before.getPattern().v1.test(t) || getPattern().v1.test(before.getPattern().v2.apply(t)),
				input -> getPattern().v2.apply(before.getPattern().v2.apply(input)));
		
	}
	default <T1> Case<T1,R,Function<T1,R>> composeFunction(Function<T1,T> before){
		return this.compose(Case.<T1,T,Function<T1,T>>of(t->true,before));
	}
	default  Case<T,T,Function<T,T>> andThenFunction(Function<R,T>  after){
		return this.andThen(Case.<R,T,Function<R,T>>of(r->true,after));
	}
	default Case<T,R,Function<T,R>> or(Predicate<T> or){
		return composeOr(Case.of(or,Function.identity()));
		
	}
	default<T1> Case<T1,R,Function<T1,R>> or(Predicate<T1> or, Function<T1,T> fn){
		return composeOr(Case.of(or,fn));
		
	}
	/**
	 * Create a new case which ands the supplied predicate with the current predicate.
	 * 
	 * <pre>
	 * assertThat(case1.and(p->false).match(100).isPresent(),is(false));
	 *
	 * assertThat(case1.and(p->true).match(100).isPresent(),is(true));
	 * 
	 * </pre>
	 * 
	 * @param and New Predicate to be and'd with current
	 * 
	 * @return New case instance which and's current predicate with supplied
	 */
	default Case<T,R,Function<T,R>> and(Predicate<T> and){
		return compose(Case.of(and,Function.identity()));
	}
	
	/**
	 * Compose a new Case which executes the Predicate and function supplied before the current predicate
	 * and function.
	 * 
	 * e.g. match(100)
	 *  new Predicate passes (100) -> apply new function (100) returns 20
	 *  			-> predicate from current case recieves (20) passes
	 *  				-> apply function from current case recieves (20)
	 *  
	 *  both predicates must pass or Optional.empty() is returned from match.
	 * 
	 * <pre>
	 * case1 =Case.of(input->true,input->input+10);
	 * 
	 * assertThat(case1.composeAnd(p->false,(Integer input)->input*2).match(100).isPresent(),is(false));
	 * 
	 * assertThat(case1.composeAnd(p->true,(Integer input)->input*2).match(100).get(),is(210));
	 * 
	 * </pre>
	 * @param and Predicate to be and'd with current predicate
	 * @param before
	 * @return
	 */
	default  <T1> Case<T1,R,Function<T1,R>>  composeAnd(Predicate<T1> and, Function<T1,T> before){
		return compose(Case.of(and,before));
	}
	default boolean isNotEmpty(){
		return !this.isEmpty();
	}
	default Optional<R> match(T value){
		if(getPattern().v1.test(value))
			return Optional.of(getPattern().v2.apply(value));
		return Optional.empty();
	}
	default CompletableFuture<Optional<R>> matchAsync(Executor executor, T value){
		return CompletableFuture.supplyAsync(()->match(value),executor);
	}
	public static <T,R,X extends Function<T,R>> Case<T,R,X> of(Predicate<T> predicate,X action){
		return new ActiveCase<T,R,X>(Tuple.tuple(predicate,action));
	}
	public static <T,R,X extends Function<T,R>> Case<T,R,X> of(Tuple2<Predicate<T>,X> pattern){
		return new ActiveCase<>(pattern);
	}
	
	public static final Case empty = new EmptyCase();//Tuple.<Predicate,Function>tuple(t->false,input->input),true);
	public static <T,R,X extends Function<T,R>> Case<T,R,X> empty(){
		return empty;
	}
}
