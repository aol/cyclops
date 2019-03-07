package cyclops.monads;

import com.oath.cyclops.anym.AnyMValue;
import cyclops.monads.Rx2Witness.maybe;
import cyclops.monads.transformers.rx2.MaybeT;
import io.reactivex.Maybe;


@Deprecated
public interface MaybeAnyM {

    public static  <W1 extends WitnessType<W1>,T> XorM<W1,maybe,T> xorM(Maybe<T> type){
        return XorM.right(anyM(type));
    }

    public static <T> Maybe<T> raw(AnyM<maybe,T> anyM){
        return Rx2Witness.maybe(anyM);
    }

    public static <W extends WitnessType<W>,T> MaybeT<W,T> liftM(AnyM<W,Maybe<T>> nested){
        return MaybeT.of(nested);
    }

    /**
     * Construct an AnyM type from a Maybe. This allows the Maybe to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     *
     * <pre>
     * {@code
     *
     *    AnyMSeq<Integer> maybe = Fluxs.anyM(Maybe.just(1,2,3));
     *    AnyMSeq<Integer> transformedMaybe = myGenericOperation(maybe);
     *
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     *
     * @param maybe To wrap inside an AnyM
     * @return AnyMSeq wrapping a Maybe
     */
    public static <T> AnyMValue<maybe,T> anyM(Maybe<T> maybe) {
        return AnyM.ofValue(maybe, Rx2Witness.maybe.INSTANCE);
    }



}
