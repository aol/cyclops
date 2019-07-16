package com.oath.cyclops.types.futurestream;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public class Continuation {

    private final Supplier<Continuation> remainderOfWorkToBeDone;

    public Continuation proceed() {
        return remainderOfWorkToBeDone.get();
    }

    public static Empty empty() {

        return new Empty();
    }
    public static EmptyRunnableContinuation emptyRunnable(Runnable r) {

        return new EmptyRunnableContinuation(r);
    }

    public static class Empty extends Continuation {

        public Empty() {
            super(() -> empty());
        }
    }

    public static class EmptyRunnableContinuation extends Continuation implements Runnable {
        final Runnable r;
        public EmptyRunnableContinuation(Runnable r) {
            super(() ->empty());
            this.r = r;
        }

        @Override
        public void run() {

            r.run();
            System.out.println("run!");
        }
    }
}
