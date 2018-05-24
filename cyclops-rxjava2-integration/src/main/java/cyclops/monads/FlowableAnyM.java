package cyclops.monads;

import com.oath.cyclops.anym.AnyMSeq;
import com.oath.cyclops.rx2.adapter.FlowableReactiveSeqImpl;
import cyclops.companion.rx2.Flowables;
import cyclops.companion.rx2.Observables;
import cyclops.monads.transformers.StreamT;
import cyclops.reactive.FlowableReactiveSeq;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.util.function.Function;
import java.util.stream.Stream;

public interface FlowableAnyM {
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,Rx2Witness.flowable,T> xorM(Flowable<T> type){
        return XorM.right(anyM(type));
    }
    public static <T> Flowable<T> raw(AnyM<Rx2Witness.flowable,T> anyM){
        return flowable(anyM);
    }

    public static <T,W extends WitnessType<W>> AnyM<W,Flowable<T>> fromStream(AnyM<W,Stream<T>> anyM){
        return anyM.map(s-> Flowables.flowableFrom(ReactiveSeq.fromStream(s)));
    }

    public static <W extends WitnessType<W>,T> StreamT<W,T> flowablify(StreamT<W,T> nested){
        AnyM<W, Stream<T>> anyM = nested.unwrap();
        AnyM<W, ReactiveSeq<T>> flowableM = anyM.map(s -> {
            if (s instanceof FlowableReactiveSeqImpl) {
                return (FlowableReactiveSeqImpl)s;
            }
            if(s instanceof ReactiveSeq){
                return ((ReactiveSeq<T>)s).visit(sync->new FlowableReactiveSeqImpl<T>(Flowable.fromIterable(sync)),
                    rs->new FlowableReactiveSeqImpl<T>(Flowable.fromPublisher(rs)),
                    async ->new FlowableReactiveSeqImpl<T>(Observables.fromStream(async).toFlowable(BackpressureStrategy.BUFFER)));
            }
            return new FlowableReactiveSeqImpl<T>(Flowable.fromIterable(ReactiveSeq.fromStream(s)));
        });
        StreamT<W, T> res = StreamT.of(flowableM);
        return res;
    }

    public static <W extends WitnessType<W>,T,R> R nestedFlowable(StreamT<W,T> nested, Function<? super AnyM<W,Flowable<T>>,? extends R> mapper){
        return mapper.apply(nestedFlowable(nested));
    }
    public static <W extends WitnessType<W>,T> AnyM<W,Flowable<T>> nestedFlowable(StreamT<W,T> nested){
        AnyM<W, Stream<T>> anyM = nested.unwrap();
        return anyM.map(s->{
            if(s instanceof FlowableReactiveSeqImpl){
                return ((FlowableReactiveSeqImpl)s).getFlowable();
            }
            if(s instanceof ReactiveSeq){
                ReactiveSeq<T> r = (ReactiveSeq<T>)s;
                return r.visit(sync->Flowable.fromIterable(sync),rs->Flowable.fromPublisher((Publisher)s),
                    async->Flowable.fromPublisher(async));
            }
            if(s instanceof Publisher){
                return Flowable.<T>fromPublisher((Publisher)s);
            }
            return Flowable.fromIterable(ReactiveSeq.fromStream(s));
        });
    }

    public static <W extends WitnessType<W>,T> StreamT<W,T> liftM(AnyM<W,Flowable<T>> nested){
        AnyM<W, ReactiveSeq<T>> monad = nested.map(s -> new FlowableReactiveSeqImpl<T>(s));
        return StreamT.of(monad);
    }




    /**
     * Construct an AnyM type from a Flowable. This allows the Flowable to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     *
     * <pre>
     * {@code
     *
     *    AnyMSeq<Integer> flowable = Flowables.anyM(Flowable.just(1,2,3));
     *    AnyMSeq<Integer> transformedFlowable = myGenericOperation(flowable);
     *
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     *
     * @param flowable To wrap inside an AnyM
     * @return AnyMSeq wrapping a flowable
     */
    public static <T> AnyMSeq<Rx2Witness.flowable,T> anyM(Flowable<T> flowable) {
        return AnyM.ofSeq(FlowableReactiveSeq.reactiveSeq(flowable), Rx2Witness.flowable.INSTANCE);
    }

    public static <T> Flowable<T> flowable(AnyM<Rx2Witness.flowable,T> flowable) {

        FlowableReactiveSeqImpl<T> flowableSeq = flowable.unwrap();
        return flowableSeq.getFlowable();
    }
}
