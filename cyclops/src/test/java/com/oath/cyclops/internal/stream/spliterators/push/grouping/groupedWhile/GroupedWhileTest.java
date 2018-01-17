package com.oath.cyclops.internal.stream.spliterators.push.grouping.groupedWhile;

import cyclops.reactive.Spouts;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by johnmcclean on 19/01/2017.
 */
public class GroupedWhileTest {
    Subscription sub ;
    AtomicInteger count = new AtomicInteger();
    AtomicInteger error = new AtomicInteger();
    AtomicInteger complete = new AtomicInteger();
    @Before
    public void setup(){
        count = new AtomicInteger();
        error = new AtomicInteger();
        complete = new AtomicInteger();
    }

    @Test
    public void groupedWhile(){
        assertThat(Spouts.iterate(0l, i->i+1l)
                .groupedWhile(i->false)
                .map(l->l.get(0))
                .limit(100)
                .collect(Collectors.toList()).size(),equalTo(100));
    }
    @Test
    public void groupedWhile10(){
        Spouts.iterate(0l, i->i+1l)
                .groupedWhile(i->false)
                .map(l->l.getOrElse(0,-1l))
                .limit(3).subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                sub = s;
            }

            @Override
            public void onNext(Long aLong) {
                if(aLong.equals(2l))
                    System.out.println("Recieved " + aLong);
                count.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                error.incrementAndGet();
            }

            @Override
            public void onComplete() {
                complete.incrementAndGet();
            }
        });
        sub.request(10l);
        assertThat(count.get(),equalTo(3));
        assertThat(complete.get(),equalTo(1));
    }
}
