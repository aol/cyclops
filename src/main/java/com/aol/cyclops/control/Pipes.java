package com.aol.cyclops.control;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.data.LazyImmutable;
import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.collections.extensions.persistent.PMapX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.react.threads.SequentialElasticPools;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Store for Pipes for cross-thread communication
 * 
 * Connected Streams will not be able to complete collect or reduce style methods unless the underlying Adapter for data transfer is closed.
 * I.e. connected Streams remain connected until either the Adapter is closed, or they disconnect (due to a limit for example).
 * 
 * @author johnmcclean
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Pipes<K, V> {

    private final ConcurrentMap<K, Adapter<V>> registered = new ConcurrentHashMap<>();

    /**
     * @return Size of registered pipes
     */
    public int size() {
        return registered.size();
    }

    /**
     * @return Persistent map of all registered pipes
     */
    public PMapX<K, Adapter<V>> registered() {
        return PMapX.fromMap(registered);
    }

    /**
     * @return Construct an empty Pipes instance
     */
    public static <K, V> Pipes<K, V> of() {
        return new Pipes<>();
    }

    public static <K, V> Pipes<K, V> of(final Map<K, Adapter<V>> registered) {
        Objects.requireNonNull(registered);
        final Pipes<K, V> pipes = new Pipes<>();
        pipes.registered.putAll(registered);
        return pipes;
    }

    public void push(final K key, final V value) {
        Optional.ofNullable(registered.get(key))
                .ifPresent(a -> a.offer(value));
    }

    /**
     * @param key : Adapter identifier
     * @return selected Queue
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Maybe<Adapter<V>> get(final K key) {
        return Maybe.ofNullable((Adapter) registered.get(key));
    }

    /**
     * @param key : Adapter identifier
     * @return LazyFutureStream from selected Queue
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Maybe<LazyFutureStream<V>> futureStream(final K key) {
        return get(key).map(a -> a.futureStream());
    }

    /**
     * @param key : Adapter identifier
     * @return LazyFutureStream from selected Queue
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Maybe<LazyFutureStream<V>> futureStream(final K key, final LazyReact reactor) {

        return get(key).map(a -> a.futureStream(reactor));
    }

    /**
     * @param key : Adapter identifier
     * @return {@link ReactiveSeq} from selected Queue
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Maybe<ReactiveSeq<V>> reactiveSeq(final K key) {
        return get(key).map(a -> a.stream());
    }

    public ListX<V> xValues(final K key, final long x) {
        final SeqSubscriber<V> sub = SeqSubscriber.subscriber();
        return get(key).peek(a -> a.stream()
                                   .subscribe(sub))
                       .map(a -> sub.stream()
                                    .limit(x)
                                    .toListX())
                       .orElse(ListX.empty());
    }

    /**
     * Extract one value from the selected pipe, if it exists
     * @param key : Adapter identifier
     * @return LazyFutureStream from selected Queue
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Maybe<V> oneValue(final K key) {
        final ValueSubscriber<V> sub = ValueSubscriber.subscriber();
        return get(key).peek(a -> a.stream()
                                   .subscribe(sub))
                       .flatMap(a -> sub.toMaybe());
    }

    public Xor<Throwable, V> oneOrError(final K key) {
        final ValueSubscriber<V> sub = ValueSubscriber.subscriber();
        return get(key).peek(a -> a.stream()
                                   .subscribe(sub))
                       .map(a -> sub.toXor())
                       .orElse(Xor.secondary(new NoSuchElementException(
                                                                        "no adapter for key " + key)));
    }

    public <X extends Throwable> Maybe<Try<V, X>> oneValueOrError(final K key, final Class<X>... classes) {
        final ValueSubscriber<V> sub = ValueSubscriber.subscriber();
        return get(key).peek(a -> a.stream()
                                   .subscribe(sub))
                       .map(a -> sub.toTry(classes));
    }

    public Maybe<Try<V, Throwable>> oneValueOrError(final K key) {
        final ValueSubscriber<V> sub = ValueSubscriber.subscriber();
        return get(key).peek(a -> a.stream()
                                   .subscribe(sub))
                       .map(a -> sub.toTry(Throwable.class));
    }

    public FutureW<V> oneOrErrorAsync(final K key, final Executor ex) {
        final CompletableFuture<V> cf = CompletableFuture.supplyAsync(() -> {

            final ValueSubscriber<V> sub = ValueSubscriber.subscriber();
            return get(key).peek(a -> a.stream()
                                       .subscribe(sub))
                           .map(a -> sub.toMaybe()
                                        .get())
                           .get();
        } , ex);

        return FutureW.of(cf);
    }

    /**
     * Return an Eval that allows retrieval of the next value from the attached pipe when get() is called
     * 
     * Maybe.some is returned if a value is present, Maybe.none is returned if the publisher is complete or an error occurs
     * 
     * @param key
     * @return
     */
    public Eval<Maybe<V>> nextValue(final K key) {
        final ValueSubscriber<V> sub = ValueSubscriber.subscriber();
        final LazyImmutable<Boolean> requested = LazyImmutable.def();
        final Maybe<Eval<Maybe<V>>> nested = get(key).peek(a -> a.stream()
                                                                 .subscribe(sub))
                                                     .map(a -> Eval.always(() -> {
                                                         if (requested.isSet()) {
                                                             sub.requestOne();
                                                         } else {
                                                             requested.setOnce(true);
                                                         }
                                                         final Maybe<V> res = sub.toMaybe();
                                                         return res;
                                                     }));
        return nested.orElse(Eval.now(Maybe.<V> none()));
    }

    /**
     * Return an Eval that allows retrieval of the next value from the attached pipe when get() is called
     * 
     * A value is returned if a value is present, otherwise null is returned if the publisher is complete or an error occurs
     * 
     * @param key
     * @return
     */
    public Eval<V> nextOrNull(final K key) {
        final ValueSubscriber<V> sub = ValueSubscriber.subscriber();
        final LazyImmutable<Boolean> requested = LazyImmutable.def();
        return get(key).peek(a -> a.stream()
                                   .subscribe(sub))
                       .map(a -> Eval.always(() -> {
                           if (requested.isSet()) {
                               sub.requestOne();
                           } else {
                               requested.setOnce(true);
                           }
                           final Maybe<V> res = sub.toMaybe();

                           return res.orElse(null);
                       }))

                       .orElse(Eval.<V> now(null));
    }

    /**
     * Register a Queue, and get back a listening LazyFutureStream that runs on a single thread
     * (not the calling thread)
     * 
     * <pre>
     * {@code
     * Pipes.register("test", QueueFactories.
    										<String>boundedNonBlockingQueue(100)
    											.build());
    	LazyFutureStream<String> stream =  PipesToLazyStreams.cpuBoundStream("test");
    	stream.filter(it->it!=null).peek(System.out::println).run();
     * 
     * }</pre>
     * 
     * @param key : Adapter identifier
     * @param adapter
     * 
     */
    public void register(final K key, final Adapter<V> adapter) {
        registered.put(key, adapter);

    }

    public void clear() {
        registered.clear();

    }

    /**
     * Subscribe synchronously to a pipe
     * 
     * @param key for registered simple-react async.Adapter
     * @param subscriber Reactive Streams subscriber for data on this pipe
     */
    public void subscribeTo(final K key, final Subscriber<V> subscriber) {
        registered.get(key)
                  .stream()
                  .subscribe(subscriber);

    }

    /**
     *  Subscribe asynchronously to a pipe
     * 
     * @param key for registered simple-react async.Adapter
     * @param subscriber Reactive Streams subscriber for data on this pipe
     */
    public void subscribeTo(final K key, final Subscriber<V> subscriber, final Executor subscribeOn) {
        CompletableFuture.runAsync(() -> subscribeTo(key, subscriber), subscribeOn);

    }

    /**
     * @param key for registered simple-react async.Adapter
     * @param publisher Reactive Streams publisher  to push data onto this pipe
     */
    public void publishTo(final K key, final Publisher<V> publisher) {
        final SeqSubscriber<V> sub = SeqSubscriber.subscriber();
        publisher.subscribe(sub);
        registered.get(key)
                  .fromStream(sub.stream());
    }

    /**
     * @param key for registered simple-react async.Adapter
     * @param publisher Reactive Streams publisher  to push data onto this pipe
     */
    public void publishToAsync(final K key, final Publisher<V> publisher) {
        SequentialElasticPools.simpleReact.react(er -> er.of(publisher)
                                                         .peek(p -> publishTo(key, p)));
    }

    public void close(final String key) {
        Optional.ofNullable(registered.get(key))
                .ifPresent(a -> a.close());

    }

}
