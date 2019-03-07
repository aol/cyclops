package cyclops.reactive;

import com.oath.cyclops.rx2.adapter.ObservableReactiveSeqImpl;
import cyclops.companion.rx2.Observables;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

/*
 Factory methods for creating Observable backed ReactiveSeq instances
 */
@Deprecated
public interface ObservableReactiveSeq {

    public static <T> ReactiveSeq<T> reactiveSeq(Observable<T> observable) {
        return new ObservableReactiveSeqImpl<>(observable);
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



}
