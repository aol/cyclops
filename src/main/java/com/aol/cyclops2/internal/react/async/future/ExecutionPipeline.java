package com.aol.cyclops2.internal.react.async.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


import cyclops.data.Seq;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

@ToString
@AllArgsConstructor
@Wither
public class ExecutionPipeline {
    private final Seq<Function> functionList;
    private final Seq<Executor> execList;
    private final Seq<Function> firstRecover;
    private final Consumer<Throwable> onFail;

    public ExecutionPipeline() {
        functionList = Seq.empty();
        execList = Seq.empty();
        firstRecover = Seq.empty();
        onFail = null;
    }

    public boolean isSequential() {
        if (execList.size() == 0)
            return true;
        if (execList.size() == 1 && execList.get(0) == null)
            return true;
        return false;
    }

    public <T> ExecutionPipeline peek(final Consumer<? super T> c) {
        return this.<T, Object> thenApply(i -> {
            c.accept(i);
            return i;
        });

    }

    public <T, R> ExecutionPipeline thenApplyAsync(final Function<? super T, ? extends R> fn, final Executor exec) {

        return new ExecutionPipeline(
                                     addFn(fn), addExec(exec), firstRecover, onFail);

    }

    public <T, R> ExecutionPipeline thenComposeAsync(final Function<Object, CompletableFuture<?>> fn, final Executor exec) {

        return new ExecutionPipeline(
                                     addFn(t -> fn.apply(t)
                                                  .join()),
                                     addExec(exec), firstRecover, onFail);
    }

    public <T, R> ExecutionPipeline thenCompose(final Function<? super T, CompletableFuture<? extends R>> fn) {
        final Function<T, R> unpacked = t -> fn.apply(t)
                                               .join();
        return new ExecutionPipeline(
                                     swapFn(unpacked), execList.size() == 0 ? execList.plus(null) : execList, firstRecover, onFail);

    }

    public <T, R> ExecutionPipeline thenApply(final Function<T, R> fn) {
        return new ExecutionPipeline(
                                     swapComposeFn(fn), execList.size() == 0 ? execList.plus(null) : execList, firstRecover, onFail);
    }

    public <X extends Throwable, T> ExecutionPipeline exceptionally(final Function<? super X, ? extends T> fn) {
        if (functionList.size() > 0) {
            final Function before = functionList.getOrElse(functionList.size() - 1,null);
            final Function except = t -> {
                try {
                    return before.apply(t);
                } catch (final Throwable e) {
                    return fn.apply((X) e);
                }
            };

            return new ExecutionPipeline(
                                         swapFn(except), execList, firstRecover, onFail);
        }

        return new ExecutionPipeline(
                                     functionList, execList, addFirstRecovery(fn), onFail);

    }

    public <X extends Throwable, T> ExecutionPipeline whenComplete(final BiConsumer<? super T, ? super X> fn) {

        final Function before = functionList.getOrElse(functionList.size() - 1,null);

        final Function except = t -> {
            T res = null;
            X ex = null;
            try {
                res = (T) before.apply(t);
            } catch (final Throwable e) {
                ex = (X) e;
            }
            fn.accept(res, ex);
            if (ex != null)
                throw (RuntimeException) ex;
            return res;
        };

        return new ExecutionPipeline(
                                     swapFn(except), execList, firstRecover, onFail);
    }

    public FinalPipeline toFinalPipeline() {

        return new FinalPipeline(
                                 functionList.stream().toArray(i->new Function[i]), execList.stream().toArray(i->new Executor[i]), firstRecover.stream().toArray(i->new Function[i]),
                                 onFail);
    }

    public static ExecutionPipeline empty() {
        final ExecutionPipeline pipeline = new ExecutionPipeline();

        return pipeline;
    }

    private Seq<Executor> addExec(final Executor exec) {
        if (execList.size() == 0)
            return execList.plus(exec);
        return execList.insertAt(execList.size(), exec);
    }

    private Seq<Function> addFirstRecovery(final Function fn) {
        if (firstRecover.size() == 0)
            return firstRecover.plus(fn);

        return firstRecover.insertAt(firstRecover.size(), fn);
    }

    private Seq<Function> addFn(final Function fn) {
        if (functionList.size() == 0)
            return functionList.plus(fn);

        return functionList.insertAt(functionList.size(), fn);
    }

    private Seq<Function> swapFn(final Function fn) {
        if (functionList.size() == 0)
            return functionList.plus(fn);
        functionList.get(functionList.size() - 1);
        final Seq<Function> removed = functionList.removeAt(functionList.size() - 1);
        return removed.insertAt(removed.size(), fn);

    }

    private Seq<Function> swapComposeFn(final Function fn) {
        if (functionList.size() == 0) {
            if (firstRecover.size() == 0) {
                return functionList.plus(fn);
            } else {
                final Function except = t -> {
                    try {
                        return fn.apply(t);
                    } catch (final Throwable e) {
                        return composeFirstRecovery().apply(e);
                    }
                };
                return functionList.plus(except);
            }

        }
        final Function before = functionList.getOrElse(functionList.size() - 1,null);
        final Seq<Function> removed = functionList.removeAt(functionList.size() - 1);
        return removed.insertAt(removed.size(), fn.compose(before));
    }

    private Function composeFirstRecovery() {
        return firstRecover.stream()
                           .reduce((fn1, fn2) -> {
                               final Function except = t -> {
                                   try {
                                       return fn1.apply(t);
                                   } catch (final Throwable e) {
                                       return fn2.apply(e);
                                   }
                               };
                               return except;

                           })
                           .get();

    }

    int functionListSize() {
        return functionList.size();
    }

    public ExecutionPipeline onFail(final Consumer<Throwable> onFail) {
        return withOnFail(onFail);
    }
}