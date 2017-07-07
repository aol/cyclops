package cyclops.async.adapters;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops2.react.async.subscription.Subscription;
import cyclops.async.QueueFactories;
import cyclops.collections.mutable.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.async.wait.DirectWaitStrategy;
import cyclops.async.wait.WaitStrategy;
import com.aol.cyclops2.internal.react.exceptions.SimpleReactProcessingException;
import com.aol.cyclops2.react.async.subscription.AlwaysContinue;
import com.aol.cyclops2.react.async.subscription.Continueable;
import com.aol.cyclops2.types.futurestream.Continuation;
import com.aol.cyclops2.util.ExceptionSoftener;
import com.aol.cyclops2.util.SimpleTimer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Wither;

/**
 * Inspired by scalaz-streams async.Queue (functionally similar, but wraps a JDK Queue - wait-free or Blocking)
 * 
 * A Queue that takes data from one or more input Streams and provides them toNested
 * one or more emitted Streams
 * 
 * Interface specifies a BlockingQueue, but non-BlockingQueues (such as ConcurrentLinkedQueue can be used
 * in conjunction with an implementation of the Continuation interface
 * @see QueueFactories#unboundedNonBlockingQueue() )
 * 
 * 
 * Example transfering data using a Queue between two streams
 * <pre>
 * {@code 
 *  Queue<String> transferQueue = QueueFactories.<String>boundedQueue(4)
                                                 .build();
        
        new LazyReact(Executors.newFixedThreadPool(4)).generate(()->"data")
                                                      .map(d->"emitted on " + Thread.currentThread().getId())
                                                      .peek(System.out::println)
                                                      .peek(d->transferQueue.offer(d))
                                                      .run();
        

        transferQueue.reactiveStream()
                  .map(e->"Consumed on " + Thread.currentThread().getId())
                  .futureOperations(Executors.newFixedThreadPool(1))
                  .forEach(System.out::println);
        
        
        
        
        while(true){
          //  System.out.println(inputQueue.size());
        }
 * 
 * 
 * }
 * </pre>
 * 
 * 
 * 
 * @author johnmcclean, thomas kountis
 *
 * @param <T>
 *            Type of data stored in Queue
 */
@Wither
@AllArgsConstructor
public class Queue<T> implements Adapter<T> {

    private final static PoisonPill POISON_PILL = new PoisonPill();
    private final static PoisonPill CLEAR_PILL = new PoisonPill();

    private volatile boolean open = true;
    private final AtomicInteger listeningStreams = new AtomicInteger();
    private final int timeout;
    private final TimeUnit timeUnit;

    private final long offerTimeout;
    private final TimeUnit offerTimeUnit;
    private final int maxPoisonPills;

    @Getter(AccessLevel.PACKAGE)
    private final BlockingQueue<T> queue;
    private final WaitStrategy<T> consumerWait;
    private final WaitStrategy<T> producerWait;
    @Getter
    @Setter
    private volatile Signal<Integer> sizeSignal;

    private volatile Continueable sub;
    private ContinuationStrategy continuationStrategy;
    private volatile boolean shuttingDown = false;

    /**
     * Construct a Queue backed by a LinkedBlockingQueue
     */
    public Queue() {
        this(new LinkedBlockingQueue<>());
    }

    /**
     * Construct an async.Queue backed by a JDK Queue from the provided QueueFactory
     * 
     * @param factory QueueFactory toNested extract JDK Queue from
     */
    public Queue(final QueueFactory<T> factory) {
        final Queue<T> q = factory.build();
        this.queue = q.queue;
        timeout = q.timeout;
        timeUnit = q.timeUnit;
        maxPoisonPills = q.maxPoisonPills;
        offerTimeout = q.offerTimeout;
        offerTimeUnit = q.offerTimeUnit;

        this.consumerWait = q.consumerWait;
        this.producerWait = q.producerWait;
    }

