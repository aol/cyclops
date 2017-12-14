package com.oath.cyclops.internal.stream.spliterators.push.zip;

import com.oath.cyclops.internal.stream.spliterators.push.*;
import org.junit.Test;
import org.reactivestreams.Subscription;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class ZipOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
       return new ZippingOperator<Integer,Integer,Integer>(new ArrayOfValuesOperator<>(),new ArrayOfValuesOperator<>(),(a, b)->a+b);
    }
    public Operator<Integer> createOne(){
        return new ZippingOperator<Integer,Integer,Integer>(new ArrayOfValuesOperator<>(1),new ArrayOfValuesOperator<>(2),(a,b)->a+b);
    }

    public Operator<Integer> createThree(){
        return new ZippingOperator<Integer,Integer,Integer>(new ArrayOfValuesOperator<>(1,2,3),new ArrayOfValuesOperator<>(10,11,12),(a,b)->a+b);
    }
    public Operator<Integer> createTwoAndError(){
        return new ZippingOperator<Integer,Integer,Integer>(Fixtures.twoAndErrorSource,new ArrayOfValuesOperator<>(10,11,12),(a, b)->a+b);

    }
    public Operator<Integer> createThreeErrors(){
        return new ZippingOperator<Integer,Integer,Integer>(new ArrayOfValuesOperator<>(10,11,12),Fixtures.threeErrorsSource,(a,b)->a+b);

    }

    @Test
    public void subscribeThreeErrors() throws Exception {
        Subscription sub = threeErrors.subscribe(values::add,errors::add,()->onComplete =true);
        sub.request(1l);
        assertThat(values.size(),equalTo(0));
        assertThat(errors.size(),equalTo(3));
        assertTrue(onComplete);

    }



}
