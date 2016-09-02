package com.aol.cyclops.internal.react.async.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

@ToString
@AllArgsConstructor
@Wither
public class ExecutionPipeline {
    private final PStack<Function> functionList;
    private final PStack<Executor> execList;
    private final PStack<Function> firstRecover;
    private final Consumer<Throwable> onFail;

    public ExecutionPipeline() {
        functionList = ConsPStack.empty();
        execList = ConsPStack.empty();
        firstRecover = ConsPStack.empty();
        onFail = null;
    }

    public boolean isSequential() {
        if (execList.size() == 0)
            return true;
        if (execList.size() == 1 && execList.get(0) == null)
            return true;
        return false;
    }

    public <T> ExecutionPipeline peek(Consumer<? super T> c) {
        return this.<T, Object> thenApply(i -> {
            c.accept(i);
            return i;
        });

    }

    public <T, R> ExecutionPipeline thenApplyAsync(Function<? super T, ? extends R> fn, Executor exec) {

        return new ExecutionPipeline(
                                     addFn(fn), addExec(exec), firstRecover, onFail);

    }

    public <T, R> ExecutionPipeline thenComposeAsync(Function<Object, CompletableFuture<?>> fn, Executor exec) {

        return new ExecutionPipeline(
                                     addFn(t -> fn.apply(t)
                                                  .join()),
                                     addExec(exec), firstRecover, onFail);
    }

    public <T, R> ExecutionPipeline thenCompose(Function<? super T, CompletableFuture<? extends R>> fn) {
        Function<T, R> unpacked = t -> fn.apply(t)
                                         .join();
        return new ExecutionPipeline(
                                     swapFn(unpacked), execList.size() == 0 ? execList.plus(null) : execList, firstRecover, onFail);

    }

    public <T, R> ExecutionPipeline thenApply(Function<T, R> fn) {
        return new ExecutionPipeline(
                                     swapComposeFn(fn), execList.size() == 0 ? execList.plus(null) : execList, firstRecover, onFail);
    }

    public <X extends Throwable, T> ExecutionPipeline exceptionally(Function<? super X, ? extends T> fn) {
        if (functionList.size() > 0) {
            Function before = functionList.get(functionList.size() - 1);
            Function except = t -> {
                try {
                    return before.apply(t);
                } catch (Throwable e) {
                    return fn.apply((X) e);
                }
            };

            return new ExecutionPipeline(
                                         swapFn(except), execList, firstRecover, onFail);
        }

        return new ExecutionPipeline(
                                     functionList, execList, addFirstRecovery(fn), onFail);

    }

    public <X extends Throwable, T> ExecutionPipeline whenComplete(BiConsumer<? super T, ? super X> fn) {

        Function before = functionList.get(functionList.size() - 1);

        Function except = t -> {
            T res = null;
            X ex = null;
            try {
                res = (T) before.apply(t);
            } catch (Throwable e) {
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
                                 functionList.toArray(new Function[0]), execList.toArray(new Executor[0]), this.firstRecover.toArray(new Function[0]),
                                 onFail);
    }

    public static ExecutionPipeline empty() {
        ExecutionPipeline pipeline = new ExecutionPipeline();

        return pipeline;
    }

    private PStack<Executor> addExec(Executor exec) {
        if (execList.size() == 0)
            return execList.plus(exec);
        return execList.plus(execList.size(), exec);
    }

    private PStack<Function> addFirstRecovery(Function fn) {
        if (firstRecover.size() == 0)
            return firstRecover.plus(fn);

        return firstRecover.plus(firstRecover.size(), fn);
    }

    private PStack<Function> addFn(Function fn) {
        if (functionList.size() == 0)
            return functionList.plus(fn);

        return functionList.plus(functionList.size(), fn);
    }

    private PStack<Function> swapFn(Function fn) {
        if (functionList.size() == 0)
            return functionList.plus(fn);
        Function before = functionList.get(functionList.size() - 1);
        PStack<Function> removed = functionList.minus(functionList.size() - 1);
        return removed.plus(removed.size(), fn);

    }

    private PStack<Function> swapComposeFn(Function fn) {
        if (functionList.size() == 0) {
            if (this.firstRecover.size() == 0) {
                return functionList.plus(fn);
            } else {
                Function except = t -> {
                    try {
                        return fn.apply(t);
                    } catch (Throwable e) {
                        return composeFirstRecovery().apply(e);
                    }
                };
                return functionList.plus(except);
            }

        }
        Function before = functionList.get(functionList.size() - 1);
        PStack<Function> removed = functionList.minus(functionList.size() - 1);
        return removed.plus(removed.size(), fn.compose(before));
    }

    private Function composeFirstRecovery() {
        return firstRecover.stream()
                           .reduce((fn1, fn2) -> {
                               Function except = t -> {
                                   try {
                                       return fn1.apply(t);
                                   } catch (Throwable e) {
                                       return fn2.apply(e);
                                   }
                               };
                               return except;

                           })
                           .get();

    }

    int functionListSize() {
        return this.functionList.size();
    }

    public ExecutionPipeline onFail(Consumer<Throwable> onFail) {
        return this.withOnFail(onFail);
    }
}