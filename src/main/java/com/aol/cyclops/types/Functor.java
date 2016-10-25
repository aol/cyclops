package com.aol.cyclops.types;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Trampoline;

/** 
 * An interface that represents a type that can transform a value from one type to another
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) stored in this Functor
 */
@FunctionalInterface
public interface Functor<T> {

    /**
     * Cast all elements in a stream to a given type, possibly throwing a
     * {@link ClassCastException}.
     * 
     * 
     * // ClassCastException ReactiveSeq.of(1, "a", 2, "b", 3).cast(Integer.class)
     * 
     */
    default <U> Functor<U> cast(final Class<? extends U> type) {
        return map(type::cast);
    }

    /**
     * Transform this functor using the supplied transformation function
     * 
     * <pre>
     * {@code 
     *  
     *  
     *    of(1,2,3).map(i->i*2)
     *    
     *    //[2,4,6]
     *  
     * }
     * </pre>
     * 
     * @param fn Transformation function
     * @return Transformed Functor
     */
    <R> Functor<R> map(Function<? super T, ? extends R> fn);

    /**
     * Peek at the current value of this Functor, without transforming it
     * 
      * <pre>
     * {@code 
     *  
     *  
     *    of(1,2,3).map(System.out::println)
     *    
     *    1
     *    2
     *    3
     *  
     * }
     * </pre>
     * @param c Consumer that recieves each element from this Functor
     * @return Functor that will peek at each value
     */
    default Functor<T> peek(final Consumer<? super T> c) {
        return map(input -> {
            c.accept(input);
            return input;
        });
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
      * </pre>
      * 
     * @param mapper TCO Transformation function
     * @return Functor transformed by the supplied transformation function
     */
    default <R> Functor<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return map(in -> mapper.apply(in)
                               .result());
    }

    /**
    * Transform the elements of this Stream with a Pattern Matching case and default value
    *
    * <pre>
    * {@code
    * List<String> result = CollectionX.of(1,2,3,4)
                                             .patternMatch(
                                                       c->c.valuesWhere(i->"even", (Integer i)->i%2==0 )
                                                     )
    * }
    * // CollectionX["odd","even","odd","even"]
    * </pre>
    *
    *
    * @param case1 Function to generate a case (or chain of cases as a single case)
    * @param otherwise Value if supplied case doesn't match
    * @return CollectionX where elements are transformed by pattern matching
    */
    default <R> Functor<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {

        return map(u -> Matchable.of(u)
                                 .matches(case1, otherwise)
                                 .get());

    }

}
