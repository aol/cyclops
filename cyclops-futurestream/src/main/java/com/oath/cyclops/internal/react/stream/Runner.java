package com.oath.cyclops.internal.react.stream;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.oath.cyclops.internal.react.async.future.FastFuture;
import com.oath.cyclops.internal.react.exceptions.SimpleReactProcessingException;
import com.oath.cyclops.types.futurestream.Continuation;
import com.oath.cyclops.async.adapters.Queue.ClosedQueueException;
import com.oath.cyclops.internal.react.exceptions.FilteredExecutionPathException;
import com.oath.cyclops.react.collectors.lazy.EmptyCollector;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Runner<U> {

    private final Runnable runnable;

    public boolean run(final LazyStreamWrapper<U> lastActive, final EmptyCollector<U> collector) {

        try {
            lastActive.injectFutures()
                      .forEach(n -> {

                          collector.accept(n);
                      });
            collector.getResults();
        } catch (final SimpleReactProcessingException e) {

        } catch (final java.util.concurrent.CompletionException e) {

        } catch (final Throwable e) {

        }

        runnable.run();
        return true;

    }

    public Continuation

    runContinuations(final LazyStreamWrapper lastActive, final EmptyCollector collector, boolean blocking) {

        final Iterator<FastFuture> it = lastActive.injectFutures()
                                                  .iterator();

        final Continuation[] cont = new Continuation[1];

        final Continuation finish = new Continuation(
                                                     () -> {
                                                            collector.afterResults(()->{
                                                            runnable.run();
                                                            throw new ClosedQueueException();
                                                        });
                                                        return Continuation.empty();


                                                     });
        final Continuation blockingFinish = new Continuation(
            () -> {

                    collector.getResults();
                    runnable.run();
                    throw new ClosedQueueException();


            });
        final Continuation finishNoCollect = new Continuation(
                                                              () -> {
                                                                  runnable.run();

                                                                  throw new ClosedQueueException();

                                                              });

        cont[0] = new Continuation(
                                   () -> {
                                       try {

                                           if (it.hasNext()) {

                                               final FastFuture f = it.next();

                                               handleFilter(cont, f);//if FastFuture has been filtered out, we need to move to the next one instead

                                               collector.accept(f);
                                           }

                                           if (it.hasNext())
                                               return cont[0];
                                           else {
                                               return blocking ? blockingFinish.proceed() : finish.proceed();
                                           }
                                       } catch (final SimpleReactProcessingException e) {

                                       } catch (final java.util.concurrent.CompletionException e) {

                                       } catch (final Throwable e) {

                                           collector.getSafeJoin()
                                                    .apply(FastFuture.failedFuture(e));
                                       }
                                       return finishNoCollect;

                                   });

        return cont[0];

    }

    private <T> void handleFilter(final Continuation[] cont, final FastFuture<T> f) {
        final AtomicInteger called = new AtomicInteger(
                                                       0);
        f.essential(event -> {

            if (event.exception != null && event.exception.getCause() instanceof FilteredExecutionPathException) {
                if (called.compareAndSet(0, 1))
                    cont[0].proceed();

            }

        });
    }

}
