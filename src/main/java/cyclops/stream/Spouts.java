package cyclops.stream;

import com.aol.cyclops2.internal.stream.ReactiveStreamX;
import com.aol.cyclops2.internal.stream.StreamX;
import com.aol.cyclops2.internal.stream.spliterators.IterateSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.Streams;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 14/01/2017.
 */

public interface Spouts {

    /**
     * Create an Subscriber for Observable style asynchronous push based Streams.
     * Streams generated from AsyncSubscribers are not backpressure aware (in cases
     * where backpressue is not needed they may perform better).
     * For backpressure aware Streams see {@link Spouts#reactiveSubscriber}
     *
     * @param <T> Stream data type
     * @return Async Stream Subscriber
     */
    static <T> AsyncSubscriber<T> asyncSubscriber(){
        return new AsyncSubscriber<T>();
    }

    /**
     *   Create an Subscriber for Observable style asynchronous push based Streams,
     *   that implements backpressure internally via the reactive-streams spec.
     *
     *   Subscribers signal demand via their subscription and publishers push data to subscribers
     *   synchronously or asynchronously, never exceeding signalled demand
     *
     * @param <T> Stream data type
     * @return An async Stream Subscriber that supports efficient backpressure via reactive-streams
     */
    static <T> ReactiveSubscriber<T> reactiveSubscriber(){
        return new ReactiveSubscriber<T>();
    }
    static <T> ReactiveSeq<T> reactiveStream(Operator<T> s){
        return new ReactiveStreamX<>(s).withAsync(ReactiveStreamX.Type.BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> asyncStream(Operator<T> s){
        return new ReactiveStreamX<>(s).withAsync(ReactiveStreamX.Type.NO_BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> syncStream(Operator<T> s){
        return new ReactiveStreamX<>(s);
    }

    static <T> ReactiveSeq<T> iterate(final T seed, final UnaryOperator<T> f) {
        return new ReactiveStreamX(new IterateOperator<T>(seed,f));

    }
    public static ReactiveSeq<Integer> range(int start, int end){
        if(start<end)
            return new ReactiveStreamX<Integer>(new RangeIntOperator(start,end));
        else
            return new ReactiveStreamX<Integer>(new RangeIntOperator(end,start));
    }
    public static  ReactiveSeq<Long> rangeLong(long start, long end){
        if(start<end)
            return new ReactiveStreamX<>(new RangeLongOperator(start,end));
        else
            return new ReactiveStreamX<>(new RangeLongOperator(end,start));
    }
    public static  <T> ReactiveSeq<T> of(T value){
        return new ReactiveStreamX<>(new SingleValueOperator<T>(value));
    }
    public static  <T> ReactiveSeq<T> of(T... values){
        return new ReactiveStreamX<>(new ArrayOfValuesOperator<T>(values));
    }
    public static  <T> ReactiveSeq<T> fromIterable(Iterable<T> iterable){
        return new ReactiveStreamX<>(new IterableSourceOperator<T>(iterable));
    }
    public static  <T> ReactiveSeq<T> fromSpliterator(Spliterator<T> spliterator){
        return new ReactiveStreamX<>(new SpliteratorToOperator<T>(spliterator));
    }
    public static  <T> ReactiveSeq<T> concat(Stream<T>... streams){
        Operator<T>[] operators = new Operator[streams.length];
        int index = 0;
        for(Stream<T> next : streams){
            if(next instanceof ReactiveStreamX){
                operators[index] = ((ReactiveStreamX)next).getSource();
            }else{
                operators[index] = new SpliteratorToOperator<T>(next.spliterator());
            }
            index++;
        }

        return new ReactiveStreamX<>(new ArrayConcatonatingOperator<T>(operators));
    }
}
