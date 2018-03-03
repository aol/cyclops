package cyclops.companion.reactor;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.oath.anym.AnyMValue;
import com.oath.cyclops.reactor.hkt.FluxKind;
import com.oath.cyclops.react.Status;
import cyclops.companion.CompletableFutures;
import cyclops.companion.Futures;
import cyclops.control.*;

import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.kinds.CompletableFutureKind;
import cyclops.kinds.OptionalKind;
import cyclops.kinds.StreamKind;
import cyclops.monads.*;
import cyclops.monads.ReactorWitness.flux;
import cyclops.monads.ReactorWitness.mono;
import com.oath.cyclops.reactor.hkt.MonoKind;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.Value;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.monads.Witness.*;
import cyclops.monads.transformers.reactor.MonoT;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import org.reactivestreams.Publisher;



import lombok.experimental.UtilityClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.oath.cyclops.reactor.hkt.MonoKind.widen;

/**
 * Companion class for working with Reactor Mono types
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Monos {

    public static  <W1,T> Coproduct<W1,mono,T> coproduct(Mono<T> list, InstanceDefinitions<W1> def1){
        return Coproduct.of(Either.right(MonoKind.widen(list)),def1, Instances.definitions());
    }
    public static  <W1,T> Coproduct<W1,mono,T> coproduct(T value,InstanceDefinitions<W1> def1){
        return coproduct(Mono.just(value),def1);
    }
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
    public static <T> Active<mono,T> allTypeclasses(Mono<T> type){
        return Active.of(widen(type), Monos.Instances.definitions());
    }
    public static <T,W2,R> Nested<mono,W2,R> mapM(Mono<T> type, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        Mono<Higher<W2, R>> e = type.map(fn);
        MonoKind<Higher<W2, R>> lk = widen(e);
        return Nested.of(lk, Monos.Instances.definitions(), defs);
    }
    /**
     * Companion class for creating Type Class instances for working with Monos
     *
     */
    @UtilityClass
    public static class Instances {

        public static InstanceDefinitions<mono> definitions() {
            return new InstanceDefinitions<mono>() {

                @Override
                public <T, R> Functor<mono> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<mono> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<mono> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<mono> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<mono>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<mono>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<mono> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<mono>> monadPlus(Monoid<Higher<mono, T>> m) {
                    return Maybe.just(Instances.monadPlus(m));
                }

                @Override
                public <C2, T> Traverse<mono> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T>  Foldable<mono> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<mono>> comonad() {
                    return Maybe.just(Instances.comonad());
                }

                @Override
                public <T> Maybe<Unfoldable<mono>> unfoldable() {
                    return Maybe.nothing();
                }
            };
        }
        /**
         *
         * Transform a Mono, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  MonoKind<Integer> future = Monos.functor().map(i->i*2, MonoKind.widen(Mono.just(3));
         *
         *  //[6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Monos
         * <pre>
         * {@code
         *   MonoKind<Integer> ft = Monos.unit()
        .unit("hello")
        .then(h->Monos.functor().map((String v) ->v.length(), h))
        .convert(MonoKind::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Monos
         */
        public static <T,R>Functor<mono> functor(){
            BiFunction<MonoKind<T>,Function<? super T, ? extends R>,MonoKind<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * MonoKind<String> ft = Monos.unit()
        .unit("hello")
        .convert(MonoKind::narrowK);

        //Mono["hello"]
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Monos
         */
        public static <T> Pure<mono> unit(){
            return General.<mono,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.oath.cyclops.hkt.jdk.MonoKind.widen;
         * import static com.oath.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asMono;
         *
        Monos.applicative()
        .ap(widen(asMono(l1(this::multiplyByTwo))),widen(Mono.just(3)));
         *
         * //[6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * MonoKind<Function<Integer,Integer>> ftFn =Monos.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(MonoKind::narrowK);

        MonoKind<Integer> ft = Monos.unit()
        .unit("hello")
        .then(h->Monos.functor().map((String v) ->v.length(), h))
        .then(h->Monos.applicative().ap(ftFn, h))
        .convert(MonoKind::narrowK);

        //Mono.just("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Monos
         */
        public static <T,R> Applicative<mono> applicative(){
            BiFunction<MonoKind< Function<T, R>>,MonoKind<T>,MonoKind<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.oath.cyclops.hkt.jdk.MonoKind.widen;
         * MonoKind<Integer> ft  = Monos.monad()
        .flatMap(i->widen(Mono.just(i)), widen(Mono.just(3)))
        .convert(MonoKind::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    MonoKind<Integer> ft = Monos.unit()
        .unit("hello")
        .then(h->Monos.monad().flatMap((String v) ->Monos.unit().unit(v.length()), h))
        .convert(MonoKind::narrowK);

        //Mono.just("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Monos
         */
        public static <T,R> Monad<mono> monad(){

            BiFunction<Higher<mono,T>,Function<? super T, ? extends Higher<mono,R>>,Higher<mono,R>> flatMap = Instances::flatMap;
            return General.monad(applicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  MonoKind<String> ft = Monos.unit()
        .unit("hello")
        .then(h->Monos.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(MonoKind::narrowK);

        //Mono.just("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<mono> monadZero(){

            return General.monadZero(monad(), MonoKind.empty());
        }
        /**
         * Combines Monos by selecting the first result returned
         *
         * <pre>
         * {@code
         *  MonoKind<Integer> ft = Monos.<Integer>monadPlus()
        .plus(MonoKind.widen(Mono.empty()), MonoKind.widen(Mono.just(10)))
        .convert(MonoKind::narrowK);
        //Mono.empty()
         *
         * }
         * </pre>
         * @return Type class for combining Monos by concatenation
         */
        public static <T> MonadPlus<mono> monadPlus(){


            Monoid<MonoKind<T>> m = Monoid.of(MonoKind.<T>widen(Mono.empty()),
                    (f,g)-> widen(Mono.first(f.narrow(),g.narrow())));

            Monoid<Higher<mono,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<MonoKind<Integer>> m = Monoid.of(MonoKind.widen(Arrays.asMono()), (a,b)->a.isEmpty() ? b : a);
        MonoKind<Integer> ft = Monos.<Integer>monadPlus(m)
        .plus(MonoKind.widen(Arrays.asMono(5)), MonoKind.widen(Arrays.asMono(10)))
        .convert(MonoKind::narrowK);
        //Arrays.asMono(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Monos
         * @return Type class for combining Monos
         */
        public static <T> MonadPlus<mono> monadPlusK(Monoid<MonoKind<T>> m){
            Monoid<Higher<mono,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T> MonadRec<mono> monadRec(){
            return new MonadRec<mono>() {
                @Override
                public <T, R> Higher<mono, R> tailRec(T initial, Function<? super T, ? extends Higher<mono, ? extends Either<T, R>>> fn) {
                    return widen(Monos.tailRec(initial,fn.andThen(MonoKind::narrow)));
                }
            };
        }

        public static <T> MonadPlus<mono> monadPlus(Monoid<Higher<mono,T>> m){
            Monoid<Higher<mono,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<mono> traverse(){

            return General.traverseByTraverse(applicative(), Instances::traverseA);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Monos.foldable()
        .foldLeft(0, (a,b)->a+b, MonoKind.widen(Arrays.asMono(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<mono> foldable(){
            return new Foldable<mono>() {
                @Override
                public <T> T foldRight(Monoid<T> m, Higher<mono, T> ds) {
                   return  m.apply(m.zero(), MonoKind.narrow(ds).block());
                }

                @Override
                public <T> T foldLeft(Monoid<T> m, Higher<mono, T> ds) {
                    return  m.apply(m.zero(), MonoKind.narrow(ds).block());
                }

                @Override
                public <T, R> R foldMap(Monoid<R> m, Function<? super T, ? extends R> fn, Higher<mono, T> ds) {
                    return  m.apply(m.zero(), MonoKind.narrow(ds).map(fn).block());
                }
            };
        }
        public static <T> Comonad<mono> comonad(){
            Function<? super Higher<mono, T>, ? extends T> extractFn = maybe -> maybe.convert(MonoKind::narrow).block();
            return General.comonad(functor(), unit(), extractFn);
        }

        private static <T> MonoKind<T> of(T value){
            return widen(Mono.just(value));
        }
        private static <T,R> MonoKind<R> ap(MonoKind<Function< T, R>> lt, MonoKind<T> list){


            return widen(Monos.combine(lt.narrow(),list.narrow(), (a, b)->a.apply(b)));

        }
        private static <T,R> Higher<mono,R> flatMap(Higher<mono,T> lt, Function<? super T, ? extends  Higher<mono,R>> fn){
            return widen(MonoKind.narrow(lt).flatMap(fn.andThen(MonoKind::narrow)));
        }
        private static <T,R> MonoKind<R> map(MonoKind<T> lt, Function<? super T, ? extends R> fn){
            return widen(lt.narrow().map(fn));
        }


        private static <C2,T,R> Higher<C2, Higher<mono, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                            Higher<mono, T> ds){
            Mono<T> future = MonoKind.narrow(ds);
            return applicative.map(MonoKind::just, fn.apply(future.block()));
        }

    }

    public static interface MonoNested {
        public static <T> Nested<mono,mono,T> mono(Mono<Mono<T>> nested){
            Mono<MonoKind<T>> f = nested.map(MonoKind::widen);
            MonoKind<MonoKind<T>> x = widen(f);
            MonoKind<Higher<mono,T>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(), Monos.Instances.definitions());
        }
        public static <T> Nested<mono,flux,T> flux(Mono<Flux<T>> nested){
            Mono<FluxKind<T>> f = nested.map(FluxKind::widen);
            MonoKind<FluxKind<T>> x = widen(f);
            MonoKind<Higher<flux,T>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(), Fluxs.Instances.definitions());
        }


        public static <T> Nested<mono,reactiveSeq,T> reactiveSeq(Mono<ReactiveSeq<T>> nested){
            MonoKind<ReactiveSeq<T>> x = widen(nested);
            MonoKind<Higher<reactiveSeq,T>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(),ReactiveSeq.Instances.definitions());
        }

        public static <T> Nested<mono,maybe,T> maybe(Mono<Maybe<T>> nested){
            MonoKind<Maybe<T>> x = widen(nested);
            MonoKind<Higher<maybe,T>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(),Maybe.Instances.definitions());
        }
        public static <T> Nested<mono,Witness.eval,T> eval(Mono<Eval<T>> nested){
            MonoKind<Eval<T>> x = widen(nested);
            MonoKind<Higher<Witness.eval,T>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(),Eval.Instances.definitions());
        }
        public static <T> Nested<mono,Witness.future,T> future(Mono<Future<T>> nested){
            MonoKind<Future<T>> x = widen(nested);
            MonoKind<Higher<Witness.future,T>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(),Future.Instances.definitions());
        }
        public static <S, P> Nested<mono,Higher<Witness.either,S>, P> xor(Mono<Either<S, P>> nested){
            MonoKind<Either<S, P>> x = widen(nested);
            MonoKind<Higher<Higher<Witness.either,S>, P>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(),Either.Instances.definitions());
        }
        public static <S,T> Nested<mono,Higher<reader,S>, T> reader(Mono<Reader<S, T>> nested, S defaultValue){
            MonoKind<Reader<S, T>> x = widen(nested);
            MonoKind<Higher<Higher<reader,S>, T>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(),Reader.Instances.definitions(defaultValue));
        }
        public static <S extends Throwable, P> Nested<mono,Higher<Witness.tryType,S>, P> cyclopsTry(Mono<cyclops.control.Try<P, S>> nested){
            MonoKind<cyclops.control.Try<P, S>> x = widen(nested);
            MonoKind<Higher<Higher<Witness.tryType,S>, P>> y = (MonoKind)x;
            return Nested.of(y,Instances.definitions(),cyclops.control.Try.Instances.definitions());
        }
        public static <T> Nested<mono,optional,T> javaOptional(Mono<Optional<T>> nested){
            Mono<OptionalKind<T>> f = nested.map(o -> OptionalKind.widen(o));
            MonoKind<OptionalKind<T>> x = MonoKind.widen(f);

            MonoKind<Higher<optional,T>> y = (MonoKind)x;
            return Nested.of(y, Instances.definitions(), cyclops.companion.Optionals.Instances.definitions());
        }
        public static <T> Nested<mono,completableFuture,T> javaCompletableFuture(Mono<CompletableFuture<T>> nested){
            Mono<CompletableFutureKind<T>> f = nested.map(o -> CompletableFutureKind.widen(o));
            MonoKind<CompletableFutureKind<T>> x = MonoKind.widen(f);
            MonoKind<Higher<completableFuture,T>> y = (MonoKind)x;
            return Nested.of(y, Instances.definitions(), CompletableFutures.Instances.definitions());
        }
        public static <T> Nested<mono,Witness.stream,T> javaStream(Mono<java.util.stream.Stream<T>> nested){
            Mono<StreamKind<T>> f = nested.map(o -> StreamKind.widen(o));
            MonoKind<StreamKind<T>> x = MonoKind.widen(f);
            MonoKind<Higher<Witness.stream,T>> y = (MonoKind)x;
            return Nested.of(y, Instances.definitions(), cyclops.companion.Streams.Instances.definitions());
        }





    }

    public static interface NestedMono{
        public static <T> Nested<reactiveSeq,mono,T> reactiveSeq(ReactiveSeq<Mono<T>> nested){
            ReactiveSeq<Higher<mono,T>> x = nested.map(MonoKind::widenK);
            return Nested.of(x,ReactiveSeq.Instances.definitions(),Instances.definitions());
        }

        public static <T> Nested<maybe,mono,T> maybe(Maybe<Mono<T>> nested){
            Maybe<Higher<mono,T>> x = nested.map(MonoKind::widenK);

            return Nested.of(x,Maybe.Instances.definitions(),Instances.definitions());
        }
        public static <T> Nested<Witness.eval,mono,T> eval(Eval<Mono<T>> nested){
            Eval<Higher<mono,T>> x = nested.map(MonoKind::widenK);

            return Nested.of(x,Eval.Instances.definitions(),Instances.definitions());
        }
        public static <T> Nested<Witness.future,mono,T> future(Future<Mono<T>> nested){
            Future<Higher<mono,T>> x = nested.map(MonoKind::widenK);

            return Nested.of(x,Future.Instances.definitions(),Instances.definitions());
        }
        public static <S, P> Nested<Higher<Witness.either,S>,mono, P> xor(Either<S, Mono<P>> nested){
            Either<S, Higher<mono,P>> x = nested.map(MonoKind::widenK);

            return Nested.of(x,Either.Instances.definitions(),Instances.definitions());
        }
        public static <S,T> Nested<Higher<reader,S>,mono, T> reader(Reader<S, Mono<T>> nested, S defaultValue){

            Reader<S, Higher<mono, T>>  x = nested.map(MonoKind::widenK);

            return Nested.of(x,Reader.Instances.definitions(defaultValue),Instances.definitions());
        }
        public static <S extends Throwable, P> Nested<Higher<Witness.tryType,S>,mono, P> cyclopsTry(cyclops.control.Try<Mono<P>, S> nested){
            cyclops.control.Try<Higher<mono,P>, S> x = nested.map(MonoKind::widenK);

            return Nested.of(x,cyclops.control.Try.Instances.definitions(),Instances.definitions());
        }
        public static <T> Nested<optional, mono, T> javaOptional(Optional<Mono<T>> nested){
            Optional<Higher<mono,T>> x = nested.map(MonoKind::widenK);

            return  Nested.of(OptionalKind.widen(x), cyclops.companion.Optionals.Instances.definitions(), Instances.definitions());
        }
        public static <T> Nested<completableFuture,mono,T> javaCompletableFuture(CompletableFuture<Mono<T>> nested){
            CompletableFuture<Higher<mono,T>> x = nested.thenApply(MonoKind::widenK);

            return Nested.of(CompletableFutureKind.widen(x), CompletableFutures.Instances.definitions(),Instances.definitions());
        }
        public static <T> Nested<Witness.stream,mono,T> javaStream(java.util.stream.Stream<Mono<T>> nested){
            java.util.stream.Stream<Higher<mono,T>> x = nested.map(MonoKind::widenK);

            return Nested.of(StreamKind.widen(x), cyclops.companion.Streams.Instances.definitions(),Instances.definitions());
        }
    }

}
