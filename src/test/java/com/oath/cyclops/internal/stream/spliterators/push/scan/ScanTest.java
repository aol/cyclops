package com.oath.cyclops.internal.stream.spliterators.push.scan;

import cyclops.reactive.Spouts;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by johnmcclean on 18/01/2017.
 */
public class ScanTest {
    Subscription sub ;
    AtomicInteger count = new AtomicInteger();
    AtomicInteger error = new AtomicInteger();
    AtomicInteger complete = new AtomicInteger();
    @Test
    public void limitPosition(){
        Spouts.iterate(0l, i->i+1l).limit(1).scanLeft(1l,(a, b)->a+b)
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

        sub.request(1l);
        assertThat(count.get(),equalTo(1));
        sub.request(1l);
        assertThat(count.get(),equalTo(2));
        assertThat(complete.get(),equalTo(0));
        sub.request(1l);
        assertThat(count.get(),equalTo(2));
        assertThat(complete.get(),equalTo(1));
    }
}
