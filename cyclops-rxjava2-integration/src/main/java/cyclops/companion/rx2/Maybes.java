package cyclops.companion.rx2;


import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.react.Status;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.Value;
import cyclops.companion.Futures;
import cyclops.control.Either;
import cyclops.control.Eval;
import cyclops.control.Future;
import cyclops.control.LazyEither;
import cyclops.data.Seq;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.Rx2Witness;
import cyclops.monads.Rx2Witness.maybe;
import cyclops.monads.WitnessType;
import cyclops.monads.XorM;
import cyclops.monads.transformers.rx2.MaybeT;
import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Companion class for working with RxJava 2 Maybe types
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Maybes {


    public static  <W1 extends WitnessType<W1>,T> XorM<W1,maybe,T> xorM(Maybe<T> type){
        return XorM.right(anyM(type));
    }


    public static <T, R> Maybe< R> tailRec(T initial, Function<? super T, ? extends Maybe<? extends Either<T, R>>> fn) {
        Maybe<? extends Either<T, R>> next[] = new Maybe[1];
        next[0] = Maybe.just(Either.left(initial));
        boolean cont = true;
        do {
            cont = next[0].map(p -> p.visit(s -> {
                next[0] = fn.apply(s);
                return true;
            }, pr -> false)).blockingGet(false);
        } while (cont);
        return next[0].map(e->e.orElse(null));
    }

    public static <T> Maybe<T> fromPublisher(Publisher<T> maybe){
        return Single.fromPublisher(maybe).toMaybe();
    }

    public static <T> Maybe<T> raw(AnyM<maybe,T> anyM){
        return Rx2Witness.maybe(anyM);
    }


    public static <W extends WitnessType<W>,T> MaybeT<W,T> liftM(AnyM<W,Maybe<T>> nested){
        return MaybeT.of(nested);
    }

    public static <T> Future[] futures(Maybe<T>... futures){

        Future[] array = new Future[futures.length];
        for(int i=0;i<array.length;i++){
            array[i]=future(futures[i]);
        }
        return array;
    }
    public static <T> cyclops.control.Maybe<T> toMaybe(Maybe<T> future){
        return cyclops.control.Maybe.fromPublisher(future.toFlowable());
    }
    public static <T> Maybe<T> fromMaybe(cyclops.control.Maybe<T> future){
        return Single.fromPublisher(future).toMaybe();

    }
    public static <T> Maybe<T> fromValue(MonadicValue<T> future){
        return Single.fromPublisher(future).toMaybe();

    }
    public static <T> Future<T> future(Maybe<T> future){
        return Future.fromPublisher(future.toFlowable());
    }

    public static <R> LazyEither<Throwable,R> either(Maybe<R> either){
        return LazyEither.fromFuture(future(either));

    }

    public static <T> cyclops.control.Maybe<T> maybe(Maybe<T> opt){
        return cyclops.control.Maybe.fromFuture(future(opt));
    }
    public static <T> Eval<T> eval(Maybe<T> opt){
        return Eval.fromFuture(future(opt));
    }

    /**
     * Construct an AnyM type from a Maybe. This allows the Maybe to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     *
     * <pre>
     * {@code
     *
     *    AnyMSeq<Integer> maybe = Fluxs.anyM(Maybe.just(1,2,3));
     *    AnyMSeq<Integer> transformedMaybe = myGenericOperation(maybe);
     *
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     *
     * @param maybe To wrap inside an AnyM
     * @return AnyMSeq wrapping a Maybe
     */
    public static <T> AnyMValue<maybe,T> anyM(Maybe<T> maybe) {
        return AnyM.ofValue(maybe, Rx2Witness.maybe.INSTANCE);
    }



    /**
     * Select the first Maybe to complete
     *
     * @see CompletableFuture#anyOf(CompletableFuture...)
     * @param fts Maybes to race
     * @return First Maybe to complete
     */
    public static <T> Maybe<T> anyOf(Maybe<T>... fts) {


        return Single.fromPublisher(Future.anyOf(futures(fts))).toMaybe();

    }
    /**
     * Wait until all the provided Future's to complete
     *
     * @see CompletableFuture#allOf(CompletableFuture...)
     *
     * @param fts Maybes to  wait on
     * @return Maybe that completes when all the provided Futures Complete. Empty Future result, or holds an Exception
     *         from a provided Future that failed.
     */
    public static <T> Maybe<T> allOf(Maybe<T>... fts) {

        return Single.fromPublisher(Future.allOf(futures(fts))).toMaybe();
    }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     *
     * <pre>
     * {@code
     *
     * Maybe<ListX<Integer>> strings = Maybes.quorum(status -> status.getCompleted() >0, Maybe.deferred(()->1),Maybe.empty(),Maybe.empty());


    strings.get().size()
    //1
     *
     * }
     * </pre>
     *
     *
     * @param breakout Predicate that determines whether the block should be
     *            continued or removed
     * @param fts FutureWs to  wait on results from
     * @param errorHandler Consumer to handle any exceptions thrown
     * @return Future which will be populated with a Quorum of results
     */
    @SafeVarargs
    public static <T> Maybe<Seq<T>> quorum(Predicate<Status<T>> breakout, Consumer<Throwable> errorHandler, Maybe<T>... fts) {

        return Single.fromPublisher(Futures.quorum(breakout,errorHandler,futures(fts))).toMaybe();


    }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     *
     * <pre>
     * {@code
     *
     * Maybe<ListX<Integer>> strings = Maybes.quorum(status -> status.getCompleted() >0, Maybe.deferred(()->1),Maybe.empty(),Maybe.empty());


    strings.get().size()
    //1
     *
     * }
     * </pre>
     *
     *
     * @param breakout Predicate that determines whether the block should be
     *            continued or removed
     * @param fts Maybes to  wait on results from
     * @return Maybe which will be populated with a Quorum of results
     */
    @SafeVarargs
    public static <T> Maybe<Seq<T>> quorum(Predicate<Status<T>> breakout, Maybe<T>... fts) {

        return Single.fromPublisher(Futures.quorum(breakout,futures(fts))).toMaybe();


    }
    /**
     * Select the first Future to return with a successful result
     *
     * <pre>
     * {@code
     * Maybe<Integer> ft = Maybe.empty();
      Maybe<Integer> result = Maybes.firstSuccess(Maybe.deferred(()->1),ft);

    ft.complete(10);
    result.get() //1
     * }
     * </pre>
     *
     * @param fts Maybes to race
     * @return First Maybe to return with a result
     */
    @SafeVarargs
    public static <T> Maybe<T> firstSuccess(Maybe<T>... fts) {
        return Single.fromPublisher(Future.firstSuccess(futures(fts))).toMaybe();

    }

    /**
     * Perform a For Comprehension over a Maybe, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Maybes.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Maybes.forEach4;
     *
          forEach4(Maybe.just(1),
                  a-> Maybe.just(a+1),
                  (a,b) -> Maybe.<Integer>just(a+b),
                  (a,b,c) -> Maybe.<Integer>just(a+b+c),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Maybe
     * @param value2 Nested Maybe
     * @param value3 Nested Maybe
     * @param value4 Nested Maybe
     * @param yieldingFunction Generates a result per combination
     * @return Maybe with a combined value generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Maybe<R> forEach4(Maybe<? extends T1> value1,
            Function<? super T1, ? extends Maybe<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Maybe<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends Maybe<R3>> value4,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        Maybe<? extends R> res = value1.flatMap(in -> {

            Maybe<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Maybe<R2> b = value3.apply(in, ina);
                return b.flatMap(inb -> {
                    Maybe<R3> c = value4.apply(in, ina, inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
        return  narrow(res);
    }


    /**
     * Perform a For Comprehension over a Maybe, accepting 2 generating functions.
     * This results in a three level nested internal iteration over the provided Maybes.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Maybes.forEach3;
     *
          forEach3(Maybe.just(1),
                  a-> Maybe.just(a+1),
                  (a,b) -> Maybe.<Integer>just(a+b),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Maybe
     * @param value2 Nested Maybe
     * @param value3 Nested Maybe
     * @param yieldingFunction Generates a result per combination
     * @return Maybe with a combined value generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Maybe<R> forEach3(Maybe<? extends T1> value1,
            Function<? super T1, ? extends Maybe<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Maybe<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {


        Maybe<? extends R> res = value1.flatMap(in -> {

            Maybe<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Maybe<R2> b = value3.apply(in, ina);


                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));


            });

        });
        return narrow(res);

    }



    /**
     * Perform a For Comprehension over a Maybe, accepting a generating function.
     * This results in a two level nested internal iteration over the provided Maybes.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Maybes.forEach;
     *
          forEach(Maybe.just(1),
                  a-> Maybe.just(a+1),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Maybe
     * @param value2 Nested Maybe
     * @param yieldingFunction Generates a result per combination
     * @return Maybe with a combined value generated by the yielding function
     */
    public static <T, R1, R> Maybe<R> forEach(Maybe<? extends T> value1,
                                             Function<? super T, Maybe<R1>> value2,
                                             BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        Maybe<R> res = value1.flatMap(in -> {

            Maybe<R1> a = value2.apply(in);
            return a.map(ina -> yieldingFunction.apply(in, ina));


        });


        return narrow(res);

    }



    /**
     * Lazily combine this Maybe with the supplied value via the supplied BiFunction
     *
     * @param maybe Maybe to combine with another value
     * @param app Value to combine with supplied maybe
     * @param fn Combiner function
     * @return Combined Maybe
     */
    public static <T1, T2, R> Maybe<R> combine(Maybe<? extends T1> maybe, Value<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Single.fromPublisher(Future.fromPublisher(maybe.toFlowable())
                                .zip(app, fn)).toMaybe());
    }

    /**
     * Lazily combine this Maybe with the supplied Maybe via the supplied BiFunction
     *
     * @param maybe Maybe to combine with another value
     * @param app Maybe to combine with supplied maybe
     * @param fn Combiner function
     * @return Combined Maybe
     */
    public static <T1, T2, R> Maybe<R> combine(Maybe<? extends T1> maybe, Maybe<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Single.fromPublisher(Future.fromPublisher(maybe.toFlowable())
                                .zip(Future.fromPublisher(app.toFlowable()), fn)).toMaybe());
    }

    /**
     * Combine the provided Maybe with the first element (if present) in the provided Iterable using the provided BiFunction
     *
     * @param maybe Maybe to combine with an Iterable
     * @param app Iterable to combine with a Maybe
     * @param fn Combining function
     * @return Combined Maybe
     */
    public static <T1, T2, R> Maybe<R> zip(Maybe<? extends T1> maybe, Iterable<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(Single.fromPublisher(Future.fromPublisher(maybe.toFlowable())
                                .zip(app, fn)).toMaybe());
    }

    /**
     * Combine the provided Maybe with the first element (if present) in the provided Publisher using the provided BiFunction
     *
     * @param maybe  Maybe to combine with a Publisher
     * @param fn Publisher to combine with a Maybe
     * @param app Combining function
     * @return Combined Maybe
     */
    public static <T1, T2, R> Maybe<R> zip(Maybe<? extends T1> maybe, BiFunction<? super T1, ? super T2, ? extends R> fn,
            Publisher<? extends T2> app) {
        Maybe<R> res = narrow(Single.fromPublisher(Future.fromPublisher(maybe.toFlowable()).zip(fn,app)).toMaybe());
        return res;
    }


    /**
     * Construct a Maybe from Iterable by taking the first value from Iterable
     *
     * @param t Iterable to populate Maybe from
     * @return Maybe containing first element from Iterable (or empty Maybe)
     */
    public static <T> Maybe<T> fromIterable(Iterable<T> t) {
        return narrow(Single.fromPublisher(Future.fromIterable(t)).toMaybe());
    }

    /**
     * Get an Iterator for the value (if any) in the provided Maybe
     *
     * @param pub Maybe to get Iterator for
     * @return Iterator over Maybe value
     */
    public static <T> Iterator<T> iterator(Maybe<T> pub) {
        return pub.toFlowable().blockingIterable().iterator();

    }

    public static <R> Maybe<R> narrow(Maybe<? extends R> apply) {
        return (Maybe<R>)apply;
    }




}
