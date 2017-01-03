package com.aol.cyclops2.react;

import java.util.concurrent.Executors;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;

import lombok.Getter;
import lombok.Setter;

/*
 * Class that defines the default Retry Executor Instance (changable via set method)
 * 
 * @author johnmcclean
 *
 */
public class RetryBuilder {

    @Getter
    @Setter
    private static volatile AsyncRetryExecutor defaultInstance = factory.defaultInstance.getRetryExecutor();

    public enum factory {
        defaultInstance(new AsyncRetryExecutor(
                                               Executors.newScheduledThreadPool(Runtime.getRuntime()
                                                                                       .availableProcessors())).retryOn(Throwable.class)
                                                                                                               .withExponentialBackoff(500, 2)
                                                                                                               . //500ms times 2 after each retry
                                                                                                               withMaxDelay(10_000)
                                                                                                               . //10 seconds
                                                                                                               withUniformJitter()
                                                                                                               . //add between +/- 100 ms randomly
                                                                                                               withMaxRetries(20));
        @Getter
        private final AsyncRetryExecutor retryExecutor;

        private factory(final AsyncRetryExecutor retryExecutor) {
            this.retryExecutor = retryExecutor;
        }
    }

    public AsyncRetryExecutor parallelism(final int parallelism) {
        return defaultInstance.withScheduler(Executors.newScheduledThreadPool(parallelism));
    }
}
