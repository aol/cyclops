package com.aol.cyclops.internal.stream.spliterators;

import com.aol.cyclops.internal.stream.publisher.PublisherIterable;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class PublisherFlatMappingSpliterator<T,R> extends Spliterators.AbstractSpliterator<R> implements CopyableSpliterator<R> {
    Spliterator<T> source;
    Function<? super T, ? extends Publisher<? extends R>> mapper;
    public PublisherFlatMappingSpliterator(final Spliterator<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapper = mapper;

    }
    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        if(active!=null){
            active.forEachRemaining(action);
        }
        source.forEachRemaining(t->{
            System.out.println("next Stream");

            Publisher<R> flatten = (Publisher<R>)mapper.apply(t);
            SeqSubscriber<R> sub = SeqSubscriber.subscriber(); //use sequential subscriber for iterable sequences, in future switch to pushsubscriber where appropriate
            flatten.subscribe(sub);
            sub.spliterator().forEachRemaining(action);

        });

    }

    Iterator<R> active;
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        System.out.println("try advance flatmap");
        if(active!=null && active.hasNext()){
            action.accept(active.next());
            return active.hasNext();
        }
        source.tryAdvance(t->{
            if(active==null || !active.hasNext()) {
                Publisher<R> flatten = (Publisher<R>)mapper.apply(t);
                SeqSubscriber<R> sub = SeqSubscriber.subscriber();
                flatten.subscribe(sub);
                active = (Iterator<R>)sub.iterator();
            }
            if(active.hasNext())
                action.accept(active.next());


        });

        return active!=null && active.hasNext();
    }
    @Override
    public Spliterator<R> copy() {
        return new PublisherFlatMappingSpliterator<>(CopyableSpliterator.copy(source),mapper);
    }
}
