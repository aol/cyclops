package cyclops.streams;

import cyclops.companion.rx2.Flowables;
import cyclops.companion.rx2.Maybes;
import cyclops.companion.rx2.Observables;
import cyclops.control.Future;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.*;

import java.util.function.Function;

/*
 * Extensions for leveraging Rx Observable operators with cyclops-react ReactiveSeq instances
 *
 * <pre>
 * {@code
 *   ReactiveSeq.of(1,2,3)
                .to(lift(new Observable.Operator<Integer,Integer>(){


                    @Override
                    public Subscriber<? super Integer> call(Subscriber<? super Integer> subscriber) {
                        return subscriber; // operator code
                    }
                            }))
                   .map(i->i+1)
                   .to(observable(o->o.buffer(10)));

   }
 * </pre>
 *
 */
public class Rx2Operators {

    public static <T,R> Function<ReactiveSeq<T>,ReactiveSeq<R>> lift(final ObservableOperator<? extends R, ? super T> operator){

        return s->Observables.reactiveSeq(Observables.observableFrom(s).lift(operator));
    }
    public static <T,R> Function<ReactiveSeq<T>,ReactiveSeq<R>> observable(final Function<? super Observable<? super T>,? extends Observable<? extends R>> fn){
        return s->Observables.<R>reactiveSeq(Observables.narrow(fn.apply(Observables.observableFrom(s))));
    }
    public static <T,R> Function<ReactiveSeq<T>,ReactiveSeq<R>> flowable(final Function<? super Flowable<? super T>,? extends Flowable<? extends R>> fn){
        return s->Flowables.<R>reactiveSeq(Flowables.narrow(fn.apply(Flowables.flowableFrom(s))));
    }
    public static <T,R> Function<Observable<T>,Observable<R>> seq(final Function<? super ReactiveSeq<? super T>,? extends ReactiveSeq<? extends R>> fn){
        return s-> Observables.observableFrom((ReactiveSeq<R>)fn.apply(Observables.reactiveSeq(s)));
    }
    public static <T,R> Function<Flowable<T>,Flowable<R>> reactiveSeq(final Function<? super ReactiveSeq<? super T>,? extends ReactiveSeq<? extends R>> fn){
        return s-> Flowables.flowableFrom((ReactiveSeq<R>)fn.apply(Flowables.reactiveSeq(s)));
    }
    public static <T,R> Function<Future<T>,Future<R>> single(final Function<? super Single<? super T>,? extends Single<? extends R>> fn){

        return s-> Future.<R>fromPublisher(Flowables.narrow(fn.apply(Single.fromPublisher(s)).toFlowable()));
    }
    public static <T,R> Function<Single<T>,Single<R>> future(final Function<? super Future<? super T>,? extends Future<? extends R>> fn){
        return s-> Single.fromPublisher(fn.apply(Future.fromPublisher(s.toFlowable())));
    }
    public static <T,R> Function<cyclops.control.Maybe<T>,cyclops.control.Maybe<R>> rxMaybe(final Function<? super Maybe<? super T>,? extends Maybe<? extends R>> fn){

        return s-> cyclops.control.Maybe.<R>fromPublisher(Flowables.narrow(fn.apply(Single.fromPublisher(s).toMaybe()).toFlowable()));
    }
    public static <T,R> Function<Maybe<T>,Maybe<R>> maybe(final Function<? super cyclops.control.Maybe<? super T>,? extends cyclops.control.Maybe<? extends R>> fn){
        return s-> Maybes.narrow(Flowable.fromPublisher(fn.apply(cyclops.control.Maybe.fromPublisher(s.toFlowable()))).firstElement());
    }
}
