package com.aol.cyclops.monad;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
@Deprecated
public interface ApplyM<T> {

	/**
	 * Apply function/s inside supplied Monad to data in current Monad
	 * 
	 * e.g. with Streams
	 * <pre>{@code 
	 * 
	 * AnyM<Integer> applied =AnyM.fromStream(Stream.of(1,2,3))
	 * 								.applyM(Stream.of( (Integer a)->a+1 ,(Integer a) -> a*2));
	
	 	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 }</pre>
	 * 
	 * @param fn Stream of Functions to apply
	 * @return AnyM with functions applied
	 */
	public <R> AnyM<R> applyMStream(Stream<Function<? super T,? extends R>> fn);
	/**
	 * Apply function/s inside supplied Monad to data in current Monad
	 * 
	 * e.g. with Streams
	 * <pre>{@code 
	 * 
	 * AnyM<Integer> applied =AnyM.fromStream(Stream.of(1,2,3))
	 * 								.applyM(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2));
	
	 	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 }</pre>
	 * 
	 * 
	 * @param fn Iterable of Functions to apply
	 * @return AnyM with functions applied
	 */
	 default <R> AnyM<R> applyMIterable(Iterable<Function<? super T,? extends R>> fn){
		 return applyMStream(StreamSupport.stream(fn.spliterator(),false));
	 }
	 /**
		 * Apply function/s inside supplied Monad to data in current Monad
		 * 
		
		 * with Optionals 
		 * <pre>{@code
		 * 
		 *  Any<Integer> applied =AnyM.fromOptional(Optional.of(2)).applyM(AnyM.fromOptional(Optional.of( (Integer a)->a+1)) );
			assertThat(applied.toList(),equalTo(Arrays.asList(3)));}
			</pre>
		 * 
		 * @param fn
		 * @return
		 */
	public <R> AnyM<R> applyMOptional(Optional<Function<? super T,? extends R>> fn);
	 /**
		 * Apply function/s inside supplied Monad to data in current Monad
		 * 
		
		 * <pre>{@code
		 * 
		 *  Any<Integer> applied =AnyM.fromOptional(Optional.of(2))..applyM(AnyM.fromCompletableFuture(CompletableFuture.completedFuture( (Integer a)->a+1)) );
			assertThat(applied.toList(),equalTo(Arrays.asList(3)));}
			</pre>
		 * 
		 * @param fn
		 * @return
		 */
	public <R> AnyM<R> applyMCompletableFuture(CompletableFuture<Function<? super T,? extends R>> fn);
	
}
