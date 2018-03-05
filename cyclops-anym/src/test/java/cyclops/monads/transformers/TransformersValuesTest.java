package cyclops.monads.transformers;
import cyclops.control.Eval;
import cyclops.monads.AnyM;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.control.Future;
import cyclops.reactive.collections.mutable.ListX;
import com.oath.anym.transformers.ValueTransformer;
import org.junit.Test;

public class TransformersValuesTest {

    FutureT<Witness.eval,Integer> future = FutureT.of(AnyM.fromEval(Eval.now(Future.ofResult(10))));

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
