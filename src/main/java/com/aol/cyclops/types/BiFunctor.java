package com.aol.cyclops.types;

import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.control.Trampoline;

/* 
 * @author johnmcclean
 *
 * @param <T>
 */

public interface BiFunctor<T1, T2> {

    <R1, R2> BiFunctor<R1, R2> bimap(Function<? super T1, ? extends R1> fn1, Function<? super T2, ? extends R2> fn2);

    default BiFunctor<T1, T2> bipeek(Consumer<? super T1> c1, Consumer<? super T2> c2) {
        return bimap(input -> {
            c1.accept(input);
            return input;
        } , input -> {
            c2.accept(input);
            return input;
        });
    }

    /**
     * Cast all elements in a stream to a given type, possibly throwing a
     * {@link ClassCastException}.
     * 
     * 
     * // ClassCastException ReactiveSeq.of(1, "a", 2, "b", 3).cast(Integer.class)
     * 
     */
    default <U1, U2> BiFunctor<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {
        return bimap(type1::cast, type2::cast);
    }

    /**
      * Performs a map operation that can call a recursive method without running out of stack space
      * <pre>
      * {@code
      * ReactiveSeq.of(10,20,30,40)
    			 .trampoline(i-> fibonacci(i))
    			 .forEach(System.out::println); 
    			 
    	Trampoline<Long> fibonacci(int i){
    		return fibonacci(i,1,0);
    	}
    	Trampoline<Long> fibonacci(int n, long a, long b) {
        	return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
    	}		 
    			 
      * 55
    	6765
    	832040
    	102334155
      * 
      * 
      * ReactiveSeq.of(10_000,200_000,3_000_000,40_000_000)
    			 .trampoline(i-> fibonacci(i))
    			 .forEach(System.out::println);
    			 
    			 
      * completes successfully
      * }
      * 
     * @param mapper
     * @return
     */
    default <R1, R2> BiFunctor<R1, R2> bitrampoline(Function<? super T1, ? extends Trampoline<? extends R1>> mapper1,
            Function<? super T2, ? extends Trampoline<? extends R2>> mapper2) {
        return bimap(in -> mapper1.apply(in)
                                  .result(),
                     in -> mapper2.apply(in)
                                  .result());
    }

}
