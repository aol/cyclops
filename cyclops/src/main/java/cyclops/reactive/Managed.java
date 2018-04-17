package cyclops.reactive;

import com.oath.cyclops.hkt.DataWitness.managed;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/*
 * Resource management monad (see https://www.iravid.com/posts/resource-management.html)
 *
 * <pre>
 *     Managed.of(this::acquireResource)
               .map(r->r.use())
               .run()

   //Acquire and automatically close a resource (in this example the Resource is autoclosable)


   </pre>
 *
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Managed<T> implements Higher<managed,T>,To<Managed<T>>, Publisher<T>{

    private final IO<T> acquire;
    private final Consumer<T> cleanup;

    public static <T> Managed<T> managed(T acq, Consumer<T> cleanup){
        return of(IO.of(()->acq),cleanup);
    }
    public static <T extends AutoCloseable> Managed<T> managed(T acq){
        return of(IO.of(()->acq), ExceptionSoftener.softenConsumer(c->c.close()));
    }
    public static <T> Managed<T> of(Publisher<T> acq, Consumer<T> cleanup){
        return of(IO.fromPublisher(acq),cleanup);
    }
    public static <T> Managed<T> of(Supplier<T> acq, Consumer<T> cleanup){
        return of(IO.of(acq),cleanup);
    }
    public static <T extends AutoCloseable> Managed<T> of(Supplier<T> acq){
        return of(IO.of(acq),ExceptionSoftener.softenConsumer(c->c.close()));
    }
    public static <T extends AutoCloseable> Managed<T> of(Publisher<T> acq){
        return of(IO.fromPublisher(acq),ExceptionSoftener.softenConsumer(c->c.close()));
    }

    public static <T> Managed<T> of(IO<T> acquire, Consumer<T> cleanup){
        return new Managed<T>(acquire,cleanup);
    }
    public static <T extends AutoCloseable> Managed<T> of(IO<T> acquire){
        return new Managed<T>(acquire,ExceptionSoftener.softenConsumer(c->c.close()));
    }
    public static  <T> Managed<ReactiveSeq<T>> sequence(Iterable<? extends Managed<T>> all) {

        Managed<ReactiveSeq<T>> identity = Managed.of(ReactiveSeq.empty(),__->{});

        BiFunction<Managed<ReactiveSeq<T>>,Managed<T>,Managed<ReactiveSeq<T>>> combineToStream = (acc, next) ->acc.zip(next,(a, b)->a.append(b));

        BinaryOperator<Managed<ReactiveSeq<T>>> combineStreams = (a, b)-> a.zip(b,(z1, z2)->z1.appendStream(z2));

        return ReactiveSeq.fromIterable(all).reduce(identity,combineToStream,combineStreams);
    }
    public static <T,R> Managed<ReactiveSeq<R>> traverse(Iterable<Managed<T>> stream,Function<? super T,? extends R> fn) {
        ReactiveSeq<Managed<R>> s = ReactiveSeq.fromIterable(stream).map(h -> h.map(fn));
        return sequence(s);
    }

    public final  <T2,R> Managed<R> zip(Managed<T2> b, BiFunction<? super T,? super T2,? extends R> fn){
        return flatMap(t1 -> b.map(t2 -> fn.apply(t1, t2)));
    }
    public final <R> IO<R> apply(Function<? super T,? extends R> fn){
        IO<R> y = IO.Comprehensions.forEach(acquire, t1 -> {
             IO<Try<R, Throwable>> res1 = IO.withCatch(() -> fn.apply(t1), Throwable.class);
            return res1;
        }, t2 -> {
            cleanup.accept(t2._1());
            Try<R, Throwable> tr = t2._2();
            IO<R> res = tr.fold(r -> IO.of(() -> r), e -> IO.fromPublisher(Future.ofError(e)));
            return res;
        });
        return y;
    }

    public final void forEach(Consumer<? super T> onNext,Consumer<Throwable> errorHandler){
        apply(i->i).stream().forEach(onNext,errorHandler);

    }

    public final Try<T,Throwable> run() {
      return  apply(i -> i).run();
    }

    public final Future<T> future(){
        return acquire.future();
    }
    public final Try<T,Throwable> runAsync(Executor ex) {
        return  apply(i -> i).runAsync(ex);
    }
    public ReactiveSeq<T> stream(){
        return apply(i->i).stream();
    }
    public final <R> Managed<R> map(Function<? super T, ? extends R> mapper){
        return of(apply(mapper),__->{});
    }
    public final <R> Managed<R> flatMap(Function<? super T, Managed<R>> mapper){
        return of(IO.flatten(apply(mapper.andThen(mn -> mn.acquire))),__->{});
    }

    public final static <T> Semigroup<Managed<T>> semigroup(Semigroup<T> s){
        return (a,b) -> a.flatMap(t1 -> b.map(t2 -> s.apply(t1, t2)));
    }
    public final static <T> Monoid<Managed<T>> monoid(Monoid<T> s){
        return Monoid.of(managed(s.zero(),__->{}),semigroup(s));
    }


    @Override
    public void subscribe(Subscriber<? super T> s) {
        stream().subscribe(s);
    }
}
