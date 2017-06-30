package com.aol.cyclops2.comprehensions;

import cyclops.control.Eval;
import cyclops.async.Future;
import cyclops.monads.transformers.FutureT;
import cyclops.collections.mutable.ListX;
import cyclops.monads.Witness;
import com.aol.cyclops2.types.anyM.transformers.ValueTransformer;
import org.junit.Test;

public class TransformersValuesTest {
    
  //  OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
  //  MaybeTValue<Integer> maybe = MaybeT.fromValue(Eval.now(Maybe.of(10)));
  //  EvalTValue<Integer> eval = EvalT.fromValue(Eval.now(Eval.later(()->10)));
  //  CompletableFutureTValue<Integer> cf = CompletableFutureT.fromValue(Eval.now(CompletableFuture.completedFuture(10)));
    FutureT<Witness.eval,Integer> future = FutureT.of(Eval.now(Future.ofResult(10)).anyM());
  //  TryTValue<Integer,Throwable> attempt = TryT.fromValue(Eval.now(Try.success(10)));
   // XorTValue<Throwable,Integer> lazy = XorT.fromValue(Eval.now(Xor.primary(10)));
    
    ListX<ValueTransformer<?,Integer>> all = ListX.of(future);// ListX.of(opt,maybe,eval,cf,future,attempt,lazy);

    int count;
    @Test
    public void all(){



        FutureT<Witness.list,Integer> future1 = FutureT.of(ListX.of(Future.ofResult(1), Future.ofResult(2)).anyM());


        future1.forEach2M(i->FutureT.of(ListX.of(Future.ofResult(i*4), Future.ofResult(i*8)).anyM()),
                    (a,b)->a+b)
                .printOut();


//        ListX<Future<Integer>> res = future1.unwrapTo(Witness::list);
        
    }

    @Test
    public void liftM(){

        //Asynchronously generated string concatonated with another, inside a list
        Future.of(()->"Asynchronously generated string ")
                .liftM(Witness.list.INSTANCE)
                .forEach2M(a->Future.of(()->a+"concatonated with another, inside a list")
                                .liftM(Witness.list.INSTANCE),
                        (a,b)->b)
                .printOut();
        

    }
}
