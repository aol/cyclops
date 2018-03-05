package cyclops.companion.rx2;

import com.oath.anym.AnyMSeq;
import com.oath.cyclops.rx2.adapter.ObservableReactiveSeq;
import cyclops.control.Either;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.Rx2Witness;
import cyclops.monads.Rx2Witness.observable;
import cyclops.monads.WitnessType;
import cyclops.monads.XorM;
import cyclops.monads.transformers.StreamT;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Companion class for working with RxJava Observable types
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Observables {


    public static  <W1 extends WitnessType<W1>,T> XorM<W1,observable,T> xorM(Observable<T> type){
        return XorM.right(anyM(type));
    }
    public static <T,W extends WitnessType<W>> AnyM<W,Observable<T>> fromStream(AnyM<W,Stream<T>> anyM){
        return anyM.map(s->fromStream(s));
    }

    public static  <T,R> Observable<R> tailRec(T initial, Function<? super T, ? extends Observable<? extends Either<T, R>>> fn) {
        Observable<Either<T, R>> next = Observable.just(Either.left(initial));

        boolean newValue[] = {true};
        for(;;){

            next = next.flatMap(e -> e.visit(s -> {
                        newValue[0]=true;
                        return fn.apply(s); },
                    p -> {
                        newValue[0]=false;
                        return Observable.just(e);
                    }));
            if(!newValue[0])
                break;

        }

        return next.filter(Either::isRight).map(e->e.orElse(null));
    }
    public static <T> Observable<T> raw(AnyM<observable,T> anyM){
        return Rx2Witness.observable(anyM);
    }
    public static <T> Observable<T> narrow(Observable<? extends T> observable) {
        return (Observable<T>)observable;
    }
    public static <T> ReactiveSeq<T> reactiveSeq(Observable<T> observable) {
        return new ObservableReactiveSeq<>(observable);
    }
    public static  <T> Observable<T> observableFrom(ReactiveSeq<T> stream){
        return stream.visit(sync->fromStream(stream),
                rs->observable(stream),
                async->Observable.create(new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> rxSubscriber) throws Exception {

                        stream.forEach(rxSubscriber::onNext,rxSubscriber::onError,rxSubscriber::onComplete);
                    }

        }));


    }
    public static  <T> Observable<T> fromStream(Stream<T> s){

        if(s instanceof  ReactiveSeq) {
            ReactiveSeq<T> stream = (ReactiveSeq<T>)s;
            return stream.visit(sync -> Observable.fromIterable(stream),
                    rs -> observable(stream),
                    async -> Observable.create(new ObservableOnSubscribe<T>() {
                        @Override
                        public void subscribe(ObservableEmitter<T> rxSubscriber) throws Exception {

                            stream.forEach(rxSubscriber::onNext,rxSubscriber::onError,rxSubscriber::onComplete);
                        }
                    }));
        }
        return Observable.fromIterable(ReactiveSeq.fromStream(s));
    }
    public static <W extends WitnessType<W>,T> StreamT<W,T> observablify(StreamT<W,T> nested){
        AnyM<W, Stream<T>> anyM = nested.unwrap();
        AnyM<W, ReactiveSeq<T>> fluxM = anyM.map(s -> {
            if (s instanceof ObservableReactiveSeq) {
                return (ObservableReactiveSeq)s;
            }
            if(s instanceof ReactiveSeq){
                return new ObservableReactiveSeq<T>(Observables.observableFrom((ReactiveSeq<T>) s));
            }
            if (s instanceof Publisher) {
                return new ObservableReactiveSeq<T>(Observables.observable((Publisher) s));
            }
            return new ObservableReactiveSeq<T>(fromStream(s));
        });
        StreamT<W, T> res = StreamT.of(fluxM);
        return res;
    }

    public static <W extends WitnessType<W>,T,R> R nestedObservable(StreamT<W,T> nested, Function<? super AnyM<W,Observable<T>>,? extends R> mapper){
        return mapper.apply(nestedObservable(nested));
    }
    public static <W extends WitnessType<W>,T> AnyM<W,Observable<T>> nestedObservable(StreamT<W,T> nested){
        AnyM<W, Stream<T>> anyM = nested.unwrap();
        return anyM.map((Stream<T> s)->{
            if(s instanceof ObservableReactiveSeq){
                return ((ObservableReactiveSeq)s).getObservable();
            }
            if(s instanceof ReactiveSeq){
                return Observables.observableFrom((ReactiveSeq<T>) s);
            }
            if (s instanceof Publisher) {
                return Observables.observable((Publisher) s);
            }
            return Observables.fromStream(s);
        });
    }

    public static <W extends WitnessType<W>,T> StreamT<W,T> liftM(AnyM<W,Observable<T>> nested){

        AnyM<W, ReactiveSeq<T>> monad = nested.map(s -> new ObservableReactiveSeq<T>(s));
        return StreamT.of(monad);
    }
    /**
     * Convert an Observable to a reactive-streams Publisher
     *
     * @param observable To convert
     * @return reactive-streams Publisher
     */
    public static <T> Publisher<T> publisher(Observable<T> observable) {
        return observable.toFlowable(BackpressureStrategy.BUFFER);
    }

    /**
     * Convert an Observable to a cyclops-react ReactiveSeq
     *
     * @param observable To conver
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> connectToReactiveSeq(Observable<T> observable) {
        return Spouts.async(s->{
           observable.subscribe(s::onNext,e->{
               s.onError(e);
               s.onComplete();
           },s::onComplete);

        });

    }


    /**
     * Convert a Publisher to an observable
     *
     * @param publisher To convert
     * @return Observable
     */
    public static <T> Observable<T> observable(Publisher<T> publisher) {
        return Flowable.fromPublisher(publisher).toObservable();
    }







    public static <T> ReactiveSeq<T> empty() {
        return reactiveSeq(Observable.empty());
    }

    public static <T> ReactiveSeq<T> error(Throwable exception) {
        return reactiveSeq(Observable.error(exception));
    }






    public static <T> ReactiveSeq<T> from(Iterable<? extends T> iterable) {
        return reactiveSeq(Observable.fromIterable(iterable));
    }



    public static ReactiveSeq<Long> interval(long interval, TimeUnit unit) {
        return interval(interval, interval, unit, Schedulers.computation());
    }


    public static ReactiveSeq<Long> interval(long interval, TimeUnit unit, Scheduler scheduler) {
        return interval(interval, interval, unit, scheduler);
    }


    public static ReactiveSeq<Long> interval(long initialDelay, long period, TimeUnit unit) {
        return interval(initialDelay, period, unit, Schedulers.computation());
    }


    public static ReactiveSeq<Long> interval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        return reactiveSeq(Observable.interval(initialDelay,period,unit,scheduler));
    }


    public static <T> ReactiveSeq<T> just(final T value) {
        return reactiveSeq(Observable.just(value));
    }
    @SafeVarargs
    public static <T> ReactiveSeq<T> just(final T... values) {
        T[] array = values;
        return reactiveSeq(Observable.fromArray(array));
    }
    public static <T> ReactiveSeq<T> of(final T value) {
        return just(value);
    }
    @SafeVarargs
    public static <T> ReactiveSeq<T> of(final T... values) {
        return just(values);
    }


    public static <T> ReactiveSeq<T> merge(Iterable<? extends Observable<? extends T>> sequences) {
        return merge(from(sequences));
    }


    public static <T> ReactiveSeq<T> merge(Iterable<? extends Observable<? extends T>> sequences, int maxConcurrent) {
        return merge(from(sequences), maxConcurrent);
    }

    public static <T> ReactiveSeq<T> merge(Observable<? extends Observable<? extends T>> source) {
        return reactiveSeq(Observable.merge(source));
    }


    public static <T> ReactiveSeq<T> merge(Observable<? extends Observable<? extends T>> source, int maxConcurrent) {
       return reactiveSeq(Observable.merge(source,maxConcurrent));
    }

    public static <T> ReactiveSeq<T> mergeDelayError(Observable<? extends Observable<? extends T>> source) {
        return reactiveSeq(Observable.mergeDelayError(source));
    }

    public static <T> ReactiveSeq<T> mergeDelayError(Observable<? extends Observable<? extends T>> source, int maxConcurrent) {
        return reactiveSeq(Observable.mergeDelayError(source,maxConcurrent));
    }

    public static <T> ReactiveSeq<T> mergeDelayError(Iterable<? extends Observable<? extends T>> sequences) {
        return mergeDelayError(from(sequences));
    }

    public static <T> ReactiveSeq<T> mergeDelayError(Iterable<? extends Observable<? extends T>> sequences, int maxConcurrent) {
        return mergeDelayError(from(sequences), maxConcurrent);
    }



    public static <T> ReactiveSeq<T> never() {
        return reactiveSeq(Observable.never());
    }

    public static ReactiveSeq<Integer> range(int start, int count) {
       return reactiveSeq(Observable.range(start,count));
    }



    public static <T> ReactiveSeq<T> switchOnNext(Observable<? extends Observable<? extends T>> sequenceOfSequences) {
        return reactiveSeq(Observable.switchOnNext(sequenceOfSequences));
    }


    public static <T> ReactiveSeq<T> switchOnNextDelayError(Observable<? extends Observable<? extends T>> sequenceOfSequences) {
        return reactiveSeq(Observable.switchOnNext(sequenceOfSequences));
    }


    public static ReactiveSeq<Long> timer(long initialDelay, long period, TimeUnit unit) {
        return interval(initialDelay, period, unit, Schedulers.computation());
    }


    public static ReactiveSeq<Long> timer(long delay, TimeUnit unit) {
        return timer(delay, unit, Schedulers.computation());
    }


    public static ReactiveSeq<Long> timer(long delay, TimeUnit unit, Scheduler scheduler) {
        return reactiveSeq(Observable.timer(delay,unit,scheduler));
    }




    /**
     * Construct an AnyM type from an Observable. This allows the Observable to be manipulated according to a standard interface
     * along with a vast array of other Java Monad implementations
     *
     * <pre>
     * {@code
     *
     *    AnyMSeq<Integer> obs = Observables.anyM(Observable.just(1,2,3));
     *    AnyMSeq<Integer> transformedObs = myGenericOperation(obs);
     *
     *    public AnyMSeq<Integer> myGenericOperation(AnyMSeq<Integer> monad);
     * }
     * </pre>
     *
     * @param obs Observable to wrap inside an AnyM
     * @return AnyMSeq wrapping an Observable
     */
    public static <T> AnyMSeq<observable,T> anyM(Observable<T> obs) {
        return AnyM.ofSeq(reactiveSeq(obs), observable.INSTANCE);
    }

    /**
     * Perform a For Comprehension over a Observable, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Observables.
     *
     *  <pre>
     * {@code
     *
     *   import static com.oath.cyclops.reactor.Observables.forEach4;
     *
    forEach4(Observable.range(1,10),
    a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
    (a,b) -> Maybe.<Integer>of(a+b),
    (a,b,c) -> Mono.<Integer>just(a+b+c),
    Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Observable
     * @param value2 Nested Observable
     * @param value3 Nested Observable
     * @param value4 Nested Observable
     * @param yieldingFunction  Generates a result per combination
     * @return Observable with an element per combination of nested Observables generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Observable<R> forEach4(Observable<? extends T1> value1,
                                                                     Function<? super T1, ? extends Observable<R1>> value2,
                                                                     BiFunction<? super T1, ? super R1, ? extends Observable<R2>> value3,
                                                                     Function3<? super T1, ? super R1, ? super R2, ? extends Observable<R3>> value4,
                                                                     Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Observable<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Observable<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Observable<R3> c = value4.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });


    }

    /**
     * Perform a For Comprehension over a Observable, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Observables.
     * <pre>
     * {@code
     *
     *  import static com.oath.cyclops.reactor.Observables.forEach4;
     *
     *  forEach4(Observable.range(1,10),
    a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
    (a,b) -> Maybe.<Integer>just(a+b),
    (a,b,c) -> Mono.<Integer>just(a+b+c),
    (a,b,c,d) -> a+b+c+d <100,
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Observable
     * @param value2 Nested Observable
     * @param value3 Nested Observable
     * @param value4 Nested Observable
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Observable with an element per combination of nested Observables generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Observable<R> forEach4(Observable<? extends T1> value1,
                                                                     Function<? super T1, ? extends Observable<R1>> value2,
                                                                     BiFunction<? super T1, ? super R1, ? extends Observable<R2>> value3,
                                                                     Function3<? super T1, ? super R1, ? super R2, ? extends Observable<R3>> value4,
                                                                     Function4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                     Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Observable<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Observable<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Observable<R3> c = value4.apply(in,ina,inb);
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    /**
     * Perform a For Comprehension over a Observable, accepting 2 generating functions.
     * This results in a three level nested internal iteration over the provided Observables.
     *
     * <pre>
     * {@code
     *
     * import static com.oath.cyclops.reactor.Observables.forEach;
     *
     * forEach(Observable.range(1,10),
    a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
    (a,b) -> Maybe.<Integer>of(a+b),
    Tuple::tuple);
     *
     * }
     * </pre>
     *
     *
     * @param value1 top level Observable
     * @param value2 Nested Observable
     * @param value3 Nested Observable
     * @param yieldingFunction Generates a result per combination
     * @return Observable with an element per combination of nested Observables generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Observable<R> forEach3(Observable<? extends T1> value1,
                                                             Function<? super T1, ? extends Observable<R1>> value2,
                                                             BiFunction<? super T1, ? super R1, ? extends Observable<R2>> value3,
                                                             Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Observable<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Observable<R2> b = value3.apply(in, ina);
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });

    }
    /**
     * Perform a For Comprehension over a Observable, accepting 2 generating functions.
     * This results in a three level nested internal iteration over the provided Observables.
     * <pre>
     * {@code
     *
     * import static com.oath.cyclops.reactor.Observables.forEach;
     *
     * forEach(Observable.range(1,10),
    a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
    (a,b) -> Maybe.<Integer>of(a+b),
    (a,b,c) ->a+b+c<10,
    Tuple::tuple).toListX();
     * }
     * </pre>
     *
     * @param value1 top level Observable
     * @param value2 Nested Observable
     * @param value3 Nested Observable
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T1, T2, R1, R2, R> Observable<R> forEach3(Observable<? extends T1> value1,
                                                             Function<? super T1, ? extends Observable<R1>> value2,
                                                             BiFunction<? super T1, ? super R1, ? extends Observable<R2>> value3,
                                                             Function3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                                                             Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Observable<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Observable<R2> b = value3.apply(in,ina);
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });



        });

    }

    /**
     * Perform a For Comprehension over a Observable, accepting an additonal generating function.
     * This results in a two level nested internal iteration over the provided Observables.
     *
     * <pre>
     * {@code
     *
     *  import static com.oath.cyclops.reactor.Observables.forEach;
     *  forEach(Observable.range(1, 10), i -> Observable.range(i, 10), Tuple::tuple)
    .subscribe(System.out::println);

    //(1, 1)
    (1, 2)
    (1, 3)
    (1, 4)
    ...
     *
     * }</pre>
     *
     * @param value1 top level Observable
     * @param value2 Nested Observable
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Observable<R> forEach(Observable<? extends T> value1, Function<? super T, Observable<R1>> value2,
                                                   BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Observable<R1> a = value2.apply(in);
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }

    /**
     *
     * <pre>
     * {@code
     *
     *   import static com.oath.cyclops.reactor.Observables.forEach;
     *
     *   forEach(Observable.range(1, 10), i -> Observable.range(i, 10),(a,b) -> a>2 && b<10,Tuple::tuple)
    .subscribe(System.out::println);

    //(3, 3)
    (3, 4)
    (3, 5)
    (3, 6)
    (3, 7)
    (3, 8)
    (3, 9)
    ...

     *
     * }</pre>
     *
     *
     * @param value1 top level Observable
     * @param value2 Nested Observable
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Observable<R> forEach(Observable<? extends T> value1,
                                                   Function<? super T, ? extends Observable<R1>> value2,
                                                   BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                   BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Observable<R1> a = value2.apply(in);
            return a.filter(in2->filterFunction.apply(in,in2))
                    .map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }


}
