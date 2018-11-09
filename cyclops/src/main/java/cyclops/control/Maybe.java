package cyclops.control;


import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.Present;
import cyclops.data.tuple.*;
import cyclops.function.checked.CheckedSupplier;
import cyclops.reactive.Spouts;
import com.oath.cyclops.types.reactive.Completable;
import cyclops.function.Monoid;
import cyclops.function.Reducer;

import cyclops.function.Function3;
import cyclops.function.Function4;
import com.oath.cyclops.hkt.DataWitness.option;

import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.InvalidObjectException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
   Represents a lazy or reactive Option type.

   Maybe differs from Option in that it

   <pre>
   1. Is lazy by default
   2. Can operate Reactively (that is respond to data that arrives asynchronously)
   </pre>


   @param <T> Data type of element stored in Maybe
 */
public interface Maybe<T> extends Option<T> {


    public static <T> Maybe<T> attempt(CheckedSupplier<T> s){
        return Maybe.fromLazy(Eval.later(()->{
            try {
                return just(s.get());
            } catch (Throwable throwable) {
                return Maybe.nothing();
            }
        }));
    }
    default <R> Maybe<R> attemptMap(Function<? super T,? extends R> fn){
        return flatMap(t->{
            try{
                return just(fn.apply(t));
            }catch(Throwable e){
                return nothing();
            }
        });

    }
    default <R> Maybe<R> attemptFlatMap(Function<? super T,? extends Option<? extends R>> fn){
        return flatMap(t->{
            try{
                return fn.apply(t);
            }catch(Throwable e){
                return nothing();
            }
        });

    }
    public static  <T,R> Maybe<R> tailRec(T initial, Function<? super T, ? extends Maybe<? extends Either<T, R>>> fn){
        return narrowK(fn.apply(initial)).flatMap( eval -> eval.fold(s->tailRec(s,fn), p->Maybe.just(p)));
    }
    public static <T> Higher<option, T> widen(Maybe<T> narrow) {
    return narrow;
  }


    static <T> Maybe<T> async(final Executor ex, final Supplier<T> s){
        return fromFuture(Future.of(s,ex));
    }




    static <T> Maybe<T> fromLazy(Eval<Maybe<T>> lazy){
        return new Lazy<T>(lazy);
    }


    /**
     * Create a reactive CompletableMaybe
     * <pre>
     *     {@code
     *     CompletableMaybe<Integer,Integer> completable = Maybe.maybe();
    Maybe<Integer> mapped = completable.map(i->i*2)
    .flatMap(i->Eval.later(()->i+1));

    completable.complete(5);
    mapped.printOut();
    //11

    CompletableMaybe<Integer,Integer> completable = Maybe.maybe();
    Maybe<Integer> mapped = completable.map(i->i*2)
    .flatMap(i->Eval.later(()->i+1));

    completable.complete(null);
    mapped.printOut();
    //
    //Maybe:None

     *     }
     * </pre>
     *
     * @param <T> Data input type to the Maybe
     * @return A reactive CompletableMaybe
     */
    static <T> CompletableMaybe<T,T> maybe(){
        CompletableFuture<T> c = new CompletableFuture<T>();
        return new Maybe.CompletableMaybe<T, T>(c,fromFuture(Future.of(c)));
    }
    @AllArgsConstructor
    static class CompletableMaybe<ORG,T2> implements Maybe<T2>, Completable<ORG> {

        public final CompletableFuture<ORG> complete;
        public final Maybe<T2> maybe;

        private Object writeReplace() {
            return toOption();
        }
        private Object readResolve() throws InvalidObjectException {
            throw new InvalidObjectException("Use Serialization Proxy instead.");
        }

        @Override
        public boolean isPresent() {
            return maybe.isPresent();
        }

        @Override
        public Maybe<T2> recover(Supplier<? extends T2> value) {
            return maybe.recover(value);
        }

        @Override
        public Maybe<T2> recover(T2 value) {
            return maybe.recover(value);
        }

