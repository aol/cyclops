package com.oath.cyclops.async.adapters;

import com.oath.cyclops.matching.Sealed2;
import com.oath.cyclops.react.async.subscription.Continueable;
import com.oath.cyclops.types.futurestream.Continuation;
import cyclops.control.Either;
import cyclops.reactive.ReactiveSeq;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * Interface for an Adapter that inputs data from 1 or more input Streams and sends it to 1 or more emitted Streams
 *
 * @author johnmcclean
 *
 * @param <T> Data type
 */
public interface Adapter<T> extends Sealed2<Queue<T>,Topic<T>> {
    public void addContinuation(Continuation cont);
    /**
     * @return A structural Pattern Matcher for this Adapter that allows matching on  Queue / Topic types
     */
    default Either<Queue<T>, Topic<T>> matches() {
        return fold(q -> Either.left(q), topic -> Either.right(topic));
    }

    /**
     * Conditionally execute one of the supplied function depending on whether or not this Adapter is a Queue or a Topic
     *
     * @param caseQueue Function to execute if this Adapter is a Queue
     * @param caseTopic Function to execute if this Adapter is a Topic
     * @return Value returned from executed funciton
     */
    <R> R fold(Function<? super Queue<T>, ? extends R> caseQueue, Function<? super Topic<T>, ? extends R> caseTopic);

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
