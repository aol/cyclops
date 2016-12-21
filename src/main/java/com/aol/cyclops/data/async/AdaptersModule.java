package com.aol.cyclops.data.async;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.types.futurestream.Continuation;

import lombok.AllArgsConstructor;

public interface AdaptersModule {

    static class StreamOfContinuations implements ContinuationStrategy {
        private final Queue<?> queue;
        private List<Continuation> continuation = new ArrayList<>();

        public StreamOfContinuations(final Queue<?> queue) {
            this.queue = queue;
        }

        @Override
        public void addContinuation(final Continuation c) {
            continuation.add(c);

        }

        @Override
        public void handleContinuation() {

            continuation = Seq.seq(continuation)
                              .<Optional<Continuation>> map(c -> {
                                  try {
                                      return Optional.of(c.proceed());
                                  } catch (final ClosedQueueException e) {

                                      return Optional.empty();
                                  }

                              })
                              .filter(Optional::isPresent)
                              .map(Optional::get)
                              .toList();

            if (continuation.size() == 0) {

                queue.close();
                throw new ClosedQueueException();
            }
        }

    }

    static class SingleContinuation implements ContinuationStrategy {
        private final Queue<?> queue;
        private Continuation continuation = null;

        public SingleContinuation(final Queue<?> queue) {
            this.queue = queue;
        }

        @Override
        public void addContinuation(final Continuation c) {
            continuation = c;

        }

        @Override
        public void handleContinuation() {

            continuation = continuation.proceed();

        }

    }

    @AllArgsConstructor
    static class QueueToBlockingQueueWrapper implements BlockingQueue {

        java.util.Queue queue;

        @Override
        public void forEach(final Consumer action) {
            queue.forEach(action);
        }

        @Override
        public int hashCode() {
            return queue.hashCode();
        }

        @Override
        public Object remove() {
            return queue.remove();
        }

        @Override
        public boolean equals(final Object obj) {
            return queue.equals(obj);
        }

        @Override
        public Object element() {
            return queue.element();
        }

        @Override
        public void clear() {
            queue.clear();
        }

        @Override
        public boolean containsAll(final Collection c) {
            return queue.containsAll(c);
        }

        @Override
        public boolean add(final Object e) {
            return queue.add(e);
        }

        @Override
        public boolean removeAll(final Collection c) {
            return queue.removeAll(c);
        }

        @Override
        public boolean offer(final Object e) {
            return queue.offer(e);
        }

        @Override
        public boolean retainAll(final Collection c) {
            return queue.retainAll(c);
        }

        @Override
        public Object poll() {
            return queue.poll();
        }

        @Override
        public Object peek() {
            return queue.peek();
        }

        @Override
        public String toString() {
            return queue.toString();
        }

        @Override
        public boolean isEmpty() {
            return queue.isEmpty();
        }

        @Override
        public int size() {
            return queue.size();
        }

        @Override
        public boolean contains(final Object o) {
            return queue.contains(o);
        }

        @Override
        public boolean remove(final Object o) {
            return queue.remove(o);
        }

        @Override
        public boolean removeIf(final Predicate filter) {
            return queue.removeIf(filter);
        }

        @Override
        public boolean addAll(final Collection c) {
            return queue.addAll(c);
        }

        @Override
        public Object[] toArray() {
            return queue.toArray();
        }

        @Override
        public Object[] toArray(final Object[] a) {
            return queue.toArray(a);
        }

        @Override
        public Iterator iterator() {
            return queue.iterator();
        }

        @Override
        public Stream stream() {
            return queue.stream();
        }

        @Override
        public Stream parallelStream() {
            return queue.parallelStream();
        }

        @Override
        public Spliterator spliterator() {
            return queue.spliterator();
        }

        @Override
        public void put(final Object e) throws InterruptedException {
            offer(e);

        }

        @Override
        public boolean offer(final Object e, final long timeout, final TimeUnit unit) throws InterruptedException {
            return offer(e);
        }

        @Override
        public Object take() throws InterruptedException {

            return poll();
        }

        @Override
        public Object poll(final long timeout, final TimeUnit unit) throws InterruptedException {

            return poll();
        }

        @Override
        public int remainingCapacity() {

            return 0;
        }

        @Override
        public int drainTo(final Collection c) {

            return 0;
        }

        @Override
        public int drainTo(final Collection c, final int maxElements) {

            return 0;
        }

    }

    static class ClosingSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements Spliterator<T> {
        private long estimate;
        final Supplier<T> s;
        private final Continueable subscription;
        private final Queue queue;
        

        public ClosingSpliterator(final long estimate, final Supplier<T> s, final Continueable subscription, final Queue queue) {
            super(estimate,IMMUTABLE);
            this.estimate = estimate;
            this.s = s;
            this.subscription = subscription;
            this.queue = queue;
            this.subscription.addQueue(queue);
            this.closed = new AtomicBoolean(false);
        }
        public ClosingSpliterator(final long estimate, final Supplier<T> s, final Continueable subscription, final Queue queue,AtomicBoolean closed) {
            super(estimate,IMMUTABLE);
            this.estimate = estimate;
            this.s = s;
            this.subscription = subscription;
            this.queue = queue;
            this.subscription.addQueue(queue);
            this.closed =closed;
            
        }

        public ClosingSpliterator(final long estimate, final Supplier<T> s, final Continueable subscription) {
            super(estimate,IMMUTABLE);
            this.estimate = estimate;
            this.s = s;
            this.subscription = subscription;
            this.queue = null;
            this.closed =  new AtomicBoolean(false);
            
        }

        @Override
        public long estimateSize() {
            return estimate;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE;
        }
       final AtomicBoolean closed ;

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
           // System.out.println("For each " + Thread.currentThread().getId());
            Spliterator.super.forEachRemaining(action);
        }

        @Override
        public boolean tryAdvance(final Consumer<? super T> action) {
            Objects.requireNonNull(action);

            try {
                if(closed.get()){
                   return false;
                }
                action.accept(s.get());
                subscription.closeQueueIfFinished(queue);
                return true;
            } catch (final ClosedQueueException e) {
                
                if (e.isDataPresent()) {
                    e.getCurrentData()
                     .forEach(action);
                }
                
                closed.set(true);
                return false;
            } catch (final Exception e) {
                closed.set(true);
                return false;
            } finally {

            }

        }

        @Override
        public Spliterator<T> trySplit() {
            
            return new ClosingSpliterator<T>(
                    estimate >>>= 1, s, subscription, queue,closed);
          
        }
        
        

    }
   
}