        @Override
        public Maybe<T2> recoverWith(Supplier<? extends Option<T2>> fn) {
            return maybe.recoverWith(fn);
        }

        @Override
        public <R> Maybe<R> map(Function<? super T2, ? extends R> mapper) {
            return maybe.map(mapper);
        }

        @Override
        public <R> Maybe<R> flatMap(Function<? super T2, ? extends MonadicValue<? extends R>> mapper) {
            return maybe.flatMap(mapper);
        }

        @Override
        public <R> R fold(Function<? super T2, ? extends R> some, Supplier<? extends R> none) {
            return maybe.fold(some,none);
        }

        @Override
        public Maybe<T2> filter(Predicate<? super T2> fn) {
            return maybe.filter(fn);
        }

        @Override
        public boolean isFailed() {
            return complete.isCompletedExceptionally();
        }

        @Override
        public boolean isDone() {
            return complete.isDone();
        }


        public boolean completeAsNone(){
            return completeExceptionally(new Queue.ClosedQueueException());
        }
        @Override
        public boolean complete(ORG done) {
            return complete.complete(done);
        }

        @Override
        public boolean completeExceptionally(Throwable error) {
            return complete.completeExceptionally(error);
        }

        @Override
        public int hashCode() {
            return maybe.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return maybe.equals(obj);
        }

        @Override
        public <R> R fold(Function<? super T2, ? extends R> fn1, Function<? super None<T2>, ? extends R> fn2) {
            return maybe.fold(fn1,fn2);
        }
    }
    static <T> Maybe<T> fromFuture(Future<T> future){
        return fromLazy(Eval.fromFuture(future.map(Maybe::of)
                                              .recover(e->Maybe.nothing())));
    }
    public static <T,R> Function<? super T, ? extends Maybe<R>> arrow(Function<?  super T, ? extends R> fn){
        return in-> Maybe.ofNullable(fn.apply(in));
    }


    public static <C2,T> Higher<C2, Higher<option,T>> widen2(Higher<C2, Maybe<T>> nestedMaybe){

        return (Higher)nestedMaybe;
    }
    /**
     * Convert the raw Higher Kinded Type for MaybeType types into the MaybeType type definition class
     *
     * @param future HKT encoded list into a MaybeType
     * @return MaybeType
     */
    public static <T> Maybe<T> narrowK(final Higher<option, T> future) {
        return Maybe.fromOption(Option.narrowK(future));
    }


    public static <T> Maybe<T> fromOption(Option<T> opt){
      if(opt instanceof Maybe)
        return (Maybe<T>)opt;
      return fromIterable(opt);
    }

    /**
     * Convert the HigherKindedType definition for a Maybe into
     *
     * @param maybe Constructor to convert back into narrowed type
     * @return Optional from Higher Kinded Type
     */
    public static <T> Optional<T> narrowOptional(final Higher<option, T> maybe) {

        return narrowK(maybe).toOptional();

    }
    @SuppressWarnings("rawtypes")
    final static Maybe EMPTY = new Nothing<>();

