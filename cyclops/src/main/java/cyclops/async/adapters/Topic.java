package cyclops.async.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oath.cyclops.types.futurestream.Continuation;
import cyclops.async.QueueFactories;
import com.oath.cyclops.types.persistent.PersistentMap;


import cyclops.control.Option;
import cyclops.data.HashMap;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.react.async.subscription.Continueable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;

/**
 * A class that can accept input streams and generate emitted streams where data sent in the Topic is guaranteed to be
 * provided to all Topic subsribers
 *
 * @author johnmcclean
 *
 * @param <T> Data type for the Topic
 */
public class Topic<T> implements Adapter<T> {

    @Getter(AccessLevel.PACKAGE)
    private final DistributingCollection<T> distributor = new DistributingCollection<T>();
    @Getter(AccessLevel.PACKAGE)
    private volatile HashMap<ReactiveSeq<?>, Queue<T>> streamToQueue = HashMap.empty();
    private final Object lock = new Object();
    private volatile int index = 0;
    private final QueueFactory<T> factory;

    /**
     * Construct a new Topic
     */
    public Topic() {
        final Queue<T> q = new Queue<T>();
        factory = QueueFactories.unboundedQueue();
        distributor.addQueue(q);
    }

    /**
     * Construct a Topic using the Queue provided
     * @param q Queue to back this Topic with
     */
    public Topic(final Queue<T> q) {
        factory = QueueFactories.unboundedQueue();
        distributor.addQueue(q);
    }
    public Topic(final Queue<T> q,QueueFactory<T> factory) {
        this.factory = factory;
        distributor.addQueue(q);
    }

    /**
     * Topic will maintain a queue for each Subscribing Stream
     * If a Stream is finished with a Topic it is good practice to disconnect from the Topic
     * so messages will no longer be stored for that Stream
     *
     * @param stream
     */
    @Synchronized("lock")
    public void disconnect(final ReactiveSeq<T> stream) {
      System.out.println();
      System.out.println("-----------DISCONECTING " + stream);
      System.out.println();
      System.out.println("map " +streamToQueue.mapValues(q-> System.identityHashCode(q)).printHAMT() + "  stream " + stream);
      Option<Queue<T>> o = streamToQueue.get(stream);
      if(!o.isPresent()){
        streamToQueue.get(stream);
      }
        distributor.removeQueue(streamToQueue.getOrElse(stream, new Queue<>()));
      System.out.println("Remove before " + this.streamToQueue.printHAMT() + " removing " + stream);
        this.streamToQueue = streamToQueue.remove(stream);
      System.out.println("Remove after " + streamToQueue.printHAMT());
        this.index--;
    }

    @Synchronized("lock")
    private <R> ReactiveSeq<R> connect(final Function<Queue<T>, ReactiveSeq<R>> streamCreator) {
        final Queue<T> queue = this.getNextQueue();
        final ReactiveSeq<R> stream = streamCreator.apply(queue);

        this.streamToQueue = streamToQueue.put(stream, queue);
        System.out.println("Put " + streamToQueue);
        return stream;
    }

    /**
     * @param stream Input data from provided Stream
     */
    @Override
    public boolean fromStream(final Stream<T> stream) {
        stream.collect(Collectors.toCollection(() -> distributor));
        return true;

    }

    /**
     * Generating a streamCompletableFutures will register the Stream as a reactiveSubscriber to this topic.
     * It will be provided with an internal Queue as a mailbox. @see Topic.disconnect to disconnect from the topic
     *
     * @return Stream of CompletableFutures that can be used as input into a SimpleReact concurrent dataflow
     */
    @Override
    public ReactiveSeq<CompletableFuture<T>> streamCompletableFutures() {
        return connect(q -> q.streamCompletableFutures());
    }

    /**
     * Generating a reactiveStream will register the Stream as a reactiveSubscriber to this topic.
     * It will be provided with an internal Queue as a mailbox. @see Topic.disconnect to disconnect from the topic
     * @return Stream of data
     */
    @Override
    public ReactiveSeq<T> stream() {

        return connect(q -> q.stream());

    }

    @Override
    public ReactiveSeq<T> stream(final Continueable s) {

        return connect(q -> q.stream(s));

    }

    private Queue<T> getNextQueue() {

        System.out.println("Sub size " + this.distributor.getSubscribers()
                .size());
        System.out.println("Subs" + this.distributor.getSubscribers());
        if (index >= this.distributor.getSubscribers()
                                     .size()) {

            this.distributor.addQueue(factory.build());

        }
        return this.distributor.getSubscribers()
                               .getOrElse(index++,null);
    }

    /**
     * Close this Topic
     *
     * @return true if closed
     */
    @Override
    public boolean close() {
        this.distributor.getSubscribers()
                        .forEach(it -> it.close());
        return true;

    }

    /**
     * @return Track changes in size in the Topic's data
     */
    public Signal<Integer> getSizeSignal(final int index) {
        return this.distributor.getSubscribers()
                               .getOrElse(index,null)
                               .getSizeSignal();
    }

    public void setSizeSignal(final int index, final Signal<Integer> s) {
        this.distributor.getSubscribers()
                        .getOrElse(index, null)
                        .setSizeSignal(s);
    }

    /**
     * Add a single datapoint to this Queue
     *
     * @param data data to add
     * @return self
     */
    @Override
    public boolean offer(final T data) {
        fromStream(Stream.of(data));
        return true;

    }

    public void addContinuation(Continuation cont) {
        distributor.subscribers.forEach(q->q.addContinuation(cont));
    }

    static class DistributingCollection<T> extends ArrayList<T> {

        private static final long serialVersionUID = 1L;
        @Getter
        private volatile Seq<Queue<T>> subscribers = Seq.empty();

        private final Object lock = new Object();

        @Synchronized("lock")
        public void addQueue(final Queue<T> q) {
            subscribers = subscribers.append(q);
        }

        @Synchronized("lock")
        public void removeQueue(final Queue<T> q) {
          System.out.println("Remove q b4" + subscribers.map(a->System.identityHashCode(a)) + " q " + System.identityHashCode(q));
            subscribers = subscribers.removeValue(q);
          System.out.println("Remove q after " + subscribers.map(a->System.identityHashCode(a)) + " q " + System.identityHashCode(q));
        }

        @Override
        public boolean add(final T e) {

            subscribers.forEach(it -> it.offer(e));
            return true;
        }

        @Override
        public boolean addAll(final Collection<? extends T> c) {
            subscribers.forEach(it -> c.forEach(next -> it.offer(next)));
            return true;
        }

    }

    @Override
    public <R> R visit(final Function<? super Queue<T>, ? extends R> caseQueue, final Function<? super Topic<T>, ? extends R> caseTopic) {
        return caseTopic.apply(this);
    }

}
