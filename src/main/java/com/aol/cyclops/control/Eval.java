package com.aol.cyclops.control;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Combiner;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.function.Memoize;
import com.aol.cyclops.util.function.F4;
import com.aol.cyclops.util.function.F3;

/**
 * Represents a computation that can be defered (always), cached (later) or immediate(now).
 * Supports tail recursion via map / flatMap. 
 * Computations are always Lazy even when performed against a Now instance. 
 * Heavily inspired by Cats Eval @link https://github.com/typelevel/cats/blob/master/core/src/main/scala/cats/Eval.scala
 * 
 * Tail Recursion example
 * <pre>
 * {@code 
 * 
 * public void odd(){
        System.out.println(even(Eval.now(200000)).get());
    }
    public Eval<String> odd(Eval<Integer> n )  {
       
       return n.flatMap(x->even(Eval.now(x-1)));
    }
    public Eval<String> even(Eval<Integer> n )  {
        return n.flatMap(x->{
            return x<=0 ? Eval.now("done") : odd(Eval.now(x-1));
        });
     }
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Type of value storable in this Eval
 */
public interface Eval<T> extends    To<Eval<T>>,
                                    MonadicValue<T> {

   
    /**
     * Create an Eval instance from a reactive-streams publisher
     * 
     * <pre>
     * {@code
     *    Eval<Integer> e = Eval.fromPublisher(Mono.just(10));
     *    //Eval[10]
     * }
     * </pre>
     * 
     * 
     * @param pub Publisher to create the Eval from
     * @return Eval created from Publisher
     */
    public static <T> Eval<T> fromPublisher(final Publisher<T> pub) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toEvalLater();
    }

    /**
     * Create an Eval instance from an Iterable
     * 
     * <pre>
     * {@code
     *    Eval<Integer> e = Eval.fromIterable(Arrays.asList(10));
     *    //Eval[10]
     * }
     * </pre>
     * @param iterable to create the Eval from
     * @return Eval created from Publisher
     */
    public static <T> Eval<T> fromIterable(final Iterable<T> iterable) {
        final Iterator<T> it = iterable.iterator();
        return Eval.later(() -> it.hasNext() ? it.next() : null);
    }

    /**
     * Create an Eval with the value specified
     * 
     * <pre>
     * {@code
     *   Eval<Integer> e = Eval.now(10);
     *   //Eval[10]
     * }</pre>
     * 
     * @param value of Eval
     * @return Eval with specified value
     */
    public static <T> Eval<T> now(final T value) {
        return always(() -> value);

    }

    /**
     * Lazily create an Eval from the specified Supplier. Supplier#get will only be called once. Return values of Eval operations will also
     * be cached (later indicates lazy and caching - characteristics can be changed using flatMap).
     * 
     * <pre>
     * {@code
     *   Eval<Integer> e = Eval.later(()->10)
     *                         .map(i->i*2);
     *   //Eval[20] - lazy so will not be executed until the value is accessed
     * }</pre>
     * 
     * 
     * @param value Supplier to (lazily) populate this Eval
     * @return Eval with specified value
     */
    public static <T> Eval<T> later(final Supplier<T> value) {
        return new Module.Later<T>(
                                   in -> value.get());
    }

    /**
     * Lazily create an Eval from the specified Supplier. Supplier#get will only be every time get is called on the resulting Eval.
     * 
     * <pre>
     * {@code
     *   Eval<Integer> e = Eval.always(()->10)
     *                         .map(i->i*2);
     *   //Eval[20] - lazy so will not be executed until the value is accessed
     * }</pre>
     * 
     * 
     * @param value  Supplier to (lazily) populate this Eval
     * @return Eval with specified value
     */
    public static <T> Eval<T> always(final Supplier<T> value) {
        return new Module.Always<T>(
                                    in -> value.get());
    }

    /**
     * Turn a collection of Evals into a single Eval with a List of values.
     * 
     * <pre>
     * {@code 
     *  Eval<ListX<Integer>> maybes =Eval.sequence(ListX.of(Eval.now(10),Eval.now(1)));
        //Eval.now(ListX.of(10,1)));
     * 
     * }
     * </pre>
     * 
     * @param evals Collection of evals to convert into a single eval with a List of values
     * @return  Eval with a  list of values
     */
    public static <T> Eval<ListX<T>> sequence(final CollectionX<Eval<T>> evals) {
        return sequence(evals.stream()).map(s -> s.toListX());

    }

    /**
     * Turn a Stream of Evals into a single Eval with a Stream of values.
     * 
     * <pre>
     * {@code 
     *  Eval<ReactiveSeq<Integer>> maybes =Eval.sequence(Stream.of(Eval.now(10),Eval.now(1)));
        //Eval.now(ReactiveSeq.of(10,1)));
     * 
     * }
     * </pre>
     * 
     * @param evals Collection of evals to convert into a single eval with a List of values
     * @return  Eval with a  list of values
     */
    public static <T> Eval<ReactiveSeq<T>> sequence(final Stream<? extends Eval<T>> evals) {
        return AnyM.sequence(evals.map(AnyM::fromEval),Witness.eval.INSTANCE)
                   .map(ReactiveSeq::fromStream)
                   .to(Witness::eval);
    }

    /**
     * Sequence and reduce a CollectionX of Evals into an Eval with a reduced value
     * 
     * <pre>
     * {@code 
     *   Eval<PSetX<Integer>> accumulated = Eval.accumulate(ListX.of(just,Eval.now(1)),Reducers.toPSetX());
         //Eval.now(PSetX.of(10,1)))
     * }
     * </pre>
     * 
     * @param evals Collection of Evals to accumulate
     * @param reducer Reducer to fold nested values into
     * @return Eval with a value
     */
    public static <T, R> Eval<R> accumulate(final CollectionX<Eval<T>> evals, final Reducer<R> reducer) {
        return sequence(evals).map(s -> s.mapReduce(reducer));
    }

    /**
     * Sequence and reduce a CollectionX of Evals into an Eval with a reduced value
     * 
     * <pre>
     * {@code
     *   Eval<String> evals =Eval.accumulate(ListX.of(just,Eval.later(()->1)),i->""+i,Monoids.stringConcat);
         //Eval.now("101")
     * }
     * </pre>
     * 
     * 
     * @param evals Collection of Evals to accumulate
     * @param mapper Funtion to map Eval contents to type required by Semigroup accumulator
     * @param reducer Combiner function to apply to converted values
     * @return  Eval with a value
     */
    public static <T, R> Eval<R> accumulate(final CollectionX<Eval<T>> evals, final Function<? super T, R> mapper, final Monoid<R> reducer) {
        return sequence(evals).map(s -> s.map(mapper)
                                          .reduce(reducer)
                                          );
    }

    /**
     *  Sequence and reduce a CollectionX of Evals into an Eval with a reduced value
     * 
     * <pre>
     * {@code 
     *   Eval<Integer> maybes =Eval.accumulate(Monoids.intSum,ListX.of(just,Eval.now(1)));
         //Eval.now(11)
     * 
     * }
     * </pre>
     * 
     * 
     * @param evals Collection of Evals to accumulate
     * @param reducer Combiner function to apply to converted values
     * @return Eval with a value
     */
    public static <T> Eval<T> accumulate(final Monoid<T> reducer,final CollectionX<Eval<T>> evals) {
        return sequence(evals).map(s -> s.reduce(reducer));
    }
   
    @Override
    default Maybe<T> toMaybe(){
        return Maybe.fromEvalNullable(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    public <T> Eval<T> unit(T unit);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    public <R> Eval<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    public <R> Eval<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);

    default PVectorX<Function<Object, Object>> steps() {
        return PVectorX.of(__ -> get());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return (Eval<R>) MonadicValue.super.coflatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#combineEager(com.aol.cyclops.Monoid, com.aol.cyclops.types.MonadicValue)
     */
    @Override
    default Eval<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return unit(this.forEach2( t1 -> v2, (t1, t2) -> monoid
                                                            .apply(t1, t2)).orElseGet(() -> orElseGet(() -> monoid.zero())));
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#flatMapIterable(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Eval<R>)MonadicValue.super.flatMapIterable(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#flatMapPublisher(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return (Eval<R>)MonadicValue.super.flatMapPublisher(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Applicative#combine(java.util.function.BinaryOperator, com.aol.cyclops.types.Applicative)
     */
    @Override
    default Eval<T> combine(BinaryOperator<Combiner<T>> combiner, Combiner<T> app) {
        return (Eval)MonadicValue.super.combine(combiner, app);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
    @Override
    default Eval<MonadicValue<T>> nest() {
        return (Eval<MonadicValue<T>>) MonadicValue.super.nest();
    }

    /* (non-Javadoc)
     * @see java.util.function.Supplier#get()
     */
    @Override
    public T get();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default Maybe<T> notNull() {

        return (Maybe<T>) MonadicValue.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    default Maybe<T> filter(final Predicate<? super T> pred) {
        return toMaybe().filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> Eval<U> cast(final Class<? extends U> type) {
        return (Eval<U>) MonadicValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default Eval<T> peek(final Consumer<? super T> c) {
        return (Eval<T>) MonadicValue.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (Eval<R>) MonadicValue.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#visit(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> R visit(final Function<? super T, ? extends R> present, final Supplier<? extends R> absent) {
        final T value = get();
        if (value != null)
            return present.apply(value);
        return absent.get();
    }

    /**
     * Narrow covariant type parameter
     * 
     * @param broad Eval with covariant type parameter
     * @return Narrowed Eval
     */
    static <R> Eval<R> narrow(final Eval<? extends R> broad) {
        return (Eval<R>) broad;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#combine(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Eval<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Eval<R>) MonadicValue.super.combine(app, fn);
    }

    /* Equivalent to combine, but accepts an Iterable and takes the first value only from that iterable.
     * (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Eval<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Eval<R>) MonadicValue.super.zip(app, fn);
    }

    /* Equivalent to combine, but accepts a Publisher and takes the first value only from that publisher.
     * 
     * (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Eval<R> zipP( final Publisher<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (Eval<R>) MonadicValue.super.zip(fn, app);

    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Eval<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (Eval<R>) MonadicValue.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> Eval<Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        return (Eval) MonadicValue.super.zipS(other);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Eval<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (Eval) MonadicValue.super.zip(other);
    }

    
    
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Eval<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            F3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            F4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Eval<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Eval<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            F3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            F4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            F4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (Eval<R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Eval<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            F3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
      
        return (Eval<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Eval<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            F3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            F3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Eval<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Eval<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Eval<R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }




    static class Module {

        static <T> Eval<T> asEval(final MonadicValue<T> value) {

            if (value instanceof Eval)
                return (Eval<T>) value;
            return value.toEvalAlways();
        }

        public static class Later<T> extends Rec<T>implements Eval<T> {

            Later(final Function<Object, ? extends T> s) {
                super(PVectorX.of(Rec.raw(Memoize.memoizeFunction(s))));
            }

            Later(final PVectorX<Function<Object, Object>> s) {
                super(s);

            }

            @Override
            public <R> Eval<R> map(final Function<? super T, ? extends R> mapper) {

                return new Later<R>(
                                    super.fns.plus(Rec.raw(Memoize.memoizeFunction(mapper))));
            }

            @Override
            public <R> Eval<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
                final RecFunction s = __ -> asEval(mapper.apply(super.apply())).steps();

                return new Later<R>(
                                    PVectorX.of(s));

            }

            @Override
            public T get() {
                return super.get();
            }

            /* (non-Javadoc)
             * @see com.aol.cyclops.lambda.monads.Unit#unit(java.lang.Object)
             */
            @Override
            public <T> Eval<T> unit(final T unit) {
                return Eval.later(() -> unit);
            }

            /* (non-Javadoc)
             * @see com.aol.cyclops.value.Value#toEvalLater()
             */
            @Override
            public Eval<T> toEvalLater() {
                return this;
            }

            /* (non-Javadoc)
             * @see java.lang.Object#hashCode()
             */
            @Override
            public int hashCode() {
                return get().hashCode();
            }

            /* (non-Javadoc)
             * @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals(final Object obj) {
                if (!(obj instanceof Eval))
                    return false;
                return Objects.equals(get(), ((Eval) obj).get());
            }

            @Override
            public String toString() {
                return mkString();
            }

        }

        public static class Always<T> extends Rec<T>implements Eval<T> {

            Always(final Function<Object, ? extends T> s) {
                super(PVectorX.of(Rec.raw(s)));
            }

            Always(final PVectorX<Function<Object, Object>> s) {
                super(s);

            }

            @Override
            public <R> Eval<R> map(final Function<? super T, ? extends R> mapper) {

                return new Always<R>(
                                     fns.plus(Rec.raw(mapper)));

            }

            @Override
            public <R> Eval<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
                final RecFunction s = __ -> asEval(mapper.apply(apply())).steps();

                return new Always<R>(
                                     PVectorX.of(s));
            }

            @Override
            public T get() {
                return super.get();
            }

            @Override
            public <T> Eval<T> unit(final T unit) {
                return Eval.always(() -> unit);
            }

            /* (non-Javadoc)
             * @see com.aol.cyclops.value.Value#toEvalAlways()
             */
            @Override
            public Eval<T> toEvalAlways() {
                return this;
            }

            /* (non-Javadoc)
             * @see java.lang.Object#hashCode()
             */
            @Override
            public int hashCode() {
                return get().hashCode();
            }

            /* (non-Javadoc)
             * @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals(final Object obj) {
                if (!(obj instanceof Eval))
                    return false;
                return Objects.equals(get(), ((Eval) obj).get());
            }

            @Override
            public String toString() {
                return mkString();
            }

        }

        private static class Rec<T> {
            final PVectorX<Function<Object, Object>> fns;
            private final static Object VOID = new Object();

            Rec(final PVectorX<Function<Object, Object>> s) {
                fns = s;
            }

            private static Function<Object, Object> raw(final Function<?, ?> fn) {
                return (Function<Object, Object>) fn;
            }

            static interface RecFunction extends Function<Object, Object> {

            }

            public PVectorX<Function<Object, Object>> steps() {
                return fns;
            }

            T apply() {
                Object input = VOID;
                for (final Function<Object, Object> n : fns) {
                    final DequeX<Function<Object, Object>> newFns = DequeX.of(n);
                    while (newFns.size() > 0) {
                        final Function<Object, Object> next = newFns.pop();
                        if (next instanceof RecFunction) {
                            newFns.plusAll((List) ((RecFunction) next).apply(VOID));
                        } else
                            input = next.apply(input);

                    }
                }
                return (T) input;
            }

            public T get() {
                return apply();
            }

        }

    }

}
