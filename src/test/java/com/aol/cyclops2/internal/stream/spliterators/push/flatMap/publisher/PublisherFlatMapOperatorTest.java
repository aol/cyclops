package com.aol.cyclops2.internal.stream.spliterators.push.flatMap.publisher;

import com.aol.cyclops2.internal.stream.spliterators.push.*;
import cyclops.stream.Spouts;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class PublisherFlatMapOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
       return new PublisherFlatMapOperator<Integer,Integer>(new ArrayOfValuesOperator<>(), i-> Spouts.of(i*2));
    }
    public Operator<Integer> createOne(){
        return new PublisherFlatMapOperator<Integer,Integer>(new SingleValueOperator<>(1), i->Spouts.of(i*2));
    }

    public Operator<Integer> createThree(){
        return  new PublisherFlatMapOperator<Integer,Integer>(new ArrayOfValuesOperator<>(1,2,3),i->Spouts.of(i*2));
    }
    public Operator<Integer> createTwoAndError(){
        return  new PublisherFlatMapOperator<Integer,Integer>(Fixtures.twoAndErrorSource, i->Spouts.of(i*2));
    }
    public Operator<Integer> createThreeErrors(){
        return  new PublisherFlatMapOperator<Integer,Integer>(Fixtures.threeErrorsSource, i->Spouts.of(i*2));
    }



}