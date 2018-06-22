package cyclops.reactive;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.future;
import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Future;
import cyclops.instances.control.FutureInstances;
import cyclops.instances.reactive.PublisherInstances;
import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IOMonad<W,T> implements IO<T> {
    private final Monad<W> monad;
    private final Higher<W,T> pub;
    private final ToPublsher<W> toPublsher;
    private final FromPublsher<W> fromPublsher;

    public static Converter<reactiveSeq> reactiveSeqConverter = new Converter<reactiveSeq>() {
        @Override
        public <T> Function<? super Publisher<? extends T>, ? extends Higher<reactiveSeq, T>> fromPublisherFn() {
            return Spouts::from;
        }

        @Override
        public Monad<reactiveSeq> monad() {
            return PublisherInstances.monad();
        }

        @Override
        public <T> Function<Higher<reactiveSeq, T>, Publisher<T>> toPublisherFn() {
            return ReactiveSeq::narrowK;
        }
    };
    public static Converter<future> futureConverter = new Converter<future>() {
        @Override
        public <T> Function<? super Publisher<? extends T>, ? extends Higher<future, T>> fromPublisherFn() {
            return p->Future.fromPublisher(p);
        }

        @Override
        public Monad<future> monad() {
            return FutureInstances.monad();
        }

        @Override
        public <T> Function<Higher<future, T>, Publisher<T>> toPublisherFn() {
            return Future::narrowK;
        }
    };
   public interface Converter<W> extends ToPublsher<W>, FromPublsher<W>{
        Monad<W> monad();
    }
    public interface ToPublsher<W>{
        <T> Function<Higher<W,T>,Publisher<T>> toPublisherFn();
    }
    public interface FromPublsher<W>{
        <T> Function<? super Publisher<? extends T>,? extends Higher<W,T>> fromPublisherFn();
    }

    public static <W,T> IOMonad<W,T> ioMonad(Monad<W> monad, Higher<W,T> hkt, ToPublsher<W> to, FromPublsher<W> from){
        return new IOMonad<W,T>(monad,hkt,to,from);
    }
    public static <W,T> IOMonad<W,T> ioMonad(Converter<W>  converter,Higher<W,T> hkt){
        return new IOMonad<W,T>(converter.monad(),hkt,converter,converter);
    }

    @Override
    public <R> IO<R> map(Function<? super T, ? extends R> s) {
        return new IOMonad<W,R>(monad,monad.map(s,pub),toPublsher,fromPublsher);
    }

    @Override
    public <R> IO<R> flatMap(Function<? super T, IO<? extends R>> s) {
        return new IOMonad<W,R>(monad,monad.flatMap(s.andThen(io->fromPublsher.<R>fromPublisherFn().apply(io.publisher())),pub),toPublsher,fromPublsher);
    }



    @Override
    public Publisher<T> publisher() {
        return toPublsher.<T>toPublisherFn().apply(pub);
    }

    public String toString(){
        return "IO["+ run().toString() + "]";
    }
}
