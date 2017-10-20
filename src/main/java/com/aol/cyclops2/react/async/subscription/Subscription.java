package com.aol.cyclops2.react.async.subscription;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import cyclops.reactive.ReactiveSeq;


import cyclops.async.adapters.Queue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subscription implements Continueable {
    private final Map<Queue, AtomicLong> limits = new HashMap<>();

    private final Map<Queue, AtomicBoolean> unlimited = new HashMap<>();
    private final Map<Queue, AtomicLong> count = new HashMap<>();
    private final List<Queue> queues = new LinkedList<>();

    private final AtomicBoolean closed = new AtomicBoolean(
                                                           false);

    private final AtomicLong timeLimitNanos = new AtomicLong(
                                                             -1);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#timeLimit()
     */
    @Override
    public long timeLimit() {
        return timeLimitNanos.get();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#registerSkip(long)
     */
    @Override
    public void registerSkip(final long skip) {
        if (queues.size() > 0)
            limits.get(currentQueue())
                  .addAndGet(skip);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#registerTimeLimit(long)
     */
    @Override
    public void registerTimeLimit(final long nanos) {
        if (timeLimitNanos.get() == -1 || timeLimitNanos.get() > nanos)
            timeLimitNanos.set(nanos);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#registerLimit(long)
     */
    @Override
    public void registerLimit(final long limit) {

        if (queues.size() > 0) {
            if (unlimited.get(currentQueue())
                         .get())
                limits.get(currentQueue())
                      .set(0);

            limits.get(currentQueue())
                  .addAndGet(limit);
            unlimited.get(currentQueue())
                     .set(false);

            queues.stream()
                  .forEach(this::closeQueueIfFinishedStateless);

        }
    }

    private Queue currentQueue() {
        return queues.get(queues.size() - 1);
    }

    @Override
    public void addQueue(final Queue q) {

        queues.add(q);
        limits.put(q, new AtomicLong(
                                     Long.MAX_VALUE - 1));
        unlimited.put(q, new AtomicBoolean(
                                           true));
        count.put(q, new AtomicLong(
                                    0l));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#closeQueueIfFinished(cyclops2.async.Queue)
     */
    @Override
    public void closeQueueIfFinished(final Queue queue) {

        closeQueueIfFinished(queue, AtomicLong::incrementAndGet);

    }

    private void closeQueueIfFinished(final Queue queue, final Function<AtomicLong, Long> fn) {

        if (queues.size() == 0)
            return;

        final long queueCount = fn.apply(count.get(queue));
        final long limit = valuesToRight(queue).stream()
                                               .reduce((acc, next) -> Math.min(acc, next))
                                               .get();

        if (queueCount >= limit) { //last entry - close THIS queue only!

            queue.closeAndClear();
            closed.set(true);
        }

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#closeQueueIfFinishedStateless(cyclops2.async.Queue)
     */
    @Override
    public void closeQueueIfFinishedStateless(final Queue queue) {

        closeQueueIfFinished(queue, AtomicLong::get);

    }

    private List<Long> valuesToRight(final Queue queue) {
        return ReactiveSeq.fromStream(queues.stream())
                  .splitAt(findQueue(queue))._2().map(limits::get)
                                               .map(AtomicLong::get)
                                               .collect(Collectors.toList());

    }

    private ReactiveSeq<Queue> queuesToLeft(final Queue queue) {
        return ReactiveSeq.fromStream(queues.stream())
                  .splitAt(findQueue(queue))._1();

    }

    private int findQueue(final Queue queue) {
        for (int i = 0; i < queues.size(); i++) {
            if (queues.get(i) == queue)
                return i;
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#closeAll(cyclops2.async.Queue)
     */
    @Override
    public void closeAll(final Queue queue) {

        closed.set(true);
        if (queue != null) {
            queue.closeAndClear();
            queuesToLeft(queue).forEach(Queue::closeAndClear);
        }

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#closeAll()
     */
    @Override
    public void closeAll() {

        closed.set(true);

        queues.stream()
              .forEach(Queue::closeAndClear);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.async.subscription.Continueable#closed()
     */
    @Override
    public boolean closed() {
        return closed.get();
    }
}
/**
reactiveStream.map().iterator().limit(4).flatMap(..).limit(2).map(..).limit(8)
subscription

reactiveStream no limit
	q1:limit (4)
	q2:limit (2)
	q3:limit (8)
	**/
