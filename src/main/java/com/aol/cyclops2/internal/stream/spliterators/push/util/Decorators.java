package com.aol.cyclops2.internal.stream.spliterators.push.util;

import com.aol.cyclops2.internal.stream.spliterators.push.StreamSubscription;
import cyclops.async.Queue;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 14/01/2017.
 */
@UtilityClass
public class Decorators {

    public static <T> Consumer<T> openCheck(StreamSubscription sub, Consumer<T> onNext){
        return e->{
            if(!sub.isOpen){
                throw new Queue.ClosedQueueException();
            }
            onNext.accept(e);
        };
    }
}
