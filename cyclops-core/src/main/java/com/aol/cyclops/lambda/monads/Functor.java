package com.aol.cyclops.lambda.monads;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.trampoline.Trampoline;


/* 
 * @author johnmcclean
 *
 * @param <T>
 */
@FunctionalInterface
public interface Functor<T> {

	
	
	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * 
	 * // ClassCastException SequenceM.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * 
	 */
	default <U> Functor<U> cast(Class<U> type){
		return map(type::cast);
	}
	<R> Functor<R>  map(Function<? super T,? extends R> fn);
	
	default   Functor<T>  peek(Consumer<? super T> c) {
		return (Functor)map(input -> {
			c.accept(input);
			return  input;
		});
	}
	/**
	  * Performs a map operation that can call a recursive method without running out of stack space
	  * <pre>
	  * {@code
	  * SequenceM.of(10,20,30,40)
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
	  * SequenceM.of(10_000,200_000,3_000_000,40_000_000)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println);
				 
				 
	  * completes successfully
	  * }
	  * 
	 * @param mapper
	 * @return
	 */
	default <R> Functor<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		return  map(in-> mapper.apply(in).result());
	 }
	
	
	default <R> Functor<Optional<R>> matchesCases(Case<T,R,Function<T,R>>... cases){
		return map(t->Cases.of(cases).<R>match(t));
	}
	
	 /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     * List<String> result = CollectionX.of(1,2,3,4)
                                              .patternMatch(
                                                        c->c.hasValuesWhere( (Integer i)->i%2==0 ).then(i->"even")
                                                      )
     * }
     * // CollectionX["odd","even","odd","even"]
     * </pre>
     *
     *
     * @param defaultValue Value if supplied case doesn't match
     * @param case1 Function to generate a case (or chain of cases as a single case)
     * @return CollectionX where elements are transformed by pattern matching
     */
    default <R> Functor<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> case1){

        return  map(u-> Matchable.of(u).mayMatch(case1).orElse(defaultValue));
    }
    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     *
     * List<String> result = CollectionX.of(-2,01,2,3,4)
     *                                        .filter(i->i>0)
                                              .patternMatch("many",
                                                        c->c.hasValuesWhere( (Integer i)->i==1 ).then(i->"one"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"two")
                                                      );
         // CollectionX["one","two","many","many"]
     * }
     *
     * </pre>
     *
     * @param defaultValue Value if supplied cases don't match
     * @param case1 Function to generate a case (or chain of cases as a single case)
     * @param case2 Function to generate a case (or chain of cases as a single case)
     * @return  CollectionX where elements are transformed by pattern matching
     */
    default <R> Functor<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> case1
                            ,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> case2){
        return map(u-> Matchable.of(u).mayMatch(case1,case2).orElse(defaultValue));
    }
    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     *
     * List<String> result = CollectionX.of(-2,01,2,3,4)
     *                                        .filter(i->i>0)
                                              .patternMatch("many",
                                                        c->c.hasValuesWhere( (Integer i)->i==1 ).then(i->"one"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"two"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"three")
                                                      )
                                                 .map(opt -> opt.orElse("many"));
     * }
     * // CollectionX["one","two","three","many"]
     * </pre>
     * @param defaultValue Value if supplied cases don't match
     * @param fn1 Function to generate a case (or chain of cases as a single case)
     * @param fn2 Function to generate a case (or chain of cases as a single case)
     * @param fn3 Function to generate a case (or chain of cases as a single case)
     * @return CollectionX where elements are transformed by pattern matching
     */
    default <R> Functor<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1,
                                                    Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
                                                    Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3){

        return map(u-> Matchable.of(u).mayMatch(fn1,fn2,fn3).orElse(defaultValue));
    }
    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     * List<String> result = CollectionX.of(-2,01,2,3,4,5)
     *                                        .filter(i->i>0)
                                              .patternMatch("many",
                                                        c->c.hasValuesWhere( (Integer i)->i==1 ).then(i->"one"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"two"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"three"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"four")
                                                      )
     * }
     * // CollectionX["one","two","three","four","many"]
     * </pre>
     * @param defaultValue Value if supplied cases don't match
     * @param fn1  Function to generate a case (or chain of cases as a single case)
     * @param fn2  Function to generate a case (or chain of cases as a single case)
     * @param fn3  Function to generate a case (or chain of cases as a single case)
     * @param fn4  Function to generate a case (or chain of cases as a single case)
     * @return  CollectionX where elements are transformed by pattern matching
     */
    default <R> Functor<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1, Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
                            Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn4){

        return map(u-> Matchable.of(u).mayMatch(fn1,fn2,fn3,fn4).orElse(defaultValue));
    }
    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     * List<String> result = CollectionX.of(-2,01,2,3,4,5,6)
     *                                        .filter(i->i>0)
                                              .patternMatch("many",
                                                        c->c.hasValuesWhere( (Integer i)->i==1 ).then(i->"one"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"two"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"three"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"four"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"five")
                                                      )
                                             .map(opt -> opt.orElse("many"));
     * }
     * // CollectionX["one","two","three","four","five","many"]
     * </pre>
     * @param defaultValue Value if supplied cases don't match
     * @param fn1 Function to generate a case (or chain of cases as a single case)
     * @param fn2 Function to generate a case (or chain of cases as a single case)
     * @param fn3 Function to generate a case (or chain of cases as a single case)
     * @param fn4 Function to generate a case (or chain of cases as a single case)
     * @param fn5 Function to generate a case (or chain of cases as a single case)
     * @return CollectionX where elements are transformed by pattern matching
     */
    default <R> Functor<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1, Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
            Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn4,
                            Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn5){

        return map(u-> Matchable.of(u).mayMatch(fn1,fn2,fn3,fn4,fn5).orElse(defaultValue));
    }

	
	
	
}
