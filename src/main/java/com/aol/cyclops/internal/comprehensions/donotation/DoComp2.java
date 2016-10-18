package com.aol.cyclops.internal.comprehensions.donotation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.BaseStream;

import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.util.function.CurryVariance;

public class DoComp2<T1, T2> extends DoComp {

    public DoComp2(final PStack<Entry> assigned, final Class orgType) {
        super(assigned, orgType);

    }

    public <T3> DoComp3<T1, T2, T3> reader(final Function<? super T1, Function<? super T2, Reader<?, ? extends T3>>> f) {
        return new DoComp3<>(
                             addToAssigned(f), getOrgType());

    }

    public <T3> DoComp3<T1, T2, T3> iterable(final Function<? super T1, Function<? super T2, Iterable<T3>>> f) {
        return new DoComp3<>(
                             addToAssigned(f), getOrgType());

    }

    public <T3> DoComp3<T1, T2, T3> publisher(final Function<? super T1, Function<? super T2, Publisher<T3>>> f) {
        return new DoComp3<>(
                             addToAssigned(f), getOrgType());

    }

    public <T3> DoComp3<T1, T2, T3> stream(final Function<? super T1, Function<? super T2, BaseStream<T3, ?>>> f) {
        return new DoComp3<>(
                             addToAssigned(f), getOrgType());

    }

    public <T3> DoComp3<T1, T2, T3> optional(final Function<? super T1, Function<? super T2, Optional<T3>>> f) {
        return new DoComp3<>(
                             addToAssigned(f), getOrgType());

    }

    public <T3> DoComp3<T1, T2, T3> future(final Function<? super T1, Function<? super T2, CompletableFuture<T3>>> f) {
        return new DoComp3<>(
                             addToAssigned(f), getOrgType());

    }

    public <T3> DoComp3<T1, T2, T3> anyM(final Function<? super T1, Function<? super T2, AnyM<T3>>> f) {
        return new DoComp3<>(
                             addToAssigned(f), getOrgType());

    }

    /**
     * Execute and Yield a result from this for comprehension using the supplied
     * function
     * 
     * e.g. sum every element across nested structures
     * 
     * <pre>
     * {@code   Do.iterable(list1).iterable(i->list2)
     * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
     * 								
     * 			}
     * </pre>
     * 
     * 
     * @param f
     *            To be applied to every element in the for comprehension
     * @return For comprehension result
     */
    public <R> AnyMSeq<R> yield(final Function<? super T1, Function<? super T2, ? extends R>> f) {
        if (getOrgType() != null)
            return new MonadWrapper<>(
                                      this.yieldInternal(f), getOrgType()).anyMSeq();
        else
            return AnyM.ofSeq(this.yieldInternal(f));
    }

    public <R> AnyMSeq<R> yield2(final BiFunction<? super T1, ? super T2, ? extends R> f) {
        return this.yield(CurryVariance.curry2(f));
    }

    /**
     * Filter data
     * 
     * 
     * 
     * <pre>
     * {@code   Do.iterable(list1).iterable(i->list2)
     * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
     * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
     * 								
     * 			}
     * </pre>
     * 
     * 
     * @param f
     *            To be applied to every element in the for comprehension
     * @return Current stage  guard / filter applied
     */
    public DoComp2<T1, T2> filter(final Function<? super T1, Function<? super T2, Boolean>> f) {
        return new DoComp2<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$internalGUARD" + getAssigned().size(), new Guard(
                                                                                                                                    f))),
                             getOrgType());
    }

}
