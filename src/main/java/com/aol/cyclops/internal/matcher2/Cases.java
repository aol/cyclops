package com.aol.cyclops.internal.matcher2;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.Decomposable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

/**
 * Represents an ordered list of pattern matching cases.
 * 
 * @author johnmcclean
 *
 * @param <T>  Input type for predicate and function (action)
 * @param <R>  Return type for function (action) which is executed if the predicate tests positive
 */
@AllArgsConstructor
public class Cases<T, R> implements Function<T, Maybe<R>> {
    @Wither
    private final PStack<Case<T, R>> cases;
    @Wither(AccessLevel.PRIVATE)
    private final boolean sequential;

    Cases() {
        cases = ConsPStack.empty();
        sequential = true;
    }

    public PStack<Case<T, R>> get() {
        return cases;
    }

    /**
     * Construct a Cases instance from a persistent stack of Pattern Matching Cases
     * Will execute sequentially when Match is called.
     * 
    * @param cases Persistent Stack of cases to build Cases from
    * @return  New Cases instance (sequential)
    */
    public static <T, R> Cases<T, R> ofPStack(final PStack<Case<T, R>> cases) {
        return new Cases<>(
                           cases, true);
    }

    /**
     * Construct a Cases instance from a list of Pattern Matching Cases
     * Will execute sequentially when Match is called.
     * 
    * @param cases Persistent Stack of cases to build Cases from
    * @return  New Cases instance (sequential)
    */
    public static <T, R> Cases<T, R> ofList(final List<Case<T, R>> cases) {
        return new Cases<>(
                           cases.stream()
                                .map(ConsPStack::singleton)
                                .reduce(ConsPStack.empty(), (acc, next) -> acc.plus(acc.size(), next.get(0))),
                           true);
    }

    /**
     * Construct a Cases instance from an array Pattern Matching Cases
     * Will execute sequentially when Match is called.
     * 
     * @param cazes Array of cases to build Cases instance from 
     * @return New Cases instance (sequential)
     */
    public static <T, R> Cases<T, R> of(final Case<T, R>... cazes) {
        return ofPStack(Stream.of(cazes)
                              .map(ConsPStack::singleton)
                              .reduce(ConsPStack.empty(), (acc, next) -> acc.plus(acc.size(), next.get(0))));

    }

    /**
     * Append an individual case with supplied Cases inserted at index
     * @see #merge
     * @param index to insert supplied cases in new Case instance
     * @param pattern Cases to append / insert
     * @return New Cases with current and supplied cases
     */
    public Cases<T, R> append(final int index, final Case<T, R> pattern) {
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
        return (final T1 t) -> (X) apply((T) t).get();
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
    @Override
    public Maybe<R> apply(final T t) {
        return match(t);
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
    public <R> Stream<R> matchFromStream(final Stream<T> s) {

        final Stream<Maybe<R>> results = s.<Maybe<R>> map(this::match);
        return results.filter(Maybe::isPresent)
                      .map(Maybe::get);
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
    public <R> CompletableFuture<Stream<R>> matchFromStreamAsync(final Executor executor, final Stream<T> s) {
        return CompletableFuture.supplyAsync(() -> matchFromStream(s), executor);
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
    public <R> Maybe<R> match(final Object... t) {
        return match((T) Arrays.asList(t));
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
     * @return Matched value wrapped in CompletableFuture &amp; Optional
     */
    public <R> CompletableFuture<Maybe<R>> matchAsync(final Executor executor, final Object... t) {
        return CompletableFuture.supplyAsync(() -> match(t), executor);
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
     *	\@Value static class MyClass implements Decomposable{ int value; String name; }		
     * </pre>
     * @param t Object to decompose and match on
     * @return Matched result wrapped in an Optional
     */
    public <R> Maybe<R> unapply(final Decomposable t) {
        return match((T) t.unapply());
    }

    /**
     * @param t
     *            Object to match against supplied cases
     * @return Value returned from matched case (if present) otherwise
     *         Optional.empty()
     */
    public <R> Maybe<R> match(final T t) {

        return Maybe.fromOptional((Optional) stream().map(pattern -> pattern.match(t))
                                                     .filter(Optional::isPresent)
                                                     .map(Optional::get)
                                                     .findFirst());

    }

    public Stream<Case<T, R>> stream() {
        return this.cases.stream();
    }

}
