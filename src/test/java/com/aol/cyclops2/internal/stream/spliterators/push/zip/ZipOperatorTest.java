package com.aol.cyclops2.internal.stream.spliterators.push.zip;

import com.aol.cyclops2.internal.stream.spliterators.push.*;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class ZipOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
       return new ZippingOperator<Integer,Integer,Integer>(new ArrayOfValuesOperator<>(),new ArrayOfValuesOperator<>(),(a,b)->a+b);
    }
    public Operator<Integer> createOne(){
        return new ZippingOperator<Integer,Integer,Integer>(new ArrayOfValuesOperator<>(1),new ArrayOfValuesOperator<>(2),(a,b)->a+b);
    }

    public Operator<Integer> createThree(){
        return new ZippingOperator<Integer,Integer,Integer>(new ArrayOfValuesOperator<>(1,2,3),new ArrayOfValuesOperator<>(10,11,12),(a,b)->a+b);
    }
    public Operator<Integer> createTwoAndError(){
        return new ZippingOperator<Integer,Integer,Integer>(Fixtures.twoAndErrorSource,new ArrayOfValuesOperator<>(10,11,12),(a,b)->a+b);

    }
    public Operator<Integer> createThreeErrors(){
        return new ZippingOperator<Integer,Integer,Integer>(new ArrayOfValuesOperator<>(10,11,12),Fixtures.threeErrorsSource,(a,b)->a+b);

    }



}