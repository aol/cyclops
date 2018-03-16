package cyclops.companion.reactor;

import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.react.Status;
import com.oath.cyclops.types.Value;
import cyclops.companion.Futures;
import cyclops.control.Either;
import cyclops.control.Eval;
import cyclops.control.Future;
import cyclops.control.Maybe;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.ReactorWitness;
import cyclops.monads.ReactorWitness.mono;
import cyclops.monads.WitnessType;
import cyclops.monads.XorM;
import cyclops.monads.transformers.reactor.MonoT;
import cyclops.reactive.collections.mutable.ListX;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Companion class for working with Reactor Mono types
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Monos {


    public static  <W1 extends WitnessType<W1>,T> XorM<W1,mono,T> xorM(Mono<T> type){
        return XorM.right(anyM(type));
    }

    public static <T> Mono<T> raw(AnyM<mono,T> anyM){
        return ReactorWitness.mono(anyM);
    }

    public static <T, R> Mono< R> tailRec(T initial, Function<? super T, ? extends Mono<? extends Either<T, R>>> fn) {
        Mono<? extends Either<T, R>> next[] = new Mono[1];
        next[0] = Mono.just(Either.left(initial));
        boolean cont = true;
        do {
            cont = next[0].map(p -> p.visit(s -> {
                next[0] = fn.apply(s);
                return true;
            }, pr -> false)).block();
        } while (cont);
        return next[0].map(e->e.orElse(null));
    }
    public static <W extends WitnessType<W>,T> MonoT<W,T> liftM(AnyM<W,Mono<T>> nested){
        return MonoT.of(nested);
    }
    public static <T,W extends WitnessType<W>> MonoT<W, T> liftM(Mono<T> opt, W witness) {
        return MonoT.of(witness.adapter().unit(opt));
    }

    public static <T> Future[] futures(Mono<T>... futures){

        Future[] array = new Future[futures.length];
        for(int i=0;i<array.length;i++){
            array[i]=future(futures[i]);
        }
        return array;
    }
    public static <T> Future<T> future(Mono<T> future){
        return Future.of(future.toFuture());
    }

    public static <R> Either<Throwable,R> either(Mono<R> either){
        return Either.fromPublisher(either);

    }

    public static <T> Maybe<T> maybe(Mono<T> opt){
        return Maybe.fromFuture(future(opt));
    }
    public static <T> Eval<T> eval(Mono<T> opt){
        return Eval.fromFuture(future(opt));
    }

    /**
     * Construct an AnyM type from a Mono. This allows the Mono to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     *
     * <pre>
     * {@code
     *
     *    AnyMSeq<Integer> mono = Fluxs.anyM(Mono.just(1,2,3));
     *    AnyMSeq<Integer> transformedMono = myGenericOperation(mono);
     *
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     *
     * @param mono To wrap inside an AnyM
     * @return AnyMSeq wrapping a Mono
     */
    public static <T> AnyMValue<mono,T> anyM(Mono<T> mono) {
        return AnyM.ofValue(mono, ReactorWitness.mono.INSTANCE);
    }



    /**
     * Select the first Mono to complete
     *
     * @see CompletableFuture#anyOf(CompletableFuture...)
     * @param fts Monos to race
     * @return First Mono to complete
     */
    public static <T> Mono<T> anyOf(Mono<T>... fts) {
        return Mono.from(Future.anyOf(futures(fts)));

    }
    /**
     * Wait until all the provided Future's to complete
     *
     * @see CompletableFuture#allOf(CompletableFuture...)
     *
     * @param fts Monos to  wait on
     * @return Mono that completes when all the provided Futures Complete. Empty Future result, or holds an Exception
     *         from a provided Future that failed.
     */
    public static <T> Mono<T> allOf(Mono<T>... fts) {

        return Mono.from(Future.allOf(futures(fts)));
    }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     *
     * <pre>
     * {@code
     *
     * Mono<ListX<Integer>> strings = Monos.quorum(status -> status.getCompleted() >0, Mono.deferred(()->1),Mono.empty(),Mono.empty());


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
    public static <T> Mono<ListX<T>> quorum(Predicate<Status<T>> breakout, Consumer<Throwable> errorHandler, Mono<T>... fts) {

        return Mono.from(Futures.quorum(breakout,errorHandler,futures(fts)));


    }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     *
     * <pre>
     * {@code
     *
     * Mono<ListX<Integer>> strings = Monos.quorum(status -> status.getCompleted() >0, Mono.deferred(()->1),Mono.empty(),Mono.empty());


    strings.get().size()
    //1
     *
     * }
     * </pre>
     *
     *
     * @param breakout Predicate that determines whether the block should be
     *            continued or removed
     * @param fts Monos to  wait on results from
     * @return Mono which will be populated with a Quorum of results
     */
    @SafeVarargs
    public static <T> Mono<ListX<T>> quorum(Predicate<Status<T>> breakout, Mono<T>... fts) {

        return Mono.from(Futures.quorum(breakout,futures(fts)));


    }
    /**
     * Select the first Future to return with a successful result
     *
     * <pre>
     * {@code
     * Mono<Integer> ft = Mono.empty();
      Mono<Integer> result = Monos.firstSuccess(Mono.deferred(()->1),ft);

    ft.complete(10);
    result.get() //1
     * }
     * </pre>
     *
     * @param fts Monos to race
     * @return First Mono to return with a result
     */
    @SafeVarargs
    public static <T> Mono<T> firstSuccess(Mono<T>... fts) {
        return Mono.from(Future.firstSuccess(futures(fts)));

    }

    /**
     * Perform a For Comprehension over a Mono, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Monos.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Monos.forEach4;
     *
          forEach4(Mono.just(1),
                  a-> Mono.just(a+1),
                  (a,b) -> Mono.<Integer>just(a+b),
                  (a,b,c) -> Mono.<Integer>just(a+b+c),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Mono
     * @param value2 Nested Mono
     * @param value3 Nested Mono
     * @param value4 Nested Mono
     * @param yieldingFunction Generates a result per combination
     * @return Mono with a combined value generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Mono<R> forEach4(Mono<? extends T1> value1,
            Function<? super T1, ? extends Mono<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Mono<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends Mono<R3>> value4,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        Future<? extends R> res = Future.fromPublisher(value1).flatMap(in -> {

            Future<R1> a = Future.fromPublisher(value2.apply(in));
            return a.flatMap(ina -> {
                Future<R2> b = Future.fromPublisher(value3.apply(in, ina));
                return b.flatMap(inb -> {
                    Future<R3> c = Future.fromPublisher(value4.apply(in, ina, inb));
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
        return Mono.from(res);
    }


    /**
     * Perform a For Comprehension over a Mono, accepting 2 generating functions.
     * This results in a three level nested internal iteration over the provided Monos.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Monos.forEach3;
     *
          forEach3(Mono.just(1),
                  a-> Mono.just(a+1),
                  (a,b) -> Mono.<Integer>just(a+b),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Mono
     * @param value2 Nested Mono
     * @param value3 Nested Mono
     * @param yieldingFunction Generates a result per combination
     * @return Mono with a combined value generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Mono<R> forEach3(Mono<? extends T1> value1,
            Function<? super T1, ? extends Mono<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Mono<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        Future<? extends R> res = Future.fromPublisher(value1).flatMap(in -> {

            Future<R1> a = Future.fromPublisher(value2.apply(in));
            return a.flatMap(ina -> {
                Future<R2> b = Future.fromPublisher(value3.apply(in, ina));


                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));


            });

        });
        return Mono.from(res);

    }



    /**
     * Perform a For Comprehension over a Mono, accepting a generating function.
     * This results in a two level nested internal iteration over the provided Monos.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Monos.forEach;
     *
          forEach(Mono.just(1),
                  a-> Mono.just(a+1),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Mono
     * @param value2 Nested Mono
     * @param yieldingFunction Generates a result per combination
     * @return Mono with a combined value generated by the yielding function
     */
    public static <T, R1, R> Mono<R> forEach(Mono<? extends T> value1,
                                             Function<? super T, Mono<R1>> value2,
                                             BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        Future<R> res = Future.fromPublisher(value1).flatMap(in -> {

            Future<R1> a = Future.fromPublisher(value2.apply(in));
            return a.map(ina -> yieldingFunction.apply(in, ina));


        });


        return Mono.from(res);

    }



    /**
     * Lazily combine this Mono with the supplied value via the supplied BiFunction
     *
     * @param mono Mono to combine with another value
     * @param app Value to combine with supplied mono
     * @param fn Combiner function
     * @return Combined Mono
     */
    public static <T1, T2, R> Mono<R> combine(Mono<? extends T1> mono, Value<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Mono.from(Future.of(mono.toFuture())
                                .zip(app, fn));
    }

    /**
     * Lazily combine this Mono with the supplied Mono via the supplied BiFunction
     *
     * @param mono Mono to combine with another value
     * @param app Mono to combine with supplied mono
     * @param fn Combiner function
     * @return Combined Mono
     */
    public static <T1, T2, R> Mono<R> combine(Mono<? extends T1> mono, Mono<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Mono.from(Future.of(mono.toFuture())
                                .zip(Future.of(app.toFuture()), fn));
    }

    /**
     * Combine the provided Mono with the first element (if present) in the provided Iterable using the provided BiFunction
     *
     * @param mono Mono to combine with an Iterable
     * @param app Iterable to combine with a Mono
     * @param fn Combining function
     * @return Combined Mono
     */
    public static <T1, T2, R> Mono<R> zip(Mono<? extends T1> mono, Iterable<? extends T2> app,
            BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return Mono.from(Future.of(mono.toFuture())
                                .zip(app, fn));
    }

    /**
     * Combine the provided Mono with the first element (if present) in the provided Publisher using the provided BiFunction
     *
     * @param mono  Mono to combine with a Publisher
     * @param fn Publisher to combine with a Mono
     * @param app Combining function
     * @return Combined Mono
     */
    public static <T1, T2, R> Mono<R> zip(Mono<? extends T1> mono, BiFunction<? super T1, ? super T2, ? extends R> fn,
            Publisher<? extends T2> app) {
        Mono<R> res = Mono.from(Future.of(mono.toFuture()).zip(fn,app));
        return res;
    }



    /**
     * Construct a Mono from Iterable by taking the first value from Iterable
     *
     * @param t Iterable to populate Mono from
     * @return Mono containing first element from Iterable (or empty Mono)
     */
    public static <T> Mono<T> fromIterable(Iterable<T> t) {
        return Mono.from(Flux.fromIterable(t));
    }

    /**
     * Get an Iterator for the value (if any) in the provided Mono
     *
     * @param pub Mono to get Iterator for
     * @return Iterator over Mono value
     */
    public static <T> Iterator<T> iterator(Mono<T> pub) {
        return Future.fromPublisher(pub).iterator();

    }

    public static <R> Mono<R> narrow(Mono<? extends R> apply) {
        return (Mono<R>)apply;
    }



}
