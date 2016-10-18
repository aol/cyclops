package com.aol.cyclops.internal.monads;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

import lombok.experimental.Wither;

//@AllArgsConstructor
public class MonadWrapper<T> implements Monad<T>, Decomposable {
    @Wither
    private final Object monad;
    private final Class orgType;

    public MonadWrapper(final Object monad) {
        this.monad = monad;
        orgType = monad.getClass();
    }

    /**
    public MonadWrapper<T> withMonad(Object o ){
        return new MonadWrapper(monad,orgType);
    }**/

    public static <T> Monad<T> of(final Object of) {
        return new MonadWrapper(
                                of);

    }

    @Override
    public Object unwrap() {
        return monad;
    }

    @Override
    public <X> AnyMValue<X> anyMValue() {
        if (monad instanceof AnyMValue)
            return (AnyMValue<X>) monad;
        return new AnyMValueImpl<X>(
                                    (Monad) this, orgType);
    }

    @Override
    public <X> AnyMSeq<X> anyMSeq() {
        if (monad instanceof AnyMSeq)
            return (AnyMSeq<X>) monad;
        return new AnyMSeqImpl<X>(
                                  (Monad) this, orgType);
    }

    @Override
    public ReactiveSeq<T> sequence() {
        if (monad instanceof ReactiveSeq)
            return (ReactiveSeq) monad;
        return ReactiveSeq.fromStream(stream());
    }

    @Override
    public String toString() {
        return monad.toString();
    }

    public MonadWrapper(final Object monad, final Class orgType) {
        super();
        this.monad = monad;
        this.orgType = orgType;
    }
}
