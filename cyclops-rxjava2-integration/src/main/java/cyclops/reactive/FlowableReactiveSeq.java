package cyclops.reactive;

import com.oath.cyclops.rx2.adapter.FlowableReactiveSeqImpl;
import cyclops.companion.rx2.Flowables;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.util.stream.Stream;

/*
 * Factory methods
 */
public interface FlowableReactiveSeq {
    public static <T> ReactiveSeq<T> reactiveSeq(Flowable<T> flowable){
        return new FlowableReactiveSeqImpl<>(flowable);
    }

    public static <T> ReactiveSeq<T> reactiveSeq(Publisher<T> flowable){
        return new FlowableReactiveSeqImpl<>(Flowable.fromPublisher(flowable));
    }

    public static ReactiveSeq<Integer> range(int start, int end){
        return reactiveSeq(Flowable.range(start,end));
    }
    public static <T> ReactiveSeq<T> of(T... data) {
        return reactiveSeq(Flowable.fromArray(data));
    }
    public static  <T> ReactiveSeq<T> of(T value){
        return reactiveSeq(Flowable.just(value));
    }

    public static <T> ReactiveSeq<T> ofNullable(T nullable){
        if(nullable==null){
            return empty();
        }
        return of(nullable);
    }



    public static <T> ReactiveSeq<T> empty() {
        return reactiveSeq(Flowable.empty());
    }


    public static <T> ReactiveSeq<T> error(Throwable error) {
        return reactiveSeq(Flowable.error(error));
    }







    public static <T> ReactiveSeq<T> from(Publisher<? extends T> source) {
        return reactiveSeq(Flowable.fromPublisher(source));
    }


    public static <T> ReactiveSeq<T> fromIterable(Iterable<? extends T> it) {
        return reactiveSeq(Flowable.fromIterable(it));
    }


    public static <T> ReactiveSeq<T> fromStream(Stream<? extends T> s) {
        return reactiveSeq(Flowables.flowableFrom(ReactiveSeq.fromStream((Stream<T>)s)));
    }








    @SafeVarargs
    public static <T> ReactiveSeq<T> just(T... data) {
        return reactiveSeq(Flowable.fromArray(data));
    }


    public static <T> ReactiveSeq<T> just(T data) {
        return reactiveSeq(Flowable.just(data));
    }
}
