package com.aol.cyclops.internal.monads;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.extensability.Comprehender;

import lombok.experimental.Wither;

//@AllArgsConstructor
public class MonadWrapper<T> implements Monad<T>, Decomposable {
    @Wither
    private final Object monad;
    private final Class orgType;
    private final Comprehender<?> comp;

    public MonadWrapper(final Object monad,Comprehender<T> comp) {
        this.monad = monad;
        orgType = monad.getClass();
        this.comp  = comp;
    }

    public Comprehender<?> adapter(){
        return comp;
    }

    public static <T> Monad<T> of(final Object of,Comprehender<?> comp) {
        return new MonadWrapper(
                                of,comp);

    }

    @Override
    public Object unwrap() {
        return monad;
    }

    @Override
    public <X> AnyMValue<X> anyMValue() {
        if (monad instanceof AnyMValue)
            return (AnyMValue<X>) monad;
        return new AnyMValueImpl(
                                    (Monad) this, orgType,comp);
    }

    @Override
    public <X> AnyMSeq<X> anyMSeq() {
        if (monad instanceof AnyMSeq)
            return (AnyMSeq<X>) monad;
        return new AnyMSeqImpl(
                                  (Monad) this, orgType,comp);
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

    public MonadWrapper(final Object monad, final Class orgType, Comprehender<?> comp) {
        super();
        this.monad = monad;
        this.orgType = orgType;
        this.comp = comp;
    }
}
