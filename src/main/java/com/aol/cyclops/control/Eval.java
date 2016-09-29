package com.aol.cyclops.control;

import static com.aol.cyclops.control.For.Values.each2;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
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
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.MonadicValue1;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.function.Memoize;

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
 * @param <T>
 */
public interface Eval<T>
        extends Supplier<T>, MonadicValue1<T>, Functor<T>, Filterable<T>, ApplicativeFunctor<T>, Matchable.ValueAndOptionalMatcher<T> {

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
    public static <T> Eval<T> fromPublisher(Publisher<T> pub) {
        ValueSubscriber<T> sub = ValueSubscriber.subscriber();
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
    public static <T> Eval<T> fromIterable(Iterable<T> iterable) {
        Iterator<T> it = iterable.iterator();
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
    public static <T> Eval<T> now(T value) {
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
    public static <T> Eval<T> later(Supplier<T> value) {

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
    public static <T> Eval<T> always(Supplier<T> value) {
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
    public static <T> Eval<ListX<T>> sequence(CollectionX<Eval<T>> evals) {
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
    public static <T> Eval<ReactiveSeq<T>> sequence(Stream<Eval<T>> evals) {
        return AnyM.sequence(evals.map(f -> AnyM.fromEval(f)), () -> AnyM.fromEval(Eval.now(ReactiveSeq.<T> empty())))
                   .map(s -> ReactiveSeq.fromStream(s))

                   .unwrap();

    }

    public static <T, R> Eval<R> accumulate(CollectionX<Eval<T>> evals, Reducer<R> reducer) {
        return sequence(evals).map(s -> s.mapReduce(reducer));
    }

    public static <T, R> Eval<R> accumulate(CollectionX<Eval<T>> maybes, Function<? super T, R> mapper, Semigroup<R> reducer) {
        return sequence(maybes).map(s -> s.map(mapper)
                                          .reduce(reducer.reducer())
                                          .get());
    }

    public static <T> Eval<T> accumulate(CollectionX<Eval<T>> maybes, Semigroup<T> reducer) {
        return sequence(maybes).map(s -> s.reduce(reducer.reducer())
                                          .get());
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
     * @see com.aol.cyclops.types.MonadicValue1#flatMap(java.util.function.Function)
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
    default <R> Eval<R> coflatMap(Function<? super MonadicValue<T>, R> mapper) {
        return (Eval<R>) MonadicValue1.super.coflatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue1#combineEager(com.aol.cyclops.Monoid, com.aol.cyclops.types.MonadicValue)
     */
    @Override
    default Eval<T> combineEager(Monoid<T> monoid, MonadicValue<? extends T> v2) {
        return unit(each2(this, t1 -> v2, (t1, t2) -> monoid.combiner()
                                                            .apply(t1, t2)).orElseGet(() -> this.orElseGet(() -> monoid.zero())));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
    @Override
    default Eval<MonadicValue<T>> nest() {
        return (Eval<MonadicValue<T>>) MonadicValue1.super.nest();
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
    default <U> Maybe<U> ofType(Class<? extends U> type) {

        return (Maybe<U>) Filterable.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default Maybe<T> filterNot(Predicate<? super T> fn) {

        return (Maybe<T>) Filterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default Maybe<T> notNull() {

        return (Maybe<T>) Filterable.super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    default Maybe<T> filter(Predicate<? super T> pred) {
        return toMaybe().filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> Eval<U> cast(Class<? extends U> type) {
        return (Eval<U>) ApplicativeFunctor.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default Eval<T> peek(Consumer<? super T> c) {
        return (Eval<T>) ApplicativeFunctor.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (Eval<R>) ApplicativeFunctor.super.trampoline(mapper);
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#visit(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> R visit(Function<? super T, ? extends R> present, Supplier<? extends R> absent) {
        T value = get();
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
    static <R> Eval<R> narrow(Eval<? extends R> broad) {
        return (Eval<R>) broad;
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#combine(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Eval<R> combine(Value<? extends T2> app, BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Eval<R>) ApplicativeFunctor.super.combine(app, fn);
    }

    
    /* Equivalent to combine, but accepts an Iterable and takes the first value only from that iterable.
     * (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Eval<R> zip(Iterable<? extends T2> app, BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Eval<R>) ApplicativeFunctor.super.zip(app, fn);
    }

   
    /* Equivalent to combine, but accepts a Publisher and takes the first value only from that publisher.
     * 
     * (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Eval<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> app) {
        return (Eval<R>) ApplicativeFunctor.super.zip(fn, app);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Eval<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (Eval<R>) MonadicValue1.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Eval<R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (Eval<R>) MonadicValue1.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> Eval<Tuple2<T, U>> zip(Stream<? extends U> other) {
        return (Eval) MonadicValue1.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> Eval<Tuple2<T, U>> zip(Seq<? extends U> other) {
        return (Eval) MonadicValue1.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Eval<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (Eval) MonadicValue1.super.zip(other);
    }

    static class Module {

        static <T> Eval<T> asEval(MonadicValue<T> value){
            if(value instanceof Eval)
                return (Eval<T>)value;
            return value.toEvalAlways();
        }
        public static class Later<T> extends Rec<T>implements Eval<T> {

            Later(Function<Object, ? extends T> s) {
                super(PVectorX.of(Rec.raw(Memoize.memoizeFunction(s))));
            }

            Later(PVectorX<Function<Object, Object>> s) {
                super(s);

            }

            public <R> Eval<R> map(Function<? super T, ? extends R> mapper) {

                return new Later<R>(
                                    super.fns.plus(Rec.raw(Memoize.memoizeFunction(mapper))));
            }

           
            public <R> Eval<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
                RecFunction s = __ -> asEval(mapper.apply(super.apply()))
                                            .steps();
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
            public <T> Eval<T> unit(T unit) {
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
            public boolean equals(Object obj) {
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

            Always(Function<Object, ? extends T> s) {
                super(PVectorX.of(Rec.raw(s)));
            }

            Always(PVectorX<Function<Object, Object>> s) {
                super(s);

            }

            public <R> Eval<R> map(Function<? super T, ? extends R> mapper) {

                return new Always<R>(
                                     fns.plus(Rec.raw(mapper)));

            }

            public <R> Eval<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
                RecFunction s = __ -> asEval(mapper.apply(apply()))
                                            .steps();
                return new Always<R>(
                                     PVectorX.of(s));
            }

            @Override
            public T get() {
                return super.get();
            }

            @Override
            public <T> Eval<T> unit(T unit) {
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
            public boolean equals(Object obj) {
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

            Rec(PVectorX<Function<Object, Object>> s) {
                fns = s;
            }

            private static Function<Object, Object> raw(Function<?, ?> fn) {
                return (Function<Object, Object>) fn;
            }

            static interface RecFunction extends Function<Object, Object> {

            }

            public PVectorX<Function<Object, Object>> steps() {
                return fns;
            }

            T apply() {
                Object input = VOID;
                for (Function<Object, Object> n : fns) {
                    DequeX<Function<Object, Object>> newFns = DequeX.of(n);
                    while (newFns.size() > 0) {
                        Function<Object, Object> next = newFns.pop();
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
