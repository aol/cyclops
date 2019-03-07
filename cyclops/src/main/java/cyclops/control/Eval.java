package cyclops.control;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.matching.Deconstruct.Deconstruct1;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.reactive.Completable;
import com.oath.cyclops.util.ExceptionSoftener;
import com.oath.cyclops.util.box.Mutable;
import cyclops.function.*;
import com.oath.cyclops.hkt.DataWitness.eval;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.*;
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
 * Supports tail recursion via transform / flatMap.
 * Eval are always Lazy even when performed against a Now instance.
 * Heavily inspired by Cats Eval @link https://github.com/typelevel/cats/blob/master/core/src/main/scala/cats/Eval.scala
 *
 * Tail Recursion example
 * <pre>
 * {@code
 *
 * public void odd(){
        System.out.println(even(Eval.now(200000)).getValue());
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
public interface Eval<T> extends To<Eval<T>>,Function0<T>,
                                  Deconstruct1<T>,
                                  MonadicValue<T>,
                                  Higher<eval ,T>{


    default Tuple1<T> unapply(){
        return Tuple.tuple(get());
    }


    public static <T> Eval<T> eval(Supplier<T> s){
        if(s instanceof Eval)
            return (Eval<T>)s;
        return later(s);
    }

    public static  <T,R> Eval<R> tailRec(T initial, Function<? super T, ? extends Eval<? extends Either<T, R>>> fn){
        return narrowK(fn.apply(initial)).flatMap( eval ->
                eval.fold(s->tailRec(s,fn), p-> Eval.now(p)));
    }
    public static <T> Higher<eval, T> widen(Eval<T> narrow) {
    return narrow;
  }


    static <T> Eval<T> async(final Executor ex, final Supplier<T> s){
        return fromFuture(Future.of(s,ex));
    }


    default  <B,R> Eval<R> zip(Supplier<B> b, BiFunction<? super T,? super B,? extends R> zipper){
        Trampoline<B>tb = b instanceof Trampoline ?  (Trampoline<B>) b : eval(b).toTrampoline();
        return Eval.later(toTrampoline().zip(tb,zipper));
    }
    default  <B,C,R> Eval<R> zip(Supplier<B> b,Supplier<C> c, Function3<? super T,? super B,? super C, ? extends R> zipper){
        Trampoline<B> tb = b instanceof Trampoline ?  (Trampoline<B>) b : eval(b).toTrampoline();
        Trampoline<C> tc = c instanceof Trampoline ?  (Trampoline<C>) c : eval(c).toTrampoline();
        return Eval.later(toTrampoline().zip(tb,tc,zipper));
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
        if(pub instanceof Eval)
            return (Eval<T>)pub;
        CompletableEval<T, T> result = eval();

        pub.subscribe(new Subscriber<T>() {
            Subscription sub;
            @Override
            public void onSubscribe(Subscription s) {
                sub =s;
                s.request(1l);
            }

            @Override
            public void onNext(T t) {
                result.complete(t);

            }

            @Override
            public void onError(Throwable t) {
                result.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                if(!result.isDone())  {
                    result.completeExceptionally(new NoSuchElementException());
                }
            }
        });
        return result;

    }

    /**
     * Create a reactive CompletableEval
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
     * @return A reactive CompletableEval
     */
    static <T> CompletableEval<T,T> eval(){
        CompletableFuture<T> c = new CompletableFuture<T>();
        return new CompletableEval<T, T>(c,fromFuture(Future.of(c)));

    }

    default ReactiveSeq<T> streamWhile(Predicate<? super T> p){
        return ReactiveSeq.generate(this).takeWhile(p);
    }
    default ReactiveSeq<T> streamUntil(Predicate<? super T> p){
        return ReactiveSeq.generate(this).takeUntil(p);
    }
    default ReactiveSeq<T> streamUntil(long time,TimeUnit unit){
        return ReactiveSeq.generate(this).take(time,unit);
    }
    @Override
    default ReactiveSeq<T> stream() {
        return Function0.super.stream();
    }

    @AllArgsConstructor
    static class CompletableEval<ORG,T2> implements Eval<T2>, Completable<ORG>{
        public final CompletableFuture<ORG> complete;
        public final Eval<T2> lazy;

        @Override
        public void forEach(Consumer<? super T2> onNext) {
            toFuture().forEach(onNext);
        }

        @Override
        public boolean isFailed() {
            return complete.isCompletedExceptionally();
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
        @Override
        public void subscribe(Subscriber<? super T2> sub) {
            lazy.subscribe(sub);
        }
        public ReactiveSeq<T2> streamWhile(Predicate<? super T2> p){
            return Spouts.generate(this).takeWhile(p);
        }
        public ReactiveSeq<T2> streamUntil(Predicate<? super T2> p){
            return Spouts.generate(this).takeUntil(p);
        }
        public ReactiveSeq<T2> streamUntil(long time,TimeUnit unit){
            return Spouts.generate(this).take(time,unit);
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
        if(iterable instanceof Eval)
            return (Eval<T>)iterable;
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
     * Lazily create an Eval from the specified Supplier. Supplier#getValue will only be called once. Return values of Eval operations will also
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
        if(value instanceof Module.Later){
            return (Eval<T>)value;
        }
        return new Module.Later<T>(
            () -> value == null ? null : value.get());
    }
    public static <T> Eval<T> defer(final Supplier<Eval<T>> value) {
        return new Module.Later<T>(
            () -> value == null || value.get() == null ? null : value.get().get());
    }

    /**
     * Lazily create an Eval from the specified Supplier. Supplier#getValue will only be every time getValue is called on the resulting Eval.
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
            () -> value == null ? null : value.get());
    }

    /**
     * Turn an Iterable of Evals into a single Eval with a ReactiveSeq of values.
     *
     * <pre>
     * {@code
     *  Eval<Seq<Integer>> maybes =Eval.sequence(Seq.of(Eval.now(10),Eval.now(1)));
        //Eval.now(Seq.of(10,1)));
     *
     * }
     * </pre>
     *
     * @param evals Collection of evals to convert into a single eval with a List of values
     * @return  Eval with a  list of values
     */
    public static <T> Eval<ReactiveSeq<T>> sequence(final Iterable<? extends Eval<T>> evals) {
        return sequence(ReactiveSeq.fromIterable(evals));

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
        return sequence(ReactiveSeq.fromStream(evals));
    }
  public static  <T> Eval<ReactiveSeq<T>> sequence(ReactiveSeq<? extends Eval<T>> stream) {

    Eval<ReactiveSeq<T>> identity = Eval.now(ReactiveSeq.empty());

    BiFunction<Eval<ReactiveSeq<T>>,Eval<T>,Eval<ReactiveSeq<T>>> combineToStream = (acc, next) ->acc.zipWith(next,(a, b)->a.append(b));

    BinaryOperator<Eval<ReactiveSeq<T>>> combineStreams = (a, b)-> a.zipWith(b,(z1, z2)->z1.appendStream(z2));

    return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <T,R> Eval<ReactiveSeq<R>> traverse(Function<? super T, ? extends R> fn, ReactiveSeq<Eval<T>> stream) {
    ReactiveSeq<Eval<R>> s = stream.map(h -> h.map(fn));
    return sequence(s);
  }
    /**
     * Sequence and reduce a CollectionX of Evals into an Eval with a reduced value
     *
     * <pre>

     * {@code
     *   Eval<PersistentSetX<Integer>> accumulated = Eval.accumulate(Seq.of(just,Eval.now(1)),Reducers.toPersistentSetX());
         //Eval.now(PersistentSetX.of(10,1)))
     * }
     * </pre>
     *
     * @param evals Collection of Evals to accumulate
     * @param reducer Reducer to fold nest values into
     * @return Eval with a value
     */
    public static <T, R> Eval<R> accumulate(final Iterable<Eval<T>> evals, final Reducer<R, T> reducer) {
        return sequence(evals).map(s -> s.foldMap(reducer));
    }

    /**
     * Sequence and reduce an Iterable of Evals into an Eval with a reduced value
     *
     * <pre>
     * {@code
     *   Eval<String> evals =Eval.accumulate(Seq.of(just,Eval.later(()->1)),i->""+i,Monoids.stringConcat);
         //Eval.now("101")
     * }
     * </pre>
     *
     *
     * @param evals Collection of Evals to accumulate
     * @param mapper Funtion to transform Eval contents to type required by Semigroup accumulator
     * @param reducer Combiner function to applyHKT to converted values
     * @return  Eval with a value
     */
    public static <T, R> Eval<R> accumulate(final Iterable<Eval<T>> evals, final Function<? super T, R> mapper, final Monoid<R> reducer) {
        return sequence(evals).map(s -> s.map(mapper)
                                          .reduce(reducer)
                                          );
    }


    public static <T> Eval<T> accumulate(final Monoid<T> reducer, final Iterable<Eval<T>> evals) {
        return sequence(evals).map(s -> s.reduce(reducer));
    }

    default Trampoline<T> toTrampoline(){
        return Trampoline.more(()->Trampoline.done(get()));
    }

    @Override
    default Maybe<T> toMaybe(){
        return Maybe.fromEvalNullable(this);
    }


    @Override
    public <T> Eval<T> unit(T unit);


    @Override
    default <R> Eval<R> map(Function<? super T, ? extends R> mapper){
        return flatMap(i->Eval.now(mapper.apply(i)));
    }


    @Override
    <R> Eval<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);





    @Deprecated
    default Eval<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return unit(this.forEach2( t1 -> v2, (t1, t2) -> monoid
                                                            .apply(t1, t2)).orElseGet(() -> orElseGet(() -> monoid.zero())));
    }



    @Override
    default <R> Eval<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Eval<R>)MonadicValue.super.concatMap(mapper);
    }


    @Override
    default <R> Eval<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            return Eval.fromPublisher(publisher);
        });
    }





    @Override
    public T get();


    @Override
    default <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }


    @Override
    default Maybe<T> notNull() {

        return (Maybe<T>) MonadicValue.super.notNull();
    }


    @Override
    default Maybe<T> filter(final Predicate<? super T> pred) {
        return toMaybe().filter(pred);
    }



    @Override
    default Eval<T> peek(final Consumer<? super T> c) {
        return (Eval<T>) MonadicValue.super.peek(c);
    }


    default Eval<T> restartUntil(Predicate<? super T> p){
        return flatMap(t->p.test(t) ? now(t) : restartUntil(p));
    }

    default Eval<T> onErrorRestart(long retries){
        return recoverWith(Throwable.class, t->{
            if (retries>0)
                return onErrorRestart(retries-1);
            throw ExceptionSoftener.throwSoftenedException(t);
        });
    }
    default <C extends Throwable> Eval<T> recoverWith(Class<C> type, Function<? super C,? extends Eval<T>> value){
        return Eval.<Eval<T>>always(() -> {
            try {
                T res = this.get();
                return Eval.now(res);
            } catch (final Throwable t) {
                if (type.isAssignableFrom(t.getClass())) {
                    return value.apply((C) t);
                }
                throw ExceptionSoftener.throwSoftenedException(t);

            }
        }).flatMap(i->i);
    }
    default void forEach(Consumer<? super T> onNext, Consumer<Throwable> onError,Runnable r){
        toFuture().forEach(onNext,onError,r);
    }
    default void forEach(Consumer<? super T> onNext, Consumer<Throwable> onError){
        toFuture().forEach(onNext,onError);
    }
    default Eval<T> recover(Function<Throwable,? extends T> value){
        return recover(Throwable.class,value);

    }
    default <C extends Throwable> Eval<T> recover(Class<C> type, Function<? super C,? extends T> value){

        return Eval.always(()->{
            try {
                return get();
            } catch (final Throwable t) {
                if (type.isAssignableFrom(t.getClass())) {
                    return value.apply((C)t);
                }
                throw ExceptionSoftener.throwSoftenedException(t);

            }
        });
    }

    @Override
    default <R> R fold(final Function<? super T, ? extends R> present, final Supplier<? extends R> absent) {
        final T value = get();
        if (value != null)
            return present.apply(value);
        return absent.get();
    }


    static <R> Eval<R> narrow(final Eval<? extends R> broad) {
        return (Eval<R>) broad;
    }



    default <T2, R> Eval<R> zipWith(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return  fromIterable(ReactiveSeq.fromIterable(this).zip(app,fn));
    }



    default <T2, R> Eval<R> zipWith(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
        return mergeMap(a->Eval.fromPublisher(app).map(b->fn.apply(a,b)));
    }








    @Override
    default <T2, R1, R2, R3, R> Eval<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Eval<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }


    @Override
    default <T2, R1, R2, R3, R> Eval<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }


    @Override
    default <T2, R1, R2, R> Eval<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                             BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                             Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }


    @Override
    default <T2, R1, R2, R> Eval<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                             BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                             Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                             Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }


    @Override
    default <R1, R> Eval<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Eval<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }


    @Override
    default <R1, R> Eval<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                     BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Eval<R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }

    default <R> Eval<R> emptyUnit(){
        return Eval.now(null);
    }


    default Future<T> toFuture(){
        return Future.fromPublisher(this);
    }

    static class Module {

        static <T> Eval<T> asEval(final MonadicValue<T> value) {

            if (value instanceof Eval)
                return (Eval<T>) value;
            return Eval.now(value.orElse(null));
        }

        public static class Later<T> implements Eval<T> {


            private final Supplier<T> memo;
            private final Trampoline<T> evaluate;
            Later(Rec<?, T> in) {
                memo = Memoize.memoizeSupplier(()->in.toTrampoline().get());
                evaluate = in.toTrampoline();
            }

            Later(Supplier<T> s){
                memo = Memoize.memoizeSupplier(s);
                evaluate = Trampoline.more(()->Trampoline.done(memo.get()));
            }

            @Override
            public <R> Eval<R> map(Function<? super T, ? extends R> mapper) {
                return flatMap(i->Eval.later(()-> mapper.apply(i)));
            }

            @Override
            public <R> Eval<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
                return new Later<R>( new Rec<T, R>(this, Memoize.memoizeFunction(mapper)));
            }
            @Override
            public Trampoline<T> toTrampoline(){
                return evaluate;
            }


            @Override
            public T get() {
                return memo.get();
            }


            @Override
            public <T> Eval<T> unit(final T unit) {
                return Eval.later(() -> unit);
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


        public static class Always<T>  implements Eval<T> {

            private final Trampoline<T> evaluate;

            Always(Rec<?, T> in) {
                evaluate =  in.toTrampoline();
            }
            Always(Supplier<T> in) {
                evaluate = Trampoline.more(()->Trampoline.done(in.get()));
            }

            public Maybe<T> filter(Predicate<? super T> predicate ){
                return Maybe.fromEval(this).filter(predicate);
            }


            @Override
            public <R> Eval<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
                Rec<T, R> rec = new Rec<T, R>(this, mapper);
                return new Always<R>(
                                     rec);
            }

            @Override
            public T get() {
                return evaluate.get();
            }
            @Override
            public Trampoline<T> toTrampoline(){
                return evaluate;
            }


            @Override
            public <T> Eval<T> unit(final T unit) {
                return Eval.always(() -> unit);
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


            public void forEach(Consumer<? super T> next){
                input.forEach(e->e.forEach(next));
            }
            public Eval<T> recover(Function<Throwable,? extends T> value){
                CompletableEval<T,T> res = eval();
                toFuture().forEach(
                    t->res.complete(t),
                    e->res.complete(value.apply(e)));
                return res;

            }

            @Override
            public Eval<T> restartUntil(Predicate<? super T> p) {
                CompletableEval<T,T> res = eval();

                toFuture().forEach(
                    t->{

                        if(p.test(t)){
                            res.complete(t);

                        }
                        else{
                            res.completeExceptionally(new NoSuchElementException());
                        }

                    },
                    e-> res.completeExceptionally(e));
                return res;
            }

            public Eval<T> onErrorRestart(long retries){
                CompletableEval<T,T> res = eval();
                long[] attempts = {retries};
                toFuture().forEach(
                    t->res.complete(t),
                    e->{
                        while(attempts[0]>0){
                            Either<Throwable,T> either =toLazyEither();
                            if(either.isRight()) {
                                res.complete(either.orElse(null));
                                break;
                            }
                            attempts[0]--;
                        }
                        res.completeExceptionally(e);


                    });
                return res;

            }

            public <C extends Throwable> Eval<T> recover(Class<C> type, Function<? super C,? extends T> value){
                CompletableEval<T,T> res = eval();
                toFuture().forEach(
                    t->res.complete(t),
                    e->{
                        if (type.isAssignableFrom(e.getClass())) {
                            res.complete(value.apply((C)e));
                        }
                    });
                return res;

            }
            public <C extends Throwable> Eval<T> recoverWith(Class<C> type, Function<? super C,? extends Eval<T>> value){
                CompletableEval<Eval<T>,Eval<T>> res = eval();
                toFuture().forEach(
                    t->res.complete(Eval.now(t)),
                    e->{
                        if (type.isAssignableFrom(e.getClass())) {
                            res.complete(value.apply((C)e));
                        }
                    });
                return res.flatMap(i->i);
            }

            public ReactiveSeq<T> streamWhile(Predicate<? super T> p){
                return Spouts.generate(this).takeWhile(p);
            }
            public ReactiveSeq<T> streamUntil(Predicate<? super T> p){
                return Spouts.generate(this).takeUntil(p);
            }
            public ReactiveSeq<T> streamUntil(long time,TimeUnit unit){
                return Spouts.generate(this).take(time,unit);
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


            @Override
           public Future<T> toFuture() {
                return input.map(Eval::get);
            }




            @Override
            public Future<T> future(final Executor ex) {
                return toFuture();
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

                Eval<T> eval = input.fold(i->i,e->{throw ExceptionSoftener.throwSoftenedException(e.getCause());});
                return eval.get();
            }


            @Override
            public ReactiveSeq<T> stream() {
                return Spouts.from(this);
            }

            @Override
            public <T> Eval<T> unit(final T unit) {
                return Eval.always(() -> unit);
            }




            @Override
            public int hashCode() {
                return get().hashCode();
            }


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

        @AllArgsConstructor
        private static class Rec<T,R> {
            private final Eval<T> eval;
            private final Function<? super T,? extends  MonadicValue<? extends R>> fn;


            public Trampoline<R> toTrampoline() {
                Trampoline<? extends R> x = Trampoline.more(() -> {
                    Trampoline<? extends R> t = eval.toTrampoline().flatMap(v -> {
                        Trampoline<? extends R> t2 = Eval.fromIterable(fn.apply(v)).toTrampoline();
                        return t2;
                    });
                    return t;
                });
                return Trampoline.narrow(x);
            }

        }

    }

    @Deprecated
  public static class Comprehensions {

    public static <T,F,R1, R2, R3,R4,R5,R6,R7> Eval<R7> forEach(Eval<T> eval,
                                                                Function<? super T, ? extends Eval<R1>> value2,
                                                                Function<? super Tuple2<? super T,? super R1>, ? extends Eval<R2>> value3,
                                                                Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Eval<R3>> value4,
                                                                Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Eval<R4>> value5,
                                                                Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Eval<R5>> value6,
                                                                Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Eval<R6>> value7,
                                                                Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, ? extends Eval<R7>> value8
    ) {

      return eval.flatMap(in -> {

        Eval<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Eval<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Eval<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Eval<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Eval<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  Eval<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f.flatMap(inf->{
                    Eval<R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                    return g;

                  });

                });
              });

            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4,R5,R6> Eval<R6> forEach(Eval<T> eval,
                                                             Function<? super T, ? extends Eval<R1>> value2,
                                                             Function<? super Tuple2<? super T,? super R1>, ? extends Eval<R2>> value3,
                                                             Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Eval<R3>> value4,
                                                             Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Eval<R4>> value5,
                                                             Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Eval<R5>> value6,
                                                             Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Eval<R6>> value7
    ) {

      return eval.flatMap(in -> {

        Eval<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Eval<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Eval<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Eval<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Eval<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  Eval<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f;
                });
              });

            });

          });


        });


      });

    }

    public static <T,F,R1, R2, R3,R4,R5> Eval<R5> forEach(Eval<T> eval,
                                                          Function<? super T, ? extends Eval<R1>> value2,
                                                          Function<? super Tuple2<? super T,? super R1>, ? extends Eval<R2>> value3,
                                                          Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Eval<R3>> value4,
                                                          Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Eval<R4>> value5,
                                                          Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Eval<R5>> value6
    ) {

      return eval.flatMap(in -> {

        Eval<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Eval<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Eval<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Eval<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Eval<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e;
              });
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4> Eval<R4> forEach(Eval<T> eval,
                                                       Function<? super T, ? extends Eval<R1>> value2,
                                                       Function<? super Tuple2<? super T,? super R1>, ? extends Eval<R2>> value3,
                                                       Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Eval<R3>> value4,
                                                       Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Eval<R4>> value5

    ) {

      return eval.flatMap(in -> {

        Eval<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Eval<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Eval<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Eval<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d;
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3> Eval<R3> forEach(Eval<T> eval,
                                                    Function<? super T, ? extends Eval<R1>> value2,
                                                    Function<? super Tuple2<? super T,? super R1>, ? extends Eval<R2>> value3,
                                                    Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Eval<R3>> value4

    ) {

      return eval.flatMap(in -> {

        Eval<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Eval<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Eval<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c;

          });


        });


      });

    }
    public static <T,F,R1, R2> Eval<R2> forEach(Eval<T> eval,
                                                Function<? super T, ? extends Eval<R1>> value2,
                                                Function<? super Tuple2<? super T,? super R1>, ? extends Eval<R2>> value3

    ) {

      return eval.flatMap(in -> {

        Eval<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Eval<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b;


        });


      });

    }
    public static <T,F,R1> Eval<R1> forEach(Eval<T> eval,
                                            Function<? super T, ? extends Eval<R1>> value2


    ) {

      return eval.flatMap(in -> {

        Eval<R1> a = value2.apply(in);
        return a;


      });

    }


  }


}
