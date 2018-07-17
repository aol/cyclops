package cyclops.reactive;

import com.oath.cyclops.hkt.DataWitness.io;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.foldable.To;
import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.data.tuple.Tuple5;
import cyclops.data.tuple.Tuple6;
import cyclops.data.tuple.Tuple7;
import com.oath.cyclops.types.functor.ReactiveTransformable;
import cyclops.function.Function3;
import cyclops.function.Memoize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public interface IO<T> extends To<IO<T>>,Higher<io,T>,ReactiveTransformable<T>,Publisher<T> {

    public static <T> IO<T> of(T s) {
        return ReactiveSeqIO.of(s);
    }


    public static <T> IO<T> of(Supplier<? extends T> s) {
        return ReactiveSeqIO.of(s);
    }

    public static <T> IO<T> of(Supplier<? extends T> s, Executor ex) {
        return ReactiveSeqIO.of(s, ex);
    }

    public static <T> IO<T> fromPublisher(Publisher<T> p) {
        return ReactiveSeqIO.fromPublisher(p);
    }

    public static <T, X extends Throwable> IO<Try<T, X>> withCatch(Try.CheckedSupplier<T, X> cf, Class<? extends X>... classes) {
        return of(() -> Try.withCatch(cf, classes));
    }

    public static <T, X extends Throwable> IO<T> recover(IO<Try<T, X>> io, Supplier<? extends T> s) {
        return io.map(t -> t.fold(i -> i, s));
    }

    public static <T> IO<T> flatten(IO<IO<T>> io) {
        return io.flatMap(i -> i);
    }

    <R> IO<R> map(Function<? super T, ? extends R> s);

    <R> IO<R> flatMap(Function<? super T, IO<? extends R>> s);

    @Override
    default <R> IO<R> retry(Function<? super T, ? extends R> fn) {
        return (IO<R>)ReactiveTransformable.super.retry(fn);
    }

    @Override
    default <R> IO<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (IO<R>)ReactiveTransformable.super.retry(fn,retries,delay,timeUnit);
    }


    default <R extends AutoCloseable> IO<R> bracket(Function<? super T, ? extends R> fn) {
        Managed<R> m = Managed.of(map(fn));
        return m.io();
    }
    default <R> IO<R> bracket(Function<? super T, ? extends R> fn, Consumer<R> consumer) {
        Managed<R> m = Managed.of(map(fn),consumer);
        return m.io();
    }

    default IO<T> ensuring(Consumer<T> action){
        return Managed.of(this,action).io();
    }

    default <B,R> IO<R> zip(IO<B> that, BiFunction<? super T, ? super B,? extends R> fn){
        return flatMap(a->that.map(t->fn.apply(a,t)));
    }

    default <B,R> IO<R> par(IO<B> that, BiFunction<? super T, ? super B,? extends R> fn){
        return IO.fromPublisher(stream().zip(fn,that));
    }

    default IO<T> race(IO<T> that){
        return fromPublisher(Spouts.amb(Seq.of(publisher(),that.publisher())));
    }


    default <R> IO<Try<R, Throwable>> mapTry(Function<? super T, ? extends R> s) {
        return map(t -> Try.withCatch(() -> s.apply(t)));
    }

    default <R, X extends Throwable> IO<Try<R, X>> mapTry(Function<? super T, ? extends R> s, Class<? extends X>... classes) {
        return map(t -> Try.withCatch(() -> s.apply(t), classes));
    }

    default void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        Spouts.from(publisher()).forEach(consumerElement, consumerError, onComplete);
    }

    default Try<T, Throwable> run() {
        return Future.fromPublisher(publisher())
            .get();
    }

    default <R> R foldRun(Function<? super Try<T, Throwable>, ? extends R> transform) {
        return transform.apply(run());
    }

    default Future<T> future() {
        return Future.fromPublisher(publisher());
    }

    Publisher<T> publisher();

    @Override
    default void subscribe(Subscriber<? super T> sub) {
        publisher().subscribe(sub);
    }

    default ReactiveSeq<T> stream() {
        return Spouts.from(publisher());
    }

    default Try<T, Throwable> runAsync(Executor e) {
        return Try.fromPublisher(Future.of(() -> run(), e))
            .flatMap(Function.identity());
    }

    public static class Comprehensions {

        public static <T, F, R1, R2, R3, R4, R5, R6, R7> IO<R7> forEach(IO<T> io,
                                                                        Function<? super T, IO<R1>> value2,
                                                                        Function<? super Tuple2<? super T, ? super R1>, IO<R2>> value3,
                                                                        Function<? super Tuple3<? super T, ? super R1, ? super R2>, IO<R3>> value4,
                                                                        Function<? super Tuple4<? super T, ? super R1, ? super R2, ? super R3>, IO<R4>> value5,
                                                                        Function<? super Tuple5<T, ? super R1, ? super R2, ? super R3, ? super R4>, IO<R5>> value6,
                                                                        Function<? super Tuple6<T, ? super R1, ? super R2, ? super R3, ? super R4, ? super R5>, IO<R6>> value7,
                                                                        Function<? super Tuple7<T, ? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6>, IO<R7>> value8
        ) {

            return io.flatMap(in -> {

                IO<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    IO<R2> b = value3.apply(Tuple.tuple(in, ina));
                    return b.flatMap(inb -> {

                        IO<R3> c = value4.apply(Tuple.tuple(in, ina, inb));

                        return c.flatMap(inc -> {
                            IO<R4> d = value5.apply(Tuple.tuple(in, ina, inb, inc));
                            return d.flatMap(ind -> {
                                IO<R5> e = value6.apply(Tuple.tuple(in, ina, inb, inc, ind));
                                return e.flatMap(ine -> {
                                    IO<R6> f = value7.apply(Tuple.tuple(in, ina, inb, inc, ind, ine));
                                    return f.flatMap(inf -> {
                                        IO<R7> g = value8.apply(Tuple.tuple(in, ina, inb, inc, ind, ine, inf));
                                        return g;

                                    });

                                });
                            });

                        });

                    });


                });


            });

        }

        public static <T, F, R1, R2, R3, R4, R5, R6> IO<R6> forEach(IO<T> io,
                                                                    Function<? super T, IO<R1>> value2,
                                                                    Function<? super Tuple2<? super T, ? super R1>, IO<R2>> value3,
                                                                    Function<? super Tuple3<? super T, ? super R1, ? super R2>, IO<R3>> value4,
                                                                    Function<? super Tuple4<? super T, ? super R1, ? super R2, ? super R3>, IO<R4>> value5,
                                                                    Function<? super Tuple5<T, ? super R1, ? super R2, ? super R3, ? super R4>, IO<R5>> value6,
                                                                    Function<? super Tuple6<T, ? super R1, ? super R2, ? super R3, ? super R4, ? super R5>, IO<R6>> value7
        ) {

            return io.flatMap(in -> {

                IO<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    IO<R2> b = value3.apply(Tuple.tuple(in, ina));
                    return b.flatMap(inb -> {

                        IO<R3> c = value4.apply(Tuple.tuple(in, ina, inb));

                        return c.flatMap(inc -> {
                            IO<R4> d = value5.apply(Tuple.tuple(in, ina, inb, inc));
                            return d.flatMap(ind -> {
                                IO<R5> e = value6.apply(Tuple.tuple(in, ina, inb, inc, ind));
                                return e.flatMap(ine -> {
                                    IO<R6> f = value7.apply(Tuple.tuple(in, ina, inb, inc, ind, ine));
                                    return f;
                                });
                            });

                        });

                    });


                });


            });

        }

        public static <T, F, R1, R2, R3, R4, R5> IO<R5> forEach(IO<T> io,
                                                                Function<? super T, IO<R1>> value2,
                                                                Function<? super Tuple2<? super T, ? super R1>, IO<R2>> value3,
                                                                Function<? super Tuple3<? super T, ? super R1, ? super R2>, IO<R3>> value4,
                                                                Function<? super Tuple4<? super T, ? super R1, ? super R2, ? super R3>, IO<R4>> value5,
                                                                Function<? super Tuple5<T, ? super R1, ? super R2, ? super R3, ? super R4>, IO<R5>> value6
        ) {

            return io.flatMap(in -> {

                IO<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    IO<R2> b = value3.apply(Tuple.tuple(in, ina));
                    return b.flatMap(inb -> {

                        IO<R3> c = value4.apply(Tuple.tuple(in, ina, inb));

                        return c.flatMap(inc -> {
                            IO<R4> d = value5.apply(Tuple.tuple(in, ina, inb, inc));
                            return d.flatMap(ind -> {
                                IO<R5> e = value6.apply(Tuple.tuple(in, ina, inb, inc, ind));
                                return e;
                            });
                        });

                    });


                });


            });

        }

        public static <T, F, R1, R2, R3, R4> IO<R4> forEach(IO<T> io,
                                                            Function<? super T, IO<R1>> value2,
                                                            Function<? super Tuple2<? super T, ? super R1>, IO<R2>> value3,
                                                            Function<? super Tuple3<? super T, ? super R1, ? super R2>, IO<R3>> value4,
                                                            Function<? super Tuple4<? super T, ? super R1, ? super R2, ? super R3>, IO<R4>> value5

        ) {

            return io.flatMap(in -> {

                IO<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    IO<R2> b = value3.apply(Tuple.tuple(in, ina));
                    return b.flatMap(inb -> {

                        IO<R3> c = value4.apply(Tuple.tuple(in, ina, inb));

                        return c.flatMap(inc -> {
                            IO<R4> d = value5.apply(Tuple.tuple(in, ina, inb, inc));
                            return d;
                        });

                    });


                });


            });

        }

        public static <T, F, R1, R2, R3> IO<R3> forEach(IO<T> io,
                                                        Function<? super T, IO<R1>> value2,
                                                        Function<? super Tuple2<? super T, ? super R1>, IO<R2>> value3,
                                                        Function<? super Tuple3<? super T, ? super R1, ? super R2>, IO<R3>> value4

        ) {

            return io.flatMap(in -> {

                IO<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    IO<R2> b = value3.apply(Tuple.tuple(in, ina));
                    return b.flatMap(inb -> {

                        IO<R3> c = value4.apply(Tuple.tuple(in, ina, inb));

                        return c;

                    });


                });


            });

        }

        public static <T, F, R1, R2> IO<R2> forEach(IO<T> io,
                                                    Function<? super T, IO<R1>> value2,
                                                    Function<? super Tuple2<T, R1>, IO<R2>> value3

        ) {

            return io.flatMap(in -> {

                IO<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    IO<R2> b = value3.apply(Tuple.tuple(in, ina));
                    return b;


                });


            });

        }

        public static <T, F, R1> IO<R1> forEach(IO<T> io,
                                                Function<? super T, IO<R1>> value2


        ) {

            return io.flatMap(in -> {

                IO<R1> a = value2.apply(in);
                return a;


            });

        }


    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ReactiveSeqIO<T> implements IO<T> {
        private final Publisher<T> fn;

        public static <T> IO<T> of(T s) {
            return new ReactiveSeqIO<T>(ReactiveSeq.narrow(Spouts.of(s)));
        }

        public static <T> IO<T> of(Supplier<? extends T> s) {
            return new ReactiveSeqIO<T>(ReactiveSeq.narrow(Spouts.generate(Memoize.memoizeSupplier(s)).take(1)));
        }

        public static <T> IO<T> of(Supplier<? extends T> s, Executor ex) {
            return new ReactiveSeqIO<T>(Future.narrow(Future.of(s, ex)));
        }

        public static <T> IO<T> fromPublisher(Publisher<T> p) {
            return new ReactiveSeqIO<T>(p);
        }

        public static <T, X extends Throwable> IO<Try<T, X>> withCatch(Try.CheckedSupplier<T, X> cf, Class<? extends X>... classes) {
            return of(() -> Try.withCatch(cf, classes));
        }

        public static <T, X extends Throwable> IO<T> recover(IO<Try<T, X>> io, Supplier<? extends T> s) {
            return io.map(t -> t.fold(i -> i, s));
        }

        public static <T> IO<T> flatten(IO<IO<T>> io) {
            return io.flatMap(i -> i);
        }

        public static <T1, T2, R> IO<R> merge(Publisher<T1> p1, Publisher<T2> p2, BiFunction<? super T1, ? super T2, ? extends R> fn2) {
            ReactiveSeq<T1> s1 = Spouts.from(p1);
            ReactiveSeq<T2> s2 = Spouts.from(p2);
            return fromPublisher(s1.zip(fn2, s2));
        }

        public static <T1, T2, T3, R> IO<R> merge(Publisher<T1> p1, Publisher<T2> p2, Publisher<T3> p3, Function3<? super T1, ? super T2, ? super T3, ? extends R> fn3) {
            return merge(merge(p1, p2, Tuple::tuple), p3, (t2, c) -> fn3.apply(t2._1(), t2._2(), c));
        }

        @Override
        public <R> IO<R> map(Function<? super T, ? extends R> s) {
            return fromPublisher(Spouts.from(fn).map(t -> s.apply(t)));
        }

        @Override
        public <R> IO<R> flatMap(Function<? super T, IO<? extends R>> s) {
            return fromPublisher(Spouts.from(fn).mergeMap(t -> s.apply(t).publisher()));
        }



        @Override
        public <R> IO<Try<R, Throwable>> mapTry(Function<? super T, ? extends R> s) {
            return map(t -> Try.withCatch(() -> s.apply(t)));
        }

        @Override
        public <R, X extends Throwable> IO<Try<R, X>> mapTry(Function<? super T, ? extends R> s, final Class<? extends X>... classes) {
            return map(t -> Try.withCatch(() -> s.apply(t), classes));
        }

        @Override
        public void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {
            Spouts.from(fn).forEach(consumerElement, consumerError, onComplete);
        }

        @Override
        public Try<T, Throwable> run() {
            return Future.fromPublisher(fn)
                .get();
        }

        @Override
        public <R> R foldRun(Function<? super Try<T, Throwable>, ? extends R> transform) {
            return transform.apply(run());
        }

        @Override
        public Future<T> future() {
            return Future.fromPublisher(fn);
        }

        @Override
        public Publisher<T> publisher() {
            return fn;
        }

        @Override
        public final void subscribe(final Subscriber<? super T> sub) {
            fn.subscribe(sub);
        }

        @Override
        public ReactiveSeq<T> stream() {
            return Spouts.from(fn);
        }

        @Override
        public Try<T, Throwable> runAsync(Executor e) {
            return Try.fromPublisher(Future.of(() -> run(), e))
                .flatMap(Function.identity());
        }

        public String toString(){
         return "IO["+ run().toString() + "]";
        }

    }
}
