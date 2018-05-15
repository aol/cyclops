package cyclops.reactive;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IOMonad<W,T> implements IO<T> {
    private final Monad<W> monad;
    private final Higher<W,T> pub;
    private final ToPublsher<W> toPublsher;
    private final FromPublsher<W> fromPublsher;
    public interface ToPublsher<W>{
        <T> Function<Higher<W,T>,Publisher<T>> convert();
    }
    public interface FromPublsher<W>{
        <T> Function<? super Publisher<? extends T>,? extends Higher<W,T>> convert();
    }

    public static <W,T> IOMonad<W,T> of(Monad<W> monad, Higher<W,T> hkt, ToPublsher<W> to, FromPublsher<W> from){
        return new IOMonad<W,T>(monad,hkt,to,from);
    }

    @Override
    public <R> IO<R> map(Function<? super T, ? extends R> s) {
        return new IOMonad<W,R>(monad,monad.map(s,pub),toPublsher,fromPublsher);
    }

    @Override
    public <R> IO<R> flatMap(Function<? super T, IO<? extends R>> s) {
        return new IOMonad<W,R>(monad,monad.flatMap(s.andThen(io->fromPublsher.<R>convert().apply(io.publisher())),pub),toPublsher,fromPublsher);
    }



    @Override
    public Publisher<T> publisher() {
        return toPublsher.<T>convert().apply(pub);
    }


}
