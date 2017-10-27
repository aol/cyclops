package com.oath.cyclops.internal.stream.spliterators.push.lazySingleValue;

import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.oath.cyclops.internal.stream.spliterators.push.*;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class LazySingleValueOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
       return new FilterOperator<>(new ArrayOfValuesOperator<>(), i->true);
    }
    public Operator<Integer> createOne(){
        return new LazySingleValueOperator<>(1, i->i);
    }

    public Operator<Integer> createThree(){
        return  new FilterOperator<>(new ArrayOfValuesOperator<>(1,2,3),i->true);
    }
    public Operator<Integer> createTwoAndError(){
        return  new FilterOperator<>(Fixtures.twoAndErrorSource, i->true);
    }
    public Operator<Integer> createThreeErrors(){
        return  new FilterOperator<>(Fixtures.threeErrorsSource, i->true);
    }



}
