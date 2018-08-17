package cyclops.reactive;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Seq;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;
import org.reactivestreams.Publisher;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static cyclops.reactive.FlowableIO.*;

public abstract class FlowableManaged<T> extends Managed<T> {

    public static <T> Managed<T> managed(T just,Consumer<T> cleanup){
        return Managed.of(just(just),cleanup);
    }
    public static <T> Managed<T> of(Supplier<? extends T> s, Consumer<T> cleanup){
        return Managed.of(FlowableIO.of(s),cleanup);
    }

    public static <T extends AutoCloseable> Managed<T> managed(T just){
        return Managed.of(just(just));
    }
    public static <T extends AutoCloseable> Managed<T> of(Supplier<? extends T> s){
        return Managed.of(FlowableIO.of(s));
    }
    public static <T extends AutoCloseable> Managed<T> of(Publisher<T> acq){
        return Managed.of(FlowableIO.fromPublisher(acq), ExceptionSoftener.softenConsumer(c->c.close()));
    }
    public final static <T> Semigroup<Managed<T>> semigroup(Semigroup<T> s){
        return (a,b) -> a.flatMap(t1 -> b.map(t2 -> s.apply(t1, t2)));
    }
    public final static <T> Monoid<Managed<T>> monoid(Monoid<T> s){
        return Monoid.of(managed(s.zero(),__->{}),semigroup(s));
    }

    public static <T> Managed<T> of(IO<T> acquire, Consumer<T> cleanup){

        return new FlowableManaged<T>(){
            public  <R> IO<R> apply(Function<? super T,? extends IO<R>> fn){
                IO<R> y = IO.Comprehensions.forEach(acquire, t1 -> {
                    IO<? extends Try<? extends IO<R>, Throwable>> res1 = FlowableIO.withCatch(() -> fn.apply(t1), Throwable.class);
                    return res1;
                }, t2 -> {

                    Try<? extends IO<R>, Throwable> tr = t2._2();
                    IO<R> res = tr.fold(r -> r, e -> FlowableIO.of(Future.ofError(e)));
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

    public static  <T> Managed<Seq<T>> sequence(Iterable<? extends Managed<T>> all) {

        Managed<Seq<T>> acc =null;
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

        FlowableManaged<T> m = this;
        return new IO.SyncIO.SyncManaged<R>(){

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
