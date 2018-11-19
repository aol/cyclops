package cyclops.companion.rx2;


import com.oath.cyclops.react.Status;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.Value;
import cyclops.companion.Futures;
import cyclops.control.Either;
import cyclops.control.Eval;
import cyclops.control.Future;
import cyclops.control.LazyEither;
import cyclops.control.Maybe;
import cyclops.data.Seq;
import cyclops.function.Function3;
import cyclops.function.Function4;
import io.reactivex.Flowable;
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
 * Companion class for working with Reactor Single types
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Singles {

    public static <T> Single<Flowable<T>> sequence(final Publisher<? extends Single<T>> fts) {

        Single<Single<Flowable<T>>> res = Flowable.fromPublisher(fts).<Single<Flowable<T>>>reduce(Single.just(Flowable.empty()), (acc, next) -> acc.zipWith(next, (a, b) -> Flowable.concat(a, Flowable.just(b))));
        return res.flatMap(i->i);
    }

    public static <T,R> Single<Flowable<R>> traverse(Function<? super T,? extends R> fn,Publisher<Single<T>> stream) {
        Flowable<Single<R>> s = Flowable.fromPublisher(stream).map(h -> h.map(i->fn.apply(i)));
        return sequence(s);
    }
    public static <T> Single<T> fromValue(MonadicValue<T> future){
        return Single.fromPublisher(future);
    }


    public static <T, R> Single< R> tailRec(T initial, Function<? super T, ? extends Single<? extends Either<T, R>>> fn) {
        Single<? extends Either<T, R>> next[] = new Single[1];
        next[0] = Single.just(Either.left(initial));
        boolean cont = true;
        do {
            cont = next[0].map(p -> p.fold(s -> {
                next[0] = fn.apply(s);
                return true;
            }, pr -> false)).blockingGet();
        } while (cont);
        return next[0].map(e->e.orElse(null));
    }

    public static <T> Future[] futures(Single<T>... futures){

        Future[] array = new Future[futures.length];
        for(int i=0;i<array.length;i++){
            array[i]=future(futures[i]);
        }
        return array;
    }
    public static <T> Future<T> future(Single<T> future){
        return Future.fromPublisher(future.toFlowable());
    }

    public static <R> LazyEither<Throwable,R> either(Single<R> either){
        return LazyEither.fromFuture(future(either));

    }

    public static <T> Maybe<T> maybe(Single<T> opt){
        return Maybe.fromFuture(future(opt));
    }
    public static <T> Eval<T> eval(Single<T> opt){
        return Eval.fromFuture(future(opt));
    }





    /**
     * Select the first Single to complete
     *
     * @see CompletableFuture#anyOf(CompletableFuture...)
     * @param fts Singles to race
     * @return First Single to complete
     */
    public static <T> Single<T> anyOf(Single<T>... fts) {
        return Single.fromPublisher(Future.anyOf(futures(fts)));

    }
    /**
     * Wait until all the provided Future's to complete
     *
     * @see CompletableFuture#allOf(CompletableFuture...)
     *
     * @param fts Singles to  wait on
     * @return Single that completes when all the provided Futures Complete. Empty Future result, or holds an Exception
     *         from a provided Future that failed.
     */
    public static <T> Single<T> allOf(Single<T>... fts) {

        return Single.fromPublisher(Future.allOf(futures(fts)));
    }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     *
     * <pre>
     * {@code
     *
     * Single<ListX<Integer>> strings = Singles.quorum(status -> status.getCompleted() >0, Single.deferred(()->1),Single.empty(),Single.empty());


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
    public static <T> Single<Seq<T>> quorum(Predicate<Status<T>> breakout, Consumer<Throwable> errorHandler, Single<T>... fts) {

        return Single.fromPublisher(Futures.quorum(breakout,errorHandler,futures(fts)));


    }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     *
     * <pre>
     * {@code
     *
     * Single<ListX<Integer>> strings = Singles.quorum(status -> status.getCompleted() >0, Single.deferred(()->1),Single.empty(),Single.empty());


    strings.get().size()
    //1
     *
     * }
     * </pre>
     *
     *
     * @param breakout Predicate that determines whether the block should be
     *            continued or removed
     * @param fts Singles to  wait on results from
     * @return Single which will be populated with a Quorum of results
     */
    @SafeVarargs
    public static <T> Single<Seq<T>> quorum(Predicate<Status<T>> breakout, Single<T>... fts) {

        return Single.fromPublisher(Futures.quorum(breakout,futures(fts)));


    }
    /**
     * Select the first Future to return with a successful result
     *
     * <pre>
     * {@code
     * Single<Integer> ft = Single.empty();
      Single<Integer> result = Singles.firstSuccess(Single.deferred(()->1),ft);

    ft.complete(10);
    result.get() //1
     * }
     * </pre>
     *
     * @param fts Singles to race
     * @return First Single to return with a result
     */
    @SafeVarargs
    public static <T> Single<T> firstSuccess(Single<T>... fts) {
        return Single.fromPublisher(Future.firstSuccess(futures(fts)));

    }

    /**
     * Perform a For Comprehension over a Single, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Singles.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Singles.forEach4;
     *
          forEach4(Single.just(1),
                  a-> Single.just(a+1),
                  (a,b) -> Single.<Integer>just(a+b),
                  (a,b,c) -> Single.<Integer>just(a+b+c),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Single
     * @param value2 Nested Single
     * @param value3 Nested Single
     * @param value4 Nested Single
     * @param yieldingFunction Generates a result per combination
     * @return Single with a combined value generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Single<R> forEach4(Single<? extends T1> value1,
            Function<? super T1, ? extends Single<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Single<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends Single<R3>> value4,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        Single<? extends R> res = value1.flatMap(in -> {

            Single<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Single<R2> b = value3.apply(in, ina);
                return b.flatMap(inb -> {
                    Single<R3> c = value4.apply(in, ina, inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
        return  narrow(res);
    }


    /**
     * Perform a For Comprehension over a Single, accepting 2 generating functions.
     * This results in a three level nested internal iteration over the provided Singles.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Singles.forEach3;
     *
          forEach3(Single.just(1),
                  a-> Single.just(a+1),
                  (a,b) -> Single.<Integer>just(a+b),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Single
     * @param value2 Nested Single
     * @param value3 Nested Single
     * @param yieldingFunction Generates a result per combination
     * @return Single with a combined value generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Single<R> forEach3(Single<? extends T1> value1,
            Function<? super T1, ? extends Single<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Single<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {


        Single<? extends R> res = value1.flatMap(in -> {

            Single<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Single<R2> b = value3.apply(in, ina);


                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));


            });

        });
        return narrow(res);

    }



    /**
     * Perform a For Comprehension over a Single, accepting a generating function.
     * This results in a two level nested internal iteration over the provided Singles.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Singles.forEach;
     *
          forEach(Single.just(1),
                  a-> Single.just(a+1),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Single
     * @param value2 Nested Single
     * @param yieldingFunction Generates a result per combination
     * @return Single with a combined value generated by the yielding function
     */
    public static <T, R1, R> Single<R> forEach(Single<? extends T> value1,
                                             Function<? super T, Single<R1>> value2,
                                             BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        Single<R> res = value1.flatMap(in -> {

            Single<R1> a = value2.apply(in);
            return a.map(ina -> yieldingFunction.apply(in, ina));


        });


        return narrow(res);

    }



    /**
     * Lazily combine this Single with the supplied value via the supplied BiFunction
     *
     * @param single Single to combine with another value
     * @param app Value to combine with supplied single
     * @param fn Combiner function
     * @return Combined Single
     */
    public static <T1, T2, R> Single<R> combine(Single<? extends T1> single, Value<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Single.fromPublisher(Future.fromPublisher(single.toFlowable())
                                .zip(app, fn));
    }

    /**
     * Lazily combine this Single with the supplied Single via the supplied BiFunction
     *
     * @param single Single to combine with another value
     * @param app Single to combine with supplied single
     * @param fn Combiner function
     * @return Combined Single
     */
    public static <T1, T2, R> Single<R> combine(Single<? extends T1> single, Single<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Single.fromPublisher(Future.fromPublisher(single.toFlowable())
                                .zip(Future.fromPublisher(app.toFlowable()), fn));
    }

    /**
     * Combine the provided Single with the first element (if present) in the provided Iterable using the provided BiFunction
     *
     * @param single Single to combine with an Iterable
     * @param app Iterable to combine with a Single
     * @param fn Combining function
     * @return Combined Single
     */
    public static <T1, T2, R> Single<R> zip(Single<? extends T1> single, Iterable<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Single.fromPublisher(Future.fromPublisher(single.toFlowable())
                                .zip(app, fn));
    }

    /**
     * Combine the provided Single with the first element (if present) in the provided Publisher using the provided BiFunction
     *
     * @param single  Single to combine with a Publisher
     * @param fn Publisher to combine with a Single
     * @param app Combining function
     * @return Combined Single
     */
    public static <T1, T2, R> Single<R> zip(Single<? extends T1> single, BiFunction<? super T1, ? super T2, ? extends R> fn,
            Publisher<? extends T2> app) {
        Single<R> res = Single.fromPublisher(Future.fromPublisher(single.toFlowable()).zip(fn,app));
        return res;
    }


    /**
     * Construct a Single from Iterable by taking the first value from Iterable
     *
     * @param t Iterable to populate Single from
     * @return Single containing first element from Iterable (or empty Single)
     */
    public static <T> Single<T> fromIterable(Iterable<T> t) {
        return Single.fromPublisher(Future.fromIterable(t));
    }

    /**
     * Get an Iterator for the value (if any) in the provided Single
     *
     * @param pub Single to get Iterator for
     * @return Iterator over Single value
     */
    public static <T> Iterator<T> iterator(Single<T> pub) {
        return pub.toFlowable().blockingIterable().iterator();

    }

    public static <R> Single<R> narrow(Single<? extends R> apply) {
        return (Single<R>)apply;
    }


}
