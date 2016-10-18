package com.aol.cyclops.internal.comprehensions.donotation;

import java.util.function.Function;

import org.pcollections.PStack;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.util.function.CurryVariance;
import com.aol.cyclops.util.function.OctFunction;

public class DoComp8<T, T1, T2, T3, T4, T5, T6, T7> extends DoComp {
    public DoComp8(final PStack<Entry> assigned, final Class orgType) {
        super(assigned, orgType);

    }

    public DoComp8<T, T1, T2, T3, T4, T5, T6, T7> filter(
            final Function<T, Function<? super T1, Function<? super T2, Function<? super T3, Function<T4, Function<? super T5, Function<? super T6, Function<T7, Boolean>>>>>>>> f) {
        return new DoComp8<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$internalGUARD" + getAssigned().size(), new Guard(
                                                                                                                                    f))),
                             getOrgType());
    }

    public <R> AnyMSeq<R> yield(
            final Function<? super T, Function<? super T1, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, Function<? super T6, Function<? super T7, ? extends R>>>>>>>> f) {
        if (getOrgType() != null)
            return new MonadWrapper<>(
                                      this.yieldInternal(f), getOrgType()).anyMSeq();
        else
            return AnyM.ofSeq(this.yieldInternal(f));
    }

    public <R> AnyMSeq<R> yield8(
            final OctFunction<? super T, ? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
        return this.yield(CurryVariance.curry8(f));
    }
}