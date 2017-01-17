package com.aol.cyclops2.internal.stream.spliterators.push.filter;

import com.aol.cyclops2.internal.stream.spliterators.push.*;
import cyclops.collections.ListX;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

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