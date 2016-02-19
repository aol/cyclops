package com.aol.cyclops.data.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
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

        public StreamOfContinuations(Queue<?> queue) {
            this.queue = queue;
        }

        @Override
        public void addContinuation(Continuation c) {
            this.continuation.add(c);

        }

        @Override
        public void handleContinuation() {

            continuation = Seq.seq(continuation).<Optional<Continuation>> map(c -> {
                try {
                    return Optional.of(c.proceed());
                } catch (ClosedQueueException e) {

                    return Optional.empty();
                }

            }).filter(Optional::isPresent).map(Optional::get).toList();

            if (continuation.size() == 0) {

                queue.close();
                throw new ClosedQueueException();
            }
        }

    }
   
   static class SingleContinuation implements ContinuationStrategy {
       private final Queue<?> queue;
       private  Continuation continuation= null;
       
       public SingleContinuation(Queue<?> queue){
           this.queue = queue;
       }
       
       @Override
       public void addContinuation(Continuation c) {
           continuation = c;

       }

       @Override
       public void handleContinuation(){
           
               continuation = continuation.proceed();
               
       }

   }

@AllArgsConstructor
static class QueueToBlockingQueueWrapper implements BlockingQueue{
        
        java.util.Queue queue;

        public  void forEach(Consumer action) {
            queue.forEach(action);
        }

        public int hashCode() {
            return queue.hashCode();
        }

        public Object remove() {
            return queue.remove();
        }

        public boolean equals(Object obj) {
            return queue.equals(obj);
        }

        public Object element() {
            return queue.element();
        }

        public void clear() {
            queue.clear();
        }

        public boolean containsAll(Collection c) {
            return queue.containsAll(c);
        }

        public boolean add(Object e) {
            return queue.add(e);
        }

        public boolean removeAll(Collection c) {
            return queue.removeAll(c);
        }

        public boolean offer(Object e) {
            return queue.offer(e);
        }

        public boolean retainAll(Collection c) {
            return queue.retainAll(c);
        }

        public Object poll() {
            return queue.poll();
        }

        public Object peek() {
            return queue.peek();
        }

        public String toString() {
            return queue.toString();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }

        public int size() {
            return queue.size();
        }

        public boolean contains(Object o) {
            return queue.contains(o);
        }

        public boolean remove(Object o) {
            return queue.remove(o);
        }

        public boolean removeIf(Predicate filter) {
            return queue.removeIf(filter);
        }

        public boolean addAll(Collection c) {
            return queue.addAll(c);
        }

        public Object[] toArray() {
            return queue.toArray();
        }

        public Object[] toArray(Object[] a) {
            return queue.toArray(a);
        }

        public Iterator iterator() {
            return queue.iterator();
        }

        public  Stream stream() {
            return queue.stream();
        }

        public  Stream parallelStream() {
            return queue.parallelStream();
        }

        public Spliterator spliterator() {
            return queue.spliterator();
        }

        @Override
        public void put(Object e) throws InterruptedException {
            offer(e);
            
        }

        @Override
        public boolean offer(Object e, long timeout, TimeUnit unit)
                throws InterruptedException {
            return offer(e);
        }

        @Override
        public Object take() throws InterruptedException {
            
            return poll();
        }

        @Override
        public Object poll(long timeout, TimeUnit unit)
                throws InterruptedException {
            
            return poll();
        }

        @Override
        public int remainingCapacity() {
            
            return 0;
        }

        @Override
        public int drainTo(Collection c) {
            
            return 0;
        }

        @Override
        public int drainTo(Collection c, int maxElements) {
            
            return 0;
        }
        
    }
   static class ClosingSpliterator<T> implements Spliterator<T> {
       private long estimate;
       final Supplier<T> s;
       private final Continueable subscription;
       private final Queue queue;

       public ClosingSpliterator(long estimate,Supplier<T> s,
               Continueable subscription,
               Queue queue) {
           this.estimate = estimate;
           this.s = s;
           this.subscription = subscription;
           this.queue = queue;
           this.subscription.addQueue(queue);
       }
       public ClosingSpliterator(long estimate,Supplier<T> s,
               Continueable subscription) {
           this.estimate = estimate;
           this.s = s;
           this.subscription = subscription;
           this.queue = null;
       }

       @Override
       public long estimateSize() {
           return estimate;
       }

       @Override
       public int characteristics() {
           return IMMUTABLE;
       }
       
   

       @Override
       public boolean tryAdvance(Consumer<? super T> action) {
            Objects.requireNonNull(action);
           
               
           try{ 
               
               action.accept(s.get());
               subscription.closeQueueIfFinished(queue);
            return true;
           }catch(ClosedQueueException e){
           
               if(e.isDataPresent())
                   action.accept((T)e.getCurrentData());
               return false;
           }catch(Exception e){
               
               return false;
           }finally {
               
           }
           
       }

       @Override
       public Spliterator<T> trySplit() {
           
           return new ClosingSpliterator(estimate >>>= 1, s,subscription,queue);
       }

      
   }
}
