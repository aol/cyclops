package com.aol.cyclops.types;

import java.util.function.Supplier;

public interface OnEmptySwitch<T,R> {
    /**
     * Switch to container created by provided Supplier, if current Container empty
     * 
     * <pre>
     * {@code 
     *     ListX.empty().onEmptySwitch(()->ListX.of(1));
     * } 
     * </pre>
     * 
     * @param supplier to create replacement container
     * @return Either this container or if empty, an alternative returned by the provided supplier
     */
     OnEmptySwitch<T,R> onEmptySwitch(Supplier<? extends R> supplier);
}
