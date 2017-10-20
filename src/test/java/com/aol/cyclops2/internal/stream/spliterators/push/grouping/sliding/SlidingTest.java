package com.aol.cyclops2.internal.stream.spliterators.push.grouping.sliding;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
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
public class SlidingTest {
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
    public void cancelPosition(){
        Spouts.concat(Spouts.of(1l,2l,3l,4l,5l),Spouts.iterate(0l, i->i+1l).sliding(2,1).skip(4).map(l->l.getOrElse(0,-1l))).limit(20)
              .subscribe(new Subscriber<Long>() {
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

        sub.request(5l);
        assertThat(count.get(),equalTo(5));
        sub.request(10l);
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

        sub.cancel();
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

    }
    @Test
    public void cancelPosition2(){
        /**
        Spouts.concat(Spouts.iterate(0l, i->i+1l).sliding(2,1).skip(4).map(l->l.getValue(0))).limit(20)
                .subscribe(new Subscriber<Long>() {
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

        sub.request(5l);
        assertThat(count.getValue(),equalTo(5));
        sub.request(10l);
        assertThat(count.getValue(),equalTo(15));
        assertThat(complete.getValue(),equalTo(0));

        sub.cancel();
        assertThat(count.getValue(),equalTo(15));
        assertThat(complete.getValue(),equalTo(0));
         **/

    }
    @Test
    public void cancelPosition3(){
        Spouts.iterate(0l, i->i+1l).sliding(2,1).skip(2).map(l->l.getOrElse(0,-1l)).limit(20)
                .subscribe(new Subscriber<Long>() {
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

        sub.request(5l);
        assertThat(count.get(),equalTo(5));
        sub.request(10l);
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

        sub.cancel();
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

    }
    @Test
    public void cancelRange3Sync(){
        ReactiveSeq.rangeLong(0l, Long.MAX_VALUE).sliding(2,1).skip(2).map(l->l.getOrElse(0,-1l)).limit(20)
                .subscribe(new Subscriber<Long>() {
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

        sub.request(5l);
        assertThat(count.get(),equalTo(5));
        sub.request(10l);
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

        sub.cancel();
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

    }

    @Test
    public void cancelRange3(){
        Spouts.rangeLong(0l, Long.MAX_VALUE).sliding(2,1).skip(2).map(l->l.getOrElse(0,-1l)).limit(20)
                .subscribe(new Subscriber<Long>() {
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

        sub.request(5l);
        assertThat(count.get(),equalTo(5));
        sub.request(10l);
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

        sub.cancel();
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

    }
    @Test
    public void cancelRangeFlatMap3(){
        Spouts.rangeLong(0l, Long.MAX_VALUE).sliding(2,1).flatMap(s->s.stream()).skip(2).limit(20)
                .subscribe(new Subscriber<Long>() {
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

        sub.request(5l);
        assertThat(count.get(),equalTo(5));
        sub.request(10l);
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

        sub.cancel();
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

    }
    @Test
    public void cancelPosition4(){
        Spouts.iterate(0l, i->i+1l).sliding(2,1).map(l->l.getOrElse(0,-1l)).limit(20)
                .subscribe(new Subscriber<Long>() {
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

        sub.request(5l);
        assertThat(count.get(),equalTo(5));
        sub.request(10l);
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

        sub.cancel();
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

    }
    @Test
    public void cancelPosition5(){
        Spouts.rangeLong(0l, Long.MAX_VALUE).sliding(2,1).map(l->l.getOrElse(0,-1l)).limit(20)
                .subscribe(new Subscriber<Long>() {
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

        sub.request(5l);
        assertThat(count.get(),equalTo(5));
        sub.request(10l);
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

        sub.cancel();
        assertThat(count.get(),equalTo(15));
        assertThat(complete.get(),equalTo(0));

    }


}
