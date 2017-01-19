package com.aol.cyclops2.internal.stream.spliterators;

import com.aol.cyclops2.types.stream.reactive.SeqSubscriber;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public static <T2,T,R> PublisherFlatMappingSpliterator<T2,R> compose(FunctionSpliterator<T2,T> fnS,Function<? super T, ? extends Publisher<? extends R>> mapper){
        Function<? super T2,? extends T> fn = fnS.function();
        return new PublisherFlatMappingSpliterator<T2,R>(CopyableSpliterator.copy(fnS.source()),mapper.<T2>compose(fn));

    }
    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        if(active!=null){
            active.forEachRemaining(action);
        }
        source.forEachRemaining(t->{


            Publisher<R> flatten = (Publisher<R>)mapper.apply(t);
            SeqSubscriber<R> sub = SeqSubscriber.subscriber(); //use sequential reactiveSubscriber for iterable sequences, in future switch to pushsubscriber where appropriate
            flatten.subscribe(sub);
            sub.spliterator().forEachRemaining(action);

        });

    }

    Iterator<R> active;
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        for(;;) {
            if (active != null && active.hasNext()) {
                action.accept(active.next());
                if (active.hasNext())
                    return true;
                else
                    active = null;

            }
            //next publisher
            boolean advance = source.tryAdvance(t -> {
                if (active == null || !active.hasNext()) {
                    Publisher<R> flatten = (Publisher<R>)mapper.apply(t);
                    SeqSubscriber<R> sub = SeqSubscriber.subscriber();
                    flatten.subscribe(sub);
                    active = (Iterator<R>)sub.iterator();
                }


            });
            if(!advance && active==null)
                return false;
        }

    }
    @Override
    public Spliterator<R> copy() {
        return new PublisherFlatMappingSpliterator<>(CopyableSpliterator.copy(source),mapper);
    }
}
