package cyclops.monads;

import com.oath.cyclops.anym.AnyMValue;
import cyclops.monads.transformers.rx2.SingleT;
import io.reactivex.Single;
@Deprecated
public interface SingleAnyM {

    public static  <W1 extends WitnessType<W1>,T> XorM<W1,Rx2Witness.single,T> xorM(Single<T> type){
        return XorM.right(anyM(type));
    }
    public static <T> Single<T> raw(AnyM<Rx2Witness.single,T> anyM){
        return Rx2Witness.single(anyM);
    }

    public static <W extends WitnessType<W>,T> SingleT<W,T> liftM(AnyM<W,Single<T>> nested){
        return SingleT.of(nested);
    }

    /**
     * Construct an AnyM type from a Single. This allows the Single to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     *
     * <pre>
     * {@code
     *
     *    AnyMSeq<Integer> single = Fluxs.anyM(Single.just(1,2,3));
     *    AnyMSeq<Integer> transformedSingle = myGenericOperation(single);
     *
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     *
     * @param single To wrap inside an AnyM
     * @return AnyMSeq wrapping a Single
     */
    public static <T> AnyMValue<Rx2Witness.single,T> anyM(Single<T> single) {
        return AnyM.ofValue(single, Rx2Witness.single.INSTANCE);
    }

}
