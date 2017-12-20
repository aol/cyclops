package com.oath.cyclops.react.threads;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oath.cyclops.internal.react.stream.ReactBuilder;
import com.oath.cyclops.util.ExceptionSoftener;

/**
 * Maintain a pool of x-react builders
 * x-react builders (SimpleReact,LazyReact) can be extracted and returned to the pool externally
 * or Streams creating function can be supplied to the ReactPool which will select an x-react builder, run the stream and return
 *  the x-react builder to the pool
 *
 * @author johnmcclean
 *
 * @param <REACTOR> x-react builder type (SimpleReact, EagerReact, LazyReact)
 */
public class ReactPool<REACTOR extends ReactBuilder> {

    private final BlockingQueue<REACTOR> queue;

    private final Supplier<REACTOR> supplier;

    private ReactPool(final int size) {
        queue = new LinkedBlockingQueue<REACTOR>(
                                                 size);
        supplier = null;
    }

    private ReactPool() {
        queue = new LinkedBlockingQueue<REACTOR>();
        supplier = null;
    }

    private ReactPool(final BlockingQueue<REACTOR> queue) {
        this.queue = queue;
        supplier = null;
    }

    private ReactPool(final Supplier<REACTOR> supplier) {
        this.queue = new LinkedBlockingQueue<REACTOR>();
        this.supplier = supplier;
    }

    /**
     * If all REACTORs are in use calling react will block.
     *
     * @param reactors Create a bounded pool of the specified REACTORs
     * @return ReactPool
     */
    public static <REACTOR extends ReactBuilder> ReactPool<REACTOR> boundedPool(final Collection<REACTOR> reactors) {
        final ReactPool<REACTOR> r = new ReactPool<>(
                                                     reactors.size());
        reactors.forEach(r::populate);
        return r;
    }

    /**
     * If all REACTORs are in use calling react will block.
     *
     * @param reactors Create a unbounded pool of the specified REACTORs, additional REACTORs can be added via populate
     * @return ReactPool
     */
    public static <REACTOR extends ReactBuilder> ReactPool<REACTOR> unboundedPool(final Collection<REACTOR> reactors) {
        final ReactPool<REACTOR> r = new ReactPool<>();
        reactors.forEach(r::populate);
        return r;
    }

    /**
     * If all REACTORs are in use calling react will create a new REACTOR to handle the extra demand.
     *
     * Generate an elastic pool of REACTORs
     *
     * @param supplier To create new REACTORs
     * @return ReactPool
     */
    public static <REACTOR extends ReactBuilder> ReactPool<REACTOR> elasticPool(final Supplier<REACTOR> supplier) {
        return new ReactPool<>(
                               supplier);

    }

    /**
     * @return Synchronous pool requires consumers and producers of the ReactPool to be in sync
     */
    public static <REACTOR extends ReactBuilder> ReactPool<REACTOR> syncrhonousPool() {

        final ReactPool<REACTOR> r = new ReactPool<>(
                                                     new SynchronousQueue<>());

        return r;
    }

    /**
     * @param next REACTOR to add to the Pool
     */
    public void populate(final REACTOR next) {

        try {
            queue.put(next);
        } catch (final InterruptedException e) {
            Thread.currentThread()
                  .interrupt();
            throw ExceptionSoftener.throwSoftenedException(e);
        }

    }

    /**
     * @param fn Function that operates on a REACTOR - typically will build an execute a Stream using that REACTOR.
     * 				This method will extract and return the REACTOR to the pool.
     * @return typically will return the result of Stream execution (result of fn.applyHKT(reactor))
     */
    public <T> T react(final Function<? super REACTOR, ? extends T> fn) {
        REACTOR reactor = null;

        try {
            reactor = nextReactor();
            return fn.apply(reactor);
        } finally {

            if (reactor != null)
                queue.offer(reactor);

        }
    }

    /**
     * @return Next available REACTOR from Pool
     */
    public REACTOR nextReactor() {
        REACTOR reactor = queue.poll();
        try {

            if (reactor == null) {
                if (isElastic()) {
                    reactor = supplier.get();

                } else
                    reactor = queue.take();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread()
                  .interrupt();
            throw ExceptionSoftener.throwSoftenedException(e);

        }
        return reactor;
    }

    private boolean isElastic() {
        return supplier != null;
    }

}
