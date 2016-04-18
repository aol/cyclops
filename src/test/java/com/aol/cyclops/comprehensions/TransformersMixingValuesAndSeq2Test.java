package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.control.monads.transformers.values.FutureWTValue;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.control.monads.transformers.values.TryTValue;
import com.aol.cyclops.control.monads.transformers.values.XorTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.util.stream.Streamable;

public class TransformersMixingValuesAndSeq2Test {

    @Test
    public void optionalTAndListT(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        ListTSeq<Integer> list = ListT.fromStream(Stream.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of(21,32)));
    }
    @Test
    public void optionalTAndListTNone(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.none());
        ListTSeq<Integer> list = ListT.fromStream(Stream.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of()));
    }
    @Test
    public void optionalTAndListTEmpty(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.empty()));
        ListTSeq<Integer> list = ListT.fromStream(Stream.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of()));
    }
    @Test
    public void optionalTAndListTEmptyStream(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        ListTSeq<Integer> list = ListT.fromStream(Stream.empty());
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of()));
    }
    @Test
    public void optionalTAndListTEmptyList(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        ListTSeq<Integer> list = ListT.fromStream(Stream.of(ListX.of()));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of()));
    }
   
}
