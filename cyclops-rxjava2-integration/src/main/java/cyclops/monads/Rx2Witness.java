package cyclops.monads;

import com.oath.cyclops.anym.extensability.MonadAdapter;
import com.oath.cyclops.rx2.adapter.*;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;


public interface Rx2Witness {

    public static <T> Maybe<T> maybe(AnyM<maybe, ? extends T> anyM){
        return anyM.unwrap();

    }
    static interface MaybeWitness<W extends MaybeWitness<W>>  extends WitnessType<W> {

    }
    public static enum maybe implements MaybeWitness<maybe> {
        INSTANCE;

        @Override
        public MonadAdapter<maybe> adapter() {
            return new MaybeAdapter();
        }

    }

    public static <T> Flowable<T> flowable(AnyM<flowable, ? extends T> anyM){
        FlowableReactiveSeqImpl<T> obs = anyM.unwrap();
        return obs.getFlowable();
    }

    static interface FlowableWitness<W extends FlowableWitness<W>>  extends WitnessType<W> {

    }
    public static enum flowable implements FlowableWitness<flowable> {
        INSTANCE;

        @Override
        public MonadAdapter<flowable> adapter() {
            return new FlowableAdapter();
        }

    }
    public static <T> Single<T> single(AnyM<single, ? extends T> anyM){
        return anyM.unwrap();

    }

    static interface SingleWitness<W extends SingleWitness<W>>  extends WitnessType<W> {

    }
    public static enum single implements SingleWitness<single> {
        INSTANCE;

        @Override
        public MonadAdapter<single> adapter() {
            return new SingleAdapter();
        }

    }


    public static <T> Observable<T> observable(AnyM<observable, ? extends T> anyM){
        ObservableReactiveSeqImpl<T> obs = anyM.unwrap();
        return obs.getObservable();
    }

    static interface ObservableWitness<W extends ObservableWitness<W>>  extends WitnessType<W> {

    }
    public static enum observable implements ObservableWitness<observable> {
        INSTANCE;

        @Override
        public MonadAdapter<observable> adapter() {
            return new ObservableAdapter();
        }

    }

}
