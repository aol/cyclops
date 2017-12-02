package com.oath.cyclops.types.recoverable;

import java.util.function.Function;

/**
 * Created by johnmcclean on 23/06/2017.
 */
public interface RecoverableFrom<T,U> extends Recoverable<U>  {
    RecoverableFrom<T,U> recover(Function<? super T,? extends U> fn);
}
