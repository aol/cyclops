package com.oath.cyclops.internal.stream.spliterators.push.grouping;

import com.oath.cyclops.internal.stream.spliterators.push.*;
import com.oath.cyclops.internal.stream.spliterators.push.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class GroupedOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
        return new MapOperator<List<Integer>,Integer>(new GroupingOperator(new ArrayOfValuesOperator<>(),
                ()->new ArrayList<Integer>(),i->i,1),i->i.get(0));
    }
    public Operator<Integer> createOne(){
        return new MapOperator<List<Integer>,Integer>(new GroupingOperator(new ArrayOfValuesOperator<>(1),
                ()->new ArrayList<Integer>(),i->i,1),i->i.get(0));

    }

    public Operator<Integer> createThree(){
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9);
        return new MapOperator<List<Integer>,Integer>(new GroupingOperator(new IterableSourceOperator(list),
                ()->new ArrayList<Integer>(),i->i,3),i->i.get(0));
    }
    public Operator<Integer> createTwoAndError(){

        return new MapOperator<List<Integer>,Integer>(new GroupingOperator(Fixtures.twoAndErrorSource,
                ()->new ArrayList<Integer>(),i->i,1),i->i.get(0));
    }
    public Operator<Integer> createThreeErrors(){
        return new MapOperator<List<Integer>,Integer>(new GroupingOperator(Fixtures.threeErrorsSource,
                ()->new ArrayList<Integer>(),i->i,1),i->i.get(0));
    }



}