    /**
     * @return Get the zero Maybe (single instance)
     */
    @SuppressWarnings("unchecked")
    static <T> Maybe<T> nothing() {
        return EMPTY;
    }




    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#concatMap(java.util.function.Function)
     */
    @Override
    default <R> Maybe<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Maybe<R>) Option.super.concatMap(mapper);
    }


    @Override
    default <R> Maybe<R> mergeMap(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            return Maybe.fromPublisher(publisher);
        });
    }

    /**
     * Construct a Maybe  that contains a single value extracted from the supplied reactive-streams Publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

    Maybe<Integer> maybe = Maybe.fromPublisher(stream);

    //Maybe[1]
     *
     * }
     * </pre>
     *
     * @param pub Publisher to extract value from
     * @return Maybe populated with first value from Publisher (Maybe.zero if Publisher zero)
     */
    public static <T> Maybe<T> fromPublisher(final Publisher<T> pub) {
        if(pub instanceof Maybe)
            return (Maybe<T>)pub;
        return fromFuture(Future.fromPublisher(pub));
    }

    /**
     *  Construct a Maybe  that contains a single value extracted from the supplied Iterable
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

    Maybe<Integer> maybe = Maybe.fromIterable(stream);

    //Maybe[1]
     *
     * }
     * </pre>
     * @param iterable Iterable  to extract value from
     * @return Maybe populated with first value from Iterable (Maybe.zero if Publisher zero)
     */
    static <T> Maybe<T> fromIterable(final Iterable<T> iterable) {
        if(iterable instanceof Maybe)
            return (Maybe<T>)iterable;
        return new Lazy<>(Eval.later(()->{
            Iterator<T> it = iterable.iterator();
            return it.hasNext() ? Maybe.just(it.next()) :  Maybe.nothing();
        })) ;

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
     *   Maybe<Integer> none = Maybe.fromOptional(Optional.zero());
     *   //Maybe.zero, None[]
     * }
     * </pre>
     *
     * @param opt Optional to construct Maybe from
     * @return Maybe created from Optional
     */
    static <T> Maybe<T> fromOptional(final Optional<T> opt) {
        if (opt.isPresent())
            return Maybe.of(opt.get());
        return nothing();
    }



    @Deprecated
    static <T> Maybe<T> fromEvalOf(final Eval<T> eval) {
        return new Just<T>(
                eval);
    }

    default Trampoline<Maybe<T>> toTrampoline() {
        return Trampoline.more(()->Trampoline.done(this));
    }

    /**
     * Construct a Maybe from the supplied Eval
     *
     * <pre>
     * {@code
     *     Maybe<Integer> maybe =  Maybe.fromEval(Eval.now(10));
     *     //Maybe[10]
     *     Maybe<Integer> maybeNull = Maybe.fromEval(()->null);
     *     //Maybe[null]
     *
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
    static <T> Maybe<T> fromLazyOption(final Supplier<Option<T>> value){
        return new Lazy<T>(Eval.later(value).map(in->Maybe.<T>fromOption(in)));
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
        return new Just<T>(
                Eval.later(() -> value));
    }


    default Maybe<T> lazy(){
        return Maybe.fromIterable(this);
    }
    default Option<T> eager(){
        return fold(s->Option.some(s),()->Option.none());
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
        return nothing();
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
     * Only successes are retained. By constrast with {@link Maybe#sequence(Iterable)} Maybe#zero/ None types are
     * tolerated and ignored.
     *
     * <pre>
     * {@code
     *  Maybe<Integer> just = Maybe.of(10);
    Maybe<Integer> none = Maybe.none();
     *
     * Maybe<Seq<Integer>> maybes = Maybe.sequenceJust(Seq.of(just, none, Maybe.of(1)));
    //Maybe.of(Seq.of(10, 1));
     * }
     * </pre>
     *
     * @param maybes Maybes to Sequence
     * @return Maybe with a List of values
     */
    public static <T> Maybe<ReactiveSeq<T>> sequenceJust(final Iterable<? extends Maybe<T>> maybes) {
        return sequence(ReactiveSeq.fromIterable(maybes).filter(Maybe::isPresent).stream());
    }
  public static  <T> Maybe<ReactiveSeq<T>> sequence(ReactiveSeq<? extends Maybe<T>> stream) {

    Maybe<ReactiveSeq<T>> identity = Maybe.just(ReactiveSeq.empty());


    BiFunction<Maybe<ReactiveSeq<T>>,Maybe<T>,Maybe<ReactiveSeq<T>>> combineToStream = (acc,next) ->acc.zip(next,(a,b)->a.append(b));

    BinaryOperator<Maybe<ReactiveSeq<T>>> combineStreams = (a,b)-> a.zip(b,(z1,z2)->z1.appendStream(z2));

    return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <T,R> Maybe<ReactiveSeq<R>> traverse(Function<? super T,? extends R> fn,ReactiveSeq<Maybe<T>> stream) {
    ReactiveSeq<Maybe<R>> s = stream.map(h -> h.map(fn));
    return sequence(s);
  }
    /**
     * Sequence operation, take a Collection of Maybes and turn it into a Maybe with a Collection
     * By constrast with {@link Maybe#sequenceJust(Iterable)} if any Maybe types are None / zero
     * the return type will be an zero Maybe / None
     *
     * <pre>
     * {@code
     *
     *  Maybe<Integer> just = Maybe.of(10);
    Maybe<Integer> none = Maybe.none();
     *
     *  Maybe<Seq<Integer>> maybes = Maybe.sequence(Seq.of(just, none, Maybe.of(1)));
    //Maybe.none();
     *
     * }
     * </pre>
     *
     *
     * @param maybes Maybes to Sequence
     * @return  Maybe with a List of values
     */
    public static <T> Maybe<ReactiveSeq<T>> sequence(final Iterable<? extends Maybe<T>> maybes) {
        return sequence(ReactiveSeq.fromIterable(maybes));

    }

    /**
     * Sequence operation, take a Stream of Maybes and turn it into a Maybe with a Stream
     * By constrast with {@link Maybe#sequenceJust(Iterable)} Maybe#zero/ None types are
     * result in the returned Maybe being Maybe.zero / None
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
    public static <T> Maybe<ReactiveSeq<T>> sequence(final Stream<? extends Maybe<T>> maybes) {
        return sequence(ReactiveSeq.fromStream(maybes));


    }

    /**
     * Accummulating operation using the supplied Reducer (@see cyclops.Reducers). A typical use case is to accumulate into a Persistent Collection type.
     * Accumulates the present results, ignores zero Maybes.
     *
     * <pre>
     * {@code
     *  Maybe<Integer> just = Maybe.of(10);
    Maybe<Integer> none = Maybe.none();
     * Maybe<PersistentSetX<Integer>> maybes = Maybe.accumulateJust(Seq.of(just, none, Maybe.of(1)), Reducers.toPersistentSetX());
    //Maybe.of(PersistentSetX.of(10, 1)));
     *
     * }
     * </pre>
     *
     * @param maybes Maybes to accumulate
     * @param reducer Reducer to accumulate values with
     * @return Maybe with reduced value
     */
    public static <T, R> Maybe<R> accumulateJust(final Iterable<Maybe<T>> maybes, final Reducer<R,T> reducer) {
        return sequenceJust(maybes).map(s -> s.foldMap(reducer));
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

     *  Maybe<String> maybes = Maybe.accumulateJust(Seq.of(just, none, Maybe.of(1)), i -> "" + i,
    SemigroupK.stringConcat);
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
    public static <T, R> Maybe<R> accumulateJust(final Iterable<Maybe<T>> maybes, final Function<? super T, R> mapper,
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
     *  Maybe<Integer> maybes = Maybe.accumulateJust(Monoids.intSum,Seq.of(just, none, Maybe.of(1)));
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
    public static <T> Maybe<T> accumulateJust(final Monoid<T> reducer,final Iterable<Maybe<T>> maybes) {
        return sequenceJust(maybes).map(s -> s.reduce(reducer));
    }



    @Override
    default <U> Maybe<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return mergeMap(a->Maybe.fromPublisher(other).map(b->Tuple.tuple(a,b)));
    }

    @Override
    default <S, U> Maybe<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Maybe)Option.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Maybe<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Maybe<R>)Option.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Maybe<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Maybe)Option.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Maybe<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Maybe<R>)Option.super.zip4(second,third,fourth,fn);
    }



    /* (non-Javadoc)
         * @see com.oath.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
         */
    @Override
    default <T2, R1, R2, R3, R> Maybe<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Maybe<R>)Option.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Maybe<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Maybe<R>)Option.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Maybe<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Maybe<R>)Option.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Maybe<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Maybe<R>)Option.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Maybe<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Maybe<R>)Option.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Maybe<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Maybe<R>)Option.super.forEach2(value1, filterFunction, yieldingFunction);
    }



    @Override
    default <T2, R> Maybe<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return flatMap(a->  Maybe.fromIterable(app).map(b->fn.apply(a,b)));
    }



    @Override
    default <T2, R> Maybe<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
       return mergeMap(a->Maybe.fromPublisher(app).map(b->fn.apply(a,b)));
    }



    @Override
    default <U> Maybe<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (Maybe) Option.super.zip(other);
    }


    @Override
    default <T> Maybe<T> unit(final T unit) {
        return Maybe.of(unit);
    }






    @Override
    default Maybe<T> toMaybe() {
        return this;
    }


    @Override
    boolean isPresent();



    Maybe<T> recover(Supplier<? extends T> value);

    Maybe<T> recover(T value);

    Maybe<T> recoverWith(Supplier<? extends Option<T>> fn);
    @Override
    default Maybe<T> orElseUse(Option<T> opt){
        return recoverWith(()->opt);
    }


    @Override
    <R> Maybe<R> map(Function<? super T, ? extends R> mapper);


    @Override
    <R> Maybe<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);


    @Override
    <R> R fold(Function<? super T, ? extends R> some, Supplier<? extends R> none);


    @Override
    Maybe<T> filter(Predicate<? super T> fn);

    @Override
    default <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) Option.super.ofType(type);
    }

    @Override
    default Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) Option.super.filterNot(fn);
    }


    @Override
    default Maybe<T> notNull() {

        return (Maybe<T>) Option.super.notNull();
    }


    
    @Override
    default Maybe<T> peek(final Consumer<? super T> c) {

        return (Maybe<T>) Option.super.peek(c);
    }




    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Just<T> implements Maybe<T>, Present<T> {


        private final Eval<T> lazy;


        private Object writeReplace() {
            return toOption();
        }
        private Object readResolve() throws InvalidObjectException {
            throw new InvalidObjectException("Use Serialization Proxy instead.");
        }


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
        public <R> R fold(final Function<? super T, ? extends R> some, final Supplier<? extends R> none) {
            return some.apply(lazy.get());
        }

        @Override
        public Maybe<T> recover(final T value) {
            return this;
        }

        @Override
        public Maybe<T> recover(final Supplier<? extends T> value) {
            return this;
        }


        @Override
        public String toString() {
            return mkString();
        }


        @Override
        public boolean isPresent() {
            return true;
        }


        @Override
        public int hashCode() {
            return Objects.hashCode(lazy.get());
        }


        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Nothing) {
                return false;
            }
            else if (obj instanceof None) {
                return  false;
            }
            if (obj instanceof Present)
                return Objects.equals(lazy.get(), ((Present) obj).orElse(null));
            else if (obj instanceof Lazy) {
                return Objects.equals(orElse(null), ((Maybe) obj).orElse(null));
            }
            return false;
        }


        @Override
        public T orElse(T value) {
            return lazy.get();
        }

        @Override
        public T orElseGet(final Supplier<? extends T> value) {
            return lazy.get();
        }

        @Override
        public <R> Maybe<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
            final Maybe<R> maybe = Maybe.super.concatMap(mapper);
            return maybe;
        }

        @Override
        public <R> Maybe<R> mergeMap(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
            final Maybe<R> m = Maybe.super.mergeMap(mapper);
            return m;
        }

        /* (non-Javadoc)
         * @see cyclops2.control.Maybe#recoverFlatMap(java.util.function.Supplier)
         */
        @Override
        public Maybe<T> recoverWith(Supplier<? extends Option<T>> fn) {
            return this;
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            this.lazy.forEach(action);
        }


        @Override
        public <R> R fold(Function<? super T, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn1.apply(this.lazy.get());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Lazy<T> implements Maybe<T> {

        private final Eval<Maybe<T>> lazy;

        private Object writeReplace() {
            return toOption();
        }
        private Object readResolve() throws InvalidObjectException {
            throw new InvalidObjectException("Use Serialization Proxy instead.");
        }
        @Override
        public <R> Maybe<R> map(final Function<? super T, ? extends R> mapper) {
            return flatMap(t -> Maybe.just(mapper.apply(t)));
        }
        private static <T> Lazy<T> lazy(Eval<Maybe<T>> lazy) {
            return new Lazy<>(
                    lazy);
        }


        @Override
        public <R> Maybe<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
            return Maybe.fromLazy(lazy.map(m->m.flatMap(mapper)));

        }

        @Override
        public Maybe<T> filter(final Predicate<? super T> test) {
            return flatMap(t -> test.test(t) ? this : Maybe.nothing());
        }

        @Override
        public <R> R fold(final Function<? super T, ? extends R> some, final Supplier<? extends R> none) {
            Maybe<T> maybe = lazy.get();
            while (maybe instanceof Lazy) {
                maybe = ((Lazy<T>) maybe).lazy.get();
            }
            return maybe.fold(some,none);
        }

        @Override
        public ReactiveSeq<T> stream() {
            return Spouts.from(this);
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
                        sub.onNext(ts.orElse(null));
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
        public Trampoline<Maybe<T>> toTrampoline() {
            Trampoline<Maybe<T>> trampoline = lazy.toTrampoline();
            return new Trampoline<Maybe<T>>() {
                @Override
                public Maybe<T> get() {
                    Maybe<T> maybe = lazy.get();
                    while (maybe instanceof Lazy) {
                        maybe = ((Lazy<T>) maybe).lazy.get();
                    }
                    return maybe;
                }
                @Override
                public boolean complete(){
                    return false;
                }
                @Override
                public Trampoline<Maybe<T>> bounce() {
                    Maybe<T> maybe = lazy.get();
                    if(maybe instanceof Lazy){
                        return maybe.toTrampoline();
                    }
                    return Trampoline.done(maybe);

                }
            };
        }


        @Override
        public Maybe<T> recover(final T value) {
            return new Lazy<T>(
                    lazy.map(m -> m.recover(value)));
        }

        @Override
        public Maybe<T> recover(final Supplier<? extends T> value) {
            return new Lazy<T>(
                    lazy.map(m -> m.recover(value)));
        }
        @Override
        public Maybe<T> recoverWith(Supplier<? extends Option<T>> fn) {
            return new Lazy<T>(
                    lazy.map(m -> m.recoverWith(fn)));
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

            return maybe.hashCode();
        }


        @Override
        public boolean equals(final Object obj) {

           if (obj instanceof Nothing) {
                return !isPresent();
            }
            else if (obj instanceof None) {
                return !isPresent();
            }else if (obj instanceof Present && isPresent())
                return Objects.equals(orElse(null), ((Present) obj).orElse(null));
            else if (obj instanceof Lazy) {
                if (isPresent())
                    return Objects.equals(orElse(null), ((Maybe) obj).orElse(null));
                else {
                    return !((Lazy) obj).isPresent();
                }
            }
            return false;
        }

        @Override
        public <R> Maybe<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
            Eval<? extends Maybe<? extends R>> res = lazy.map(m -> m.concatMap(mapper));
            Eval<Maybe<R>> narrowed = (Eval)res;
            return Maybe.<R>fromLazy(narrowed);

        }

        @Override
        public <R> Maybe<R> mergeMap(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
            return Maybe.fromLazy(lazy.map(m->m.mergeMap(mapper)));

        }

        @Override
        public <R> R fold(Function<? super T, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return this.lazy.get().fold(fn1,fn2);
        }
    }

    public static class Nothing<T> implements Maybe<T> {

        private Object writeReplace() {
            return toOption();
        }
        private Object readResolve() throws InvalidObjectException {
            throw new InvalidObjectException("Use Serialization Proxy instead.");
        }
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
        public Maybe<T> recover(final T value) {
            return Maybe.of(value);
        }

        @Override
        public Maybe<T> recover(final Supplier<? extends T> value) {
            return new Just<T>(
                    Eval.later((Supplier<T>)value));
        }
        @Override
        public Maybe<T> recoverWith(Supplier<? extends Option<T>> fn) {

            return new Just<>(Eval.narrow(Eval.later(fn))).flatMap(m->m);

        }


        @Override
        public <R> R fold(final Function<? super T, ? extends R> some, final Supplier<? extends R> none) {
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

            if (obj instanceof Option) {
                return !((Option) obj).isPresent();
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
        public <R> Nothing<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
            return (Nothing<R>) EMPTY;
        }

        @Override
        public <R> Nothing<R> mergeMap(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
            return (Nothing<R>) EMPTY;
        }
        @Override
        public void forEach(Consumer<? super T> action) {

        }

        @Override
        public <R> R fold(Function<? super T, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            Option.None<T> none = None.NOTHING_EAGER;
            return fn2.apply(none);
        }
    }



  public static class Comprehensions {

    public static <T,F,R1, R2, R3,R4,R5,R6,R7> Maybe<R7> forEach(Maybe<T> free,
                                                                        Function<? super T, ? extends Maybe<R1>> value2,
                                                                        Function<? super Tuple2<? super T,? super R1>, ? extends Maybe<R2>> value3,
                                                                        Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Maybe<R3>> value4,
                                                                        Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Maybe<R4>> value5,
                                                                        Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Maybe<R5>> value6,
                                                                        Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Maybe<R6>> value7,
                                                                        Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, ? extends Maybe<R7>> value8
    ) {

      return free.flatMap(in -> {

        Maybe<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Maybe<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Maybe<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Maybe<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Maybe<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  Maybe<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f.flatMap(inf->{
                    Maybe<R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                    return g;

                  });

                });
              });

            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4,R5,R6> Maybe<R6> forEach(Maybe<T> free,
                                                                     Function<? super T, ? extends Maybe<R1>> value2,
                                                                     Function<? super Tuple2<? super T,? super R1>, ? extends Maybe<R2>> value3,
                                                                     Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Maybe<R3>> value4,
                                                                     Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Maybe<R4>> value5,
                                                                     Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Maybe<R5>> value6,
                                                                     Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Maybe<R6>> value7
    ) {

      return free.flatMap(in -> {

        Maybe<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Maybe<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Maybe<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Maybe<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Maybe<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  Maybe<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f;
                });
              });

            });

          });


        });


      });

    }

    public static <T,F,R1, R2, R3,R4,R5> Maybe<R5> forEach(Maybe<T> free,
                                                                  Function<? super T, ? extends Maybe<R1>> value2,
                                                                  Function<? super Tuple2<? super T,? super R1>, ? extends Maybe<R2>> value3,
                                                                  Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Maybe<R3>> value4,
                                                                  Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Maybe<R4>> value5,
                                                                  Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Maybe<R5>> value6
    ) {

      return free.flatMap(in -> {

        Maybe<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Maybe<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Maybe<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Maybe<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Maybe<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e;
              });
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4> Maybe<R4> forEach(Maybe<T> free,
                                                               Function<? super T, ? extends Maybe<R1>> value2,
                                                               Function<? super Tuple2<? super T,? super R1>, ? extends Maybe<R2>> value3,
                                                               Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Maybe<R3>> value4,
                                                               Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Maybe<R4>> value5

    ) {

      return free.flatMap(in -> {

        Maybe<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Maybe<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Maybe<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Maybe<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d;
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3> Maybe<R3> forEach(Maybe<T> free,
                                                            Function<? super T, ? extends Maybe<R1>> value2,
                                                            Function<? super Tuple2<? super T,? super R1>, ? extends Maybe<R2>> value3,
                                                            Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Maybe<R3>> value4

    ) {

      return free.flatMap(in -> {

        Maybe<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Maybe<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Maybe<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c;

          });


        });


      });

    }
    public static <T,F,R1, R2> Maybe<R2> forEach(Maybe<T> free,
                                                        Function<? super T, ? extends Maybe<R1>> value2,
                                                        Function<? super Tuple2<? super T,? super R1>, ? extends Maybe<R2>> value3

    ) {

      return free.flatMap(in -> {

        Maybe<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Maybe<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b;


        });


      });

    }
    public static <T,F,R1> Maybe<R1> forEach(Maybe<T> free,
                                                    Function<? super T, ? extends Maybe<R1>> value2


    ) {

      return free.flatMap(in -> {

        Maybe<R1> a = value2.apply(in);
        return a;


      });

    }


  }


}
