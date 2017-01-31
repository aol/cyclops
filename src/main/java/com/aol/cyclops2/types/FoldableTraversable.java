package com.aol.cyclops2.types;

import cyclops.control.Eval;
import cyclops.async.Future;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Try;
import com.aol.cyclops2.types.stream.ConvertableSequence;
import com.aol.cyclops2.types.stream.CyclopsCollectable;
import com.aol.cyclops2.types.stream.HeadAndTail;
import cyclops.function.Fn1;
import org.jooq.lambda.Seq;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 17/12/2016.
 */
public interface FoldableTraversable<T> extends Fn1<Long,T>,
                                                Traversable<T>,
                                                CyclopsCollectable<T>,
                                                ConvertableSequence<T>,
                                                ExtendedTraversable<T>{




    /**
     * Perform an async fold on the provided executor
     *
     *  <pre>
     *  {@code
     *    Future<Integer> sum =  ListX.of(1,2,3)
     *                                 .map(this::load)
     *                                 .foldFuture(list->list.reduce(0,(a,b)->a+b),exec)
     *
     *  }
     *  </pre>
     *
     * Similar to @see {@link ReactiveSeq#futureOperations(Executor)}, but returns Future
     *
     * @param fn Folding function
     * @param ex Executor to perform fold on
     * @return Future that will contain the result when complete
     */
    default <R> Future<R> foldFuture(Executor ex,Function<? super FoldableTraversable<T>,? extends R> fn){
        return Future.ofSupplier(()->fn.apply(this),ex);
    }
    default Future<Void> runFuture(Executor ex, Consumer<? super FoldableTraversable<T>> fn){
        return Future.ofSupplier(()-> { fn.accept(this); return null;},ex);
    }

    /**
     * Perform a maybe caching fold (results are memoized)
     *  <pre>
     *  {@code
     *    Eval<Integer> sum =  ListX.of(1,2,3)
     *                                 .map(this::load)
     *                                 .foldLazy(list->list.reduce(0,(a,b)->a+b))
     *
     *  }
     *  </pre>
     *
     *  Similar to @see {@link ReactiveSeq#lazyOperations()}, but always returns Eval (e.g. with nested Optionals)
     *
     * @param fn Folding function
     * @return Eval that lazily performs the fold once
     */
    default <R> Eval<R> foldLazy(Function<? super FoldableTraversable<T>,? extends R> fn){
        return Eval.later(()->fn.apply(this));
    }
    default Eval<Void> runLazy(Consumer<? super FoldableTraversable<T>> fn){
        return Eval.later(()->{ fn.accept(this); return null;});
    }

    /**
     * Try a fold, capturing any unhandling execution exceptions (that match the provided classes)
     *  <pre>
     *  {@code
     *    Try<Integer,Throwable> sum =  ListX.of(1,2,3)
     *                                       .map(this::load)
     *                                       .foldLazy(list->list.reduce(0,(a,b)->a+b),IOException.class)
     *
     *  }
     *  </pre>
     * @param fn Folding function
     * @param classes Unhandled Exception types to capture in Try
     * @return Try that eagerly executes the fold and captures specified unhandled exceptions
     */
    default <R, X extends Throwable> Try<R, X> foldTry(Function<? super FoldableTraversable<T>,? extends R> fn,
                                                       final Class<X>... classes){
        return Try.catchExceptions(classes).tryThis(()->fn.apply(this));
    }
    @Override
    default T apply(Long index){
        return this.get(index).orElse(null);
    }


    @Override
    ReactiveSeq<T> stream();
    @Override
    default Seq<T> seq(){
        return Seq.seq(this);
    }
    /**
     * Destructures this Traversable into it's head and tail. If the traversable instance is not a SequenceM or Stream type,
     * whenStream may be more efficient (as it is guaranteed to be maybe).
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
    default <X extends Throwable> Subscription subscribe(Consumer<? super T> consumer){
        Subscription result = CyclopsCollectable.super.subscribe(consumer,e->e.printStackTrace(),()->{});
        return result;
    }

    @Override
    default <X extends Throwable> Subscription subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError){
        Subscription result = CyclopsCollectable.super.subscribe(consumer,consumerError,()->{});
        return result;
    }

    @Override
    default <X extends Throwable> Subscription subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete){
        Subscription result = CyclopsCollectable.super.subscribe(consumer,consumerError,onComplete);
        return result;
    }
    @Override
    default <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer){
        return stream().forEach(numberOfElements,consumer);
    }

    @Override
    default <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError){
        return stream().forEach(numberOfElements,consumer,consumerError);
    }

    @Override
    default <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete){
        return stream().forEach(numberOfElements,consumer,consumerError,onComplete);
    }

    @Override
    default <X extends Throwable> void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError){
        stream().forEach(consumerElement,consumerError);
    }

    @Override
    default <X extends Throwable> void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete){
        stream().forEach(consumerElement, consumerError, onComplete);
    }
}