    Queue(final BlockingQueue<T> queue, final WaitStrategy<T> consumer, final WaitStrategy<T> producer) {
        this.queue = queue;
        timeout = -1;
        timeUnit = TimeUnit.MILLISECONDS;
        maxPoisonPills = 90000;
        offerTimeout = Integer.MAX_VALUE;
        offerTimeUnit = TimeUnit.DAYS;

        this.consumerWait = consumer;
        this.producerWait = producer;
    }

    /**
     * Queue accepts a BlockingQueue toNested make use of Blocking semantics
     *
     * 
     * @param queue
     *            BlockingQueue toNested back this Queue
     */
    public Queue(final BlockingQueue<T> queue) {
        this(queue, new DirectWaitStrategy<T>(), new DirectWaitStrategy<T>());
    }

    Queue(final BlockingQueue<T> queue, final Signal<Integer> sizeSignal) {
        this(queue, new DirectWaitStrategy<T>(), new DirectWaitStrategy<T>());
    }

    public Queue(final java.util.Queue<T> q, final WaitStrategy<T> consumer, final WaitStrategy<T> producer) {
        this(new AdaptersModule.QueueToBlockingQueueWrapper(
                                             q),
             consumer, producer);
    }

    public static <T> Queue<T> createMergeQueue() {
        final Queue<T> q = new Queue<>();
        q.continuationStrategy = new AdaptersModule.StreamOfContinuations(
                                                           q);
        return q;
    }

    /**
     * @return Sequential Infinite (until Queue is closed) Stream of data from
     *         this Queue
     * 
     */
    @Override
    public ReactiveSeq<T> stream() {
        listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
        return ReactiveSeq.fromStream(closingStream(this::get, new AlwaysContinue()));
    }
    /**
     * Return a standard (unextended) JDK Stream connected toNested this Queue
     * To disconnect cleanly close the queue
     * 
     * <pre>
     * {@code 
     *        use queue.reactiveStream().parallel() toNested convert toNested a parallel Stream
     *  }
     * </pre>
     * 
     * @param closeScalingFactor Scaling factor for Queue closed messages toNested propagate toNested connected parallel Streams.
     *              Scaling Factor may need toNested be high toNested reach all connect parallel threads.
     * 
     * @return Java 8 Stream connnected toNested this Queue
     */
    public Stream<T> jdkStream(int closeScalingFactor){
        int cores = Runtime.getRuntime().availableProcessors();
        String par = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
        int connected = par !=null ? Integer.valueOf(par) : cores;
        int update = 0;
        do{
            update = listeningStreams.get()+ connected*closeScalingFactor;
        }while(!listeningStreams.compareAndSet(listeningStreams.get(), update));
        
        return closingStream(this::get, new AlwaysContinue());
    }
    
    /**
     * Return a standard (unextended) JDK Stream connected toNested this Queue
     * To disconnect cleanly close the queue
     * 
     * <pre>
     * {@code 
     *        use queue.reactiveStream().parallel() toNested convert toNested a parallel Stream
     *  }
     * </pre>
     * @see Queue#jdkStream(int) for an alternative that sends more poision pills for use with parallel Streams.
     * 
     * @return Java 8 Stream connnected toNested this Queue
     */
    public Stream<T> jdkStream() {
       return jdkStream(2);
    }

    public Stream<T> jdkStream(final Continueable s){
        this.sub = s;
        listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
        return closingStream(this::get, s);
    }
    @Override
    public ReactiveSeq<T> stream(final Continueable s) {
        this.sub = s;
        listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
        return ReactiveSeq.fromStream(closingStream(this::get, s));
    }

    public ReactiveSeq<Collection<T>> streamBatchNoTimeout(final Continueable s, final Function<Supplier<T>, Supplier<Collection<T>>> batcher) {
        this.sub = s;
        listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
        return ReactiveSeq.fromStream(closingStreamBatch(batcher.apply(() -> ensureOpen(this.timeout, this.timeUnit)), s));
    }

