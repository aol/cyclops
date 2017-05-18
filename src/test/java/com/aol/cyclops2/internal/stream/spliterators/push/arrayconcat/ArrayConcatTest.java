package com.aol.cyclops2.internal.stream.spliterators.push.arrayconcat;

import cyclops.stream.Spouts;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by johnmcclean on 18/01/2017.
 */
public class ArrayConcatTest {
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
    public void requestNegative(){
        Spouts.concat(Spouts.of(),Spouts.iterate(0l, i->i+1l))
                .filter(i->true).limit(20).subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                sub=s;

            }

            @Override
            public void onNext(Long aLong) {
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
        sub.request(-10l);

        assertThat(error.get(),equalTo(1));

    }
    @Test
    public void checkEmissionSize(){
        Spouts.concat(Spouts.of(),Spouts.iterate(0l, i->i+1l))
                    .filter(i->true).limit(20).subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                sub=s;

            }

            @Override
            public void onNext(Long aLong) {
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
        sub.request(5l);
        assertThat(count.get(),equalTo(15));
        sub.cancel();
        assertThat(count.get(),equalTo(15));
        sub.request(2l);
        assertThat(count.get(),equalTo(15));


    }


}
