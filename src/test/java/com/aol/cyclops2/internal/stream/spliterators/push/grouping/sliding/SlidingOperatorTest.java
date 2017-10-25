package com.aol.cyclops2.internal.stream.spliterators.push.grouping.sliding;

import com.aol.cyclops2.internal.stream.spliterators.push.*;
import cyclops.collections.immutable.VectorX;

import java.util.Arrays;
import java.util.List;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class SlidingOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
        return new MapOperator<VectorX<Integer>,Integer>(new SlidingOperator(new ArrayOfValuesOperator<>(),
                i->i,1,1),i->i.getOrElse(0,-1));
    }
    public Operator<Integer> createOne(){
        return new MapOperator<VectorX<Integer>,Integer>(new SlidingOperator(new ArrayOfValuesOperator<>(1),
                i->i,1,1),i->i.getOrElse(0,-1));

    }

    public Operator<Integer> createThree(){
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9);
        return new MapOperator<VectorX<Integer>,Integer>(new SlidingOperator(new IterableSourceOperator(list),
                i->i,3,3),i->i.getOrElse(0,-1));
    }
    public Operator<Integer> createTwoAndError(){

        return new MapOperator<VectorX<Integer>,Integer>(new SlidingOperator(Fixtures.twoAndErrorSource,
                i->i,1,1),i->i.getOrElse(0,-1));
    }
    public Operator<Integer> createThreeErrors(){
        return new MapOperator<List<Integer>,Integer>(new SlidingOperator(Fixtures.threeErrorsSource
                ,i->i,1,1),i->i.get(0));
    }



}