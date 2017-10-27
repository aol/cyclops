package com.oath.cyclops.internal.stream.spliterators.push.filter;

import com.oath.cyclops.internal.stream.spliterators.push.*;
import com.oath.cyclops.internal.stream.spliterators.push.*;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class FilterOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
       return new FilterOperator<>(new ArrayOfValuesOperator<>(), i->true);
    }
    public Operator<Integer> createOne(){
        return new FilterOperator<>(new SingleValueOperator<>(1), i->true);
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