    public ReactiveSeq<Collection<T>> streamBatch(final Continueable s,
            final Function<BiFunction<Long, TimeUnit, T>, Supplier<Collection<T>>> batcher) {
        this.sub = s;
        listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
        return ReactiveSeq.fromStream(closingStreamBatch(batcher.apply((timeout, timeUnit) -> ensureOpen(timeout, timeUnit)), s));
    }
    public ReactiveSeq<ListX<T>> streamGroupedByTime(long time, TimeUnit t){
        return streamGroupedBySizeAndTime(Integer.MAX_VALUE,time,t);

    }
    public boolean checkTime(long current, long start,long toRun){
        boolean result = current-start < toRun;

        return result;
    }
    public ReactiveSeq<ListX<T>> streamGroupedBySizeAndTime(int size, long time, TimeUnit t){
        long toRun = t.toNanos(time);
        return streamBatch(new Subscription(), source->{

            return ()->{
                List<T> result = new ArrayList<>();


                long start = System.nanoTime();
              try {
                  while (result.size() < size && checkTime(System.nanoTime(), start, toRun)) {
                      try {
                          T next = source.apply(100l, TimeUnit.MICROSECONDS);
                          if (next != null) {

                              result.add(next);
                          }
                      } catch (Queue.QueueTimeoutException e) {

                      }


                  }
              }catch(Queue.ClosedQueueException e){
                  if(result.size()>0)
                    throw new ClosedQueueException(ListX.of(result));
              }


                return result;
            };
        }).filter(l->l.size()>0)
                .map(ListX::fromIterable);
    }

    public ReactiveSeq<T> streamControl(final Continueable s, final Function<Supplier<T>, Supplier<T>> batcher) {

        listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
        return ReactiveSeq.fromStream(closingStream(batcher.apply(() -> ensureOpen(this.timeout, this.timeUnit)), s));
    }

    public ReactiveSeq<CompletableFuture<T>> streamControlFutures(final Continueable s, final Function<Supplier<T>, CompletableFuture<T>> batcher) {
        this.sub = s;
        listeningStreams.incrementAndGet(); //assumes all Streams that ever connected, remain connected
        return ReactiveSeq.fromStream(closingStreamFutures(() -> batcher.apply(() -> ensureOpen(this.timeout, this.timeUnit)), s));
    }

    private Stream<Collection<T>> closingStreamBatch(final Supplier<Collection<T>> s, final Continueable sub) {

        final Stream<Collection<T>> st = StreamSupport.stream(new AdaptersModule.ClosingSpliterator<>(
                                                                                     Long.MAX_VALUE, s, sub, this),
                                                              false);

        return st;
    }

    private Stream<T> closingStream(final Supplier<T> s, final Continueable sub) {

        final Stream<T> st = StreamSupport.stream(new AdaptersModule.ClosingSpliterator<T>(
                                                                         Long.MAX_VALUE, s, sub, this),
                                                 false);

        return st;
    }

    private Stream<CompletableFuture<T>> closingStreamFutures(final Supplier<CompletableFuture<T>> s, final Continueable sub) {

        final Stream<CompletableFuture<T>> st = StreamSupport.stream(new AdaptersModule.ClosingSpliterator<>(
                                                                                            Long.MAX_VALUE, s, sub, this),
                                                                     false);

        return st;
    }

    /**
     * @return Infinite (until Queue is closed) Stream of CompletableFutures
     *         that can be used as input into a SimpleReact concurrent dataflow
     * 
     *         This Stream itself is Sequential, SimpleReact will applyHKT
     *         concurrency / parralellism via the constituent CompletableFutures
     * 
     */
    @Override
    public ReactiveSeq<CompletableFuture<T>> streamCompletableFutures() {
        return stream().map(CompletableFuture::completedFuture);
    }

    /**
     * @param stream
     *            Input data from provided Stream
     */
    @Override
    public boolean fromStream(final Stream<T> stream) {
        stream.collect(Collectors.toCollection(() -> queue));
        return true;
    }

