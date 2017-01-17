package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class GroupedByTimeAndSizeOperatorTest extends AbstractOperatorTest{


    public Operator<Integer> createEmpty(){
        return new GroupedByTimeAndSizeOperator(new ArrayOfValuesOperator<>(),
                ()->new ArrayList<Integer>(),i->i,1, TimeUnit.SECONDS,2);
    }
    public Operator<Integer> createOne(){
        return new GroupedByTimeAndSizeOperator(new ArrayOfValuesOperator<>(1),
                ()->new ArrayList<Integer>(),i->i,1, TimeUnit.SECONDS,2);

    }

    public Operator<Integer> createThree(){
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9);
        return new GroupedByTimeAndSizeOperator(new IterableSourceOperator(list),
                ()->new ArrayList<Integer>(),i->i,1, TimeUnit.SECONDS,2);
    }
    public Operator<Integer> createTwoAndError(){

        return new GroupedByTimeAndSizeOperator(Fixtures.twoAndErrorSource,
                ()->new ArrayList<Integer>(),i->i,1, TimeUnit.SECONDS,1);
    }
    public Operator<Integer> createThreeErrors(){
        return new GroupedByTimeAndSizeOperator(Fixtures.twoAndErrorSource,
                ()->new ArrayList<Integer>(),i->i,1, TimeUnit.SECONDS,6);
    }



}