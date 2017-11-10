package com.oath.cyclops.internal.stream.spliterators.push.map;

import com.oath.cyclops.internal.stream.spliterators.push.*;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class MapOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
       return new MapOperator<Integer,Integer>(new ArrayOfValuesOperator<>(), i->i*2);
    }
    public Operator<Integer> createOne(){
        return new MapOperator<Integer,Integer>(new SingleValueOperator<>(1), i->i*2);
    }

    public Operator<Integer> createThree(){
        return  new MapOperator<Integer,Integer>(new ArrayOfValuesOperator<>(1,2,3),i->i*2);
    }
    public Operator<Integer> createTwoAndError(){
        return  new MapOperator<Integer,Integer>(Fixtures.twoAndErrorSource, i->i*2);
    }
    public Operator<Integer> createThreeErrors(){
        return  new MapOperator<Integer,Integer>(Fixtures.threeErrorsSource, i->i*2);
    }



}
