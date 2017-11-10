package com.oath.cyclops.types.futurestream;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public class Continuation {

    private final Supplier<Continuation> remainderOfWorkToBeDone;

    public Continuation proceed() {
        return remainderOfWorkToBeDone.get();
    }

    public static Continuation empty() {

        return new Continuation(
                                () -> empty());
    }
}