    private T ensureOpen(final long timeout, final TimeUnit timeUnit) {

        if (!open && queue.size() == 0)
            throw new ClosedQueueException();
        
        final SimpleTimer timer = new SimpleTimer();
        try {

            final long timeoutNanos = timeUnit.toNanos(timeout);
            T data = null;
            try {

                if (this.continuationStrategy != null) {

                    final SimpleTimer streamTimer = new SimpleTimer();
                    try {
                        while (open && (data = ensureClear(queue.poll())) == null) {

                            final SimpleTimer contTimer = new SimpleTimer();

                            this.continuationStrategy.handleContinuation();

                            if (timeout != -1)
                                handleTimeout(timer, timeoutNanos);

                        }
                        if (data != null)
                            return (T) nillSafe(ensureNotPoisonPill(ensureClear(data)));
                    }finally{
                     }
                }
                if (!open && queue.size() == 0)
                    throw new ClosedQueueException();

                if (timeout == -1) {
                    if (this.sub != null && this.sub.timeLimit() > -1) {
                        data = ensureClear(consumerWait.take(() -> queue.poll(sub.timeLimit(), TimeUnit.NANOSECONDS)));
                        if (data == null)
                            throw new QueueTimeoutException();
                    } else {
                        SimpleTimer takeTimer = new SimpleTimer();
                        data = ensureClear(consumerWait.take(() -> queue.take()));
                        if (data == null)
                            throw new QueueTimeoutException();

                    }
                } else {

                    data = ensureClear(consumerWait.take(() -> queue.poll(timeout, timeUnit)));
                    if (data == null)
                        throw new QueueTimeoutException();

                }
            } catch (final InterruptedException e) {
                Thread.currentThread()
                        .interrupt();
                throw ExceptionSoftener.throwSoftenedException(e);
            }

            ensureNotPoisonPill(data);
            if (sizeSignal != null)
                this.sizeSignal.set(queue.size());

            return (T) nillSafe(data);
        }finally{
        }

    }

    private void handleTimeout(final SimpleTimer timer, final long timeout) {
        if (timer.getElapsedNanoseconds() > timeout) {

            throw new QueueTimeoutException();
        }

    }

    private T ensureClear(T poll) {
        if (CLEAR_PILL == poll) {
            if (queue.size() > 0)
                poll = ensureClear(queue.poll());

            this.queue.clear();
        }

        return poll;
    }

    private T ensureNotPoisonPill(final T data) {
        if (data instanceof PoisonPill) {
            throw new ClosedQueueException();
        }

        return data;
    }

    /**
     * Exception thrown if Queue closed
     * 
     * @author johnmcclean
     *
     */
    @AllArgsConstructor
    public static class ClosedQueueException extends SimpleReactProcessingException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final List currentData;

        public ClosedQueueException() {
            currentData = null;
        }

        public boolean isDataPresent() {
            return currentData != null;
        }

