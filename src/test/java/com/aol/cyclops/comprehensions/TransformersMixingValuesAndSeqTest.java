package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.anyOf;
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
import com.aol.cyclops.control.Streamable;
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

public class TransformersMixingValuesAndSeqTest {

    @Test
    public void optionalTAndListT(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        ListT<Integer> list = ListT.fromIterable(ListX.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void completableFutureTAndListT(){
        
        CompletableFutureTValue<Integer> opt = CompletableFutureT.fromValue(Maybe.just(CompletableFuture.completedFuture(10)));
        ListT<Integer> list =  ListT.fromIterable(ListX.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void maybeTAndListT(){
        
        MaybeTValue<Integer> opt = MaybeT.fromValue(Eval.now(Maybe.of(10)));
        ListT<Integer> list = ListT.fromIterable(ListX.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void listTAndListT(){
        
        ListT<Integer> opt = ListT.fromIterable(ListX.of(ListX.of(10)));
        ListT<Integer> list = ListT.fromIterable(ListX.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21,32)));
    }
   
    @Test
    public void evalTAndListT(){
        
        EvalTValue<Integer> opt =EvalT.fromValue(Eval.now(Eval.now(10)));
        ListT<Integer> list = ListT.fromIterable(ListX.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void futureWTAndListT(){
        
        FutureWTValue<Integer> opt =FutureWT.fromValue(Eval.now(FutureW.ofResult(10)));
        ListT<Integer> list = ListT.fromIterable(ListX.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void xorTAndListT(){
        
        XorTValue<?,Integer> opt =XorT.fromValue(Eval.now(Xor.primary(10)));
        ListT<Integer> list = ListT.fromIterable(ListX.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void tryTAndListT(){
        
        TryTValue<Integer,Throwable> opt =TryT.fromValue(Eval.now(Try.success(10)));
        ListT<Integer> list = ListT.fromIterable(ListX.of(ListX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void optionalTAndSetT(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        SetTSeq<Integer> Set = SetT.fromIterable(ListX.of(SetX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->Set , (a,b)->a+b).toSet(),anyOf(equalTo(SetX.of(21)),equalTo(SetX.of(32))));
    }
    @Test
    public void completableFutureTAndSetT(){
        
        CompletableFutureTValue<Integer> opt = CompletableFutureT.fromValue(Maybe.just(CompletableFuture.completedFuture(10)));
        SetTSeq<Integer> Set = SetT.fromIterable(ListX.of(SetX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->Set , (a,b)->a+b).toSet(),anyOf(equalTo(SetX.of(21)),equalTo(SetX.of(32))));
    }
    @Test
    public void maybeTAndSetT(){
        
        MaybeTValue<Integer> opt = MaybeT.fromValue(Eval.now(Maybe.of(10)));
        SetTSeq<Integer> Set = SetT.fromIterable(ListX.of(SetX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->Set , (a,b)->a+b).toSet(),anyOf(equalTo(SetX.of(21)),equalTo(SetX.of(32))));
    }
    @Test
    public void evalTAndSetT(){
        
        EvalTValue<Integer> opt =EvalT.fromValue(Eval.now(Eval.now(10)));
        SetTSeq<Integer> Set = SetT.fromIterable(ListX.of(SetX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->Set , (a,b)->a+b).toSet().size(),equalTo(1));
    }
    @Test
    public void futureWTAndSetT(){
        
        FutureWTValue<Integer> opt =FutureWT.fromValue(Eval.now(FutureW.ofResult(10)));
        SetTSeq<Integer> Set = SetT.fromIterable(ListX.of(SetX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->Set , (a,b)->a+b).toSet(),anyOf(equalTo(SetX.of(21)),equalTo(SetX.of(32))));
    }
    @Test
    public void xorTAndSetT(){
        
        XorTValue<?,Integer> opt =XorT.fromValue(Eval.now(Xor.primary(10)));
        SetTSeq<Integer> set = SetT.fromIterable(ListX.of(SetX.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->set , (a,b)->a+b).toSet(),anyOf(equalTo(SetX.of(21)),equalTo(SetX.of(32))));
    }
    @Test
    public void optionalTAndStreamableT(){
       
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        StreamableTSeq<Integer> streamable = StreamableT.fromIterable(ListX.of(Streamable.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->streamable , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void completableFutureTAndStreamableT(){
        
        CompletableFutureTValue<Integer> opt = CompletableFutureT.fromValue(Maybe.just(CompletableFuture.completedFuture(10)));
        StreamableTSeq<Integer> streamable = StreamableT.fromIterable(ListX.of(Streamable.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->streamable , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void maybeTAndStreamableT(){
        
        MaybeTValue<Integer> opt = MaybeT.fromValue(Eval.now(Maybe.of(10)));
        StreamableTSeq<Integer> streamable = StreamableT.fromIterable(ListX.of(Streamable.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->streamable , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void evalTAndStreamableT(){
        
        EvalTValue<Integer> opt =EvalT.fromValue(Eval.now(Eval.now(10)));
        StreamableTSeq<Integer> streamable = StreamableT.fromIterable(ListX.of(Streamable.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->streamable , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void futureWTAndStreamableT(){
        
        FutureWTValue<Integer> opt =FutureWT.fromValue(Eval.now(FutureW.ofResult(10)));
        StreamableTSeq<Integer> streamable = StreamableT.fromIterable(ListX.of(Streamable.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->streamable , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void xorTAndStreamableT(){
        
        XorTValue<?,Integer> opt =XorT.fromValue(Eval.now(Xor.primary(10)));
        StreamableTSeq<Integer> streamable = StreamableT.fromIterable(ListX.of(Streamable.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->streamable , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    
    @Test
    public void optionalTAndStreamT(){
        
        OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
        StreamTSeq<Integer> list = StreamT.fromIterable(ListX.of(Stream.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void completableFutureTAndStreamT(){
        
        CompletableFutureTValue<Integer> opt = CompletableFutureT.fromValue(Maybe.just(CompletableFuture.completedFuture(10)));
        StreamTSeq<Integer> list = StreamT.fromIterable(ListX.of(Stream.of(11,22)));
        assertThat(For.Publishers.each2(opt, a->list , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void maybeTAndStreamT(){
        
        MaybeTValue<Integer> opt = MaybeT.fromValue(Eval.now(Maybe.of(10)));
        StreamTSeq<Integer> list = StreamT.fromIterable(ListX.of(Stream.of(11,22)));
       
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of(21,32)));
    }
    @Test
    public void evalTAndStreamT(){
        
        EvalTValue<Integer> opt =EvalT.fromValue(Eval.now(Eval.now(10)));
      
        assertThat(For.Publishers.each2(opt, a->StreamT.fromIterable(ListX.of(Stream.of(11,22))) , (a,b)->a+b).toList(),equalTo(ListX.of(21)));
    }
    @Test
    public void futureWTAndStreamT(){
        
        FutureWTValue<Integer> opt =FutureWT.fromValue(Eval.now(FutureW.ofResult(10)));
        StreamTSeq<Integer> list = StreamT.fromIterable(ListX.of(Stream.of(11,22)));
       
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of(21,32)));
    }
    @Test
    public void xorTAndStreamT(){
        
        XorTValue<?,Integer> opt =XorT.fromValue(Eval.now(Xor.primary(10)));
        StreamTSeq<Integer> list = StreamT.fromIterable(ListX.of(Stream.of(11,22)));
        assertThat(For.Publishers.each2(list, a->opt , (a,b)->a+b).toList(),equalTo(ListX.of(21,32)));
    }
}
