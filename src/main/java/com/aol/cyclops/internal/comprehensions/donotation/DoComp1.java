
package com.aol.cyclops.internal.comprehensions.donotation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;

import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.types.anyM.AnyMSeq;

public class DoComp1<T1> extends DoComp {
    public DoComp1(final PStack<Entry> assigned, final Class orgType) {
        super(assigned, orgType);

    }

    public <T2> DoComp2<T1, T2> reader(final Function<? super T1, Reader<?, ? extends T2>> f) {
        return new DoComp2<>(
                             addToAssigned(f), getOrgType());

    }

    public <T2> DoComp2<T1, T2> iterable(final Function<? super T1, Iterable<T2>> f) {
        return new DoComp2<>(
                             addToAssigned(f), getOrgType());

    }

    public <T2> DoComp2<T1, T2> publisher(final Function<? super T1, ? extends Publisher<T2>> f) {
        return new DoComp2<>(
                             addToAssigned(f), getOrgType());
    }

    public <T2> DoComp2<T1, T2> stream(final Function<? super T1, BaseStream<T2, ?>> f) {
        return new DoComp2<>(
                             addToAssigned(f), getOrgType());

    }

    public <T2> DoComp2<T1, T2> optional(final Function<? super T1, Optional<T2>> f) {
        return new DoComp2<>(
                             addToAssigned(f), getOrgType());

    }

    public <T2> DoComp2<T1, T2> future(final Function<? super T1, CompletableFuture<T2>> f) {
        return new DoComp2<>(
                             addToAssigned(f), getOrgType());

    }

    public <T2> DoComp2<T1, T2> anyM(final Function<? super T1, AnyM<T2>> f) {
        return new DoComp2<>(
                             addToAssigned(f), getOrgType());

    }

    /**
     * Execute and Yield a result from this for comprehension using the supplied function
     * 
     * e.g. sum every element across nested structures
     * 
     * <pre>{@code   Do.iterable(list1)
    			  	   .yield(i -> i+10);
    						
    	}</pre>
     * 
     * 
     * @param f To be applied to every element in the for comprehension
     * @return For comprehension result
     */
    public <R> AnyMSeq<R> yield(final Function<? super T1, ? extends R> f) {
        if (getOrgType() != null)
            return new MonadWrapper<>(
                                      this.yieldInternal(f), getOrgType()).anyMSeq();
        else
            return AnyM.ofSeq(this.yieldInternal(f));
    }

    /**
     * Filter data
     * 
     * 
     * 
     * <pre>{@code   Do.iterable(list1)
     				   .filter(i -> i>5)
    			  	   .yield(i-> i+10);
    						
    	}</pre>
     * 
     * 
     * @param f To be applied to every element in the for comprehension
     * @return Current stage  guard / filter applied
     */
    public DoComp1<T1> filter(final Predicate<? super T1> f) {
        return new DoComp1<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$internalGUARD" + getAssigned().size(), new Guard(
                                                                                                                                    f))),
                             getOrgType());
    }

}
