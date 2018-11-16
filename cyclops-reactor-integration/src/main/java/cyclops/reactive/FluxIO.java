package cyclops.reactive;

import cyclops.companion.reactor.Fluxs;
import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Seq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FluxIO<T> implements IO<T> {
    private final Flux<T> flowable;

    public static <T> IO<T> of(Future<T> f){
        return of(Flux.from(f));
    }
    public static <T> IO<T> of(Flux<T> flowable){
        return new FluxIO<>(flowable);
    }
    public static <T> IO<T> just(T s){
        return new FluxIO<T>(Flux.just(s));
    }

    public static <T> IO<T> of(Supplier<? extends T> s){
        return new FluxIO<T>(Fluxs.generate(()->s.get()));
    }

    public static <T> IO<T> of(Supplier<? extends T> s, Scheduler ex){
        Flux<T> x = Fluxs.generate(() -> s.get());
        x = x.subscribeOn(ex);
        return new FluxIO<T>(x);
    }


    public static <T> IO<T> fromPublisher(Publisher<T> p){
        return new FluxIO<T>(Flux.from(p));
    }

    public static <T,X extends Throwable> IO<Try<T,X>> withCatch(Try.CheckedSupplier<T, X> cf, Class<? extends X>... classes){
        return of(()-> Try.withCatch(cf,classes));
    }


    public static <T1,T2,R> IO<R> merge(Publisher<T1> p1, Publisher<T2> p2, BiFunction<? super T1, ? super T2, ? extends R> fn2){
        Flux<T1> s1 = Flux.from(p1);
        Flux<T2> s2 = Flux.from(p2);
        return fromPublisher(s1.zipWith(s2,(a,b)->fn2.apply(a,b)));
    }

    @Override
    public <B, R> IO<R> par(IO<B> that, BiFunction<? super T, ? super B, ? extends R> fn) {
        return IO.fromPublisher(flowable.zipWith(that,fn));
    }

    @Override
    public IO<T> race(IO<T> that) {
        return fromPublisher(Flux.first(Seq.of(publisher(), that.publisher())));
    }

    @Override
    public <R> IO<R> map(Function<? super T, ? extends R> s) {
        return of(flowable.map(s));
    }

    @Override
    public <R> IO<R> flatMap(Function<? super T, IO<? extends R>> s) {
        return of(flowable.flatMap(s));
    }

    @Override
    public <R extends AutoCloseable> IO<R> bracket(Function<? super T, ? extends R> fn) {
        Managed<R> m = FluxManaged.of(map(fn));
        return m.io();
    }

    @Override
    public <R> IO<R> bracket(Function<? super T, ? extends R> fn, Consumer<R> consumer) {
        Managed<R> m = FluxManaged.of(map(fn),consumer);
        return m.io();
    }
    @Override
    public <R extends AutoCloseable,R1> Managed.Tupled<R,R1> bracketWith(Function<? super T, ? extends R> fn, Function<? super R, ? extends R1> with) {
        Managed.Tupled<? extends R, ? extends R1> x = FluxManaged.of(map(fn)).with(with);
        return (Managed.Tupled<R, R1> )x;
    }
    @Override
    public void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        flowable.subscribe(consumerElement,consumerError,onComplete);
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
        return Spouts.from(flowable);
    }

}
