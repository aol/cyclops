package com.aol.cyclops.types;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.HeadAndTail;

public interface IterableFoldable<T> extends Foldable<T>, 
                                             Iterable<T> {
    default IterableFoldable<T> foldable(){
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
    default <R> R visit(BiFunction<? super T,? super ReactiveSeq<T>,? extends R> match, Supplier<? extends R> ifEmpty){
        
        HeadAndTail<T> ht = foldable().headAndTail();
        if(ht.isHeadPresent())
            return match.apply(ht.head(),ht.tail());
        return ifEmpty.get();
        
    }
    default <R> R visit(BiFunction<? super Maybe<T>,? super ReactiveSeq<T>,? extends R> match){
        HeadAndTail<T> ht = foldable().headAndTail();
       return match.apply(ht.headMaybe(),ht.tail());
        
    }
    /**
     * extract head and tail together, where head is expected to be present
     * 
     * <pre>
     * {
     *  &#064;code
     *  SequenceM&lt;String&gt; helloWorld = ReactiveSeq.of(&quot;hello&quot;, &quot;world&quot;, &quot;last&quot;);
     *  HeadAndTail&lt;String&gt; headAndTail = helloWorld.headAndTail();
     *  String head = headAndTail.head();
     *  assertThat(head, equalTo(&quot;hello&quot;));
     * 
     *  SequenceM&lt;String&gt; tail = headAndTail.tail();
     *  assertThat(tail.headAndTail().head(), equalTo(&quot;world&quot;));
     * }
     * </pre>
     * 
     * @return
     */
    default HeadAndTail<T> headAndTail(){
        return foldable().headAndTail();
    }
    
}
