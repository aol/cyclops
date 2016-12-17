package com.aol.cyclops.types;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations;
import org.jooq.lambda.Seq;
import org.reactivestreams.Subscription;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 17/12/2016.
 */
public interface FoldableTraversable<T> extends Traversable<T>,
                                                ReactiveStreamsTerminalOperations<T>,
                                                CyclopsCollectable<T>,
                                                ConvertableSequence<T>,
                                                ExtendedTraversable<T>{



    @Override
    ReactiveSeq<T> stream();
    @Override
    default Seq<T> seq(){
        return Seq.seq(this);
    }
    /**
     * Destructures this Traversable into it's head and tail. If the traversable instance is not a SequenceM or Stream type,
     * whenStream may be more efficient (as it is guaranteed to be lazy).
     *
     * <pre>
     * {@code
     * ListX.of(1,2,3,4,5,6,7,8,9)
    .dropRight(5)
    .plus(10)
    .visit((x,xs) ->
    xs.join(x.>2?"hello":"world")),()->"NIL"
    );
     *
     * }
     * //2world3world4
     *
     * </pre>
     *
     *
     * @param match
     * @return
     */
    default <R> R visit(final BiFunction<? super T, ? super ReactiveSeq<T>, ? extends R> match, final Supplier<? extends R> ifEmpty) {

        final HeadAndTail<T> ht = stream().headAndTail();
        if (ht.isHeadPresent())
            return match.apply(ht.head(), ht.tail());
        return ifEmpty.get();

    }


    /**
     * extract head and tail together, where head is expected to be present
     * Example :
     *
     * <pre>
     * {@code
     *  ReactiveSeq<String> helloWorld = ReactiveSeq.Of("hello","world","last");
    HeadAndTail<String> headAndTail = helloWorld.headAndTail();
    String head = headAndTail.head();

    //head == "hello"

    ReactiveSeq<String> tail =  headAndTail.tail();
    //["world","last]

    }
     *  </pre>
     *
     * @return
     */
    default HeadAndTail<T> headAndTail() {
        return stream().headAndTail();
    }
    @Override
    default <X extends Throwable> Subscription forEachX(long numberOfElements, Consumer<? super T> consumer){
        return stream().forEachX(numberOfElements,consumer);
    }

    @Override
    default <X extends Throwable> Subscription forEachXWithError(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError){
        return stream().forEachXWithError(numberOfElements,consumer,consumerError);
    }

    @Override
    default <X extends Throwable> Subscription forEachXEvents(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete){
        return stream().forEachXEvents(numberOfElements,consumer,consumerError,onComplete);
    }

    @Override
    default <X extends Throwable> void forEachWithError(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError){
        stream().forEachWithError(consumerElement,consumerError);
    }

    @Override
    default <X extends Throwable> void forEachEvent(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete){
        stream().forEachEvent(consumerElement, consumerError, onComplete);
    }
}
