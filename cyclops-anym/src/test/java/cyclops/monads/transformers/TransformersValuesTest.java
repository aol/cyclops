package cyclops.monads.transformers;
import cyclops.monads.AnyM;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import cyclops.control.Eval;
import cyclops.async.Future;
import cyclops.monads.transformers.FutureT;
import cyclops.collections.mutable.ListX;
import com.oath.anym.transformers.ValueTransformer;
import org.junit.Test;

public class TransformersValuesTest {

  //  OptionalTValue<Integer> opt = OptionalT.fromValue(Maybe.just(Optional.of(10)));
  //  MaybeTValue<Integer> maybe = MaybeT.fromValue(Eval.now(Maybe.of(10)));
  //  EvalTValue<Integer> eval = EvalT.fromValue(Eval.now(Eval.later(()->10)));
  //  CompletableFutureTValue<Integer> cf = CompletableFutureT.fromValue(Eval.now(CompletableFuture.completedFuture(10)));
    FutureT<Witness.eval,Integer> future = FutureT.of(AnyM.fromEval(Eval.now(Future.ofResult(10))));
  //  TryTValue<Integer,Throwable> recover = TryT.fromValue(Eval.now(Try.success(10)));
   // XorTValue<Throwable,Integer> lazy = XorT.fromValue(Eval.now(Xor.lazyRight(10)));

    ListX<ValueTransformer<?,Integer>> all = ListX.of(future);// ListX.of(opt,maybe,eval,cf,future,recover,lazy);

    int count;
    @Test
    public void all(){



        FutureT<Witness.list,Integer> future1 = FutureT.of(AnyM.fromList(ListX.of(Future.ofResult(1), Future.ofResult(2))));


        future1.forEach2M(i->FutureT.of(AnyM.fromList(ListX.of(Future.ofResult(i*4), Future.ofResult(i*8)))),
                    (a,b)->a+b)
                .printOut();


//        ListX<Future<Integer>> res = future1.unwrapTo(Witness::list);

    }

    @Test
    public void liftM(){

        //Asynchronously generated string concatonated with another, inside a list
        AnyMs
                .liftM(Future.of(()->"Asynchronously generated string "),Witness.list.INSTANCE)
                .forEach2M(a->AnyMs.liftM(Future.of(()->a+"concatonated with another, inside a list")
                    ,Witness.list.INSTANCE),
                        (a,b)->b)
                .printOut();


    }
}
