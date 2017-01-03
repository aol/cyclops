package com.aol.cyclops2.react.collectors.lazy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

@AllArgsConstructor
@Getter
@Wither
@Builder
public class MaxActive {

    private final int maxActive;
    private final int reduceTo;

    public static final MaxActive IO = new MaxActive(
                                                     100, 90);
    public static final MaxActive CPU = new MaxActive(
                                                      Runtime.getRuntime()
                                                             .availableProcessors(),
                                                      Runtime.getRuntime()
                                                             .availableProcessors() - 1);
    public static final MaxActive SEQUENTIAL = new MaxActive(
                                                             10, 1);

}
