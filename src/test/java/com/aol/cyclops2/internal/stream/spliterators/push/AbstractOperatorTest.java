package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.collections.mutable.ListX;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public abstract class AbstractOperatorTest {
    Operator<Integer> empty;
    Operator<Integer> one;
    Operator<Integer> three;
    Operator<Integer> twoAndError;
    protected Operator<Integer> threeErrors;

    @Before
    public void setup(){
        values = ListX.empty();
        errors = ListX.empty();
        onComplete = false;
        empty = createEmpty();
        one = createOne();
        three = createThree();
        twoAndError = createTwoAndError();
        threeErrors = createThreeErrors();
    }

    public abstract Operator<Integer> createEmpty();
    public abstract Operator<Integer> createOne();

    public abstract Operator<Integer> createThree();
    public abstract Operator<Integer> createTwoAndError();
    public abstract Operator<Integer> createThreeErrors();

    protected ListX<Integer> values;
    protected ListX<Throwable> errors;
    protected boolean onComplete;

    @Test
    public void subscribeEmpty() throws Exception {
        Subscription sub = empty.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeOne() throws Exception {
        Subscription sub = one.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(1l);
        assertThat(values.size(),equalTo(1));
        assertThat(errors.size(),equalTo(0));
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(1));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeOneMaxLong() throws Exception {
        Subscription sub = one.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(Long.MAX_VALUE);
        assertThat(values.size(),equalTo(1));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(1));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeOneCancel() throws Exception {
        Subscription sub = one.subscribe(values::add,errors::add,()->onComplete =true);
        sub.cancel();
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
    }
    @Test
    public void subscribeThree() throws Exception {
        Subscription sub = three.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(1l);
        assertThat(values.size(),equalTo(1));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(2));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
        sub.request(3l);
        assertThat("Values " + values,values.size(),equalTo(3));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeThreeMaxLong() throws Exception {
        Subscription sub = three.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(Long.MAX_VALUE);
        assertThat(values.size(),equalTo(3));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(3));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
        sub.request(3l);
        assertThat("Values " + values,values.size(),equalTo(3));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeThreeCancel() throws Exception {
        Subscription sub = three.subscribe(values::add,errors::add,()->onComplete =true);
        sub.cancel();
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
    }

    @Test
    public void subscribeTwoAndError() throws Exception {
        Subscription sub = twoAndError.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(1l);
        assertThat(values.size(),equalTo(1));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(2));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(2));
        assertThat(errors.size(),equalTo(1));
        sub.request(1l);
        assertTrue(onComplete);
    }
    @Test
    public void subscribeTwoAndErrorMaxLong() throws Exception {
        Subscription sub = twoAndError.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(Long.MAX_VALUE);
        assertThat(values.size(),equalTo(2));
        assertThat(errors.size(),equalTo(1));
        assertTrue(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(2));
        assertThat(errors.size(),equalTo(1));
        assertTrue(onComplete);
        sub.request(3l);
        assertThat("Values " + values,values.size(),equalTo(2));
        assertThat(errors.size(),equalTo(1));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeTwoAndErrorCancel() throws Exception {
        Subscription sub = twoAndError.subscribe(values::add,errors::add,()->onComplete =true);
        sub.cancel();
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
    }
    @Test
    public void subscribeThreeErrors() throws Exception {
        Subscription sub = threeErrors.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(1));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(2));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(3));
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(3));
        assertTrue(onComplete);
    }

    @Test
    public void subscribeThreeErrorsMaxLong() throws Exception {
        Subscription sub = threeErrors.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(Long.MAX_VALUE);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(3));
        assertTrue(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(3));
        assertTrue(onComplete);
        sub.request(3l);
        assertThat("Values " + values,values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(3));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeThreeErrorsCancel() throws Exception {
        Subscription sub = threeErrors.subscribe(values::add,errors::add,()->onComplete =true);
        sub.cancel();
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertFalse(onComplete);
    }
    @Test
    public void subscribeAllEmpty() throws Exception {
        empty.subscribeAll(values::add,errors::add,()->onComplete =true);

        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeAllOne() throws Exception {
        one.subscribeAll(values::add,errors::add,()->onComplete =true);

        assertThat(values.size(),equalTo(   1));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
    }

    @Test
    public void subscribeAllThree() throws Exception {
        three.subscribeAll(values::add,errors::add,()->onComplete =true);

        assertThat(values.size(),equalTo(   3));
        assertThat(errors.size(),equalTo(0));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeAllTwoAndError() throws Exception {
        twoAndError.subscribeAll(values::add,errors::add,()->onComplete =true);

        assertThat(values.size(),equalTo(   2));
        assertThat(errors.size(),equalTo(1));
        assertTrue(onComplete);
    }
    @Test
    public void subscribeAllThreeErrors() throws Exception {
        threeErrors.subscribeAll(values::add,errors::add,()->onComplete =true);

        assertThat(values.size(),equalTo(   0));
        assertThat(errors.size(),equalTo(3));
        assertTrue(onComplete);
    }



}