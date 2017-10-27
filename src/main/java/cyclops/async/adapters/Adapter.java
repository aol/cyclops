package cyclops.async.adapters;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import com.oath.cyclops.types.futurestream.Continuation;
import cyclops.async.LazyReact;
import cyclops.reactive.FutureStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.control.Either;
import com.oath.cyclops.react.async.subscription.Continueable;

/**
 *
 * Interface for an Adapter that inputs data from 1 or more input Streams and sends it to 1 or more emitted Streams
 *
 * @author johnmcclean
 *
 * @param <T> Data type
 */
public interface Adapter<T> {
    public void addContinuation(Continuation cont);
    /**
     * @return A structural Pattern Matcher for this Adapter that allows matching on  Queue / Topic types
     */
    default Either<Queue<T>, Topic<T>> matches() {
        return visit(q -> Either.left(q), topic -> Either.right(topic));
    }

    /**
     * Conditionally execute one of the supplied function depending on whether or not this Adapter is a Queue or a Topic
     *
     * @param caseQueue Function to execute if this Adapter is a Queue
     * @param caseTopic Function to execute if this Adapter is a Topic
     * @return Value returned from executed funciton
     */
    <R> R visit(Function<? super Queue<T>, ? extends R> caseQueue, Function<? super Topic<T>, ? extends R> caseTopic);

    /**
     * Offer a single datapoint to this adapter
     *
     * @param data data to add
     * @return self
     */
    public boolean offer(T data);

    /**
     * @param stream Input data from provided Stream
     */
    public boolean fromStream(Stream<T> stream);

    /**
     * Generate a LazyFutureStream from the data that is passed to this Adapter using the supplied LazyReact futureStream builder
     *
     * @param reactor FutureStream builder used to determine parallelism and other settings for the created FutureStream
     * @return LazyFutureStream of data from this Adapter
     */
    default FutureStream<T> futureStream(final LazyReact reactor) {
        return reactor.fromStream(stream());
    }

    /**
     * @return Sequential future reactiveStream generated from this adapter {@see  Adapter#futureStream(LazyReact) }
     */
    default FutureStream<T> futureStream() {
        return new LazyReact().fromStream(stream());
    }

    /**
     * @return Stream of data
     */
    public ReactiveSeq<T> stream();

    /**
     * @return Stream of data
     */
    public ReactiveSeq<T> stream(Continueable s);

    /**
     * @return Stream of CompletableFutures that can be used as input into a SimpleReact concurrent dataflow
     */
    public ReactiveSeq<CompletableFuture<T>> streamCompletableFutures();

    /**
     * Close this adapter
     *
     * @return true if closed
     */
    public boolean close();
}
