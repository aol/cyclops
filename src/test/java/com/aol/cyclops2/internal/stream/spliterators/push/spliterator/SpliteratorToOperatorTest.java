package com.aol.cyclops2.internal.stream.spliterators.push.spliterator;

import com.aol.cyclops2.internal.stream.ReactiveStreamX;
import com.aol.cyclops2.internal.stream.spliterators.push.FilterOperator;
import com.aol.cyclops2.internal.stream.spliterators.push.SpliteratorToOperator;
import cyclops.collectionx.mutable.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class SpliteratorToOperatorTest {
    ListX<Integer> values;
    ListX<Throwable> errors;
    boolean onComplete;
    @Before
    public void setup(){
        values = ListX.empty();
        errors = ListX.empty();
        onComplete = false;
        count = new AtomicInteger();
        error = new AtomicInteger();
        complete = new AtomicInteger();
    }
    Subscription sub ;
    AtomicInteger count = new AtomicInteger();
    AtomicInteger error = new AtomicInteger();
    AtomicInteger complete = new AtomicInteger();
    @Test
    public void cancel(){

        Spouts.fromSpliterator(ReactiveSeq.iterate(0l,i->i+1l).spliterator())
                .limit(5)
                .filter(i->true).subscribe(new Subscriber<Long>() {
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

        sub.request(1l);
        assertThat(count.get(),equalTo(1));
        assertThat(error.get(),equalTo(0));
        assertThat(complete.get(),equalTo(0));
        sub.request(3l);
        assertThat(count.get(),equalTo(4));
        assertThat(error.get(),equalTo(0));
        assertThat(complete.get(),equalTo(0));
        sub.cancel();
        assertThat(count.get(),equalTo(4));
        assertThat(error.get(),equalTo(0));
        assertThat(complete.get(),equalTo(0));
    }
    @Test
    public void cancel2(){

        Spouts.fromSpliterator(ReactiveSeq.iterate(0l,i->i+1l).spliterator())
                .limit(3)
                .filter(i->true).subscribe(new Subscriber<Long>() {
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

        sub.cancel();
        assertThat(count.get(),equalTo(0));
        assertThat(error.get(),equalTo(0));
        assertThat(complete.get(),equalTo(0));
        sub.request(1l);
        assertThat(count.get(),equalTo(0));
        assertThat(error.get(),equalTo(0));
        assertThat(complete.get(),equalTo(0));
        sub.request(1l);
        sub.request(1l);
        assertThat(count.get(),equalTo(0));
        assertThat(error.get(),equalTo(0));
        assertThat(complete.get(),equalTo(0));

    }
    @Test
    public void request1(){
        new SpliteratorToOperator<Integer>(ReactiveSeq.of(1,2,3).spliterator())
                .subscribe(values::add,errors::add,()->onComplete=true)
        .request(1l);

        assertThat(values.size(),equalTo(1));
    }

    @Test
    public void requestOne(){
        Subscription sub = new FilterOperator<>(new SpliteratorToOperator<Integer>(ReactiveSeq.fill(10).limit(100).spliterator()),
                i->true)
                .subscribe(values::add,errors::add,()->onComplete=true);

        sub.request(1l);
        assertThat(values.size(),equalTo(1));
        assertFalse(onComplete);
        sub.cancel();
        sub.request(1l);
        assertThat(values.size(),equalTo(1));
        assertFalse(onComplete);
    }
    @Test
    public void requestTwo(){
        new SpliteratorToOperator<Integer>(ReactiveSeq.fill(10).limit(100).spliterator())
                .subscribe(values::add,errors::add,()->onComplete=true)
                .request(2l);
        assertThat(values.size(),equalTo(2));
    }

    @Test
    public void streamOf2(){
        new ReactiveStreamX<>(new SpliteratorToOperator<>(Stream.of(2,3).spliterator()))
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        sub=s;

                    }

                    @Override
                    public void onNext(Integer aLong) {
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

        sub.request(3l);
        assertThat(count.get(),equalTo(2));
        assertThat(complete.get(),equalTo(1));


    }
}
