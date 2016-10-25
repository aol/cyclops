
package com.aol.cyclops.internal.comprehensions.donotation;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.BaseStream;

import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;

public class DoComp0 extends DoComp {
    public DoComp0(final PStack<Entry> assigned) {
        super(assigned, null);

    }

    public <T1> DoComp1<T1> reader(final Reader<?, T1> seq) {
        return new DoComp1<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$monad" + getAssigned().size(), seq)),
                             getOrgType());

    }

    /**
     * Add a Iterable as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public <T1> DoComp1<T1> iterable(final Iterable<T1> o) {
        Class orgType = null;
        if (o instanceof List)
            orgType = List.class;
        else if (o instanceof Set)
            orgType = Set.class;
        return new DoComp1<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$monad" + getAssigned().size(), o)),
                             orgType);
    }

    public <T1> DoComp1<T1> publisher(final Publisher<T1> o) {
        if (o instanceof List) {
        } else if (o instanceof Set) {
        }
        return new DoComp1<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$monad" + getAssigned().size(), o)),
                             getOrgType());
    }

    /**
     * Add a Stream as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do.add(stream)
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public <T1> DoComp1<T1> stream(final BaseStream<T1, ?> o) {
        return new DoComp1<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$monad" + getAssigned().size(), o)),
                             getOrgType());

    }

    /**
     * Add a Optional as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public <T1> DoComp1<T1> optional(final Optional<T1> o) {
        return new DoComp1<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$monad" + getAssigned().size(), o)),
                             getOrgType());

    }

    /**
     * Add a CompletableFuture as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public <T1> DoComp1<T1> future(final CompletableFuture<T1> o) {
        return new DoComp1<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$monad" + getAssigned().size(), o)),
                             getOrgType());

    }

    /**
     * Add a AnyM as next nested level in the comprehension
     * 
     * 
     * 
     * <pre>{@code   Do
     				   .filter( -> i1>5)
    			  	   .yield( -> );
    						
    	}</pre>
     * 
     * 
     * @param o Defines next level in comprehension
     * @return Next stage in for comprehension builder
     */
    public <T1> DoComp1<T1> anyM(final AnyM<T1> o) {
        return new DoComp1<>(
                             getAssigned().plus(getAssigned().size(), new Entry(
                                                                                "$$monad" + getAssigned().size(), o)),
                             getOrgType());

    }

}