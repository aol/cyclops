package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.For;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public class TransformersMixingValuesAndSeq2Test {

    @Test
    public void optionalTAndListT(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        ListT<Integer> list = ListT.fromStream(Stream.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of(21,32)));
    }
    @Test
    public void optionalTAndListTNone(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.none());
        ListT<Integer> list = ListT.fromStream(Stream.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of()));
    }
    @Test
    public void optionalTAndListTEmpty(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.empty()));
        ListT<Integer> list = ListT.fromStream(Stream.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of()));
    }
    @Test
    public void optionalTAndListTEmptyStream(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        ListT<Integer> list = ListT.fromStream(Stream.empty());
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of()));
    }
    @Test
    public void optionalTAndListTEmptyList(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        ListT<Integer> list = ListT.fromStream(Stream.of(ListX.of()));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of()));
    }
   
}
