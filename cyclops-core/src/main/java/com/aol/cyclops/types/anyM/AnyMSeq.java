package com.aol.cyclops.types.anyM;

import static com.aol.cyclops.monad.Utils.firstOrNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.matcher2.CheckValues;
import com.aol.cyclops.internal.monads.AnyMSeqImpl;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.monad.AnyMonads;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.sequence.ConvertableSequence;
import com.aol.cyclops.types.sequence.SequenceMCollectable;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

public interface AnyMSeq<T> extends AnyM<T>,
									ConvertableSequence<T>,
									Traversable<T>,
									SequenceMCollectable<T>,
									ZippingApplicativable<T> {

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.sequence.SequenceMCollectable#collectable()
	 */
	@Override
	default Collectable<T> collectable() {
		return ZippingApplicativable.super.collectable();
	}
	default Value<T> toFirstValue(){
		
		return ()-> firstOrNull(toListX());
	}
	default <ST> Xor<ST,ListX<T>> toXor(){
		return toValue().toXor();
	}
	default <PT> Xor<ListX<T>,PT> toXorSecondary(){
		return toValue().toXorSecondary();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> AnyMSeq<U> cast(Class<U> type) {
		
		return (AnyMSeq<U>)ZippingApplicativable.super.cast(type);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> AnyMSeq<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (AnyMSeq<R>)ZippingApplicativable.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Functor#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> AnyMSeq<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (AnyMSeq<R>)ZippingApplicativable.super.patternMatch(defaultValue, case1);
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#cycle(int)
	 */
	@Override
	default AnyMSeq<T> cycle(int times) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.cycle(times);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#cycle(com.aol.cyclops.Monoid, int)
	 */
	@Override
	default AnyMSeq<T> cycle(Monoid<T> m, int times) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.cycle(m, times);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default AnyMSeq<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.cycleWhile(predicate);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default AnyMSeq<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.cycleUntil(predicate);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> AnyMSeq<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
		
		return (AnyMSeq<R>)ZippingApplicativable.super.zip(other, zipper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> AnyMSeq<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (AnyMSeq<Tuple2<T, U>>)ZippingApplicativable.super.zipStream(other);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> AnyMSeq<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (AnyMSeq<Tuple2<T, U>>)ZippingApplicativable.super.zip(other);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> AnyMSeq<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (AnyMSeq)ZippingApplicativable.super.zip3(second, third);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> AnyMSeq<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return ( AnyMSeq<Tuple4<T, T2, T3, T4>>)ZippingApplicativable.super.zip4(second, third, fourth);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#zipWithIndex()
	 */
	@Override
	default AnyMSeq<Tuple2<T, Long>> zipWithIndex() {
		
		return (AnyMSeq<Tuple2<T, Long>>)ZippingApplicativable.super.zipWithIndex();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#sliding(int)
	 */
	@Override
	default AnyMSeq<ListX<T>> sliding(int windowSize) {
		
		return (AnyMSeq<ListX<T>>)ZippingApplicativable.super.sliding(windowSize);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#sliding(int, int)
	 */
	@Override
	default AnyMSeq<ListX<T>> sliding(int windowSize, int increment) {
		
		return (AnyMSeq<ListX<T>>)ZippingApplicativable.super.sliding(windowSize, increment);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#distinct()
	 */
	@Override
	default AnyMSeq<T> distinct() {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.distinct();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#scanLeft(com.aol.cyclops.Monoid)
	 */
	@Override
	default AnyMSeq<T> scanLeft(Monoid<T> monoid) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.scanLeft(monoid);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
	 */
	@Override
	default <U> AnyMSeq<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
		
		return (AnyMSeq<U>)ZippingApplicativable.super.scanLeft(seed, function);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#scanRight(com.aol.cyclops.Monoid)
	 */
	@Override
	default AnyMSeq<T> scanRight(Monoid<T> monoid) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.scanRight(monoid);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
	 */
	@Override
	default <U> AnyMSeq<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
		
		return (AnyMSeq<U>)ZippingApplicativable.super.scanRight(identity, combiner);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#sorted()
	 */
	@Override
	default AnyMSeq<T> sorted() {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.sorted();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#sorted(java.util.Comparator)
	 */
	@Override
	default AnyMSeq<T> sorted(Comparator<? super T> c) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.sorted(c);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#skip(long)
	 */
	@Override
	default AnyMSeq<T> skip(long num) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default AnyMSeq<T> skipWhile(Predicate<? super T> p) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.skipWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default AnyMSeq<T> skipUntil(Predicate<? super T> p) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.skipUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#intersperse(java.lang.Object)
	 */
	@Override
	default AnyMSeq<T> intersperse(T value) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.intersperse(value);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#skipLast(int)
	 */
	@Override
	default AnyMSeq<T> skipLast(int num) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.skipLast(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#slice(long, long)
	 */
	@Override
	default AnyMSeq<T> slice(long from, long to) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Traversable#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> AnyMSeq<T> sorted(Function<? super T, ? extends U> function) {
		
		return (AnyMSeq<T>)ZippingApplicativable.super.sorted(function);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.EmptyUnit#emptyUnit()
	 */
	@Override
	<T> AnyMSeq<T> emptyUnit() ;

		/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#stream()
	 */
	@Override
	SequenceM<T> stream();

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#unwrap()
	 */
	@Override
	 <R> R unwrap();

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#monad()
	 */
	@Override
	<X> X monad() ;

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#filter(java.util.function.Predicate)
	 */
	@Override
	AnyMSeq<T> filter(Predicate<? super T> p) ;

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#map(java.util.function.Function)
	 */
	@Override
	<R> AnyMSeq<R> map(Function<? super T, ? extends R> fn) ;

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#peek(java.util.function.Consumer)
	 */
	@Override
	default AnyMSeq<T> peek(Consumer<? super T> c) {
		
		return map(i->{
			c.accept(i);
			return i;
		});
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#bind(java.util.function.Function)
	 */
	@Override
	<R> AnyMSeq<R> bind(Function<? super T, ?> fn);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#liftAndBind(java.util.function.Function)
	 */
	@Override
	 <R> AnyMSeq<R> liftAndBind(Function<? super T, ?> fn);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#flatten()
	 */
	@Override
	 <T1> AnyM<T1> flatten();

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#aggregate(com.aol.cyclops.monad.AnyM)
	 */
	@Override
	AnyMSeq<List<T>> aggregate(AnyM<T> next) ;

	/**
	 * Perform a two level nested internal iteration over this Stream and the supplied monad (allowing null handling, exception handling
	 * etc to be injected, for example)
	 * 
	 * <pre>
	 * {@code 
	 * AnyM.fromArray(1,2,3)
						.forEachAnyM2(a->AnyM.fromIntStream(IntStream.range(10,13)),
									a->b->a+b);
									
	 * 
	 *  //AnyM[11,14,12,15,13,16]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param monad Nested Monad to iterate over
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return LazyFutureStream with elements generated via nested iteration
	 */
	 <R1, R> AnyMSeq<R> forEach2(Function<? super T, ? extends AnyM<R1>> monad, Function<? super T, Function<? super R1, ? extends R>> yieldingFunction);
		
		

    /**
	 * Perform a two level nested internal iteration over this Stream and the supplied monad (allowing null handling, exception handling
	 * etc to be injected, for example)
	 * 
	 * <pre>
	 * {@code 
	 * AnyM.fromArray(1,2,3)
						.forEach2(a->AnyM.fromIntStream(IntStream.range(10,13)),
						            a->b-> a<3 && b>10,
									a->b->a+b);
									
	 * 
	 *  //AnyM[14,15]
	 * }
	 * </pre>
	 * @param monad Nested Monad to iterate over
	 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
	 * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
	 * @return
	 */
	 <R1,R> AnyMSeq<R> forEach2(Function<? super T,? extends AnyM<R1>> monad, 
				Function<? super T, Function<? super R1, Boolean>> filterFunction,
					Function<? super T,Function<? super R1,? extends R>> yieldingFunction );
 	/** 
	 * Perform a three level nested internal iteration over this Stream and the supplied streams
	  *<pre>
	 * {@code 
	 * AnyM.fromArray(1,2)
						.forEach2(a->AnyM.fromIntStream(IntStream.range(10,13)),
						(a->b->AnyM.fromArray(""+(a+b),"hello world"),
									a->b->c->c+":"a+":"+b);
									
	 * 
	 *  //AnyM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
	 * </pre> 
	 * @param monad1 Nested monad to flatMap over
	 * @param stream2 Nested monad to flatMap over
	 * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
	 * @return AnyM with elements generated via nested iteration
	 */
	 <R1, R2, R> AnyMSeq<R> forEach3(Function<? super T, ? extends AnyM<R1>> monad1, 	
				Function<? super T,Function<? super R1,? extends AnyM<R2>>> monad2,
		Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction, 
			Function<? super T, Function<? super R1, Function<? super R2,? extends R>>> yieldingFunction);
	
	


	
	
	/**
	 * Perform a three level nested internal iteration over this AnyM and the supplied monads
	 *<pre>
	 * {@code 
	 * AnyM.fromArray(1,2,3)
					.forEach3(a->AnyM.fromStream(IntStream.range(10,13)),
						 a->b->AnyM.fromArray(""+(a+b),"hello world"),
					         a->b->c-> c!=3,
								a->b->c->c+":"a+":"+b);
								
	 * 
	 *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
 * </pre> 
	 * 
	 * @param monad1 Nested Stream to iterate over
	 * @param monad2 Nested Stream to iterate over
	 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
	 * @param yieldingFunction Function with pointers to the current element from both Monads that generates the new elements
	 * @return AnyM with elements generated via nested iteration
	 */
	<R1, R2, R> AnyMSeq<R> forEach3( Function<? super T, ? extends AnyM<R1>> monad1, 	
					Function<? super T,Function<? super R1,? extends AnyM<R2>>> monad2,
					Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction);


	/**
	 * flatMap operation
	  * 
	 * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
	 * In particular left-identity becomes
	 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
	 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
	 * only the first value is accepted.
	 * 
	 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
	 * <pre>
	 * {@code 
	 *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->AnyM.fromArray(i+1,i+2));
	 *   
	 *   //AnyM[Stream[2,3,3,4,4,5]]
	 * }
	 * </pre>
	 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
	 * <pre>
	 * {@code 
	 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->AnyM.fromArray(i+1,i+2));
	 *   
	 *   //AnyM[Optional[2]]
	 * }
	 * </pre>
	 * @param fn flatMap function
	 * @return  flatMapped AnyM
	 */
	 <R> AnyMSeq<R> flatMap(Function<? super T,? extends AnyM<? extends R>> fn) ;
	 /**
		 * Apply function/s inside supplied Monad to data in current Monad
		 * 
		 * e.g. with Streams
		 * <pre>{@code 
		 * 
		 * AnyM<Integer> applied =AnyM.fromStream(Stream.of(1,2,3))
		 * 								.applyM(AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
		
		 	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
		 }</pre>
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
		<R> AnyMSeq<R> applyM(AnyM<Function<? super T,? extends R>> fn);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#toSequence(java.util.function.Function)
	 */
	@Override
	 <NT> SequenceM<NT> toSequence(Function<? super T, ? extends Stream<? extends NT>> fn) ;

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#toSequence()
	 */
	@Override
	<T> SequenceM<T> toSequence() ;

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#asSequence()
	 */
	@Override
	SequenceM<T> asSequence() ;
	
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#unit(java.lang.Object)
	 */
	@Override
	<T> AnyMSeq<T> unit(T value);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#empty()
	 */
	@Override
	<T> AnyMSeq<T> empty();

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#replicateM(int)
	 */
	@Override
	 AnyMSeq<List<T>> replicateM(int times) ;

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#reduceM(com.aol.cyclops.Monoid)
	 */
	@Override
	AnyM<T> reduceM(Monoid<AnyM<T>> reducer);


	/**
	 * Convert a Stream of Monads to a Monad with a List applying the supplied function in the process
	 * 
	<pre>{@code 
       Stream<CompletableFuture<Integer>> futures = createFutures();
       AnyMSeq<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
		</pre>
	 * 
	 * @param seq Stream of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	public static <T,R> AnyMSeq<ListX<R>> traverse(Stream<? extends AnyMSeq<T>> seq, Function<? super T,? extends R> fn){
		
		return AnyMSeqImpl.from(new AnyMonads().traverse(seq,fn));
	}

	
	/**
	 * Convert a Collection of Monads to a Monad with a List
	 * 
	 * <pre>
	 * {@code
		List<CompletableFuture<Integer>> futures = createFutures();
		AnyMSeq<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));

	   //where AnyM wraps  CompletableFuture<List<Integer>>
	  }</pre>
	 * 
	 * @see com.aol.cyclops.monad.AsAnyMList for helper methods to convert a List of Monads / Collections to List of AnyM
	 * @param seq Collection of monads to convert
	 * @return Monad with a List
	 */ 
	public static <T1>  AnyMSeq<ListX<T1>> sequence(Collection<? extends AnyMSeq<T1>> seq){
		return AnyMSeqImpl.from(new AnyMonads().sequence(seq));
	}
	/**
	 * Convert a Stream of Monads to a Monad with a List
	 * 
	 * <pre>{@code
		Stream<CompletableFuture<Integer>> futures = createFutures();
		AnyMSeq<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));

	   //where AnyM wraps  CompletableFuture<List<Integer>>
	  }</pre>
	 * 
	 * @see com.aol.cyclops.monad.AsAnyMList for helper methods to convert a List of Monads / Collections to List of AnyM
	 * @param seq Stream of monads to convert
	 * @return Monad with a List
	 */
	public static <T1>  AnyMSeq<SequenceM<T1>> sequence(Stream<? extends AnyMSeq<T1>> seq){
		return AnyMSeqImpl.from(new AnyMonads().sequence(seq));
	}
	/**
	 * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
	 * AnyM view simplifies type related challenges.
	 * 
	 * @param fn
	 * @return
	 */
	public static <U,R> Function<AnyMSeq<U>,AnyMSeq<R>> liftM(Function<? super U,? extends R> fn){
		return u -> u.map( input -> fn.apply(input)  );
	}
	
	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * AnyM view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * e.g.
	 * 
	 * <pre>{@code
	 * 	BiFunction<AnyMSeq<Integer>,AnyMSeq<Integer>,AnyMSeq<Integer>> add = Monads.liftM2(this::add);
	 *   
	 *  Optional<Integer> result = add.apply(getBase(),getIncrease());
	 *  
	 *   private Integer add(Integer a, Integer b){
				return a+b;
		}
	 * }</pre>
	 * The add method has no null handling, but we can lift the method to Monadic form, and use Optionals to automatically handle null / empty value cases.
	 * 
	 * 
	 * @param fn BiFunction to lift
	 * @return Lifted BiFunction
	 */
	public static <U1,U2,R> BiFunction<AnyMSeq<U1>,AnyMSeq<U2>,AnyMSeq<R>> liftM2(BiFunction<? super U1,? super U2,? extends R> fn){
		
		return (u1,u2) -> u1.bind( input1 -> u2.map(input2 -> fn.apply(input1,input2)  ).unwrap());
	}
	/**
	 * Lift a jOOλ Function3  into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
	 * 
	 * <pre>
	 * {@code
	 * Function3 <AnyMSeq<Double>,AnyMSeq<Entity>,AnyMSeq<String>,AnyMSeq<Integer>> fn = liftM3(this::myMethod);
	 *    
	 * }
	 * </pre>
	 * 
	 * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
	 * 
	 * @param fn Function to lift
	 * @return Lifted function
	 */
	public static <U1,U2,U3,R> Function3<AnyMSeq<U1>,AnyMSeq<U2>,AnyMSeq<U3>,AnyMSeq<R>> liftM3(Function3<? super U1,? super U2,? super U3,? extends R> fn){
		return (u1,u2,u3) -> u1.bind( input1 -> 
									u2.bind(input2 -> 
										u3.map(input3->fn.apply(input1,input2,input3)  )).unwrap());
	}
	/**
	 * Lift a TriFunction into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
	 * 
	 * <pre>
	 * {@code
	 * TriFunction<AnyMSeq<Double>,AnyMSeq<Entity>,AnyMSeq<String>,AnyMSeq<Integer>> fn = liftM3(this::myMethod);
	 *    
	 * }
	 * </pre>
	 * 
	 * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
	 * 
	 * @param fn Function to lift
	 * @return Lifted function
	 */
	public static <U1,U2,U3,R> TriFunction<AnyMSeq<U1>,AnyMSeq<U2>,AnyMSeq<U3>,AnyMSeq<R>> liftM3Cyclops(TriFunction<? super U1,? super U2,? super U3,? extends R> fn){
		return (u1,u2,u3) -> u1.bind( input1 -> 
									u2.bind(input2 -> 
										u3.map(input3->fn.apply(input1,input2,input3)  ).unwrap()).unwrap());
	}
	/**
	 * Lift a  jOOλ Function4 into Monadic form.
	 * 
	 * @param fn Quad funciton to lift
	 * @return Lifted Quad function
	 */
	public static <U1,U2,U3,U4,R> Function4<AnyMSeq<U1>,AnyMSeq<U2>,AnyMSeq<U3>,AnyMSeq<U4>,AnyMSeq<R>> liftM4(Function4<? super U1,? super U2,? super U3,? super U4,? extends R> fn){
		
		return (u1,u2,u3,u4) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.map(input4->fn.apply(input1,input2,input3,input4)  ))).unwrap());
	}
	/**
	 * Lift a QuadFunction into Monadic form.
	 * 
	 * @param fn Quad funciton to lift
	 * @return Lifted Quad function
	 */
	public static <U1,U2,U3,U4,R> QuadFunction<AnyMSeq<U1>,AnyMSeq<U2>,AnyMSeq<U3>,AnyMSeq<U4>,AnyMSeq<R>> liftM4Cyclops(QuadFunction<? super U1,? super U2,? super U3,? super U4,? extends R> fn){
		
		return (u1,u2,u3,u4) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.map(input4->fn.apply(input1,input2,input3,input4)  ).unwrap()).unwrap()).unwrap());
	}
	/**
	 * Lift a  jOOλ Function5 (5 parameters) into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted Function
	 */
	public static <U1,U2,U3,U4,U5,R> Function5<AnyMSeq<U1>,AnyMSeq<U2>,AnyMSeq<U3>,AnyMSeq<U4>,AnyMSeq<U5>,AnyMSeq<R>> liftM5(Function5<? super U1,? super U2,? super U3,? super U4,? super U5,? extends R> fn){
		
		return (u1,u2,u3,u4,u5) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1,input2,input3,input4,input5)  )))).unwrap());
	}
	/**
	 * Lift a QuintFunction (5 parameters) into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted Function
	 */
	public static <U1,U2,U3,U4,U5,R> QuintFunction<AnyMSeq<U1>,AnyMSeq<U2>,AnyMSeq<U3>,AnyMSeq<U4>,AnyMSeq<U5>,AnyMSeq<R>> liftM5Cyclops(QuintFunction<? super U1,? super U2,? super U3,? super U4,? super U5,? extends R> fn){
		
		return (u1,u2,u3,u4,u5) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1,input2,input3,input4,input5)  ).unwrap()).unwrap()).unwrap()).unwrap());
	}
	/**
	 * Lift a Curried Function {@code(2 levels a->b->fn.apply(a,b) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,R> Function<AnyMSeq<U1>,Function<AnyMSeq<U2>,AnyMSeq<R>>> liftM2(Function<U1,Function<U2,R>> fn){
		return u1 -> u2 -> u1.bind( input1 -> u2.map(input2 -> fn.apply(input1).apply(input2)  ).unwrap());

	}
	/**
	 * Lift a Curried Function {@code(3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,R> Function<AnyMSeq<U1>,Function<AnyMSeq<U2>,Function<AnyMSeq<U3>,AnyMSeq<R>>>> liftM3(Function<? super U1,Function<? super U2,Function<? super U3,? extends R>>> fn){
		return u1 -> u2 ->u3 -> u1.bind( input1 -> 
									u2.bind(input2 -> 
										u3.map(input3->fn.apply(input1).apply(input2).apply(input3)  )).unwrap());
	}
	
	/**
	 * Lift a Curried Function {@code(4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,U4,R> Function<AnyMSeq<U1>,Function<AnyMSeq<U2>,Function<AnyMSeq<U3>,Function<AnyMSeq<U4>,AnyMSeq<R>>>>> liftM4(Function<? super U1,Function<? super U2,Function<? super U3,Function<? super U4,? extends R>>>> fn){
		
		return u1->u2->u3->u4 -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.map(input4->fn.apply(input1).apply(input2).apply(input3).apply(input4)  ))).unwrap());
	}
	/**
	 * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) ) }into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,U4,U5,R> Function<AnyMSeq<U1>,Function<AnyMSeq<U2>,Function<AnyMSeq<U3>,Function<AnyMSeq<U4>,Function<AnyMSeq<U5>,AnyMSeq<R>>>>>> liftM5(Function<? super U1,Function<? super U2,Function<? super U3,Function<? super U4,Function<? super U5,? extends R>>>>> fn){
		
		return u1 ->u2 ->u3 ->u4 ->u5  -> u1.bind( input1 -> 
										   u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1).apply(input2).apply(input3).apply(input4).apply(input5)  )))).unwrap());
	}
	


}
