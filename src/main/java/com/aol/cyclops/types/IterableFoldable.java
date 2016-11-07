package com.aol.cyclops.types;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.HeadAndTail;

/**
 * A non-scalar Foldable type
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements stored in this Foldable
 */
public interface IterableFoldable<T> extends Foldable<T>, Iterable<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#foldable()
     */
    @Override
    default IterableFoldable<T> foldable() {
        return stream();
    }

    /**
     * Destructures this Traversable into it's head and tail. If the traversable instance is not a SequenceM or Stream type,
     * whenStream may be more efficient (as it is guaranteed to be lazy).
     * 
     * <pre>
     * {@code 
     * ListX.of(1,2,3,4,5,6,7,8,9)
             .dropRight(5)
             .plus(10)
             .visit((x,xs) ->
                 xs.join(x.>2?"hello":"world")),()->"NIL"
             );
     * 
     * }
     * //2world3world4
     * 
     * </pre>
     * 
     * 
     * @param match
     * @return
     */
    default <R> R visit(final BiFunction<? super T, ? super ReactiveSeq<T>, ? extends R> match, final Supplier<? extends R> ifEmpty) {

        final HeadAndTail<T> ht = foldable().headAndTail();
        if (ht.isHeadPresent())
            return match.apply(ht.head(), ht.tail());
        return ifEmpty.get();

    }
   

    /**
     * extract head and tail together, where head is expected to be present
     * Example : 
     * 
     * <pre>
     * {@code 
     *  ReactiveSeq<String> helloWorld = ReactiveSeq.Of("hello","world","last");
        HeadAndTail<String> headAndTail = helloWorld.headAndTail();
        String head = headAndTail.head();
        
         //head == "hello"
        
        ReactiveSeq<String> tail =  headAndTail.tail();
        //["world","last]
        
        }
     *  </pre>
     *  
     * @return
     */
    default HeadAndTail<T> headAndTail() {
        return foldable().headAndTail();
    }

}
