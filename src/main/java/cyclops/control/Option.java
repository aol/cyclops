package cyclops.control;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.matching.Sealed2;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.recoverable.Recoverable;
import cyclops.async.Future;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.lazy.Maybe;
import cyclops.control.lazy.Trampoline;
import cyclops.function.*;
import cyclops.control.anym.AnyM;
import cyclops.control.anym.Witness;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

/*
 Eager / strict Option type. Base type for the lazy / reactive Maybe type.
 Less powerful, but may perform better than Maybe (simpler Object structure)
 */
public interface Option<T> extends To<Option<T>>,
                                   OrElseValue<T,Option<T>>,
                                   MonadicValue<T>,
                                   Recoverable<T>,
                                   Sealed2<T,Option.None<T>>,
                                   Iterable<T>{



    @SuppressWarnings("rawtypes")
    final static Option EMPTY = new Option.None<>();

    /**
     * @return Get the zero Maybe (singleUnsafe instance)
     */
    @SuppressWarnings("unchecked")
    static <T> Option<T> none() {
        return EMPTY;
    }

    static <T> Option<T> some(T value){
        return new Option.Some<>(value);
    }

    static <T> Option<T> fromFuture(Future<T> future){
        return future.toOption();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapI(java.util.function.Function)
     */
    @Override
    default <R> Option<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Option<R>) MonadicValue.super.flatMapI(mapper);
    }

    default Option<T> orElseUse(Option<T> opt){
        if(isPresent())
            return this;
        return opt;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapP(java.util.function.Function)
     */
    @Override
    default <R> Option<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            return Option.fromPublisher(publisher);
        });
    }

    /**
     * Construct a Maybe  that contains a singleUnsafe value extracted from the supplied reactiveBuffer-streams Publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

    Option<Integer> maybe = Option.fromPublisher(reactiveStream);

    //Maybe[1]
     *
     * }
     * </pre>
     *
     * @param pub Publisher to extract value from
     * @return Maybe populated with first value from Publisher (Option.zero if Publisher zero)
     */
    public static <T> Option<T> fromPublisher(final Publisher<T> pub) {
        return fromFuture(Future.fromPublisher(pub));
    }

    /**
     *  Construct a Maybe  that contains a singleUnsafe value extracted from the supplied Iterable
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

    Option<Integer> maybe = Option.fromIterable(reactiveStream);

    //Maybe[1]
     *
     * }
     * </pre>
     * @param iterable Iterable  to extract value from
     * @return Maybe populated with first value from Iterable (Option.zero if Publisher zero)
     */
    static <T> Option<T> fromIterable(final Iterable<T> iterable) {
        Iterator<T> it = iterable.iterator();
        if(it.hasNext()){
            return Option.some(it.next());
        }
        return Option.none();

    }


    static <R> Option<R> fromStream(Stream<R> apply) {
        return fromIterable(ReactiveSeq.fromStream(apply));
    }
    /**
     * Construct an equivalent Maybe from the Supplied Optional
     * <pre>
     * {@code
     *   Option<Integer> some = Option.fromOptional(Optional.of(10));
     *   //Maybe[10], Some[10]
     *
     *   Option<Integer> none = Option.fromOptional(Optional.zero());
     *   //Option.zero, None[]
     * }
     * </pre>
     *
     * @param opt Optional to construct Maybe from
     * @return Maybe created from Optional
     */
    static <T> Option<T> fromOptional(final Optional<T> opt) {
        if (opt.isPresent())
            return Option.of(opt.get());
        return none();
    }




    default Trampoline<Maybe<T>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this.toMaybe()));
    }


    /**
     * Construct an Maybe which contains the provided (non-null) value.
     * Alias for @see {@link Maybe#of(Object)}
     *
     * <pre>
     * {@code
     *
     *    Option<Integer> some = Option.just(10);
     *    some.transform(i->i*2);
     * }
     * </pre>
     *
     * @param value Value to wrap inside a Maybe
     * @return Maybe containing the supplied value
     */
    static <T> Option<T> just(final T value) {
        return of(value);
    }

    /**
     * Construct an Maybe which contains the provided (non-null) value
     * Equivalent to @see {@link Maybe#just(Object)}
     * <pre>
     * {@code
     *
     *    Option<Integer> some = Option.of(10);
     *    some.transform(i->i*2);
     * }
     * </pre>
     *
     * @param value Value to wrap inside a Maybe
     * @return Maybe containing the supplied value
     */
    static <T> Option<T> of(final T value) {
        Objects.requireNonNull(value);
        return new Option.Some(value);

    }



    default Maybe<T> lazy(){
        return Maybe.fromIterable(this);
    }
    default Option<T> eager(){
        return this;
    }
    /**
     * <pre>
     * {@code
     *    Option<Integer> maybe  = Option.ofNullable(null);
     *    //None
     *
     *    Option<Integer> maybe = Option.ofNullable(10);
     *    //Maybe[10], Some[10]
     *
     * }
     * </pre>
     *
     *
     * @param value
     * @return
     */
    static <T> Option<T> ofNullable(final T value) {
        if (value != null)
            return of(value);
        return none();
    }

    /**
     * Narrow covariant type parameter
     *
     * @param broad Maybe with covariant type parameter
     * @return Narrowed Maybe
     */
    static <T> Option<T> narrow(final Option<? extends T> broad) {
        return (Option<T>) broad;
    }

    /**
     * Sequence operation, take a Collection of Maybes and turn it into a Maybe with a Collection
     * Only successes are retained. By constrast with {@link Maybe#sequence(CollectionX)} Maybe#zero/ None types are
     * tolerated and ignored.
     *
     * <pre>
     * {@code
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.none();
     *
     * Option<ListX<Integer>> maybes = Option.sequenceJust(ListX.of(just, none, Option.of(1)));
    //Option.of(ListX.of(10, 1));
     * }
     * </pre>
     *
     * @param maybes Maybes to Sequence
     * @return Maybe with a List of values
     */
    public static <T> Option<ListX<T>> sequenceJust(final CollectionX<Option<T>> maybes) {
        return AnyM.sequence(maybes.stream()
                                    .filter(Option::isPresent)
                                    .map(Option::toMaybe)
                            .map(AnyM::fromMaybe).toListX(), Witness.maybe.INSTANCE)
                .to(Witness::maybe).toOption();
    }

    /**
     * Sequence operation, take a Collection of Maybes and turn it into a Maybe with a Collection
     * By constrast with {@link Maybe#sequenceJust(CollectionX)} if any Maybe types are None / zero
     * the return type will be an zero Maybe / None
     *
     * <pre>
     * {@code
     *
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.none();
     *
     *  Option<ListX<Integer>> maybes = Option.sequence(ListX.of(just, none, Option.of(1)));
    //Option.none();
     *
     * }
     * </pre>
     *
     *
     * @param maybes Maybes to Sequence
     * @return  Maybe with a List of values
     */
    public static <T> Option<ListX<T>> sequence(final CollectionX<Option<T>> maybes) {
        return sequence(maybes.stream()).map(s -> s.toListX());

    }

    /**
     * Sequence operation, take a Stream of Maybes and turn it into a Maybe with a Stream
     * By constrast with {@link Maybe#sequenceJust(CollectionX)} Maybe#zero/ None types are
     * result in the returned Maybe being Option.zero / None
     *
     *
     * <pre>
     * {@code
     *
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.none();

     *  Option<ReactiveSeq<Integer>> maybes = Option.sequence(Stream.of(just, none, Option.of(1)));
    //Option.none();
     *
     * }
     * </pre>
     *
     *
     * @param maybes Maybes to Sequence
     * @return  Maybe with a Stream of values
     */
    public static <T> Option<ReactiveSeq<T>> sequence(final Stream<Option<T>> maybes) {
        return AnyM.sequence(maybes.map(Option::toMaybe).map(AnyM::fromMaybe), Witness.maybe.INSTANCE)
                .map(ReactiveSeq::fromStream)
                .to(Witness::maybe).toOption();


    }

    /**
     * Accummulating operation using the supplied Reducer (@see cyclops2.Reducers). A typical use case is to accumulate into a Persistent Collection type.
     * Accumulates the present results, ignores zero Maybes.
     *
     * <pre>
     * {@code
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.none();
     * Option<PersistentSetX<Integer>> maybes = Option.accumulateJust(ListX.of(just, none, Option.of(1)), Reducers.toPersistentSetX());
    //Option.of(PersistentSetX.of(10, 1)));
     *
     * }
     * </pre>
     *
     * @param maybes Maybes to accumulate
     * @param reducer Reducer to accumulate values with
     * @return Maybe with reduced value
     */
    public static <T, R> Option<R> accumulateJust(final CollectionX<Option<T>> maybes, final Reducer<R> reducer) {
        return sequenceJust(maybes).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Maybes which have a value present, using the supplied mapping function to
     * convert the data from each Maybe before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }..
     *
     * <pre>
     * {@code
     *  Option<Integer> just = Option.of(10);
    Option<Integer> none = Option.none();

     *  Option<String> maybes = Option.accumulateJust(ListX.of(just, none, Option.of(1)), i -> "" + i,
    SemigroupK.stringConcat);
    //Option.of("101")
     *
     * }
     * </pre>
     *
     * @param maybes Maybes to accumulate
     * @param mapper Mapping function to be applied to the result of each Maybe
     * @param reducer Monoid to combine values from each Maybe
     * @return Maybe with reduced value
     */
    public static <T, R> Option<R> accumulateJust(final CollectionX<Option<T>> maybes, final Function<? super T, R> mapper,
                                                 final Monoid<R> reducer) {
        return sequenceJust(maybes).map(s -> s.map(mapper)
                .reduce(reducer));
    }

    /**
     * Accumulate the results only from those Maybes which have a value present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.

     *
     * <pre>
     * {@code
     *
     *  Option<Integer> maybes = Option.accumulateJust(Monoids.intSum,ListX.of(just, none, Option.of(1)));
    //Option.of(11)
     *
     * }
     * </pre>
     *
     *
     *
     * @param maybes Maybes to accumulate
     * @param reducer Monoid to combine values from each Maybe
     * @return Maybe with reduced value
     */
    public static <T> Option<T> accumulateJust(final Monoid<T> reducer,final CollectionX<Option<T>> maybes) {
        return sequenceJust(maybes).map(s -> s.reduce(reducer));
    }

    @Override
    default <R> Option<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (Option<R>)MonadicValue.super.zipWith(fn);
    }

    @Override
    default <R> Option<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (Option<R>)MonadicValue.super.zipWithS(fn);
    }

    @Override
    default <R> Option<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (Option<R>)MonadicValue.super.zipWithP(fn);
    }

    @Override
    default <R> Option<R> retry(final Function<? super T, ? extends R> fn) {
        return (Option<R>)MonadicValue.super.retry(fn);
    }

    @Override
    default <U> Option<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (Option)MonadicValue.super.zipP(other);
    }

    @Override
    default <R> Option<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Option<R>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> Option<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Option)MonadicValue.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Option<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Option<R>)MonadicValue.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Option<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Option)MonadicValue.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Option<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Option<R>)MonadicValue.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <R> Option<R> flatMapS(final Function<? super T, ? extends Stream<? extends R>> mapper) {
        return (Option<R>)MonadicValue.super.flatMapS(mapper);
    }

    /* (non-Javadoc)
         * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
         */
    @Override
    default <T2, R1, R2, R3, R> Option<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Option<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Option<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Option<R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Option<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Option<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Option<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Option<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Option<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Option<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Option<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Option<R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }


    /*
     * Apply a function across to values at once. If this Maybe is none, or the
     * supplied value represents none Option.none is returned. Otherwise a Maybe
     * with the function applied with this value and the supplied value is
     * returned
     *
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.applicative.ApplicativeFunctor#combine(com.aol.
     * cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Option<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                .apply(v))).flatMap(tuple -> app.visit(i -> Option.just(tuple._2().apply(i)), () -> Option.none()));
    }

    /*
     * Equivalent to combine, but accepts an Iterable and takes the first value
     * only from that iterable. (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable,
     * java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Option<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                .apply(v))).flatMap(tuple -> Option.fromIterable(app)
                .visit(i -> Option.just(tuple._2().apply(i)), () -> Option.none()));
    }


    /*
     * Equivalent to combine, but accepts a Publisher and takes the first value
     * only from that publisher. (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.function.BiFunction,
     * org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Option<R> zipP(final Publisher<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                .apply(v))).flatMap(tuple -> Option.fromPublisher(app)
                .visit(i -> Option.just(tuple._2().apply(i)), () -> Option.none()));

    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Applicative#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Applicative)
     */
    @Override
    default  Option<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (Option<T>)MonadicValue.super.zip(combiner, app);
    }



    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.stream.Stream,
     * java.util.function.BiFunction)
     */
    @Override
    default <U, R> Option<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (Option<R>) MonadicValue.super.zipS(other, zipper);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> Option<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (Option) MonadicValue.super.zipS(other);
    }


    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Option<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (Option) MonadicValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Option<T> unit(final T unit) {
        return Option.of(unit);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Option<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return (Option<R>) MonadicValue.super.coflatMap(mapper);
    }

    /*
     * cojoin (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    default Option<MonadicValue<T>> nest() {
        return (Option<MonadicValue<T>>) MonadicValue.super.nest();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.MonadicValue2#combine(cyclops2.function.Monoid,
     * com.aol.cyclops2.types.MonadicValue2)
     */
    @Override
    default Option<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return (Option<T>) MonadicValue.super.combineEager(monoid, v2);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.value.Value#toMaybe()
     */
    @Override
    default Maybe<T> toMaybe(){
        return lazy();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Convertable#isPresent()
     */
    @Override
    boolean isPresent();



    Option<T> recover(Supplier<? extends T> value);

    Option<T> recover(T value);

    Option<T> recoverWith(Supplier<? extends Option<T>> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#transform(java.util.function.Function)
     */
    @Override
    <R> Option<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    <R> Option<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Convertable#visit(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    <R> R visit(Function<? super T, ? extends R> some, Supplier<? extends R> none);

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.lambda.monads.Filters#filter(java.util.function.
     * Predicate)
     */
    @Override
    Option<T> filter(Predicate<? super T> fn);

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.lambda.monads.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Option<U> ofType(final Class<? extends U> type) {

        return (Option<U>) MonadicValue.super.ofType(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.lambda.monads.Filters#filterNot(java.util.function.
     * Predicate)
     */
    @Override
    default Option<T> filterNot(final Predicate<? super T> fn) {

        return (Option<T>) MonadicValue.super.filterNot(fn);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default Option<T> notNull() {

        return (Option<T>) MonadicValue.super.notNull();
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default Option<T> peek(final Consumer<? super T> c) {

        return (Option<T>) MonadicValue.super.peek(c);
    }

    @Override
    default <T1> Option<T1> emptyUnit(){
        return Option.none();
    }

    /*
         * (non-Javadoc)
         *
         * @see com.aol.cyclops2.lambda.monads.Functor#trampoline(java.util.function.
         * Function)
         */
    @Override
    default <R> Option<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (Option<R>) MonadicValue.super.trampoline(mapper);
    }

    public static <T> Option<T> fromNullable(T t) {
        if(t==null)
            return none();
        return some(t);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Some<T> implements Option<T>, Present<T> {
        private final T value;


        public T get() {
            return value;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public Option<T> recover(Supplier<? extends T> value) {
            return this;
        }

        @Override
        public Option<T> recover(T value) {
            return this;
        }

        @Override
        public Option<T> recoverWith(Supplier<? extends Option<T>> fn) {
            return this;
        }

        @Override
        public <R> Option<R> map(Function<? super T, ? extends R> mapper) {
            return new Some(mapper.apply(value));
        }

        @Override
        public <R> Option<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
            Option<? extends R> x = mapper.apply(value).toOption();
            return Option.narrow(x);
        }

        @Override
        public <R> R visit(Function<? super T, ? extends R> some, Supplier<? extends R> none) {
            return some.apply(value);
        }

        @Override
        public Option<T> filter(Predicate<? super T> fn) {
            return fn.test(value) ? this : None.NOTHING_EAGER;
        }
        @Override
        public String toString() {
            return mkString();
        }
        /*
       * (non-Javadoc)
       *
       * @see java.lang.Object#hashCode()
       */
        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj) {
            if(obj instanceof Some){
                Some s = (Some)obj;
                return Objects.equals(value,s.value);
            }
            if (obj instanceof Present)
                return Objects.equals(value, ((Maybe) obj).orElse(null));
            else if (obj instanceof Option) {
                Option<T> opt = (Option<T>)obj;
                if(opt.isPresent())
                    return Objects.equals(value,opt.orElse(null));

            }
            return false;
        }

        @Override
        public <R> R fold(Function<? super T, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn1.apply(value);
        }

        @Override
        public T orElse(T alt) {
            return value;
        }
    }
    public static class None<T> implements Option<T> {
        public static None NOTHING_EAGER = new None();

        @Override
        public <R> Option<R> map(final Function<? super T, ? extends R> mapper) {
            return NOTHING_EAGER;
        }

        @Override
        public <R> Option<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
            return NOTHING_EAGER;

        }

        @Override
        public Option<T> filter(final Predicate<? super T> test) {
            return NOTHING_EAGER;
        }



        @Override
        public Option<T> recover(final T value) {
            return Option.of(value);
        }

        @Override
        public Option<T> recover(final Supplier<? extends T> value) {
            return Option.of(value.get());
        }
        @Override
        public Option<T> recoverWith(Supplier<? extends Option<T>> fn) {

            return fn.get();

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

            if (obj instanceof None)
                return true;
            else if (obj instanceof Option) {
                Option<T> opt = (Option<T>)obj;
               return !opt.isPresent();
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
        public <R> None<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
            return  NOTHING_EAGER;
        }

        @Override
        public <R> None<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
            return NOTHING_EAGER;
        }
        @Override
        public void forEach(Consumer<? super T> action) {

        }

        @Override
        public <R> R fold(Function<? super T, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn2.apply(this);
        }
    }

}