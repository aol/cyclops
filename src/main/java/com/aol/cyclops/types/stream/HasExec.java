package com.aol.cyclops.types.stream;

import java.util.concurrent.Executor;

/**
 * Represents a type that has an executor for asynchronous execution
 * 
 * @author johnmcclean
 *
 */
public interface HasExec {
    /**
     * @return Executor used for asynchronous execution.
     */
    Executor getExec();
}
