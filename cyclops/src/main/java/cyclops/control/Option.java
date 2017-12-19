package cyclops.control;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.option;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.matching.Sealed2;
import com.oath.cyclops.types.*;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.recoverable.Recoverable;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.data.tuple.*;
import cyclops.function.*;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.io.Serializable;
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
                                    Zippable<T>,
                                    Recoverable<T>,
                                    Sealed2<T,Option.None<T>>,
                                    Iterable<T>,
                                    Higher<option,T>,
                                    Serializable{





    @SuppressWarnings("rawtypes")
    final static Option EMPTY = new Option.None<>();
    public static  <T,R> Option<R> tailRec(T initial, Function<? super T, ? extends Option<? extends Either<T, R>>> fn){
      Option<? extends Either<T, R>> next[] = new Option[1];
      next[0] = Option.some(Either.left(initial));
      boolean cont = true;
      do {
        cont = next[0].visit(p -> p.visit(s -> {
          next[0] = narrowK(fn.apply(s));
          return true;
        }, pr -> false), () -> false);
      } while (cont);

      return next[0].map(x->x.visit(l->null,r->r));
    }
    public static <T> Option<T> narrowK(final Higher<option, T> opt) {
      return (Option<T>)opt;
    }
    public static <T> Higher<option, T> widen(Option<T> narrow) {
    return narrow;
  }
    public static <C2,T> Higher<C2, Higher<option,T>> widen2(Higher<C2, Option<T>> nestedMaybe){

      return (Higher)nestedMaybe;
    }
    /**
     * @return Get the zero Maybe (single instance)
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
     * @see com.oath.cyclops.types.MonadicValue#concatMap(java.util.function.Function)
     */
    @Override
    default <R> Option<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Option<R>) MonadicValue.super.concatMap(mapper);
    }

    default Option<T> orElseUse(Option<T> opt){
        if(isPresent())
            return this;
        return opt;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#flatMapP(java.util.function.Function)
     */
    @Override
    default <R> Option<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            return Option.fromPublisher(publisher);
        });
    }

    /**
     * Construct a Maybe  that contains a single value extracted from the supplied reactiveBuffer-streams Publisher
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
        return Spouts.from(pub)
                     .take(1)
                     .takeOne()
                     .toOption();

    }

    /**
     *  Construct an Option  that contains a single value extracted from the supplied Iterable
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

         Option<Integer> maybe = Option.fromIterable(reactiveStream);

        //Option[1]
     *
     * }
     * </pre>
     * @param iterable Iterable  to extract value from
     * @return Option populated with first value from Iterable (Option.zero if Publisher zero)
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
     * Construct an equivalent Option from the Supplied Optional
     * <pre>
     * {@code
     *   Option<Integer> some = Option.fromOptional(Optional.of(10));
     *   //Option[10], Some[10]
     *
     *   Option<Integer> none = Option.fromOptional(Optional.zero());
     *   //Option.empty, None[]
     * }
     * </pre>
     *
     * @param opt Optional to construct Maybe from
     * @return Option created from Optional
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
     * Construct an Option which contains the provided (non-null) value
     * Equivalent to @see {@link Option#some(Object)}
     * <pre>
     * {@code
     *
     *    Option<Integer> some = Option.of(10);
     *    some.map(i->i*2);
     * }
     * </pre>
     *
     * @param value Value to wrap inside a Maybe
     * @return Option containing the supplied value
     */
    static <T> Option<T> of(final T value) {
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
     *    //Option[10], Some[10]
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
     * Sequence operation, take a Collection of Options and turn it into a Option with a Collection
     * Only successes are retained. By constrast with {@link Option#sequence(IterableX)} Option#none/ None types are
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
     * @param maybes Option to Sequence
     * @return Option with a List of values
     */
    public static <T> Option<ReactiveSeq<T>> sequenceJust(final IterableX<? extends Option<T>> maybes) {
        return sequence(maybes.stream().filter(Option::isPresent));
    }

    /**
     * Sequence operation, take a Collection of Options and turn it into a Option with a Collection
     * By constrast with {@link Option#sequenceJust(IterableX)} if any Option types are None / zero
     * the return type will be an zero Option / None
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
     * @param maybes Option to Sequence
     * @return  Option with a List of values
     */
    public static <T> Option<ReactiveSeq<T>> sequence(final IterableX<? extends Option<T>> maybes) {
        return sequence(maybes.stream());

    }

    /**
     * Sequence operation, take a Stream of Option and turn it into a Option with a Stream
     * By constrast with {@link Maybe#sequenceJust(IterableX)} Option#zero/ None types are
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
     * @param maybes Option to Sequence
     * @return  Option with a Stream of values
     */
    public static <T> Option<ReactiveSeq<T>> sequence(final Stream<? extends Option<T>> maybes) {
        return sequence(ReactiveSeq.fromStream(maybes));


    }
  public static  <T> Option<ReactiveSeq<T>> sequence(ReactiveSeq<? extends Option<T>> stream) {

    Option<ReactiveSeq<T>> identity = Option.some(ReactiveSeq.empty());

    BiFunction<Option<ReactiveSeq<T>>,Option<T>,Option<ReactiveSeq<T>>> combineToStream = (acc,next) ->acc.zip(next,(a,b)->a.appendAll(b));

    BinaryOperator<Option<ReactiveSeq<T>>> combineStreams = (a,b)-> a.zip(b,(z1,z2)->z1.appendStream(z2));

    return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <T,R> Option<ReactiveSeq<R>> traverse(Function<? super T,? extends R> fn,ReactiveSeq<Option<T>> stream) {
    ReactiveSeq<Option<R>> s = stream.map(h -> h.map(fn));
    return sequence(s);
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
    public static <T, R> Option<R> accumulateJust(final IterableX<Option<T>> maybes, final Reducer<R,T> reducer) {
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
    public static <T, R> Option<R> accumulateJust(final IterableX<Option<T>> maybes, final Function<? super T, R> mapper,
                                                 final Monoid<R> reducer) {
        return sequenceJust(maybes).map(s -> s.map(mapper)
                .reduce(reducer));
    }

    /**
     * Accumulate the results only from those Maybes which have a value present, using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops.Monoids }.

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
    public static <T> Option<T> accumulateJust(final Monoid<T> reducer,final IterableX<Option<T>> maybes) {
        return sequenceJust(maybes).map(s -> s.reduce(reducer));
    }

  @Override
    default <R> Option<R> retry(final Function<? super T, ? extends R> fn) {
        return (Option<R>)MonadicValue.super.retry(fn);
    }

    @Override
    default <U> Option<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (Option)Zippable.super.zipWithPublisher(other);
    }

    @Override
    default <R> Option<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Option<R>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> Option<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Option)Zippable.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Option<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Option<R>)Zippable.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Option<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Option)Zippable.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Option<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Option<R>)Zippable.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <R> Option<R> flatMapS(final Function<? super T, ? extends Stream<? extends R>> mapper) {
        return (Option<R>)MonadicValue.super.flatMapS(mapper);
    }

    /* (non-Javadoc)
         * @see com.oath.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
         */
    @Override
    default <T2, R1, R2, R3, R> Option<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Option<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
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
     * @see com.oath.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Option<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Option<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Option<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Option<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Option<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Option<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Option<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Option<R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }


    @Override
    default <T2, R> Option<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

      return flatMap(a->Option.fromIterable(app).map(b->fn.apply(a,b)));
    }



    @Override
    default <T2, R> Option<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
      return flatMap(a->Option.fromPublisher(app).map(b->fn.apply(a,b)));

    }


  /*
   * (non-Javadoc)
   *
   * @see com.oath.cyclops.types.Zippable#zip(java.lang.Iterable)
   */
    @Override
    default <U> Option<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (Option) Zippable.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Option<T> unit(final T unit) {
        return Option.of(unit);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Option<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return (Option<R>) MonadicValue.super.coflatMap(mapper);
    }

    /*
     * cojoin (non-Javadoc)
     *
     * @see com.oath.cyclops.types.MonadicValue#nest()
     */
    @Override
    default Option<MonadicValue<T>> nest() {
        return (Option<MonadicValue<T>>) MonadicValue.super.nest();
    }

  /*
   * (non-Javadoc)
   *
   * @see com.oath.cyclops.value.Value#toMaybe()
   */
    @Override
    default Maybe<T> toMaybe(){
        return lazy();
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.foldable.Convertable#isPresent()
     */
    @Override
    boolean isPresent();



    Option<T> recover(Supplier<? extends T> value);

    Option<T> recover(T value);

    Option<T> recoverWith(Supplier<? extends Option<T>> fn);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#transform(java.util.function.Function)
     */
    @Override
    <R> Option<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    <R> Option<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.foldable.Convertable#visit(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    <R> R visit(Function<? super T, ? extends R> some, Supplier<? extends R> none);

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.lambda.monads.Filters#filter(java.util.function.
     * Predicate)
     */
    @Override
    Option<T> filter(Predicate<? super T> fn);

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.lambda.monads.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Option<U> ofType(final Class<? extends U> type) {

        return (Option<U>) MonadicValue.super.ofType(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.lambda.monads.Filters#filterNot(java.util.function.
     * Predicate)
     */
    @Override
    default Option<T> filterNot(final Predicate<? super T> fn) {

        return (Option<T>) MonadicValue.super.filterNot(fn);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.lambda.monads.Filters#notNull()
     */
    @Override
    default Option<T> notNull() {

        return (Option<T>) MonadicValue.super.notNull();
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
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
         * @see com.oath.cyclops.lambda.monads.Functor#trampoline(java.util.function.
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
        private static final long serialVersionUID = 1L;
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
        private static final long serialVersionUID = 1L;
        public static None NOTHING_EAGER = new None();

      private Object readResolve() {
        return NOTHING_EAGER;
      }
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
        public <R> None<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
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
  public static class Comprehensions {

    public static <T,F,R1, R2, R3,R4,R5,R6,R7> Option<R7> forEach(Option<T> option,
                                                                        Function<? super T, ? extends Option<R1>> value2,
                                                                        Function<? super Tuple2<? super T,? super R1>, ? extends Option<R2>> value3,
                                                                        Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Option<R3>> value4,
                                                                        Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Option<R4>> value5,
                                                                        Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Option<R5>> value6,
                                                                        Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Option<R6>> value7,
                                                                        Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, ? extends Option<R7>> value8
    ) {

      return option.flatMap(in -> {

        Option<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Option<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Option<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Option<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Option<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  Option<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f.flatMap(inf->{
                    Option<R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                    return g;

                  });

                });
              });

            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4,R5,R6> Option<R6> forEach(Option<T> option,
                                                                     Function<? super T, ? extends Option<R1>> value2,
                                                                     Function<? super Tuple2<? super T,? super R1>, ? extends Option<R2>> value3,
                                                                     Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Option<R3>> value4,
                                                                     Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Option<R4>> value5,
                                                                     Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Option<R5>> value6,
                                                                     Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Option<R6>> value7
    ) {

      return option.flatMap(in -> {

        Option<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Option<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Option<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Option<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Option<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  Option<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f;
                });
              });

            });

          });


        });


      });

    }

    public static <T,F,R1, R2, R3,R4,R5> Option<R5> forEach(Option<T> option,
                                                                  Function<? super T, ? extends Option<R1>> value2,
                                                                  Function<? super Tuple2<? super T,? super R1>, ? extends Option<R2>> value3,
                                                                  Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Option<R3>> value4,
                                                                  Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Option<R4>> value5,
                                                                  Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Option<R5>> value6
    ) {

      return option.flatMap(in -> {

        Option<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Option<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Option<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Option<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Option<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e;
              });
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4> Option<R4> forEach(Option<T> option,
                                                               Function<? super T, ? extends Option<R1>> value2,
                                                               Function<? super Tuple2<? super T,? super R1>, ? extends Option<R2>> value3,
                                                               Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Option<R3>> value4,
                                                               Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Option<R4>> value5

    ) {

      return option.flatMap(in -> {

        Option<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Option<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Option<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Option<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d;
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3> Option<R3> forEach(Option<T> option,
                                                            Function<? super T, ? extends Option<R1>> value2,
                                                            Function<? super Tuple2<? super T,? super R1>, ? extends Option<R2>> value3,
                                                            Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Option<R3>> value4

    ) {

      return option.flatMap(in -> {

        Option<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Option<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Option<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c;

          });


        });


      });

    }
    public static <T,F,R1, R2> Option<R2> forEach(Option<T> option,
                                                        Function<? super T, ? extends Option<R1>> value2,
                                                        Function<? super Tuple2<? super T,? super R1>, ? extends Option<R2>> value3

    ) {

      return option.flatMap(in -> {

        Option<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Option<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b;


        });


      });

    }
    public static <T,F,R1> Option<R1> forEach(Option<T> option,
                                                    Function<? super T, ? extends Option<R1>> value2


    ) {

      return option.flatMap(in -> {

        Option<R1> a = value2.apply(in);
        return a;


      });

    }


  }


}
