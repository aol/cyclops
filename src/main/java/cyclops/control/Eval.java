package cyclops.control;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.hkt.Higher;
import cyclops.typeclasses.*;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.reactive.Completable;
import cyclops.async.Future;
import cyclops.collections.box.Mutable;
import cyclops.collections.mutable.ListX;
import cyclops.function.*;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.eval;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.EvalT;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Represents a computation that can be deferred (always), cached (later) or immediate(now).
 * Supports tail recursion via map / flatMap.
 * Unrestricted are always Lazy even when performed against a Now instance.
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
public interface Eval<T> extends To<Eval<T>>,
                                    MonadicValue<T>,
                                    Higher<eval ,T>{

    public static  <T,R> Eval<R> tailRec(T initial, Function<? super T, ? extends Eval<? extends Xor<T, R>>> fn){
        return narrowK(fn.apply(initial)).flatMap( eval ->
                eval.visit(s->tailRec(s,fn),p->Eval.now(p)));
    }
    public static  <T> Kleisli<eval,Eval<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), Eval::widen);
    }
    public static <T> Higher<eval, T> widen(Eval<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<eval,T,Eval<T>> kindCokleisli(){
        return Cokleisli.of(Eval::narrowK);
    }
    public static <W1,T> Nested<eval,W1,T> nested(Eval<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<eval,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,eval,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<eval,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }

    default <W2,R> Nested<eval,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    static <T> Eval<T> async(final Executor ex, final Supplier<T> s){
        return fromFuture(Future.of(s,ex));
    }
    default <W extends WitnessType<W>> EvalT<W, T> liftM(W witness) {
        return EvalT.of(witness.adapter().unit(this));
    }

    default AnyM<eval,T> anyM(){
        return AnyM.fromEval(this);
    }



    /**
     * Convert the raw Higher Kinded Type for Evals types into the Eval interface
     *
     * @param future HKT encoded list into a OptionalType
     * @return Eval
     */
    public static <T> Eval<T> narrowK(final Higher<eval, T> future) {
        return (Eval<T>)future;
    }
    /**
     * Create an Eval instance from a reactiveBuffer-streams publisher
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
        return fromFuture(Future.fromPublisher(pub));
    }

    /**
     * Create a reactiveBuffer CompletableEval
     *
     * <pre>
     *     {@code
     *      CompletableEval<Integer,Integer> completable = Eval.eval();
            Eval<Integer> mapped = completable.map(i->i*2)
                                              .flatMap(i->Eval.later(()->i+1));

            completable.complete(5);

            mapped.printOut();
            //11
     *     }
     * </pre>
     *
     * @param <T> Data input type to the Eval
     * @return A reactiveBuffer CompletableEval
     */
    static <T> CompletableEval<T,T> eval(){
        Completable.CompletablePublisher<T> c = new Completable.CompletablePublisher<T>();
        return new CompletableEval<T, T>(c,fromFuture(Future.fromPublisher(c)));

    }
    @AllArgsConstructor
    static class CompletableEval<ORG,T2> implements Eval<T2>, Completable<ORG>{
        public final Completable.CompletablePublisher<ORG> complete;
        public final Eval<T2> lazy;

        @Override
        public boolean isFailed() {
            return complete.isFailed();
        }

        @Override
        public boolean isDone() {
            return complete.isDone();
        }

        @Override
        public boolean complete(ORG complete) {
            return this.complete.complete(complete);
        }

        @Override
        public boolean completeExceptionally(Throwable error) {
            return complete.completeExceptionally(error);
        }

        @Override
        public <T> Eval<T> unit(T unit) {
            return lazy.unit(unit);
        }

        @Override
        public <R> Eval<R> map(Function<? super T2, ? extends R> mapper) {
            return lazy.map(mapper);
        }

        @Override
        public <R> Eval<R> flatMap(Function<? super T2, ? extends MonadicValue<? extends R>> mapper) {
            return lazy.flatMap(mapper);
        }

        @Override
        public T2 get() {
            return lazy.get();
        }
    }

    public static <T> Eval<T> coeval(final Future<Eval<T>> pub) {
        return new Module.FutureAlways<T>(pub);
    }
    public static <T> Eval<T> fromFuture(final Future<T> pub) {
        return coeval(pub.map(Eval::now));
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
     * be cached (later indicates maybe and caching - characteristics can be changed using flatMap).
     *
     * <pre>
     * {@code
     *   Eval<Integer> e = Eval.later(()->10)
     *                         .map(i->i*2);
     *   //Eval[20] - maybe so will not be executed until the value is accessed
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
    public static <T> Eval<T> defer(final Supplier<Eval<T>> value) {
        return new Module.Later<T>(
                in -> value.get().get());
    }

    /**
     * Lazily create an Eval from the specified Supplier. Supplier#get will only be every time get is called on the resulting Eval.
     *
     * <pre>
     * {@code
     *   Eval<Integer> e = Eval.always(()->10)
     *                         .map(i->i*2);
     *   //Eval[20] - maybe so will not be executed until the value is accessed
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
     * Turn a toX of Evals into a singleUnsafe Eval with a List of values.
     *
     * <pre>
     * {@code
     *  Eval<ListX<Integer>> maybes =Eval.sequence(ListX.of(Eval.now(10),Eval.now(1)));
        //Eval.now(ListX.of(10,1)));
     *
     * }
     * </pre>
     *
     * @param evals Collection of evals to convert into a singleUnsafe eval with a List of values
     * @return  Eval with a  list of values
     */
    public static <T> Eval<ListX<T>> sequence(final CollectionX<Eval<T>> evals) {
        return sequence(evals.stream()).map(s -> s.to().listX());

    }

    /**
     * Turn a Stream of Evals into a singleUnsafe Eval with a Stream of values.
     *
     * <pre>
     * {@code
     *  Eval<ReactiveSeq<Integer>> maybes =Eval.sequence(Stream.of(Eval.now(10),Eval.now(1)));
        //Eval.now(ReactiveSeq.of(10,1)));
     *
     * }
     * </pre>
     *
     * @param evals Collection of evals to convert into a singleUnsafe eval with a List of values
     * @return  Eval with a  list of values
     */
    public static <T> Eval<ReactiveSeq<T>> sequence(final Stream<? extends Eval<T>> evals) {
        return AnyM.sequence(evals.map(AnyM::fromEval), eval.INSTANCE)
                   .map(ReactiveSeq::fromStream)
                   .to(Witness::eval);
    }

    /**
     * Sequence and reduce a CollectionX of Evals into an Eval with a reduced value
     *
     * <pre>

     * {@code
     *   Eval<PersistentSetX<Integer>> accumulated = Eval.accumulate(ListX.of(just,Eval.now(1)),Reducers.toPersistentSetX());
         //Eval.now(PersistentSetX.of(10,1)))
     * }
     * </pre>
     *
     * @param evals Collection of Evals to accumulate
     * @param reducer Reducer to fold nest values into
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
     * @param reducer Combiner function to applyHKT to converted values
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
     * @param reducer Combiner function to applyHKT to converted values
     * @return Eval with a value
     */
    public static <T> Eval<T> accumulate(final Monoid<T> reducer,final CollectionX<Eval<T>> evals) {
        return sequence(evals).map(s -> s.reduce(reducer));
    }

    default Trampoline<T> toTrampoline(){
        return Trampoline.more(()->Trampoline.done(get()));
    }

    @Override
    default Maybe<T> toMaybe(){
        return Maybe.fromEvalNullable(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    public <T> Eval<T> unit(T unit);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    public <R> Eval<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    public <R> Eval<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);


    default PVector<Function<Object, Object>> steps() {
        return TreePVector.singleton(__ -> get());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return (Eval<R>) MonadicValue.super.coflatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#combineEager(cyclops2.function.Monoid, com.aol.cyclops2.types.MonadicValue)
     */
    @Override
    default Eval<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return unit(this.forEach2( t1 -> v2, (t1, t2) -> monoid
                                                            .apply(t1, t2)).orElseGet(() -> orElseGet(() -> monoid.zero())));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapI(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Eval<R>)MonadicValue.super.flatMapI(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapP(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            return Eval.fromPublisher(publisher);
        });
    }


    @Override
    default Eval<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (Eval)MonadicValue.super.zip(combiner, app);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#nest()
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
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#notNull()
     */
    @Override
    default Maybe<T> notNull() {

        return (Maybe<T>) MonadicValue.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    default Maybe<T> filter(final Predicate<? super T> pred) {
        return toMaybe().filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> Eval<U> cast(final Class<? extends U> type) {
        return (Eval<U>) MonadicValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default Eval<T> peek(final Consumer<? super T> c) {
        return (Eval<T>) MonadicValue.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Eval<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (Eval<R>) MonadicValue.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Convertable#visit(java.util.function.Function, java.util.function.Supplier)
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
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#combine(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Eval<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Eval<R>) MonadicValue.super.combine(app, fn);
    }

    /* Equivalent to combine, but accepts an Iterable and takes the first value only from that iterable.
     * (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Eval<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Eval<R>) MonadicValue.super.zip(app, fn);
    }

    /* Equivalent to combine, but accepts a Publisher and takes the first value only from that publisher.
     *
     * (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> Eval<R> zipP( final Publisher<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (Eval<R>) MonadicValue.super.zipP(app,fn);

    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Eval<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (Eval<R>) MonadicValue.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> Eval<Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        return (Eval) MonadicValue.super.zipS(other);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Eval<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (Eval) MonadicValue.super.zip(other);
    }

    @Override
    default <R> Eval<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (Eval<R>)MonadicValue.super.zipWith(fn);
    }

    @Override
    default <R> Eval<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (Eval<R>)MonadicValue.super.zipWithS(fn);
    }

    @Override
    default <R> Eval<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (Eval<R>)MonadicValue.super.zipWithP(fn);
    }

    @Override
    default <R> Eval<R> retry(final Function<? super T, ? extends R> fn) {
        return (Eval<R>)MonadicValue.super.retry(fn);
    }

    @Override
    default <U> Eval<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (Eval)MonadicValue.super.zipP(other);
    }

    @Override
    default <R> Eval<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Eval<R>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> Eval<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Eval)MonadicValue.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Eval<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Eval<R>)MonadicValue.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Eval<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Eval)MonadicValue.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Eval<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Eval<R>)MonadicValue.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <R> Eval<R> flatMapS(final Function<? super T, ? extends Stream<? extends R>> mapper) {
        return (Eval<R>)MonadicValue.super.flatMapS(mapper);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Eval<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Eval<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Eval<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Eval<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Eval<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Eval<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
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

        public static class Later<T> extends Rec<T> implements Eval<T> {

            Later(final Function<Object, ? extends T> s) {
                super(TreePVector.singleton(Rec.raw(Memoize.memoizeFunction(s))));
            }

            Later(final PVector<Function<Object, Object>> s) {
                super(s);

            }

            @Override
            public <R> Eval<R> map(final Function<? super T, ? extends R> mapper) {

                return new Later<R>(
                                    super.fns.plus(Rec.raw(Memoize.memoizeFunction(mapper))));
            }

            @Override
            public <R> Eval<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
                final RecFunction s = __ -> asEval(mapper.apply(super.applyRec())).steps();

                return new Later<R>(TreePVector.singleton(s));

            }

            @Override
            public T get() {
                return super.get();
            }

            /* (non-Javadoc)
             * @see com.aol.cyclops2.lambda.monads.Pure#unit(java.lang.Object)
             */
            @Override
            public <T> Eval<T> unit(final T unit) {
                return Eval.later(() -> unit);
            }

            /* (non-Javadoc)
             * @see com.aol.cyclops2.value.Value#toEvalLater()
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


        public static class Always<T> extends Rec<T> implements Eval<T> {

            Always(final Function<Object, ? extends T> s) {
                super(TreePVector.singleton(Rec.raw(s)));
            }

            Always(final PVector<Function<Object, Object>> s) {
                super(s);

            }


            public Maybe<T> filter(Predicate<? super T> predicate ){
                return Maybe.fromEval(this).filter(predicate);
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
                                     TreePVector.singleton(s));
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
             * @see com.aol.cyclops2.value.Value#toEvalAlways()
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

        public static class FutureAlways<T> implements Eval<T> {

           final Future<Eval<T>> input;


            FutureAlways( Future<Eval<T>> input) {

                this.input=  input;
            }


            public void forEach(Consumer<? super T> cons){
                input.peek(e->e.forEach(cons));
            }
            @Override
            public <R> Eval<R> map(final Function<? super T, ? extends R> mapper) {
                return new FutureAlways<R>(input.map(e->e.map(mapper)));

            }

            @Override
            public <R> Eval<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
                return new FutureAlways<R>(input.map(e->e.flatMap(mapper)));
            }



            @Override
            public ReactiveSeq<T> iterate(UnaryOperator<T> fn) {
                return Spouts.from(input).map(Eval::get).flatMap(i->Spouts.iterate(i,fn));
            }

            @Override
            public ReactiveSeq<T> generate() {
                return Spouts.from(input).map(Eval::get).flatMap(i->Spouts.generate(()->i));
            }

            /**
             * @return This convertable converted to a Future
             */
            @Override
           public Future<T> toFuture() {
                return input.map(Eval::get);
            }



            /**
             * This convertable converted to a Future asyncrhonously using the supplied Executor
             *
             * @param ex Executor to execute the conversion on
             * @return  This convertable converted to a Future asyncrhonously
             */
            @Override
            public Future<T> toFutureAsync(final Executor ex) {
                return toFuture();
            }

            /**
             * @return A CompletableFuture, populated immediately by a call to get
             */
            @Override
            public CompletableFuture<T> toCompletableFuture() {
                return toFuture().getFuture();
            }


            /**
             * @param exec Executor to asyncrhonously populate the CompletableFuture
             * @return  A CompletableFuture populated asynchronously on the supplied Executor by calling get
             */
            @Override
            public CompletableFuture<T> toCompletableFutureAsync(final Executor exec) {
                return toFuture().getFuture();
            }
            @Override
            public final void subscribe(final Subscriber<? super T> sub) {
                Mutable<Future<Eval<T>>> future = Mutable.of(input);
                sub.onSubscribe(new Subscription() {

                    AtomicBoolean running = new AtomicBoolean(
                            true);
                    AtomicBoolean cancelled = new AtomicBoolean(false);

                    @Override
                    public void request(final long n) {

                        if (n < 1) {
                            sub.onError(new IllegalArgumentException(
                                    "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                        }

                        if (!running.compareAndSet(true, false)) {

                            return;

                        }
                        future.mutate(f -> f.peek(e->{

                                e.forEach(v->{

                                    sub.onNext(v);
                                });
                        })
                                .recover(t -> {
                                    sub.onError(t);
                                    return null;
                                })
                                .peek(i -> sub.onComplete()));


                    }


                    @Override
                    public void cancel() {

                        cancelled.set(true);
                        future.get().cancel();

                    }

                });

            }
            @Override
            public T get() {

                Eval<T> eval = input.get();
                return eval.get();
            }

            @Override
            public <T> Eval<T> unit(final T unit) {
                return Eval.always(() -> unit);
            }

            /* (non-Javadoc)
             * @see com.aol.cyclops2.value.Value#toEvalAlways()
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
            final PVector<Function<Object, Object>> fns;
            private final static Object VOID = new Object();

            Rec(final PVector<Function<Object, Object>> s) {
                fns = s;
            }



            private static Function<Object, Object> raw(final Function<?, ?> fn) {
                return (Function<Object, Object>) fn;
            }

            static interface RecFunction extends Function<Object, Object> {

            }

            public  PVector<Function<Object, Object>> steps() {
                return fns;
            }
            public Trampoline<T> toTrampoline(){
                return new Trampoline<T>() {
                    @Override
                    public T get() {
                        return Rec.this.get();
                    }

                    @Override
                    public boolean complete(){
                        return false;
                    }

                    public Trampoline<T> bounce() {
                        Object input = init();
                        for (int i=0; i< fns.size();i++) {

                                final Function<Object, Object> next = fns.get(i);
                                if (next instanceof RecFunction) {
                                    PVector<Function<Object, Object>> remaining = fns.subList(i+1,fns.size());
                                    PVector<Function<Object, Object>> nextSteps = (PVector) ((RecFunction) next).apply(VOID);
                                    nextSteps.addAll(remaining);
                                   return new Later(nextSteps).toTrampoline();
                                } else {
                                    input = next.apply(input);


                                }


                        }
                        return Trampoline.done((T) input);

                    }
                };
            }


            public Object init(){
                return VOID;
            }
            T applyRec() {
                Object input = init();
                for (final Function<Object, Object> n : fns) {
                    final Deque<Function<Object, Object>> newFns = new ArrayDeque();
                    newFns.add(n);
                    while (newFns.size() > 0) {
                        final Function<Object, Object> next = newFns.pop();
                        if (next instanceof RecFunction) {
                            newFns.addAll((List) ((RecFunction) next).apply(VOID));
                        } else
                            input = next.apply(input);

                    }
                }
                return (T) input;
            }

            public T get() {
                return applyRec();
            }

        }

    }

    /**
     * Companion class for creating Type Class instances for working with Evals
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {

        public static InstanceDefinitions<eval> definitions(){
            return new InstanceDefinitions<eval>() {
                @Override
                public <T, R> Functor<eval> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<eval> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<eval> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<eval> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<eval>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<eval>> monadPlus() {
                    return Maybe.none();
                }

                @Override
                public <T> MonadRec<eval> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<eval>> monadPlus(Monoid<Higher<eval, T>> m) {
                    return Maybe.none();
                }

                @Override
                public <C2, T>Traverse<eval> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<eval> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<eval>> comonad() {
                    return Maybe.just(Instances.comonad());
                }

                @Override
                public <T> Maybe<Unfoldable<eval>> unfoldable() {
                    return Maybe.none();
                }
            };
        }


        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  Eval<Integer> list = Evals.functor().map(i->i*2, Eval.widen(Arrays.asEval(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Evals
         * <pre>
         * {@code
         *   Eval<Integer> list = Evals.unit()
        .unit("hello")
        .applyHKT(h->Evals.functor().map((String v) ->v.length(), h))
        .convert(Eval::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Evals
         */
        public static <T,R>Functor<eval> functor(){
            BiFunction<Eval<T>,Function<? super T, ? extends R>,Eval<R>> map = Instances::map;
            return General.functor(map);
        }

        /**
         * <pre>
         * {@code
         * Eval<String> list = Evals.unit()
        .unit("hello")
        .convert(Eval::narrowK3);

        //Arrays.asEval("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Evals
         */
        public static <T> Pure<eval> unit(){
            return General.<eval,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.Eval.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asEval;
         *
        Evals.zippingApplicative()
        .ap(widen(asEval(l1(this::multiplyByTwo))),widen(asEval(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * Eval<Function<Integer,Integer>> listFn =Evals.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(Eval::narrowK3);

        Eval<Integer> list = Evals.unit()
        .unit("hello")
        .applyHKT(h->Evals.functor().map((String v) ->v.length(), h))
        .applyHKT(h->Evals.applicative().ap(listFn, h))
        .convert(Eval::narrowK3);

        //Arrays.asEval("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Evals
         */
        public static <T,R> Applicative<eval> applicative(){
            BiFunction<Eval< Function<T, R>>,Eval<T>,Eval<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.Eval.widen;
         * Eval<Integer> list  = Evals.monad()
        .flatMap(i->widen(EvalX.range(0,i)), widen(Arrays.asEval(1,2,3)))
        .convert(Eval::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    Eval<Integer> list = Evals.unit()
        .unit("hello")
        .applyHKT(h->Evals.monad().flatMap((String v) ->Evals.unit().unit(v.length()), h))
        .convert(Eval::narrowK3);

        //Arrays.asEval("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Evals
         */
        public static <T,R> Monad<eval> monad(){

            BiFunction<Higher<eval,T>,Function<? super T, ? extends Higher<eval,R>>,Higher<eval,R>> flatMap = Instances::flatMap;
            return General.monad(applicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  Eval<String> list = Evals.unit()
        .unit("hello")
        .applyHKT(h->Evals.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(Eval::narrowK3);

        //Arrays.asEval("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<eval> monadZero(){

            return General.monadZero(monad(), Eval.now(null));
        }
        public static <T,R> MonadRec<eval> monadRec(){

            return new MonadRec<eval>(){

                @Override
                public <T, R> Higher<eval, R> tailRec(T initial, Function<? super T, ? extends Higher<eval, ? extends Xor<T, R>>> fn) {
                    return Eval.tailRec(initial,fn.andThen(Eval::narrowK));
                }
            };
        }



        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<eval> traverse(){

            return General.traverseByTraverse(applicative(), Instances::traverseA);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Evals.foldable()
        .foldLeft(0, (a,b)->a+b, Eval.widen(Arrays.asEval(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T,R> Foldable<eval> foldable(){
            BiFunction<Monoid<T>,Higher<eval,T>,T> foldRightFn =  (m,l)-> Eval.narrowK(l).orElse(m.zero());
            BiFunction<Monoid<T>,Higher<eval,T>,T> foldLeftFn = (m,l)-> Eval.narrowK(l).orElse(m.zero());
            Fn3<Monoid<R>, Function<T, R>, Higher<Witness.eval, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);
            return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
        }

        public static <T> Comonad<eval> comonad(){
            Function<? super Higher<eval, T>, ? extends T> extractFn = maybe -> maybe.convert(Eval::narrowK).get();
            return General.comonad(functor(), unit(), extractFn);
        }
        private <T> Eval<T> of(T value){
            return Eval.now(value);
        }
        private static <T,R> Eval<R> ap(Eval<Function< T, R>> lt,  Eval<T> maybe){
            return lt.combine(maybe, (a,b)->a.apply(b));

        }
        private static <T,R> Higher<eval,R> flatMap( Higher<eval,T> lt, Function<? super T, ? extends  Higher<eval,R>> fn){
            return Eval.narrowK(lt).flatMap(fn.andThen(Eval::narrowK));
        }
        private static <T,R> Eval<R> map(Eval<T> lt, Function<? super T, ? extends R> fn){
            return  lt.map(fn);
        }


        private static <C2,T,R> Higher<C2, Higher<eval, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                        Higher<eval, T> ds){
            Eval<T> eval = Eval.narrowK(ds);
            return applicative.map(Eval::now, fn.apply(eval.get()));
        }

    }

}
