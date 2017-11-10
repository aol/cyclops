package com.oath.cyclops.internal.stream.spliterators.push.arrayconcat;

import com.oath.cyclops.internal.stream.spliterators.push.*;

import java.util.stream.Stream;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class ArrayConcatonatingOperatorTest extends AbstractOperatorTest {


    public Operator<Integer> createEmpty(){
        return new ArrayConcatonatingOperator<>(new ArrayOfValuesOperator<>(),new ArrayOfValuesOperator<>());
    }
    public Operator<Integer> createOne(){
        return new ArrayConcatonatingOperator<>(new ArrayOfValuesOperator<>(),
                new ArrayOfValuesOperator<>(1));
    }

    public Operator<Integer> createThree(){

        return new ArrayConcatonatingOperator<>(new ArrayOfValuesOperator<>(1),
                new ArrayOfValuesOperator<>(),
                new SpliteratorToOperator<>(Stream.of(2,3).spliterator()));
        /**
        return new ArrayConcatonatingOperator<>(new SpliteratorToOperator<>(Stream.of(1).spliterator()),
                new ArrayOfValuesOperator<>(),
                new ArrayOfValuesOperator<>(2,3));**/
    }
    public Operator<Integer> createTwoAndError(){
        return new ArrayConcatonatingOperator<>(new ArrayOfValuesOperator<>(),
                new SpliteratorToOperator<>(Stream.of(1).spliterator()),
                Fixtures.oneAndErrorSource);
    }
    public Operator<Integer> createThreeErrors(){
        return new ArrayConcatonatingOperator<>(new ArrayOfValuesOperator<>(),
                new SpliteratorToOperator<>(Stream.of().spliterator()),
                Fixtures.threeErrorsSource);
    }



}
