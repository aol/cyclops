package cyclops.control;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.*;
import cyclops.Monoids;
import cyclops.async.Future;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collections.ListX;
import cyclops.higherkindedtypes.OptionalKind;
import cyclops.monads.Witness;
import com.aol.cyclops2.types.stream.reactive.ValueSubscriber;
import cyclops.function.Curry;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Totally lazy more powerful general Option(al) type. Maybe is lazy like a Java
 * 8 Stream that represents 0 or 1 values rather than eager like a Java 8
 * Optional. map / peek/ filter and flatMap build the execution chaing, but are
 * not executed until the value inside the Maybe is required.
 * 
 * The Maybe interface has two implementations Some which holds a value and None which represents no value
 * 
 * <pre>
 * {@code 
 *    
 *    //eagerly load data
 *    Optional.of(10)
 *            .map(this::load);
 *    
 *    //lazily tee up loading of data until needed
 *    Maybe.of(10)
 *         .map(this::load);        
 *            .
 * 
 * }
 * </pre>
 * 
 * Maybe is tail recursive
 * 
 * <pre>
 * {@code 
 *  @Test
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
 * Maybe is convertable to all cyclops2-react data types.
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element stored in Maybe
 */
public interface Maybe<T> extends To<Maybe<T>>,
                                  MonadicValue<T>,
                                  Higher<Maybe.µ,T> {

    public static class µ {
    }
    default AnyM<Witness.maybe,T> anyM(){
        return AnyM.fromMaybe(this);
    }
    static <T> Maybe<T> fromLazy(Eval<Maybe<T>> lazy){
        return new Lazy<T>(lazy);
    }



    static <T> Maybe<T> fromFuture(Future<T> future){
        return fromLazy(Eval.fromFuture(future.recover(e->null)).map(Maybe::ofNullable));
    }
    public static <T,R> Function<? super T, ? extends Maybe<R>> arrow(Function<?  super T, ? extends R> fn){
        return in-> Maybe.ofNullable(fn.apply(in));
    }
    /**
     * Construct an equivalent Maybe from the Supplied Optional
     * <pre>
     * {@code
     *   MaybeType<Integer> some = MaybeType.fromOptional(Optional.of(10));
     *   //Maybe[10], Some[10]
     *
     *   MaybeType<Integer> none = MaybeType.fromOptional(Optional.empty());
     *   //Maybe.empty, None[]
     * }
     * </pre>
     *
     * @param opt Optional to construct Maybe from
     * @return Maybe created from Optional
     */
    public static <T> Maybe<T> fromOptional(Higher<OptionalKind.µ,T> optional){
        return   fromOptional(OptionalKind.narrowK(optional));

    }

    public static <C2,T> Higher<C2, Higher<Maybe.µ,T>> widen2(Higher<C2, Maybe<T>> nestedMaybe){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<MaybeType.µ,T> must be a StreamType
        return (Higher)nestedMaybe;
    }
    /**
     * Convert the raw Higher Kinded Type for MaybeType types into the MaybeType type definition class
     *
     * @param future HKT encoded list into a MaybeType
     * @return MaybeType
     */
    public static <T> Maybe<T> narrowK(final Higher<Maybe.µ, T> future) {
        return (Maybe<T>)future;
    }

    /**
     * Convert the HigherKindedType definition for a Maybe into
     *
     * @param MaybeType Constructor to convert back into narrowed type
     * @return Optional from Higher Kinded Type
     */
    public static <T> Optional<T> narrowOptional(final Higher<Maybe.µ, T> maybe) {

        return narrowK(maybe).toOptional();

    }
    @SuppressWarnings("rawtypes")
    final static Maybe EMPTY = new Nothing<>();

    /**
     * @return Get the empty Maybe (single instance)
     */
    @SuppressWarnings("unchecked")
    static <T> Maybe<T> none() {
        return EMPTY;
    }



    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapI(java.util.function.Function)
     */
    @Override
    default <R> Maybe<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Maybe<R>) MonadicValue.super.flatMapI(mapper);
    }

   
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapP(java.util.function.Function)
     */
    @Override
    default <R> Maybe<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            return Maybe.fromPublisher(publisher);
        });
    }
  
    /**
     * Construct a Maybe  that contains a single value extracted from the supplied reactive-streams Publisher
     * <pre>
     * {@code 
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);
        
        Maybe<Integer> maybe = Maybe.fromPublisher(reactiveStream);
        
        //Maybe[1]
     * 
     * }
     * </pre> 
     * 
     * @param pub Publisher to extract value from
     * @return Maybe populated with first value from Publisher (Maybe.empty if Publisher empty)
     */
    public static <T> Maybe<T> fromPublisher(final Publisher<T> pub) {
        return fromFuture(Future.fromPublisher(pub));
    }

    /**
     *  Construct a Maybe  that contains a single value extracted from the supplied Iterable
     * <pre>
     * {@code 
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);
        
        Maybe<Integer> maybe = Maybe.fromIterable(reactiveStream);
        
        //Maybe[1]
     * 
     * }
     * </pre> 
     * @param iterable Iterable  to extract value from
     * @return Maybe populated with first value from Iterable (Maybe.empty if Publisher empty)
     */
    static <T> Maybe<T> fromIterable(final Iterable<T> iterable) {
        return Maybe.fromEval(Eval.fromIterable(iterable));
    }

    static <R> Maybe<R> fromStream(Stream<? extends R> apply) {
        return Maybe.fromEval(Eval.later(()->apply.collect(Collectors.toList())))
                    .flatMap(l->Maybe.fromIterable(l));
    }
    /**
     * Construct an equivalent Maybe from the Supplied Optional
     * <pre>
     * {@code 
     *   Maybe<Integer> some = Maybe.fromOptional(Optional.of(10));
     *   //Maybe[10], Some[10]
     *  
     *   Maybe<Integer> none = Maybe.fromOptional(Optional.empty());
     *   //Maybe.empty, None[]
     * }
     * </pre>
     * 
     * @param opt Optional to construct Maybe from
     * @return Maybe created from Optional
     */
    static <T> Maybe<T> fromOptional(final Optional<T> opt) {
        if (opt.isPresent())
            return Maybe.of(opt.get());
        return none();
    }

    static <T> Maybe<T> fromOptionalKind(final OptionalKind<T> opt){
        return fromOptional(OptionalKind.narrow(opt));
    }
    
    @Deprecated
    static <T> Maybe<T> fromEvalOf(final Eval<T> eval) {
        return new Just<T>(
                           eval);
    }
    
    /**
     * Construct a Maybe from the supplied Eval
     * 
     * <pre>
     * {@code 
     *     Maybe<Integer> maybe =  Maybe.fromEval(Eval.now(10));
     *     //Maybe[10]
     *      
     * }
     * </pre>
     * 
     * @param eval Eval to construct Maybe from
     * @return Maybe created from Eval
     */
    static <T> Maybe<T> fromEval(final Eval<T> eval) {
        return new Just<T>(
                           eval);
    }

    static <T> Maybe<T> fromEvalNullable(final Eval<T> eval) {
        return new Lazy<T>(
                eval.map(u->Maybe.ofNullable(u)));
    }
    
    static <T> Maybe<T> fromEvalOptional(final Eval<Optional<T>> value){
        return new Lazy<T>(value.map(in->Maybe.<T>fromOptional(in)));
    }

    /**
     * Construct an Maybe which contains the provided (non-null) value.
     * Alias for @see {@link Maybe#of(Object)}
     * 
     * <pre>
     * {@code 
     * 
     *    Maybe<Integer> some = Maybe.just(10);
     *    some.map(i->i*2);
     * }
     * </pre>
     * 
     * @param value Value to wrap inside a Maybe
     * @return Maybe containing the supplied value
     */
    static <T> Maybe<T> just(final T value) {
        return of(value);
    }

    /**
     * Construct an Maybe which contains the provided (non-null) value
     * Equivalent to @see {@link Maybe#just(Object)}
     * <pre>
     * {@code 
     * 
     *    Maybe<Integer> some = Maybe.of(10);
     *    some.map(i->i*2);
     * }
     * </pre>
     * 
     * @param value Value to wrap inside a Maybe
     * @return Maybe containing the supplied value
     */
    static <T> Maybe<T> of(final T value) {
        Objects.requireNonNull(value);
        return new Just<T>(
                           Eval.later(() -> value));
    }

    /**
     * <pre>
     * {@code 
     *    Maybe<Integer> maybe  = Maybe.ofNullable(null);
     *    //None
     *     
     *    Maybe<Integer> maybe = Maybe.ofNullable(10);
     *    //Maybe[10], Some[10]
     * 
     * }
     * </pre>
     * 
     * 
     * @param value
     * @return
     */
    static <T> Maybe<T> ofNullable(final T value) {

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
    static <T> Maybe<T> narrow(final Maybe<? extends T> broad) {
        return (Maybe<T>) broad;
    }

    /**
     * Sequence operation, take a Collection of Maybes and turn it into a Maybe with a Collection
     * Only successes are retained. By constrast with {@link Maybe#sequence(CollectionX)} Maybe#empty/ None types are 
     * tolerated and ignored.
     * 
     * <pre>
     * {@code 
     *  Maybe<Integer> just = Maybe.of(10);
        Maybe<Integer> none = Maybe.none();
     * 
     * Maybe<ListX<Integer>> maybes = Maybe.sequenceJust(ListX.of(just, none, Maybe.of(1)));
       //Maybe.of(ListX.of(10, 1));
     * }
     * </pre>
     * 
     * @param maybes Maybes to Sequence
     * @return Maybe with a List of values
     */
    public static <T> Maybe<ListX<T>> sequenceJust(final CollectionX<Maybe<T>> maybes) {
        return AnyM.sequence(maybes.stream().filter(Maybe::isPresent).map(AnyM::fromMaybe).toListX(),Witness.maybe.INSTANCE)
                   .to(Witness::maybe);
    }

    /**
     * Sequence operation, take a Collection of Maybes and turn it into a Maybe with a Collection
     * By constrast with {@link Maybe#sequenceJust(CollectionX)} if any Maybe types are None / empty 
     * the return type will be an empty Maybe / None
     * 
     * <pre>
     * {@code
     * 
     *  Maybe<Integer> just = Maybe.of(10);
        Maybe<Integer> none = Maybe.none();
     *  
     *  Maybe<ListX<Integer>> maybes = Maybe.sequence(ListX.of(just, none, Maybe.of(1)));
        //Maybe.none();
     * 
     * }
     * </pre>
     * 
     * 
     * @param maybes Maybes to Sequence
     * @return  Maybe with a List of values
     */
    public static <T> Maybe<ListX<T>> sequence(final CollectionX<Maybe<T>> maybes) {
        return sequence(maybes.stream()).map(s -> s.toListX());

    }

    /**
     * Sequence operation, take a Stream of Maybes and turn it into a Maybe with a Stream
     * By constrast with {@link Maybe#sequenceJust(CollectionX)} Maybe#empty/ None types are 
     * result in the returned Maybe being Maybe.empty / None 
     * 
     * 
     * <pre>
     * {@code 
     * 
     *  Maybe<Integer> just = Maybe.of(10);
        Maybe<Integer> none = Maybe.none();
        
     *  Maybe<ReactiveSeq<Integer>> maybes = Maybe.sequence(Stream.of(just, none, Maybe.of(1)));
        //Maybe.none();
     * 
     * }
     * </pre> 
     * 
     * 
     * @param maybes Maybes to Sequence
     * @return  Maybe with a Stream of values
     */
    public static <T> Maybe<ReactiveSeq<T>> sequence(final Stream<Maybe<T>> maybes) {
        return AnyM.sequence(maybes.map(AnyM::fromMaybe),Witness.maybe.INSTANCE)
                   .map(ReactiveSeq::fromStream)
                   .to(Witness::maybe);
        

    }

    /**
     * Accummulating operation using the supplied Reducer (@see cyclops2.Reducers). A typical use case is to accumulate into a Persistent Collection type.
     * Accumulates the present results, ignores empty Maybes.
     * 
     * <pre>
     * {@code 
     *  Maybe<Integer> just = Maybe.of(10);
        Maybe<Integer> none = Maybe.none();
        
     * Maybe<PSetX<Integer>> maybes = Maybe.accumulateJust(ListX.of(just, none, Maybe.of(1)), Reducers.toPSetX());
       //Maybe.of(PSetX.of(10, 1)));
     * 
     * }
     * </pre>
     * 
     * @param maybes Maybes to accumulate
     * @param reducer Reducer to accumulate values with
     * @return Maybe with reduced value
     */
    public static <T, R> Maybe<R> accumulateJust(final CollectionX<Maybe<T>> maybes, final Reducer<R> reducer) {
        return sequenceJust(maybes).map(s -> s.mapReduce(reducer));
    }

    /**
     * Accumulate the results only from those Maybes which have a value present, using the supplied mapping function to
     * convert the data from each Maybe before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }..
     * 
     * <pre>
     * {@code 
     *  Maybe<Integer> just = Maybe.of(10);
        Maybe<Integer> none = Maybe.none();
        
     *  Maybe<String> maybes = Maybe.accumulateJust(ListX.of(just, none, Maybe.of(1)), i -> "" + i,
                                                     Semigroups.stringConcat);
        //Maybe.of("101")
     * 
     * }
     * </pre>
     * 
     * @param maybes Maybes to accumulate
     * @param mapper Mapping function to be applied to the result of each Maybe
     * @param reducer Monoid to combine values from each Maybe
     * @return Maybe with reduced value
     */
    public static <T, R> Maybe<R> accumulateJust(final CollectionX<Maybe<T>> maybes, final Function<? super T, R> mapper,
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
     *  Maybe<Integer> maybes = Maybe.accumulateJust(Monoids.intSum,ListX.of(just, none, Maybe.of(1)));
        //Maybe.of(11)
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
    public static <T> Maybe<T> accumulateJust(final Monoid<T> reducer,final CollectionX<Maybe<T>> maybes) {
        return sequenceJust(maybes).map(s -> s.reduce(reducer));
    }

    @Override
    default <R> Maybe<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (Maybe<R>)MonadicValue.super.zipWith(fn);
    }

    @Override
    default <R> Maybe<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (Maybe<R>)MonadicValue.super.zipWithS(fn);
    }

    @Override
    default <R> Maybe<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (Maybe<R>)MonadicValue.super.zipWithP(fn);
    }

    @Override
    default <R> Maybe<R> retry(final Function<? super T, ? extends R> fn) {
        return (Maybe<R>)MonadicValue.super.retry(fn);
    }

    @Override
    default <U> Maybe<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (Maybe)MonadicValue.super.zipP(other);
    }

    @Override
    default <R> Maybe<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Maybe<R>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> Maybe<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Maybe)MonadicValue.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Maybe<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Maybe<R>)MonadicValue.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Maybe<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Maybe)MonadicValue.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Maybe<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Maybe<R>)MonadicValue.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <R> Maybe<R> flatMapS(final Function<? super T, ? extends Stream<? extends R>> mapper) {
        return (Maybe<R>)MonadicValue.super.flatMapS(mapper);
    }

    /* (non-Javadoc)
         * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
         */
    @Override
    default <T2, R1, R2, R3, R> Maybe<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Maybe<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Maybe<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (Maybe<R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Maybe<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
      
        return (Maybe<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Maybe<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Maybe<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Maybe<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Maybe<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Maybe<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Maybe<R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
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
     * com.aol.cyclops2.types.applicative.ApplicativeFunctor#combine(com.aol.
     * cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Maybe<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> app.visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));
    }

    /*
     * Equivalent to combine, but accepts an Iterable and takes the first value
     * only from that iterable. (non-Javadoc)
     * 
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable,
     * java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Maybe<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Maybe.fromIterable(app)
                                                                              .visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));
    }

    
    /*
     * Equivalent to combine, but accepts a Publisher and takes the first value
     * only from that publisher. (non-Javadoc)
     * 
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.function.BiFunction,
     * org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Maybe<R> zipP(final Publisher<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Maybe.fromPublisher(app)
                                                                              .visit(i -> Maybe.just(tuple.v2.apply(i)), () -> Maybe.none()));

    }
    

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Applicative#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Applicative)
     */
    @Override
    default  Maybe<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (Maybe<T>)MonadicValue.super.zip(combiner, app);
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream,
     * java.util.function.BiFunction)
     */
    @Override
    default <U, R> Maybe<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (Maybe<R>) MonadicValue.super.zipS(other, zipper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> Maybe<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (Maybe) MonadicValue.super.zipS(other);
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Maybe<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (Maybe) MonadicValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Maybe<T> unit(final T unit) {
        return Maybe.of(unit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Maybe<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return (Maybe<R>) MonadicValue.super.coflatMap(mapper);
    }

    /*
     * cojoin (non-Javadoc)
     * 
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    default Maybe<MonadicValue<T>> nest() {
        return (Maybe<MonadicValue<T>>) MonadicValue.super.nest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.types.MonadicValue2#combine(cyclops2.function.Monoid,
     * com.aol.cyclops2.types.MonadicValue2)
     */
    @Override
    default Maybe<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return (Maybe<T>) MonadicValue.super.combineEager(monoid, v2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.value.Value#toMaybe()
     */
    @Override
    default Maybe<T> toMaybe() {
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Convertable#isPresent()
     */
    @Override
    boolean isPresent();

    Maybe<T> recover(Supplier<T> value);

    Maybe<T> recover(T value);
    
    Maybe<T> recoverWith(Supplier<? extends Maybe<T>> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    <R> Maybe<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    <R> Maybe<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Convertable#visit(java.util.function.Function, java.util.function.Supplier)
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
    Maybe<T> filter(Predicate<? super T> fn);

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.lambda.monads.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.lambda.monads.Filters#filterNot(java.util.function.
     * Predicate)
     */
    @Override
    default Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default Maybe<T> notNull() {

        return (Maybe<T>) MonadicValue.super.notNull();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> Maybe<U> cast(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.cast(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default Maybe<T> peek(final Consumer<? super T> c) {

        return (Maybe<T>) MonadicValue.super.peek(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops2.lambda.monads.Functor#trampoline(java.util.function.
     * Function)
     */
    @Override
    default <R> Maybe<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (Maybe<R>) MonadicValue.super.trampoline(mapper);
    }




    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Just<T> implements Maybe<T> {

        private final Eval<T> lazy;

        @Override
        public <R> Maybe<R> map(final Function<? super T, ? extends R> mapper) {
            return new Just<>(
                              lazy.map(t -> mapper.apply(t)));
        }
        
        @Override
        public <R> Maybe<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
            Eval<? extends Maybe<? extends R>> ret = lazy.map(mapper.andThen(v->v.toMaybe()));
            
            
            final Eval<Maybe<R>> e3 =  (Eval<Maybe<R>>)ret;
            return new Lazy<>(
                              e3);
          

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
        public <R> Just<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
            final Maybe<R> maybe = Maybe.super.flatMapI(mapper);
            return (Just<R>) Maybe.just(maybe.get());
        }

        @Override
        public <R> Just<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
            final Maybe<R> m = Maybe.super.flatMapP(mapper);
            return (Just<R>) Maybe.just(m.get());
        }

        /* (non-Javadoc)
         * @see cyclops2.control.Maybe#recoverWith(java.util.function.Supplier)
         */
        @Override
        public Maybe<T> recoverWith(Supplier<? extends Maybe<T>> fn) {
            return this;
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            this.lazy.forEach(action);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Lazy<T> implements Maybe<T> {

        private final Eval<Maybe<T>> lazy;

        @Override
        public <R> Maybe<R> map(final Function<? super T, ? extends R> mapper) {
            return flatMap(t -> Maybe.just(mapper.apply(t)));
        }
        private static <T> Lazy<T> lazy(Eval<Maybe<T>> lazy) {
            return new Lazy<>(
                              lazy);
        }
        
       
        public Maybe<T> resolve() {

          return lazy.get()
                       .visit(Maybe::just,Maybe::none);
        }
        @Override
        public <R> Maybe<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
            return Maybe.fromLazy(lazy.map(m->m.flatMap(mapper)));
        //    return lazy(Eval.later( () -> resolve().flatMap(mapper)));
            

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
        public final void subscribe(final Subscriber<? super T> sub) {
            lazy.subscribe(new Subscriber<Maybe<T>>() {
                boolean onCompleteSent = false;
                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(Maybe<T> ts) {
                    if(ts.isPresent()){
                        sub.onNext(ts.get());
                    }else if(!onCompleteSent){
                        sub.onComplete();
                        onCompleteSent =true;
                    }
                }

                @Override
                public void onError(Throwable t) {
                    sub.onError(t);
                }

                @Override
                public void onComplete() {
                    if(!onCompleteSent){
                        sub.onComplete();
                        onCompleteSent =true;
                    }
                }
            });
        }

        @Override
        public Maybe<T> recover(final T value) {
            return new Lazy<T>(
                               lazy.map(m -> m.recover(value)));
        }

        @Override
        public Maybe<T> recover(final Supplier<T> value) {
            return new Lazy<T>(
                               lazy.map(m -> m.recover(value)));
        }
        @Override
        public Maybe<T> recoverWith(Supplier<? extends Maybe<T>> fn) {
            return new Lazy<T>(
                                    lazy.map(m -> m.recoverWith(fn)));
        }

        @Override
        public String toString() {
            /**
            Maybe<T> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<T>) maybe).lazy.get();
            }
            return maybe.mkString();
             **/
            return "lazy!";
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
        public <R> Lazy<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
            final Maybe<R> m = Maybe.super.flatMapI(mapper);
            return new Lazy(
                            Eval.later(() -> m.get()));
        }

        @Override
        public <R> Lazy<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
            final Maybe<R> m = (Lazy<R>) Maybe.super.flatMapP(mapper);
            return new Lazy(
                            Eval.later(() -> m.get()));
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
            throw new NoSuchElementException("No value present");
        }

        @Override
        public Maybe<T> recover(final T value) {
            return Maybe.of(value);
        }

        @Override
        public Maybe<T> recover(final Supplier<T> value) {
            return new Just<>(
                              Eval.later(value));
        }
        @Override
        public Maybe<T> recoverWith(Supplier<? extends Maybe<T>> fn) {
   
            return new Just<>(Eval.narrow(Eval.later(fn))).flatMap(m->m);

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
        public <R> Nothing<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
            return (Nothing<R>) EMPTY;
        }

        @Override
        public <R> Nothing<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
            return (Nothing<R>) EMPTY;
        }
        @Override
        public void forEach(Consumer<? super T> action) {

        }
    }

    /**
     * Companion class for creating Type Class instances for working with Maybes
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {



        /**
         *
         * Transform a maybe, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  Maybe<Integer> maybe = Maybes.functor().map(i->i*2, Maybe.widen(Maybe.just(1));
         *
         *  //[2]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Maybes
         * <pre>
         * {@code
         *   Maybe<Integer> maybe = Maybes.unit()
        .unit("hello")
        .transform(h->Maybes.functor().map((String v) ->v.length(), h))
        .convert(Maybe::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Maybes
         */
        public static <T,R>Functor<µ> functor(){
            BiFunction<Maybe<T>,Function<? super T, ? extends R>,Maybe<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * Maybe<String> maybe = Maybes.unit()
        .unit("hello")
        .convert(Maybe::narrowK);

        //Maybe.just("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Maybes
         */
        public static <T> Pure<µ> unit(){
            return General.<Maybe.µ,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.Maybe.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Maybe.just;
         *
        Maybes.zippingApplicative()
        .ap(widen(asMaybe(l1(this::multiplyByTwo))),widen(asMaybe(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * Maybe<Function<Integer,Integer>> maybeFn =Maybes.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(Maybe::narrowK);

        Maybe<Integer> maybe = Maybes.unit()
        .unit("hello")
        .transform(h->Maybes.functor().map((String v) ->v.length(), h))
        .transform(h->Maybes.applicative().ap(maybeFn, h))
        .convert(Maybe::narrowK);

        //Maybe.just("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Maybes
         */
        public static <T,R> Applicative<Maybe.µ> applicative(){
            BiFunction<Maybe< Function<T, R>>,Maybe<T>,Maybe<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.Maybe.widen;
         * Maybe<Integer> maybe  = Maybes.monad()
        .flatMap(i->widen(MaybeX.range(0,i)), widen(Maybe.just(1,2,3)))
        .convert(Maybe::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    Maybe<Integer> maybe = Maybes.unit()
        .unit("hello")
        .transform(h->Maybes.monad().flatMap((String v) ->Maybes.unit().unit(v.length()), h))
        .convert(Maybe::narrowK);

        //Maybe.just("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Maybes
         */
        public static <T,R> Monad<µ> monad(){

            BiFunction<Higher<Maybe.µ,T>,Function<? super T, ? extends Higher<Maybe.µ,R>>,Higher<Maybe.µ,R>> flatMap = Instances::flatMap;
            return General.monad(applicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  Maybe<String> maybe = Maybes.unit()
        .unit("hello")
        .transform(h->Maybes.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(Maybe::narrowK);

        //Maybe.just("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<µ> monadZero(){

            return General.monadZero(monad(), Maybe.none());
        }
        /**
         * <pre>
         * {@code
         *  Maybe<Integer> maybe = Maybes.<Integer>monadPlus()
        .plus(Maybe.widen(Maybe.just()), Maybe.widen(Maybe.just(10)))
        .convert(Maybe::narrowK);
        //Maybe.just(10))
         *
         * }
         * </pre>
         * @return Type class for combining Maybes by concatenation
         */
        public static <T> MonadPlus<Maybe.µ> monadPlus(){
            Monoid<Maybe<T>> mn = Monoids.firstPresentMaybe();
            Monoid<Maybe<T>> m = Monoid.of(mn.zero(), (f,g)->
                    mn.apply(Maybe.narrow(f), Maybe.narrow(g)));

            Monoid<Higher<Maybe.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<Maybe<Integer>> m = Monoid.of(Maybe.widen(Maybe.just()), (a,b)->a.isEmpty() ? b : a);
        Maybe<Integer> maybe = Maybes.<Integer>monadPlus(m)
        .plus(Maybe.widen(Maybe.just(5)), Maybe.widen(Maybe.just(10)))
        .convert(Maybe::narrowK);
        //Maybe[5]
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Maybes
         * @return Type class for combining Maybes
         */
        public static <T> MonadPlus<µ> monadPlus(Monoid<Maybe<T>> m){
            Monoid<Higher<Maybe.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<µ> traverse(){

            return General.traverseByTraverse(applicative(), Instances::traverseA);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Maybes.foldable()
        .foldLeft(0, (a,b)->a+b, Maybe.widen(Maybe.just(1)));

        //1
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<µ> foldable(){
            BiFunction<Monoid<T>,Higher<Maybe.µ,T>,T> foldRightFn =  (m,l)-> Maybe.narrowK(l).orElse(m.zero());
            BiFunction<Monoid<T>,Higher<Maybe.µ,T>,T> foldLeftFn = (m,l)-> Maybe.narrowK(l).orElse(m.zero());
            return General.foldable(foldRightFn, foldLeftFn);
        }

        public static <T> Comonad<µ> comonad(){
            Function<? super Higher<Maybe.µ, T>, ? extends T> extractFn = maybe -> maybe.convert(Maybe::narrowK).get();
            return General.comonad(functor(), unit(), extractFn);
        }


        private <T> Maybe<T> of(T value){
            return Maybe.of(value);
        }
        private static <T,R> Maybe<R> ap(Maybe<Function< T, R>> lt,  Maybe<T> maybe){
            return lt.combine(maybe, (a,b)->a.apply(b)).toMaybe();

        }
        private static <T,R> Higher<Maybe.µ,R> flatMap( Higher<Maybe.µ,T> lt, Function<? super T, ? extends  Higher<Maybe.µ,R>> fn){
            return Maybe.narrowK(lt).flatMap(fn.andThen(Maybe::narrowK));
        }
        private static <T,R> Maybe<R> map(Maybe<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);

        }


        private static <C2,T,R> Higher<C2, Higher<Maybe.µ, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                         Higher<Maybe.µ, T> ds){

            Maybe<T> maybe = Maybe.narrowK(ds);
            Higher<C2, Maybe<R>> res = maybe.visit(some-> applicative.map(m->Maybe.of(m), fn.apply(some)),
                    ()->applicative.unit(Maybe.<R>none()));

            return Maybe.widen2(res);
        }

    }

}
