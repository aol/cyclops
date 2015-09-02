package com.aol.cyclops.sequence;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.sequence.streamable.Streamable;

public class SeqUtils {
	/**
	 * Reverse a Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(StreamUtils.reverse(Stream.of(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
	 * }
	 * </pre>
	 * 
	 * @param stream Stream to reverse
	 * @return Reversed stream
	 */
	public static <U> Stream<U> reverse(Stream<U> stream){
		return reversedStream(stream.collect(Collectors.toList()));
	}
	/**
	 * Create a reversed Stream from a List
	 * <pre>
	 * {@code 
	 * StreamUtils.reversedStream(asList(1,2,3))
				.map(i->i*100)
				.forEach(System.out::println);
		
		
		assertThat(StreamUtils.reversedStream(Arrays.asList(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param list List to create a reversed Stream from
	 * @return Reversed Stream
	 */
	public static <U> Stream<U> reversedStream(List<U> list){
		return new ReversedIterator<>(list).stream();
	}
	
	/**
	 * Create a Stream that finitely cycles the provided Streamable, provided number of times
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(StreamUtils.cycle(3,Streamable.of(1,2,2))
								.collect(Collectors.toList()),
									equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	 * }
	 * </pre>
	 * @param s Streamable to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(int times,Streamable<U> s){
		return Stream.iterate(s.stream(),s1-> s.stream()).limit(times).flatMap(Function.identity());
	}
	
}
