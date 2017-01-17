package com.aol.cyclops2.internal.stream.spliterators.push.spliterator;

import com.aol.cyclops2.internal.stream.spliterators.push.FilterOperator;
import com.aol.cyclops2.internal.stream.spliterators.push.SpliteratorToOperator;
import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertTrue(onComplete);
    }
    @Test
    public void requestTwo(){
        new SpliteratorToOperator<Integer>(ReactiveSeq.fill(10).limit(100).spliterator())
                .subscribe(values::add,errors::add,()->onComplete=true)
                .request(2l);
        assertThat(values.size(),equalTo(2));
    }

}
