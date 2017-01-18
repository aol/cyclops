package com.aol.cyclops2.internal.stream.spliterators.push.flatMap.stream;

import com.aol.cyclops2.internal.stream.spliterators.push.*;
import cyclops.stream.Spouts;
import org.junit.Test;
import org.reactivestreams.Subscription;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class FlatMapOperatorTest extends AbstractOperatorTest {
    @Test
    public void subscribeThreeErrorsMaxLong() throws Exception {
        Subscription sub = createThreeErrors().subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(Long.MAX_VALUE);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(1));
   //     assertTrue(onComplete);
        sub.request(1l);
        assertThat("Values " + values,values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(2));
      //  assertTrue(onComplete);
        sub.request(3l);
        assertThat("Values " + values,values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(3));
        assertTrue(onComplete);
    }

    public Operator<Integer> createEmpty(){
       return new FlatMapOperator<Integer,Integer>(new ArrayOfValuesOperator<>(), i-> Spouts.of(i*2));
    }
    public Operator<Integer> createOne(){
        return new FlatMapOperator<Integer,Integer>(new SingleValueOperator<>(1), i->Spouts.of(i*2));
    }

    public Operator<Integer> createThree(){
        return  new FlatMapOperator<Integer,Integer>(new ArrayOfValuesOperator<>(1,2,3),i->Spouts.of(i*2));
    }
    public Operator<Integer> createTwoAndError(){
        return  new FlatMapOperator<Integer,Integer>(Fixtures.twoAndErrorSource, i->Spouts.of(i*2));
    }
    public Operator<Integer> createThreeErrors(){
        return  new FlatMapOperator<Integer,Integer>(Fixtures.threeErrorsSource, i->Spouts.of(i*2));
    }



}