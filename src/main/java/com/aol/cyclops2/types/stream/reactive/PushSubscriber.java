package com.aol.cyclops2.types.stream.reactive;

import com.aol.cyclops2.internal.stream.spliterators.push.PushingSpliterator;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;

/**
 * A reactive-streams Subscriber that can generate various forms of sequences from a publisher
 * 
 * <pre>
 * {@code 
 *    PushSubscriber<Integer> ints = PushSubscriber.subscriber();
 *    ReactiveSeq.of(1,2,3)
 *               .publish(ints);
 *    
 *   ListX list = ints.toListX();
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Subscriber type
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class PushSubscriber<T> implements Subscriber<T>{



    private volatile Subscription s;

    private final PushingSpliterator<T> split= new PushingSpliterator<T>();





    public static <T> PushSubscriber<T> subscriber() {
        return new PushSubscriber<T>();
    }



    @Override
    public void onSubscribe(final Subscription s) {
        Objects.requireNonNull(s);
        if (this.s == null) {
            this.s = s;
            s.request(1);
        } else
            s.cancel();

    }

    @Override
    public void onNext(final T t) {

        Objects.requireNonNull(t);
        split.getAction().accept(t);
    }

    @Override
    public void onError(final Throwable t) {
        Objects.requireNonNull(t);
        split.getError().accept(t);
    }

    @Override
    public void onComplete() {

        this.split.getOnComplete().run();

    }


    public ReactiveSeq<T> stream(){
        return ReactiveSeq.fromSpliterator(split);
    }


}
