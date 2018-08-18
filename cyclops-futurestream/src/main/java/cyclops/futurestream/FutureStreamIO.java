package cyclops.futurestream;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Seq;
import cyclops.reactive.IO;
import cyclops.reactive.Managed;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FutureStreamIO<T> implements IO<T> {

    private final FutureStream<T> flowable;

    public static <T, X extends Throwable> IO<Try<T, X>> withCatch(Try.CheckedSupplier<T, X> cf, Class<? extends X>... classes) {
        return of(() -> Try.withCatch(cf, classes));
    }
    public static <T> IO<T> of(FutureStream<T> flowable){
        return new FutureStreamIO<>(flowable);
    }

    public static <T> IO<T> of(T value){
        return new FutureStreamIO<>(LazyReact.sequentialBuilder().of(value));
    }

    public static <T> IO<T> of(Supplier<? extends T> value){
        return new FutureStreamIO<T>(FutureStream.narrow(LazyReact.sequentialBuilder().ofAsync(value)));
    }
    public static <T> IO<T> of(Supplier<? extends T> value, Executor executor){
        return new FutureStreamIO<T>(FutureStream.narrow(new LazyReact(executor).ofAsync(value)));
    }
    public static <T> IO<T> of(Executor executor, Supplier<T>... values){
        return new FutureStreamIO<T>(FutureStream.narrow(new LazyReact(values.length,executor).ofAsync(values)));
    }
    public static <T> IO<T> of(Future<T> flowable){
        return new FutureStreamIO<>(FutureStream.builder().from(flowable.getFuture()));
    }

    @Override
    public <B, R> IO<R> par(IO<B> that, BiFunction<? super T, ? super B, ? extends R> fn) {
        return of(flowable.zip(fn,that));
    }


    @Override
    public <R> IO<R> map(Function<? super T, ? extends R> s) {
        return of(flowable.map(s));
    }

    @Override
    public <R> IO<R> flatMap(Function<? super T, IO<? extends R>> s) {
        return of(flowable.mergeMap(s));
    }


    @Override
    public <R extends AutoCloseable> IO<R> bracket(Function<? super T, ? extends R> fn) {
        Managed<R> m = FutureStreamManaged.of(map(fn));
        return m.io();
    }
    @Override
    public <R> IO<R> bracket(Function<? super T, ? extends R> fn, Consumer<R> consumer) {
        Managed<R> m = FutureStreamManaged.of(map(fn),consumer);
        return m.io();
    }


    @Override
    public IO<T> ensuring(Consumer<T> action){
        return FutureStreamManaged.of(this,action).io();
    }
    @Override
    public void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        flowable.forEach(consumerElement,consumerError,onComplete);
    }

    @Override
    public Future<T> future() {
        return Future.fromPublisher(flowable);
    }

    @Override
    public Publisher<T> publisher() {
        return flowable;
    }

    @Override
    public ReactiveSeq<T> stream() {
        return flowable;
    }

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static abstract class FutureStreamManaged<T> extends Managed<T> {



        public static <T> Managed<T> managed(T acq, Consumer<T> cleanup){
            return of(FutureStreamIO.of(()->acq),cleanup);
        }
        public static <T extends AutoCloseable> Managed<T> managed(T acq){
            return of(FutureStreamIO.of(()->acq), ExceptionSoftener.softenConsumer(c->c.close()));
        }
        public static <T> Managed<T> of(Publisher<T> acq, Consumer<T> cleanup){
            return of(IO.fromPublisher(acq),cleanup);
        }
        public static <T> Managed<T> of(Supplier<? extends T> acq, Consumer<T> cleanup){
            return of(FutureStreamIO.of(acq),cleanup);
        }
        public static <T extends AutoCloseable> Managed<T> of(Supplier<? extends T> acq){
            return of(FutureStreamIO.of(acq),ExceptionSoftener.softenConsumer(c->c.close()));
        }
        public static <T extends AutoCloseable> Managed<T> of(Publisher<T> acq){
            return of(IO.fromPublisher(acq),ExceptionSoftener.softenConsumer(c->c.close()));
        }

        public static <T> Managed<T> of(IO<T> acquire, Consumer<T> cleanup){

            return new FutureStreamManaged<T>(){
                public  <R> IO<R> apply(Function<? super T,? extends IO<R>> fn){
                    IO<R> y = IO.Comprehensions.forEach(acquire, t1 -> {
                        IO<? extends Try<? extends IO<R>, Throwable>> res1 = FutureStreamIO.withCatch(() -> fn.apply(t1), Throwable.class);
                        return res1;
                    }, t2 -> {

                        Try<? extends IO<R>, Throwable> tr = t2._2();
                        IO<R> res = tr.fold(r -> r, e -> FutureStreamIO.of(Future.ofError(e)));
                        cleanup.accept(t2._1());

                        return res;
                    });
                    return y;
                }
            };
        }

        public static <T extends AutoCloseable> Managed<T> of(IO<T> acquire){
            return of(acquire,ExceptionSoftener.softenConsumer(c->c.close()));
        }

        public static  <T> cyclops.reactive.Managed<Seq<T>> sequence(Iterable<? extends Managed<T>> all) {

            cyclops.reactive.Managed<Seq<T>> acc =null;
            for(Managed<T> n : all){
                if(acc==null)
                    acc=n.map(Seq::of);
                else
                    acc = acc.zip(n,(a,b)->a.append(b));

            }
            return acc;

        }


        public <R> Managed<R> map(Function<? super T, ? extends R> mapper){
            return of(apply(mapper.andThen(IO::of)),__->{});
        }
        public  <R> Managed<R> flatMap(Function<? super T, cyclops.reactive.Managed<R>> f){

            FutureStreamManaged<T> m = this;
            return new FutureStreamManaged<R>(){

                @Override
                public <R1> IO<R1> apply(Function<? super R, ? extends IO<R1>> fn) {
                    IO<R1> x = m.apply(r1 -> {
                        IO<R1> r = f.apply(r1).apply(r2 -> fn.apply(r2));
                        return r;
                    });
                    return x;
                }
            };

        }



    }


}