       /** @Override
        public Throwable fillInStackTrace() {
            return this;
        }**/

    }

    /**
     * Exception thrown if Queue polling timesout
     * 
     * @author johnmcclean
     *
     */
    public static class QueueTimeoutException extends SimpleReactProcessingException {
        @Override
        public Throwable fillInStackTrace() {

            return this;
        }

        private static final long serialVersionUID = 1L;
    }

    private static class PoisonPill {
    }

    public T poll(final long time, final TimeUnit unit) throws QueueTimeoutException {
        return this.ensureOpen(time, unit);
    }

    public T get() {

        return ensureOpen(this.timeout, this.timeUnit);

    }

    /**
     * Add a singleUnsafe data point toNested the queue
     * 
     * If the queue is a bounded queue and is full, will return false
     * 
     * @param data Data toNested add
     * @return true if successfully added.
     */
    public boolean add(final T data) {


        try {
            final boolean result = queue.add((T) nullSafe(data));
            if (result) {
                if (sizeSignal != null)
                    this.sizeSignal.set(queue.size());
            }
            return result;

        } catch (final IllegalStateException e) {
            return false;
        }
    }

    /**
     * Offer a singleUnsafe datapoint toNested this Queue
     * 
     * If the queue is a bounded queue and is full it will block until space comes available or until
     * offer time out is reached (default is Integer.MAX_VALUE DAYS).
     * 
     * @param data
     *            data toNested add
     * @return self
     */
    @Override
    public boolean offer(final T data) {

        if (!open)
            throw new ClosedQueueException();
        try {
            final boolean result = producerWait.offer(() -> this.queue.offer((T) nullSafe(data), this.offerTimeout, this.offerTimeUnit));

            if (sizeSignal != null)
                this.sizeSignal.set(queue.size());
            return result;
        } catch (final InterruptedException e) {
            Thread.currentThread()
                  .interrupt();
            throw ExceptionSoftener.throwSoftenedException(e);
        }

    }

    private boolean timeout(final SimpleTimer timer) {

        if (timer.getElapsedNanoseconds() >= offerTimeUnit.toNanos(this.offerTimeout))
            return true;
        return false;
    }

    public  static <T> T nillSafe(final T data) {

        if (NILL == data)
            return null;
        else
            return data;
    }

    public  static <T> T nullSafe(final T data) {
        if (data == null)
            return (T)NILL;
        else
            return data;
    }

   
    /**
     * Close this Queue
     * Poison Pills are used toNested communicate closure toNested connected Streams
     * A Poison Pill is added per connected Stream toNested the Queue
     * If a BlockingQueue is backing this async.Queue it will block until
     * able toNested add toNested the Queue.
     * 
     * @return true if closed
     */
    @Override
    public boolean close() {
        this.open = false;
        
        for (int i = 0; i < listeningStreams.get(); i++) {
           try{
              this.queue.offer((T) POISON_PILL);
           }catch(Exception e){
                    
           }
            
        }
        
        return true;
    }

    /**
     * 
     * @param pillsToSend Number of poison pills toNested send toNested connected Streams
     */
    public void disconnectStreams(int pillsToSend){
        for (int i = 0; i < pillsToSend; i++) {
            try{
               this.queue.offer((T) POISON_PILL);
            }catch(Exception e){
                     
            }
             
         }
    }
    
    public void closeAndClear() {

        this.open = false;

        add((T) CLEAR_PILL);

    }

    public static final NIL NILL = new NIL();

    public static class NIL {
    }

    public java.util.Queue<T> asJDKQueue() {
        Queue<T> host = this;
        return new AbstractQueue<T>() {
            @Override
            public Iterator<T> iterator() {
                return host.jdkStream().iterator();
            }

            @Override
            public int size() {
                return host.size();
            }

            @Override
            public boolean offer(T t) {
                return host.offer(t);
            }

            @Override
            public T poll() {
                return host.get();
            }

            @Override
            public T peek() {
                return host.queue.peek();
            }
        };

    }


    @AllArgsConstructor
    public static class QueueReader<T> {
        @Getter
        Queue<T> queue;

        public boolean notEmpty() {
            return queue.queue.size() != 0;
        }

        @Getter
        private volatile T last = null;

        private int size() {
            return queue.queue.size();
        }

        public T next() {

            last = queue.ensureOpen(queue.timeout, queue.timeUnit);

            return last;
        }

        public boolean isOpen() {
            return queue.open || notEmpty();
        }

        public Collection<T> drainToOrBlock() {

            final Collection<T> result = new ArrayList<>();
            if (size() > 0)
                queue.queue.drainTo(result);
            else {
                try {

                    result.add(queue.ensureOpen(queue.timeout, queue.timeUnit));

                } catch (final ClosedQueueException e) {

                    queue.open = false;
                    throw e;
                }
            }

            return result.stream()
                         .filter(it -> it != POISON_PILL)
                         .collect(Collectors.toList());
        }
    }

    public int size() {
        return queue.size();
    }

    public boolean isOpen() {
        return this.open;
    }

    public void addContinuation(final Continuation c) {
        if (this.continuationStrategy == null)
            continuationStrategy = new AdaptersModule.SingleContinuation(
                                                          this);
        this.continuationStrategy.addContinuation(c);
    }

    public void setContinuations(Queue<T> queue){
        queue.continuationStrategy = this.continuationStrategy;
    }
    @Override
    public <R> R visit(final Function<? super Queue<T>, ? extends R> caseQueue, final Function<? super Topic<T>, ? extends R> caseTopic) {
        return caseQueue.apply(this);
    }

    public String toString(){
        return "Q " + queue;
    }

}
