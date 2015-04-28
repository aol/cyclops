package com.aol.cyclops.matcher;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

@AllArgsConstructor
public class Cases implements Function {
	@Wither
	private final PStack<Case> cases;
	@Wither(AccessLevel.PRIVATE)
	private final boolean sequential;

	public Cases() {
		cases = ConsPStack.empty();
		sequential =true;
	}

	private static <T> Collector<T, PStack<T>, PStack<T>> collector() {
		final Supplier<PStack<T>> supplier = ConsPStack::empty;
		final BiConsumer<PStack<T>, T> accumulator = PStack::plus;
		final BinaryOperator<PStack<T>> combiner = (left, right) -> {
			left.plusAll(right);
			return left;
		};

		return Collector.of(supplier, accumulator, combiner);
	}
	
	/**
	 * This is the default behaviour, cases are matched sequentially when a matching function is called
	 * @return New Cases object that will match sequentially 
	 * 
	 */
	public Cases sequential(){
		return this.withSequential(true);
	}
	/**
	 * Default behaviour is to match sequentially
	 * @return A new cases object that will match in parallel
	 */
	public Cases parallel(){
		return this.withSequential(false);
	}
	
	
	public Cases merge(Cases patterns){
		return this.withCases(cases.plusAll(size(), patterns.cases));
	}

	public Cases filter(Predicate<Case> predicate) {
		return withCases(cases.stream().filter(data -> predicate.test(data))
				.collect(collector()));
	}

	public Cases filterPredicate(Predicate<Predicate> predicate) {
		return withCases(cases.stream()
				.filter(data -> predicate.test(data.getPredicate()))
				.collect(collector()));
	}

	public Cases filterFunction(Predicate<Function> predicate) {
		return withCases(cases.stream()
				.filter(data -> predicate.test(data.getAction()))
				.collect(collector()));
	}

	public Cases mapPredicate(Function<Predicate, Predicate> predicateMapper) {
		return map(caseData -> {
			return Case.of(predicateMapper.apply(caseData.getPredicate()),
					caseData.getAction());
		});
	}

	public Cases mapFunction(
			Function<Function, Function> actionMapper) {
		return map(caseData -> {
			return Case.of(caseData.getPredicate(),
					actionMapper.apply(caseData.getAction()));
		});
	}

	public Cases map(Function<Case, Case> mapper) {

		return this.withCases(cases.stream().map(mapper).collect(collector()));

	}

	public Cases flatMap(Function<PStack<Case>, Cases> mapper) {
		return mapper.apply(cases);
	}

	public Cases append(int index, Case pattern) {
		return this.withCases(cases.plus(index, pattern));
	}

	public int size() {
		return cases.size();
	}

	public <T, X> Function<T, X> asUnwrappedFunction() {
		return (T t) -> (X) apply(t).get();
	}

	public <T, X> Function<T, Stream<X>> asStreamFunction() {

		return (T t) -> (Stream<X>) Stream.of(t).map(this::apply)
				.filter(Optional::isPresent).map(Optional::get);
	}

	/*
	 * @param t Object to match against
	 * 
	 * @return Value from matched case if present
	 * 
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	public Optional<Object> apply(Object t) {
		return match(t);
	}

	/**
	 * Each input element can generated multiple matched values
	 * 
	 * @param s
	 *            Stream of data to match against (input to matcher)
	 * @return Stream of values from matched cases
	 */
	public <R> Stream<R> matchManyFromStream(Stream s) {
		return s.flatMap(this::matchMany);
	}
	public <R> CompletableFuture<Stream<R>> matchManyFromStreamAsync(Executor executor, Stream s){
		return CompletableFuture.supplyAsync(()->matchManyFromStream(s), executor);
	}

	/**
	 * 
	 * @param t
	 *            input to match against - can generate multiple values
	 * @return Stream of values from matched cases for the input
	 */
	public <R> Stream<R> matchMany(Object t) {
		return (Stream) stream().map(pattern -> pattern.match(t))
				.filter(Optional::isPresent).map(Optional::get);

	}
	public <R> CompletableFuture<Stream<R>> matchManyAsync(Executor executor, Object t){
		return CompletableFuture.supplyAsync(()->matchMany(t), executor);
	}
	/**
	 * Each input element can generated a single matched value
	 * 
	 * @param s
	 *            Stream of data to match against (input to matcher)
	 * @return Stream of matched values, one case per input value can match
	 */
	public <R> Stream<R> matchFromStream(Stream s) {

		Stream<Optional<R>> results = s.<Optional<R>> map(this::match);
		return results.filter(Optional::isPresent).map(Optional::get);
	}
	public <R> CompletableFuture<Stream<R>> matchFromStreamAsync(Executor executor, Stream s){
		return CompletableFuture.supplyAsync(()->matchFromStream(s), executor);
	}
	
	public <R> Optional<R> match(Object... t) {
		return match(Arrays.asList(t));
	}
	public <R> CompletableFuture<Optional<R>> matchAsync(Executor executor, Object... t){
		return CompletableFuture.supplyAsync(()->match(t), executor);
	}
	public <R> Optional<R> unapply(Decomposable t) {
		return match(t.unapply());
	}

	/**
	 * @param t
	 *            Object to match against supplied cases
	 * @return Value returned from matched case (if present) otherwise
	 *         Optional.empty()
	 */
	public <R> Optional<R> match(Object t) {

		return (Optional) stream().map(pattern -> pattern.match(t))
				.filter(Optional::isPresent).map(Optional::get).findFirst();

	}
	
	
	private Stream<Case> stream(){
		if(this.sequential)
			return cases.stream();
		return cases.parallelStream();
	}

}
