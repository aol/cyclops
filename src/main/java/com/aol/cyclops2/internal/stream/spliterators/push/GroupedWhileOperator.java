package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class GroupedWhileOperator<T,C extends Collection<? super T>,R> extends BaseOperator<T,R> {



    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    final Predicate<? super T> predicate;

    public GroupedWhileOperator(Operator<T> source, Supplier<? extends C> factory,
                                Function<? super C, ? extends R> finalizer,
                                final Predicate<? super T> predicate){
        super(source);
        this.factory = factory;
        this.finalizer = finalizer;
        this.predicate = predicate;



    }

    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Collection[] next = {factory.get()};
        return source.subscribe(e-> {
                    try {
                        next[0].add(e);
                        if(predicate.test(e)){
                            onNext.accept(finalizer.apply((C)next[0]));
                            next[0] = factory.get();
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    onNext.accept(finalizer.apply((C)next[0]));
                    onComplete.run();
                });
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Collection[] next = {factory.get()};
        source.subscribeAll(e-> {
                    try {
                        next[0].add(e);
                        if(predicate.test(e)){
                            onNext.accept(finalizer.apply((C)next[0]));
                            next[0] = factory.get();
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    onNext.accept(finalizer.apply((C)next[0]));
                    onCompleteDs.run();
                });
    }
}
