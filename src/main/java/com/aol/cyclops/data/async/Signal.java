package com.aol.cyclops.data.async;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import lombok.Getter;

/**
 * Datastructure that accepts a Stream of data and outputs a Stream of changes
 * 
 * <pre>
 * E.g. Stream.of(5,5,5,5,5,5,5,6,1,2,3,5,5,5,5) 
 * Results in Stream.of(5,6,1,2,3,5)
 * </pre>
 * @author johnmcclean
 *
 * @param <T> Data type of signal
 */
public class Signal<T> {

    private final AtomicReference<T> discreteState = new AtomicReference<>(
                                                                           null);

    @Getter
    private final Adapter<T> continuous;
    @Getter
    private final Adapter<T> discrete;

    /**
     * 
     * Construct a new Signal
     * 
     * @param continuous Adapter to handle the continuous flow (not only different values)
     * @param discrete  Adapter to handle the discrete (changed) flow
     */
    public Signal(final Adapter<T> continuous, final Adapter<T> discrete) {

        this.continuous = continuous;
        this.discrete = discrete;
    }

    /**
     * @return Signal backed by a queue
     */
    public static <T> Signal<T> queueBackedSignal() {
        return new Signal<T>(
                             new Queue<T>(
                                          new LinkedBlockingQueue<T>(), null),
                             new Queue<T>(
                                          new LinkedBlockingQueue<T>(), null));
    }

    /**
     * @return Signal backed by a topic
     */
    public static <T> Signal<T> topicBackedSignal() {
        return new Signal(
                          new Topic<>(), new Topic<>());
    }

    /**
     * @param stream Populate this Signal from a Stream
     */
    public void fromStream(final Stream<T> stream) {
        stream.forEach(next -> set(next));
    }

    /**
     * Set the current value of this signal
     * 
     * @param newValue Replacement value
     * @return newValue
     */
    public T set(final T newValue) {
        continuous.offer(newValue);

        setDiscreteIfDiff(newValue);
        return newValue;
    }

    private void setDiscreteIfDiff(final T newValue) {
        T oldVal = discreteState.get();
        while (!discreteState.compareAndSet(oldVal, newValue)) {
            oldVal = discreteState.get();
        }

        if (!Objects.equals(oldVal, newValue))
            discrete.offer(newValue);
    }

    /**
     * Close this Signal
     * 
     * 
     */
    public void close() {

        continuous.close();
        discrete.close();
    }

}
