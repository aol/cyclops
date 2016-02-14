package com.aol.cyclops.types;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public interface Foldable<T> {

	public ReactiveSeq<T> stream();
	
	/**
	 * Attempt to map this Sequence to the same type as the supplied Monoid
	 * (Reducer) Then use Monoid to reduce values
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
	 * 
	 * //4
	 * }
	 * </pre>
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Reducer<R> reducer){
		return stream().mapReduce(reducer);
	}

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid, using
	 * supplied function Then use Monoid to reduce values
	 * 
	 * <pre>
	 *  {@code
	 *  SequenceM.of("one","two","three","four")
	 *           .mapReduce(this::toInt,Reducers.toTotalInt());
	 *  
	 *  //10
	 *  
	 *  int toInt(String s){
	 * 		if("one".equals(s))
	 * 			return 1;
	 * 		if("two".equals(s))
	 * 			return 2;
	 * 		if("three".equals(s))
	 * 			return 3;
	 * 		if("four".equals(s))
	 * 			return 4;
	 * 		return -1;
	 * 	   }
	 *  }
	 * </pre>
	 * 
	 * @param mapper
	 *            Function to map Monad type
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer){
		return stream().mapReduce(mapper,reducer);
	}
	/**
	 * <pre>
	 * {@code 
	 * SequenceM.of("hello","2","world","4").reduce(Reducers.toString(","));
	 * 
	 * //hello,2,world,4
	 * }
	 * </pre>
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values
	 * @return reduced values
	 */
	default T reduce(Monoid<T> reducer){
		return stream().reduce(reducer);
	}

	/*
	 * <pre> {@code assertThat(SequenceM.of(1,2,3,4,5).map(it -> it*100).reduce(
	 * (acc,next) -> acc+next).get(),equalTo(1500)); } </pre>
	 */
	default Optional<T> reduce(BinaryOperator<T> accumulator){
		return stream().reduce(accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BinaryOperator)
	 */
	default T reduce(T identity, BinaryOperator<T> accumulator){
		return stream().reduce(identity, accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner){
		return stream().reduce(identity,accumulator,combiner);
	}

	/**
	 * Reduce with multiple reducers in parallel NB if this Monad is an Optional
	 * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
	 * was one value To reduce over the values on the list, called
	 * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Monoid&lt;Integer&gt; sum = Monoid.of(0, (a, b) -&gt; a + b);
	 * 	Monoid&lt;Integer&gt; mult = Monoid.of(1, (a, b) -&gt; a * b);
	 * 	List&lt;Integer&gt; result = SequenceM.of(1, 2, 3, 4).reduce(Arrays.asList(sum, mult).stream());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(10, 24)));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * @param reducers
	 * @return
	 */
	default ListX<T> reduce(Stream<? extends Monoid<T>> reducers){
		return stream().reduce(reducers);
	}

	/**
	 * Reduce with multiple reducers in parallel NB if this Monad is an Optional
	 * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
	 * was one value To reduce over the values on the list, called
	 * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * <pre>
	 * {@code 
	 * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
	 * 		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
	 * 		List<Integer> result = SequenceM.of(1,2,3,4))
	 * 										.reduce(Arrays.asList(sum,mult) );
	 * 				
	 * 		 
	 * 		assertThat(result,equalTo(Arrays.asList(10,24)));
	 * 
	 * }
	 * 
	 * @param reducers
	 * @return
	 */
	default ListX<T> reduce(Iterable<? extends Monoid<T>> reducers){
		return stream().reduce(reducers);
	}


	/**
	 * 
	 * <pre>
	 * 		{@code
	 * 		SequenceM.of("a","b","c").foldRight(Reducers.toString(""));
	 *        
	 *         // "cab"
	 *         }
	 * </pre>
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	default T foldRight(Monoid<T> reducer){
		return stream().foldRight(reducer);
	}

	/**
	 * Immutable reduction from right to left
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(SequenceM.of("a","b","c").foldRight("", String::concat).equals("cba"));
	 * }
	 * </pre>
	 * 
	 * @param identity
	 * @param accumulator
	 * @return
	 */
	default T foldRight(T identity, BinaryOperator<T> accumulator){
		return stream().foldRight(identity,accumulator);
	}

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using
	 * mapToType on the monoid interface) Then use Monoid to reduce values
	 * 
	 * <pre>
	 * 		{@code
	 * 		SequenceM.of(1,2,3).foldRightMapToType(Reducers.toString(""));
	 *        
	 *         // "321"
	 *         }
	 * </pre>
	 * 
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <T> T foldRightMapToType(Reducer<T> reducer){
		return stream().foldRightMapToType(reducer);
	}
	

}
