package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.control.monads.transformers.values.FutureWTValue;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.control.monads.transformers.values.TransformerValue;
import com.aol.cyclops.control.monads.transformers.values.TryTValue;
import com.aol.cyclops.control.monads.transformers.values.XorTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public class TransformersValuesTest {
    
    OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
    MaybeTValue<Integer> maybe = MaybeT.fromValue(Eval.now(Maybe.of(10)));
    EvalTValue<Integer> eval = EvalT.fromValue(Eval.now(Eval.later(()->10)));
    CompletableFutureTValue<Integer> cf = CompletableFutureT.fromValue(Eval.now(CompletableFuture.completedFuture(10)));
    FutureWTValue<Integer> future = FutureWT.fromValue(Eval.now(FutureW.ofResult(10)));
    TryTValue<Integer,Throwable> attempt = TryT.fromValue(Eval.now(Try.success(10)));
    XorTValue<Throwable,Integer> either = XorT.fromValue(Eval.now(Xor.primary(10)));
    
    ListX<TransformerValue<Integer>> all = ListX.of(opt,maybe,eval,cf,future,attempt,either);
    @Test
    public void optionalTAndMaybeT(){
        
       
        
        assertThat(For.Values.each2(opt, a->maybe , (a,b)->a+b).toList(),equalTo(ListX.of(20)));
    }
    int count;
    @Test
    public void all(){
       
        count =0;
        all.combinations(2).forEach(s->{
            ListX<TransformerValue<Integer>> list = s.toListX();
            TransformerValue<Integer> one = list.get(0);
            TransformerValue<Integer> two = list.get(1);
            count++;
            assertThat(For.Values.each2(one, a->two , (a,b)->a+b).toList(),equalTo(ListX.of(20)));
        });
        assertThat(count,equalTo(21));
        
        
    }
}
