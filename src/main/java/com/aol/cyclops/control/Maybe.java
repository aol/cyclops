package com.aol.cyclops.control;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.MonadicValue1;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.function.Curry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Totally lazy more powerful general Option(al) type. Maybe is lazy like a Java
 * 8 Stream that represents 0 or 1 values rather than eager like a Java 8
 * Optional. map / peek/ filter and flatMap build the execution chaing, but are
 * not executed until the value inside the Maybe is required.
 * 
 * Maybe is tail recursive
 * 
 * <pre>
 * {@code 
 * &#64;Test
    public void odd() {
        System.out.println(even(Maybe.just(200000)).get());
    }

    public Maybe<String> odd(Maybe<Integer> n) {

        return n.flatMap(x -> even(Maybe.just(x - 1)));
    }

    public Maybe<String> even(Maybe<Integer> n) {
        return n.flatMap(x -> {
            return x <= 0 ? Maybe.just("done") : odd(Maybe.just(x - 1));
        });
    }
 * 
 * }
 * </pre>
 * 
 * Maybe is a functor (map) monad (flatMap) and an applicative (ap)
 * 
 * Maybe has pattern matching built in (visit, matches, patternMatch)
 * 
 * Maybe is convertable to all cyclops-react data types.
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Maybe<T>
		extends MonadicValue1<T>, Supplier<T>, ConvertableFunctor<T>, Filterable<T>, ApplicativeFunctor<T>, Matchable.ValueAndOptionalMatcher<T> {

	final static Maybe EMPTY = new Nothing<>();

	static <T> Maybe<T> none() {
		return EMPTY;
	}

	@Override
	default <R> Maybe<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		return (Maybe<R>) MonadicValue1.super.flatMapIterable(mapper);
	}

	@Override
	default <R> Maybe<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
		MonadicValue<R> m = MonadicValue1.super.flatMapPublisher(mapper);
		return (Maybe<R>) m;
	}

	public static <T> Maybe<T> fromPublisher(final Publisher<T> pub) {
		final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
		pub.subscribe(sub);
		return sub.toMaybe();
	}

	static <T> Maybe<T> fromIterable(final Iterable<T> iterable) {
		return Maybe.fromEvalOf(Eval.fromIterable(iterable));
	}

	static <T> Maybe<T> fromOptional(final Optional<T> opt) {
		if (opt.isPresent())
			return Maybe.of(opt.get());
		return none();
	}

	static <T> Maybe<T> fromEvalOf(final Eval<T> eval) {
		return new Just<T>(eval);
	}

	static <T> Maybe<T> just(final T value) {
		return of(value);
	}

	static <T> Maybe<T> of(final T value) {
		Objects.requireNonNull(value);
		return new Just<T>(Eval.later(() -> value));
	}

	static <T> Maybe<T> ofNullable(final T value) {

		if (value != null)
			return of(value);
		return none();
	}

	static <T> Maybe<T> narrow(final Maybe<? extends T> broad) {
		return (Maybe<T>) broad;
	}

	public static <T> Maybe<ListX<T>> sequenceJust(final CollectionX<Maybe<T>> opts) {
		final Maybe<ListX<T>> unwrapped = AnyM.sequence(opts.map(o -> AnyM.fromMaybe(o))).unwrap();
		return unwrapped;
	}

	public static <T> Maybe<ListX<T>> sequence(final CollectionX<Maybe<T>> maybes) {
		return sequence(maybes.stream()).map(s -> s.toListX());

	}

	public static <T> Maybe<ReactiveSeq<T>> sequence(final Stream<Maybe<T>> maybes) {
		return AnyM.sequence(maybes.map(f -> AnyM.fromMaybe(f)), () -> AnyM.fromMaybe(Maybe.just(Stream.<T> empty()))).map(s -> ReactiveSeq.fromStream(s))
				.unwrap();

	}

	public static <T, R> Maybe<R> accumulateJust(final CollectionX<Maybe<T>> maybes, final Reducer<R> reducer) {
		return sequenceJust(maybes).map(s -> s.mapReduce(reducer));
	}

	public static <T, R> Maybe<R> accumulateJust(final CollectionX<Maybe<T>> maybes, final Function<? super T, R> mapper, final Semigroup<R> reducer) {
		return sequenceJust(maybes).map(s -> s.map(mapper).reduce(reducer.reducer()).get());
	}

	public static <T> Maybe<T> accumulateJust(final CollectionX<Maybe<T>> maybes, final Semigroup<T> reducer) {
		return sequenceJust(maybes).map(s -> s.reduce(reducer.reducer()).get());
	}

	/*
	 * Apply a function across to values at once. If this Maybe is none, or the
	 * supplied value represents none Maybe.none is returned. Otherwise a Maybe
	 * with the function applied with this value and the supplied value is
	 * returned
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.cyclops.types.applicative.ApplicativeFunctor#combine(com.aol.
	 * cyclops.types.Value, java.util.function.BiFunction)
	 */
	@Override
	default <T2, R> Maybe<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

		return map(v -> Tuple.tuple(v, Curry.curry2(fn).apply(v))).flatMap(tuple -> app.visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));
	}

	/*
	 * Equivalent to combine, but accepts an Iterable and takes the first value
	 * only from that iterable. (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable,
	 * java.util.function.BiFunction)
	 */
	@Override
	default <T2, R> Maybe<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

		return map(v -> Tuple.tuple(v, Curry.curry2(fn).apply(v)))
				.flatMap(tuple -> Maybe.fromIterable(app).visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));
	}

	/**
	 * Equivalent to combine, but accepts a Publisher and takes the first value
	 * only from that publisher.
	 * 
	 * @param app
	 * @param fn
	 * @return
	 */
	/*
	 * Equivalent to combine, but accepts a Publisher and takes the first value
	 * only from that publisher. (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.Zippable#zip(java.util.function.BiFunction,
	 * org.reactivestreams.Publisher)
	 */
	@Override
	default <T2, R> Maybe<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
		return map(v -> Tuple.tuple(v, Curry.curry2(fn).apply(v)))
				.flatMap(tuple -> Maybe.fromPublisher(app).visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq,
	 * java.util.function.BiFunction)
	 */
	@Override
	default <U, R> Maybe<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
		return (Maybe<R>) MonadicValue1.super.zip(other, zipper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream,
	 * java.util.function.BiFunction)
	 */
	@Override
	default <U, R> Maybe<R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

		return (Maybe<R>) MonadicValue1.super.zip(other, zipper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
	 */
	@Override
	default <U> Maybe<Tuple2<T, U>> zip(final Stream<? extends U> other) {

		return (Maybe) MonadicValue1.super.zip(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> Maybe<Tuple2<T, U>> zip(final Seq<? extends U> other) {

		return (Maybe) MonadicValue1.super.zip(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
	 */
	@Override
	default <U> Maybe<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

		return (Maybe) MonadicValue1.super.zip(other);
	}

	@Override
	default <T> Maybe<T> unit(final T unit) {
		return Maybe.of(unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
	 */
	@Override
	default <R> Maybe<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
		return (Maybe<R>) MonadicValue1.super.coflatMap(mapper);
	}

	/*
	 * cojoin (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.MonadicValue#nest()
	 */
	@Override
	default Maybe<MonadicValue<T>> nest() {
		return (Maybe<MonadicValue<T>>) MonadicValue1.super.nest();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.types.MonadicValue2#combine(com.aol.cyclops.Monoid,
	 * com.aol.cyclops.types.MonadicValue2)
	 */
	@Override
	default Maybe<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
		return (Maybe<T>) MonadicValue1.super.combineEager(monoid, v2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.value.Value#toMaybe()
	 */
	@Override
	default Maybe<T> toMaybe() {
		return this;
	}

	@Override
    boolean isPresent();

	Maybe<T> recover(Supplier<T> value);

	Maybe<T> recover(T value);

	@Override
    <R> Maybe<R> map(Function<? super T, ? extends R> mapper);

	@Override
    <R> Maybe<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);

	@Override
    <R> R visit(Function<? super T, ? extends R> some, Supplier<? extends R> none);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.
	 * Predicate)
	 */
	@Override
    Maybe<T> filter(Predicate<? super T> fn);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.lambda.monads.Filterable#ofType(java.lang.Class)
	 */
	@Override
	default <U> Maybe<U> ofType(final Class<? extends U> type) {

		return (Maybe<U>) Filterable.super.ofType(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.
	 * Predicate)
	 */
	@Override
	default Maybe<T> filterNot(final Predicate<? super T> fn) {

		return (Maybe<T>) Filterable.super.filterNot(fn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	@Override
	default Maybe<T> notNull() {

		return (Maybe<T>) Filterable.super.notNull();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> Maybe<U> cast(final Class<? extends U> type) {

		return (Maybe<U>) ApplicativeFunctor.super.cast(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	@Override
	default Maybe<T> peek(final Consumer<? super T> c) {

		return (Maybe<T>) ApplicativeFunctor.super.peek(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.
	 * Function)
	 */
	@Override
	default <R> Maybe<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

		return (Maybe<R>) ApplicativeFunctor.super.trampoline(mapper);
	}

	@Override
	default <R> Maybe<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {

		return (Maybe<R>) ApplicativeFunctor.super.patternMatch(case1, otherwise);
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Just<T> implements Maybe<T> {

		private final Eval<T> lazy;

		@Override
		public <R> Maybe<R> map(final Function<? super T, ? extends R> mapper) {
			return new Just<>(lazy.map(t -> mapper.apply(t)));
		}

		@Override
		public <R> Maybe<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
			return new Lazy(Eval.later(() -> this)).flatMap(mapper);

		}

		@Override
		public Maybe<T> filter(final Predicate<? super T> test) {
			if (test.test(lazy.get()))
				return this;
			return EMPTY;
		}

		@Override
		public <R> R visit(final Function<? super T, ? extends R> some, final Supplier<? extends R> none) {
			return map(some).get();
		}

		@Override
		public Maybe<T> recover(final T value) {
			return this;
		}

		@Override
		public Maybe<T> recover(final Supplier<T> value) {
			return this;
		}

		@Override
		public String toString() {
			return mkString();
		}

		@Override
		public T get() {
			return lazy.get();
		}

		@Override
		public boolean isPresent() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(lazy.get());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof Just)
				return Objects.equals(lazy.get(), ((Just) obj).get());
			else if (obj instanceof Lazy) {
				return Objects.equals(get(), ((Lazy) obj).get());
			}
			return false;
		}

		@Override
		public T orElse(final T value) {
			return lazy.get();
		}

		@Override
		public T orElseGet(final Supplier<? extends T> value) {
			return lazy.get();
		}
		
		@Override
		public <R> Just<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
			Maybe<R> maybe = Maybe.super.flatMapIterable(mapper);
			return (Just<R>) Maybe.just(maybe.get());
		}

		@Override
		public <R> Just<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
			Maybe<R> m = Maybe.super.flatMapPublisher(mapper);
			return (Just<R>) Maybe.just(m.get());
		}

	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	static final class Lazy<T> implements Maybe<T> {

		private final Eval<Maybe<T>> lazy;

		@Override
		public <R> Maybe<R> map(final Function<? super T, ? extends R> mapper) {
			return flatMap(t -> Maybe.just(mapper.apply(t)));
		}

		@Override
		public <R> Maybe<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
			final Supplier<Eval<Maybe<R>>> s = () -> Eval.later(() -> Maybe.none());
			final Eval<Maybe<R>> eval = lazy.get().visit(some -> Eval.later(() -> narrow(mapper.apply(some).toMaybe())), s);
			return new Lazy<R>(eval);

		}

		@Override
		public Maybe<T> filter(final Predicate<? super T> test) {
			return flatMap(t -> test.test(t) ? this : Maybe.none());
		}

		@Override
		public <R> R visit(final Function<? super T, ? extends R> some, final Supplier<? extends R> none) {
			final Maybe<R> mapped = map(some);
			if (isPresent()) {
				return mapped.get();
			}
			return none.get();

		}

		@Override
		public Maybe<T> recover(final T value) {
			return new Lazy<T>(lazy.map(m -> m.recover(value)));
		}

		@Override
		public Maybe<T> recover(final Supplier<T> value) {
			return new Lazy<T>(lazy.map(m -> m.recover(value)));
		}

		@Override
		public String toString() {
			Maybe<T> maybe = lazy.get();
			while (maybe instanceof Lazy) {
				maybe = ((Lazy<T>) maybe).lazy.get();
			}
			return maybe.mkString();
		}

		@Override
		public T get() {
			Maybe<T> maybe = lazy.get();
			while (maybe instanceof Lazy) {
				maybe = ((Lazy<T>) maybe).lazy.get();
			}
			return maybe.get();
		}

		@Override
		public boolean isPresent() {
			Maybe<T> maybe = lazy.get();
			while (maybe instanceof Lazy) {
				maybe = ((Lazy<T>) maybe).lazy.get();
			}
			return maybe.isPresent();
		}

		@Override
		public T orElse(final T value) {
			Maybe<T> maybe = lazy.get();
			while (maybe instanceof Lazy) {
				maybe = ((Lazy<T>) maybe).lazy.get();
			}
			return maybe.orElse(value);
		}

		@Override
		public T orElseGet(final Supplier<? extends T> value) {
			Maybe<T> maybe = lazy.get();
			while (maybe instanceof Lazy) {
				maybe = ((Lazy<T>) maybe).lazy.get();
			}
			return maybe.orElseGet(value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			Maybe<T> maybe = lazy.get();
			while (maybe instanceof Lazy) {
				maybe = ((Lazy<T>) maybe).lazy.get();
			}
			return Objects.hashCode(maybe.get());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {

			if (obj instanceof Just)
				return Objects.equals(get(), ((Just) obj).get());
			else if (obj instanceof Nothing) {
				return !isPresent();
			} else if (obj instanceof Lazy) {
				if (isPresent())
					return Objects.equals(get(), ((Lazy) obj).get());
				else {
					return !((Lazy) obj).isPresent();
				}
			}
			return false;
		}
		
		@Override
		public <R> Lazy<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
			Maybe<R> m = Maybe.super.flatMapIterable(mapper);
			return new Lazy(Eval.later(() -> m.get()));
		}

		@Override
		public <R> Lazy<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
			Maybe<R> m = (Lazy<R>) Maybe.super.flatMapPublisher(mapper);
			return new Lazy(Eval.later(() -> m.get()));
		}

	}

	public static class Nothing<T> implements Maybe<T> {

		@Override
		public <R> Maybe<R> map(final Function<? super T, ? extends R> mapper) {
			return EMPTY;
		}

		@Override
		public <R> Maybe<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
			return EMPTY;

		}

		@Override
		public Maybe<T> filter(final Predicate<? super T> test) {
			return EMPTY;
		}

		@Override
		public T get() {
			return Optional.<T> ofNullable(null).get();
		}

		@Override
		public Maybe<T> recover(final T value) {
			return Maybe.of(value);
		}

		@Override
		public Maybe<T> recover(final Supplier<T> value) {
			return new Just<>(Eval.later(value));
		}

		@Override
		public <R> R visit(final Function<? super T, ? extends R> some, final Supplier<? extends R> none) {
			return none.get();
		}

		@Override
		public Optional<T> toOptional() {
			return Optional.ofNullable(null);
		}

		@Override
		public String toString() {
			return mkString();
		}

		@Override
		public boolean isPresent() {
			return false;
		}

		@Override
		public boolean equals(final Object obj) {

			if (obj instanceof Nothing)
				return true;
			if (obj instanceof Lazy) {
				return !((Lazy) obj).isPresent();
			}
			return false;
		}

		@Override
		public T orElse(final T value) {
			return value;
		}

		@Override
		public T orElseGet(final Supplier<? extends T> value) {
			return value.get();
		}
		
		@Override
		public <R> Nothing<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
			return (Nothing<R>) EMPTY;
		}

		@Override
		public <R> Nothing<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
			return (Nothing<R>) EMPTY;
		}
	}

}
