package cyclops.monads;

import com.oath.cyclops.anym.AnyMValue;
import cyclops.monads.transformers.reactor.MonoT;
import reactor.core.publisher.Mono;

public interface MonoAnyM {
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,ReactorWitness.mono,T> xorM(Mono<T> type){
        return XorM.right(anyM(type));
    }

    public static <T> Mono<T> raw(AnyM<ReactorWitness.mono,T> anyM){
        return ReactorWitness.mono(anyM);
    }

    /**
     * Construct an AnyM type from a Mono. This allows the Mono to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     *
     * <pre>
     * {@code
     *
     *    AnyMSeq<Integer> mono = Fluxs.anyM(Mono.just(1,2,3));
     *    AnyMSeq<Integer> transformedMono = myGenericOperation(mono);
     *
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     *
     * @param mono To wrap inside an AnyM
     * @return AnyMSeq wrapping a Mono
     */
    public static <T> AnyMValue<ReactorWitness.mono,T> anyM(Mono<T> mono) {
        return AnyM.ofValue(mono, ReactorWitness.mono.INSTANCE);
    }

    public static <W extends WitnessType<W>,T> MonoT<W,T> liftM(AnyM<W,Mono<T>> nested){
        return MonoT.of(nested);
    }
    public static <T,W extends WitnessType<W>> MonoT<W, T> liftM(Mono<T> opt, W witness) {
        return MonoT.of(witness.adapter().unit(opt));
    }
}
