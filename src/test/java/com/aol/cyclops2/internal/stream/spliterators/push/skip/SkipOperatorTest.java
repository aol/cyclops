package com.aol.cyclops2.internal.stream.spliterators.push.skip;

import com.aol.cyclops2.internal.stream.spliterators.push.*;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class SkipOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
       return new SkipOperator<>(new ArrayOfValuesOperator<>(1,2),2);
    }
    public Operator<Integer> createOne(){
        return new SkipOperator<>(new SingleValueOperator<>(1), 0);
    }

    public Operator<Integer> createThree(){
        return  new SkipOperator<>(new ArrayOfValuesOperator<>(1,2,3,4,5,6),3);
    }
    public Operator<Integer> createTwoAndError(){
        return  new SkipOperator<>(Fixtures.twoAndErrorSource, 0);
    }
    public Operator<Integer> createThreeErrors(){
        return  new SkipOperator<>(Fixtures.threeErrorsSource, 0);
    }



}