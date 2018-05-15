package cyclops.reactive;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;
import org.reactivestreams.Publisher;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static cyclops.reactive.FluxIO.just;

public final class FluxManaged {

    public static <T> Managed<T> managed(T just,Consumer<T> cleanup){
        return Managed.of(just(just),cleanup);
    }
    public static <T> Managed<T> of(Supplier<? extends T> s, Consumer<T> cleanup){
        return Managed.of(FluxIO.of(s),cleanup);
    }

    public static <T extends AutoCloseable> Managed<T> managed(T just){
        return Managed.of(just(just));
    }
    public static <T extends AutoCloseable> Managed<T> of(Supplier<? extends T> s){
        return Managed.of(FluxIO.of(s));
    }
    public static <T extends AutoCloseable> Managed<T> of(Publisher<T> acq){
        return Managed.of(FluxIO.fromPublisher(acq), ExceptionSoftener.softenConsumer(c->c.close()));
    }
    public final static <T> Semigroup<Managed<T>> semigroup(Semigroup<T> s){
        return (a,b) -> a.flatMap(t1 -> b.map(t2 -> s.apply(t1, t2)));
    }
    public final static <T> Monoid<Managed<T>> monoid(Monoid<T> s){
        return Monoid.of(managed(s.zero(),__->{}),semigroup(s));
    }
}
