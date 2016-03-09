package com.aol.cyclops.util.stream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.ToStream;
import com.aol.cyclops.types.stream.future.FutureOperations;


/**
 * Represents something that can generate a Stream, repeatedly
 * 
 * @author johnmcclean
 *
 * @param <T> Data type for Stream
 */
public interface Streamable<T> extends ToStream<T>, CyclopsCollectable<T>, 
											ConvertableSequence<T>, 
											Functor<T>,
											Filterable<T>,
											Traversable<T>,
											Unit<T>,
											ZippingApplicativable<T>{
	
	default Collectable<T> collectable(){
	  
		return Seq.seq(stream());
	}
	
	/**
	 * (Lazily) Construct a Streamable from a Stream.
	 * 
	 * @param stream to construct Streamable from
	 * @return Streamable
	 */
	public static <T> Streamable<T> fromStream(Stream<T> stream){
		return AsStreamable.fromStream(stream);
	}
	/**
	 * (Lazily) Construct a Streamable from an Iterable.
	 * 
	 * @param iterable to construct Streamable from
	 * @return Streamable
	 */
	public static <T> Streamable<T> fromIterable(Iterable<T> iterable){
		return AsStreamable.fromIterable(iterable);
	}
	public static <T> Streamable<T> fromIterator(Iterator<T> it){
		
		return AsStreamable.fromIterable(()->it);
	}

	default <T> Streamable<T> unit(T t){
		return of(t);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.IterableFunctor#unitIterator(java.util.Iterator)
	 */
	@Override
	default  <T> Streamable<T> unitIterator(Iterator<T> it) {
		return Streamable.fromIterator(it);
	}
	
	
	/**
	 * Construct a Streamable that returns a Stream
	 * 
	 * @param values to construct Streamable from
	 * @return Streamable
	 */
	public static<T> Streamable<T> of(T... values){
		Exception e;
		Iterable<T> it = Arrays.asList(values);
		return new Streamable<T>(){
			public ReactiveSeq<T> stream(){
				return ReactiveSeq.of(values);
			}
			public Iterable<T> getStreamable(){
				return it;
			}
			public ReactiveSeq<T> reactiveSeq(){
				return ReactiveSeq.of(values);
			}
			
			
		};
	}
	public static <T> Streamable<T> empty(){
		return of();
	}
	/**
	 * <pre>
	 * {@code 
	 *   Streamable.of(1,2,3,4,5).tail()
	 *   
	 *   //Streamable[2,3,4,5]
	 * }</pre>
	 * 
	 * @return The tail of this Streamable
	 */
	default Streamable<T> tail(){
		return Streamable.fromStream(reactiveSeq().headAndTail().tail());
	}
	/**
	 * <pre>
	 * {@code 
	 * Streamable.of(1,2,3,4,5).head()
	 *  
	 *  //1
	 * }</pre>
	 * @return The head of this Streamable
	 */
	default T head(){
		return reactiveSeq().headAndTail().head();
	}
	
	/**
	 * Create a new Streamablw with all elements in this Streamable followed by the elements in the provided Streamable
	 * 
	 * <pre>
	 * {@code 
	 * 	Streamable.of(1,2,3).appendAll(Streamable.of(4,5,6))
	 * 
	 *   //Streamable[1,2,3,4,5,6]
	 * }
	 * </pre>
	 * 
	 * @param t Streamable to append
	 * @return New Streamable with provided Streamable appended
	 */
	default Streamable<T> appendAll(Streamable<T> t){
		return Streamable.fromStream(this.reactiveSeq().appendStream(t.reactiveSeq()));
	}
	/**
	 * Remove all occurances of the specified element from the SequenceM
	 * <pre>
	 * {@code
	 * 	Streamable.of(1,2,3,4,5,1,2,3).remove(1)
	 * 
	 *  //Streamable[2,3,4,5,2,3]
	 * }
	 * </pre>
	 * 
	 * @param t element to remove
	 * @return Filtered Stream / SequenceM
	 */
	default Streamable<T> remove(T t){
		return Streamable.fromStream( reactiveSeq().remove(t));
	}
	/**
	 * Prepend given values to the start of the Stream
	 * <pre>
	 * {@code 
	 * List<String> result = 	Streamable.of(1,2,3)
	 * 									 .prepend(100,200,300)
										 .map(it ->it+"!!")
										 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	 * }
	 * @param values to prepend
	 * @return Streamable with values prepended
	 */
	default Streamable<T> prepend(T t){
		return Streamable.fromStream( reactiveSeq().prepend(t));
	}
	/*
	 * Return the distinct Stream of elements
	 * 
	 * <pre>
	 * {@code List<Integer> list =  Streamable.of(1,2,2,2,5,6)
	 *           	 						 .distinct()
	 *				 						 .collect(Collectors.toList()); 
	 * }
	 *</pre>
	 */
	default Streamable<T> distinct(){
		return Streamable.fromStream( reactiveSeq().distinct());
	}
	/**
	 * Fold a Streamable Left
	 * <pre>
	 * {@code 
	 *   Streamable.of("hello","world")
	 *   			.foldLeft("",(a,b)->a+":"+b);
	 *   
	 *   //"hello:world"
	 * }
	 * </pre>
	 * 
	 * @param identity - identity value
	 * @param function folding function
	 * @return Value from reduction
	 */
	default <U> U foldLeft(U identity, BiFunction<U, ? super T, U> function) {
        return reactiveSeq().foldLeft(identity, function);
    }
	 /**
	  * Fold a Streamable fromt the right
	  * <pre>
	 * {@code 
	 *   Streamable.of("hello","world")
	 *   			.foldRight("",(a,b)->a+":"+b);
	 *   
	 *   //"world:hello"
	 * }
	 * @param seed - identity value 
	 * @param function folding function
	 * @return Single reduced value
	 */
	default <U> U foldRight(U seed, BiFunction<? super T, U, U> function)  {
        return reactiveSeq().foldRight(seed, function);
    }
	/**
	 * Map the values in the Streamable from one set of values / types to another
	 * 
	 * <pre>
	 * {@code 
	 * 	Streamable.of(1,2,3).map(i->i+2);
	 *  //Streamable[3,4,5]
	 *  
	 *  Streamable.of(1,2,3).map(i->"hello"+(i+2));
	 *  
	 *   //Streamable["hello3","hello4","hello5"]
	 * }
	 * </pre>
	 * 
	 * @param fn mapper function
	 * @return Mapped Streamable
	 */
	default <R> Streamable<R> map(Function<? super T,? extends R> fn){
		return Streamable.fromStream(reactiveSeq().map(fn));
	}
	/**
	 * Peek at each value in a Streamable as it passes through unchanged
	 * 
	 * <pre>
	 * {@code
	 *    Streamable.of(1,2,3)
	 *              .peek(System.out::println)
	 *              .map(i->i+2);
	 * }
	 * </pre>
	 * 
	 * @param fn Consumer to peek with
	 * @return Streamable that will peek at values as they pass through
	 */
	default  Streamable<T> peek(Consumer<? super T> fn){
		return Streamable.fromStream(reactiveSeq().peek(fn));
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#filtered(java.util.function.Predicate)
	 */
	default  Streamable<T> filter(Predicate<? super T> fn){
		return Streamable.fromStream(reactiveSeq().filter(fn));
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#flatMap(java.util.function.Function)
	 */
	default <R> Streamable<R> flatMap(Function<? super T,Streamable<? extends R>> fn){
		return Streamable.fromStream(reactiveSeq().flatMap(i->fn.apply(i).reactiveSeq()));
	}
	
	/**
	 * @return number of elements in this Streamable
	 */
	default long count(){
		return reactiveSeq().count();
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
	 */
    default void forEachOrdered(Consumer<? super T> action){
    	reactiveSeq().forEachOrdered(action);
    }

    /* (non-Javadoc)
	 * @see java.util.stream.Stream#toArray()
	 */
    default Object[] toArray(){
    	return reactiveSeq().toArray();
    }

    /* (non-Javadoc)
	 * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
	 */
    default <A> A[] toArray(IntFunction<A[]> generator){
    	return reactiveSeq().toArray(generator);
    }
	/**
	 * <pre>
	 * {@code 
	 *   Streamable.of(1,2,3)
	 *             .toList(); 
	 * 
	 *  //List[1,2,3]
	 * }
	 * </pre>
	 * 
	 * @return Streamable converted to a List
	 */
	default List<T> toList(){
	
		if(getStreamable() instanceof List)
			return ListX.fromIterable((List)getStreamable());
		return reactiveSeq().toList();
	}
	default  <R> R collect(Supplier<R> supplier,
            BiConsumer<R, ? super T> accumulator,
            BiConsumer<R, R> combiner){
		return reactiveSeq().collect(supplier,accumulator,combiner);
	}
	default  <R, A> R collect(Collector<? super T, A, R> collector){
		
		return reactiveSeq().collect(collector);
	}
	/**
	 * Add the contents of this Stream to the mutable collection supplied by 
	 * the provided collectionFactory
	 * 
	 * <pre>
	 * {@code 
	 *   Streamable.of(1,2,3).toCollection( ()->new ArrayList());
	 *   
	 *   //ArrayList[1,2,3]
	 * }
	 * </pre>
	 * 
	 * @param collectionFactory
	 * @return contents of this Stream in a mutable collection
	 */
	default <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory){
		
		return reactiveSeq().toCollection(collectionFactory);
	}
	
	/**
	 * Generate the permutations based on values in the Streamable
	
	 * 
	 */
    default Streamable<Streamable<T>> permutations() {
        if (isEmpty()) {
            return Streamable.empty();
        } else {
            final Streamable<T> tail = tail();
            if (tail.isEmpty()) {
                return Streamable.of(this);
            } else {
                final Streamable<Streamable<T>> zero = Streamable.empty();
                return distinct().foldLeft(zero, (xs, x) -> {
                    final Function<Streamable<T>, Streamable<T>> prepend = l -> l.prepend(x);
                    return xs.appendAll(remove(x).permutations().map(prepend));
                });
            }
        }
    }
	/**
	 * Return a Streamable with elements before the provided start index removed, and elements after the provided
	 * end index removed
	 * 
	 * <pre>
	 * {@code 
	 *   Streamable.of(1,2,3,4,5,6).subStream(1,3);
	 *   
	 *   
	 *   //Streamable[2,3]
	 * }
	 * </pre>
	 * 
	 * @param start index inclusive
	 * @param end index exclusive
	 * @return Sequence between supplied indexes of original Sequence
	 */
    default Streamable<T> subStream(int start, int end){
    	return Streamable.fromStream(reactiveSeq().subStream(start,end));
    }
    /**
	 * Gets the element at index (it must be present)
	 * 
	 * <pre>
	 * {@code 
	 * Streamable.of(1,2,3,4,5).get(2)
	 * //3
	 * }
	 * </pre>
	 * 
	 * @param index to extract element from
	 * @return Element and Sequence
	 */
    default T elementAt(int index){
    	return this.reactiveSeq().elementAt(index).v1;
    }
    /**
	 * [equivalent to count]
	 * 
	 * @return size
	 */
    default int size(){
    	return reactiveSeq().size();
    }
    /**
	 * <pre>
	 * {@code
	 *   Streamable.of(1,2,3).combinations(2)
	 *   
	 *   //Streamable[Streamable[1,2],Streamable[1,3],Streamable[2,3]]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param size of combinations
	 * @return All combinations of the elements in this stream of the specified size
	 */
    default  Streamable<Streamable<T>> combinations(int size) {
        if (size == 0) {
            return Streamable.of(Streamable.empty()); 
        } else {
            return Streamable.fromStream(IntStream.range(0, size()).boxed().
                <Streamable<T>> flatMap(i -> subStream(i+1, size()).combinations( size - 1).map(t -> t.prepend(elementAt(i))).reactiveSeq()));
        }
    }
    /**
	 * <pre>
	 * {@code
	 *   Streamable.of(1,2,3).combinations()
	 *   
	 *   //Streamable[Streamable[],Streamable[1],Streamable[2],Streamable[3],Streamable[1,2],Streamable[1,3],Streamable[2,3]
	 *   			,Streamable[1,2,3]]
	 * }
	 * </pre>
	 * 
	 * 
	 * @return All combinations of the elements in this stream
	 */
    default  Streamable<Streamable<T>> combinations(){
		return range(0, size()+1).map(this::combinations).flatMap(s->s);
	    
	}
    
    
    	/**
    	 * join / flatten one level of a nested hierarchy
    	 * 
    	 * <pre>
    	 * {@code 
    	 *  Streamable.of(Arrays.asList(1,2)).flatten();
    	 *  
    	 *  //stream of (1,  2);		
    	 *  
    	 * }
    	 * 
    	 * </pre>
    	 * 
    	 * @return Flattened / joined one level
    	 */
    	default <T1> Streamable<T1> flatten(){
    		return Streamable.fromStream(reactiveSeq().flatten());
    	}
    	
    	/**
    	 * Type safe unwrap 
    	 * <pre>
    	 * {@code 
    	 * Optional<List<String>> stream = Streamable.of("hello","world")
    												.toOptional();
    												
    		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
    	 * }
    	 * 
    	 * </pre>
    	 * @return
    	 */
    	default Optional<ListX<T>> toOptional(){
    		return reactiveSeq().toOptional();
    	}
    	/**
    	 * <pre>
    	 * {@code 
    	 * CompletableFuture<List<String>> cf = Streamable.of("hello","world")
    											.toCompletableFuture();
    		assertThat(cf.join(),equalTo(Arrays.asList("hello","world")));
    	 * }
    	 * </pre>
    	 * @return
    	 */
    	default CompletableFuture<ListX<T>> toCompletableFuture(){
    		return reactiveSeq().toCompletableFuture();
    	}
    	
    	/**
    	 * Convert to a Stream with the values repeated specified times
    	 * 
    	 * <pre>
    	 * {@code 
    	 * 		assertThat(Streamable.of(1,2,2)
    								.cycle(3)
    								.collect(Collectors.toList()),
    								equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));

    	 * 
    	 * }
    	 * </pre>
    	 * @param times
    	 *            Times values should be repeated within a Stream
    	 * @return Streamable with values repeated
    	 */
    	 default Streamable<T> cycle(int times){
    		 return Streamable.fromStream(reactiveSeq().cycle(times));
    	 }
    	/**
    	 * Convert to a Stream with the values infinitely cycled
    	 * 
    	 * <pre>
    	 * {@code 
    	 *   assertEquals(asList(1, 1, 1, 1, 1,1),Streamable.of(1).cycle().limit(6).toList());
    	 *   }
    	 * </pre>
    	 * 
    	 * @return Stream with values repeated
    	 */
    	default Streamable<T> cycle(){
    		 return Streamable.fromStream(reactiveSeq().cycle());
    	}
    	/**
    	 * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
    	 * can be safely applied to the leading stream. Not thread-safe.
    	 * <pre>
    	 * {@code 
    	 *  Tuple2<Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).duplicate();
    		 assertTrue(copies.v1.anyMatch(i->i==2));
    		 assertTrue(copies.v2.anyMatch(i->i==2));
    	 * 
    	 * }
    	 * </pre>
    	 * 
    	 * @return duplicated stream
    	 */
    	default Tuple2<Streamable<T>,Streamable<T>> duplicate(){
    		return Tuple.tuple(this,this);
    	}
    	default Tuple3<Streamable<T>,Streamable<T>,Streamable<T>> triplicate(){
    		return Tuple.tuple(this,this,this);
    	}
    	default Tuple4<Streamable<T>,Streamable<T>,Streamable<T>,Streamable<T>> quadruplicate(){
    		return Tuple.tuple(this,this,this,this);
    	}
    	
    	
    	
    	/**
    	 * Split at supplied location 
    	 * <pre>
    	 * {@code 
    	 * Streamable.of(1,2,3).splitAt(1)
    	 * 
    	 *  //Streamable[1], Streamable[2,3]
    	 * }
    	 * 
    	 * </pre>
    	 */
    	default Tuple2<Streamable<T>,Streamable<T>> splitAt(int where){
    		
    		return reactiveSeq().splitAt(where).map1(s->fromStream(s)).map2(s->fromStream(s));
    	}
    	/**
    	 * Split this Streamable after the first element (if present)
    	 * 
    	 * <pre>
    	 * {@code 
    	 *  Streamable.of(1,2,3).splitAtHead()
    	 *  
    	 *  //Tuple[1,Streamable[2,3]]
    	 *  
    	 * }</pre>
    	 * 
    	 * 
    	 * @return Split Streamable
    	 */
    	default Tuple2<Optional<T>, Streamable<T>> splitAtHead() {
            return reactiveSeq().splitAtHead().map2(s->fromStream(s));
        }
    	/**
    	 * Split stream at point where predicate no longer holds
    	 * <pre>
    	 * {@code
    	 *   Streamable.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
    	 *   
    	 *   //Streamable[1,2,3] Streamable[4,5,6]
    	 * }
    	 * </pre>
    	 */
    	default Tuple2<Streamable<T>,Streamable<T>> splitBy(Predicate<T> splitter){
    		return reactiveSeq().splitBy(splitter).map1(s->fromStream(s)).map2(s->fromStream(s));
    	}
    	/**
    	 * Partition a Stream into two one a per element basis, based on predicate's boolean value
    	 * <pre>
    	 * {@code 
    	 *  Streamable.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0) 
    	 *  
    	 *  //Streamable[1,3,5], Streamable[2,4,6]
    	 * }
    	 *
    	 * </pre>
    	 */
    	default Tuple2<Streamable<T>,Streamable<T>> partition(Predicate<T> splitter){
    		return reactiveSeq().partitionSequence(splitter).map1(s->fromStream(s)).map2(s->fromStream(s));
    	}
    	
    	
    	
    	/**
    	 * Convert to a Stream with the result of a reduction operation repeated
    	 * specified times
    	 * 
    	 * <pre>
    	 * {@code 
    	 *   		List<Integer> list = AsGenericMonad,asMonad(Stream.of(1,2,2))
    	 * 										.cycle(Reducers.toCountInt(),3)
    	 * 										.collect(Collectors.toList());
    	 * 	//is asList(3,3,3);
    	 *   }
    	 * </pre>
    	 * 
    	 * @param m
    	 *            Monoid to be used in reduction
    	 * @param times
    	 *            Number of times value should be repeated
    	 * @return Stream with reduced values repeated
    	 */
    	default Streamable<T> cycle(Monoid<T> m, int times){
    		return fromStream(reactiveSeq().cycle(m,times));
    	}

    	
   
    	/**
    	 * Repeat in a Stream while specified predicate holds
    	 * 
    	 * <pre>
    	 * {@code
    	 * count =0;
    		assertThat(Streamable.of(1,2,2)
    							.cycleWhile(next -> count++<6)
    							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
    	 * }
    	 * </pre>
    	 * 
    	 * @param predicate
    	 *            repeat while true
    	 * @return Repeating Stream
    	 */
    	default Streamable<T> cycleWhile(Predicate<? super T> predicate){
    		return Streamable.fromStream(reactiveSeq().cycleWhile(predicate));
    	}

    	/**
    	 * Repeat in a Stream until specified predicate holds
    	 * <pre>
    	 * {@code 
    	 * 	count =0;
    		assertThat(Streamable.of(1,2,2)
    							.cycleUntil(next -> count++>6)
    							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1)));

    	 * 
    	 * }
    	 * 
    	 * 
    	 * @param predicate
    	 *            repeat while true
    	 * @return Repeating Stream
    	 */
    	default Streamable<T> cycleUntil(Predicate<? super T> predicate){
    		return Streamable.fromStream(reactiveSeq().cycleUntil(predicate));
    	}
    	
    	/**
    	 * Zip 2 streams into one
    	 * 
    	 * <pre>
    	 * {@code 
    	 * List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();
           // [[1,"a"],[2,"b"]]
    		 } 
    	 * </pre>
    	 * 
    	 */
    	default <U> Streamable<Tuple2<T, U>> zip(Streamable<U> other){
    		return fromStream(reactiveSeq().zip(other.reactiveSeq()));
    	}
    	default <U,R> Streamable<R> zip(Streamable<U> other,BiFunction<T, U, R> zipper){
    		return fromStream(reactiveSeq().zip(other.reactiveSeq(), zipper));
    	}
    	/**
    	 * zip 3 Streams into one
    	 * <pre>
    	 * {@code 
    	 * List<Tuple3<Integer,Integer,Character>> list =
    				of(1,2,3,4,5,6).zip3(of(100,200,300,400),of('a','b','c'))
    											.collect(Collectors.toList());
    	 * 
    	 * //[[1,100,'a'],[2,200,'b'],[3,300,'c']]
    	 * }
    	 * 
    	 *</pre>
    	 */
    	default <S,U> Streamable<Tuple3<T,S,U>> zip3(Streamable<? extends S> second,Streamable<? extends U> third){
    		return fromStream(reactiveSeq().zip3(second.reactiveSeq(),third.reactiveSeq()));
    	}
    	/**
    	 * zip 4 Streams into 1
    	 * 
    	 * <pre>
    	 * {@code 
    	 * List<Tuple4<Integer,Integer,Character,String>> list =
    				of(1,2,3,4,5,6).zip4(of(100,200,300,400),of('a','b','c'),of("hello","world"))
    												.collect(Collectors.toList());
    			
    	 * }
    	 *  //[[1,100,'a',"hello"],[2,200,'b',"world"]]
    	 * </pre>
    	 */
    	 default <T2,T3,T4> Streamable<Tuple4<T,T2,T3,T4>> zip4(Streamable<T2> second,Streamable<T3> third,Streamable<T4> fourth){
    		 return fromStream(reactiveSeq().zip4(second.reactiveSeq(),third.reactiveSeq(),fourth.reactiveSeq()));
    	 }
    	/** 
    	 * Add an index to the current Stream
    	 * 
    	 * <pre>
    	 * {@code 
    	 * assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
    	 * }
    	 * </pre>
    	 */
    	default Streamable<Tuple2<T,Long>> zipWithIndex(){
    		return fromStream(reactiveSeq().zipWithIndex());
    	}
    	/**
    	 * Generic zip function. E.g. Zipping a Stream and an Optional
    	 * 
    	 * <pre>
    	 * {
    	 * 	&#064;code
    	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = asMonad(Stream.of(1, 2, 3)).zip(
    	 * 			asMonad(Optional.of(2)), (a, b) -&gt; Arrays.asList(a, b));
    	 * 	// [[1,2]]
    	 * }
    	 * </pre>
    	 * 
    	 * @param second
    	 *            Monad to zip with
    	 * @param zipper
    	 *            Zipping function
    	 * @return Stream zipping two Monads
    	 */
    	default <S, R> Streamable<R> zipStreamable(Streamable<? extends S> second,
    			BiFunction<? super T, ? super S, ? extends R> zipper) {
    		return fromStream(reactiveSeq().zipSequence(second.reactiveSeq(), zipper));
    	}
    	/**
    	 * Zip this Streamable against any monad type.
    	 * 
    	 * <pre>
    	 * {@code
    	 * Stream<List<Integer>> zipped = anyM(Stream.of(1,2,3))
    										.asSequence()
    										.zip(anyM(Optional.of(2)), 
    											(a,b) -> Arrays.asList(a,b)).toStream();
    		
    		
    		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
    		assertThat(zip.get(0),equalTo(1));
    		assertThat(zip.get(1),equalTo(2));
    	 * }
    	 * </pre>
    	 * 
    	 */
    	default <S, R> Streamable<R> zipAnyM(AnyMSeq<? extends S> second,
    			BiFunction<? super T, ? super S, ? extends R> zipper){
    		return fromStream(reactiveSeq().zipAnyM(second, zipper));
    	}

    	/**
    	 * Zip this Monad with a Stream
    	 * 
    	 * <pre>
    	 * {
    	 * 	&#064;code
    	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = asMonad(Stream.of(1, 2, 3)).zip(
    	 * 			Stream.of(2, 3, 4), (a, b) -&gt; Arrays.asList(a, b));
    	 * 
    	 * 	// [[1,2][2,3][3,4]]
    	 * }
    	 * </pre>
    	 * 
    	 * @param second
    	 *            Stream to zip with
    	 * @param zipper
    	 *            Zip funciton
    	 * @return This monad zipped with a Stream
    	 */
    	 default <S, R> Streamable<R> zipStream(BaseStream<? extends S,? extends BaseStream<? extends S,?>> second,
    			BiFunction<? super T, ? super S, ? extends R> zipper){
    		 return fromStream(reactiveSeq().zipStream(second,zipper));
    	 }

    	/**
    	 * Create a sliding view over this Sequence
    	 * 
    	 * <pre>
    	 * {@code 
    	 * List<List<Integer>> list = anyM(Stream.of(1,2,3,4,5,6))
    									.asSequence()
    									.sliding(2)
    									.collect(Collectors.toList());
    		
    	
    		assertThat(list.get(0),hasItems(1,2));
    		assertThat(list.get(1),hasItems(2,3));
    	 * 
    	 * }
    	 * 
    	 * </pre>
    	 * @param windowSize
    	 *            Size of sliding window
    	 * @return Streamable with sliding view
    	 */
    	default Streamable<ListX<T>> sliding(int windowSize){
    		return fromStream(reactiveSeq().sliding(windowSize));
    	}
    	/**
    	 *  Create a sliding view over this Sequence
    	 * <pre>
    	 * {@code 
    	 * List<List<Integer>> list = anyM(Stream.of(1,2,3,4,5,6))
    									.asSequence()
    									.sliding(3,2)
    									.collect(Collectors.toList());
    		
    	
    		assertThat(list.get(0),hasItems(1,2,3));
    		assertThat(list.get(1),hasItems(3,4,5));
    	 * 
    	 * }
    	 * 
    	 * </pre>
    	 * 
    	 * @param windowSize number of elements in each batch
    	 * @param increment for each window
    	 * @return Streamable with sliding view
    	 */
    	default Streamable<ListX<T>> sliding(int windowSize,int increment){
    		return fromStream(reactiveSeq().sliding(windowSize,increment));
    	}

    	/**
    	 * Group elements in a Stream
    	 * 
    	 * <pre>
    	 * {
    	 * 	&#064;code
    	 * 	List&lt;List&lt;Integer&gt;&gt; list = monad(Stream.of(1, 2, 3, 4, 5, 6)).grouped(3)
    	 * 			.collect(Collectors.toList());
    	 * 
    	 * 	assertThat(list.get(0), hasItems(1, 2, 3));
    	 * 	assertThat(list.get(1), hasItems(4, 5, 6));
    	 * 
    	 * }
    	 * </pre>
    	 * 
    	 * @param groupSize
    	 *            Size of each Group
    	 * @return Stream with elements grouped by size
    	 */
    	default Streamable<ListX<T>> grouped(int groupSize){
    		return fromStream(reactiveSeq().grouped(groupSize));
    	}
    	/**
    	 * Use classifier function to group elements in this Sequence into a Map
    	 * <pre>
    	 * {@code 
    	 * Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
    		        assertEquals(asList(2, 4), map1.get(0));
    		        assertEquals(asList(1, 3), map1.get(1));
    		        assertEquals(2, map1.size());
    	 * 
    	 * }
    	 * 
    	 * </pre>
    	 */
    	default <K> MapX<K, List<T>> groupBy(Function<? super T, ? extends K> classifier){
    		return reactiveSeq().groupBy(classifier);
    	}

    

    	/**
    	 * Scan left using supplied Monoid
    	 * 
    	 * <pre>
    	 * {@code  
    	 * 
    	 * 	assertEquals(asList("", "a", "ab", "abc"),Streamable.of("a", "b", "c")
    	 * 													.scanLeft(Reducers.toString("")).toList());
    	 *         
    	 *         }
    	 * </pre>
    	 * 
    	 * @param monoid
    	 * @return
    	 */
    	default Streamable<T> scanLeft(Monoid<T> monoid){
    		return fromStream(reactiveSeq().scanLeft(monoid));	
    	}
    	/**
    	 * Scan left
    	 * <pre>
    	 * {@code 
    	 *  assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
            		is(4));
    	 * }
    	 * </pre>
    	 */
    	default <U> Streamable<U> scanLeft(U identity, BiFunction<U, ? super T, U> function){
    		return fromStream(reactiveSeq().scanLeft(identity,function));		
    	}
    	
    	/**
    	 * Scan right
    	 * <pre>
    	 * {@code 
    	 * assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
                is(asList("", "c", "bc", "abc").size()));
    	 * }
    	 * </pre>
    	 */
    	default Streamable<T> scanRight(Monoid<T> monoid){
    		return fromStream(reactiveSeq().scanRight(monoid));		
    	}
    	/**
    	 * Scan right
    	 * 
    	 * <pre>
    	 * {@code 
    	 * assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
                is(asList(0, 3, 5, 6).size()));
    	 * 
    	 * }
    	 * </pre>
    	 */
    	default <U> Streamable<U> scanRight(U identity,BiFunction<? super T, U, U>  combiner){
    		return fromStream(reactiveSeq().scanRight(identity,combiner));			
    	}
    	

    	
    	/**
    	 * <pre>
    	 * {@code assertThat(Streamable.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
    	 * </pre>
    	 * 
    	 */
    	 default Streamable<T> sorted(){
    		 return fromStream(reactiveSeq().sorted());		
    	 }

    	/**
    	 *<pre>
    	 * {@code 
    	 * 	assertThat(Streamable.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
    	 * }
    	 * </pre>
    	 * @param c
    	 *            Compartor to sort with
    	 * @return Sorted Monad
    	 */
    	default Streamable<T> sorted(Comparator<? super T> c){
    		return fromStream(reactiveSeq().sorted(c));			
    	}

    	/**
    	 * <pre>
    	 * {@code assertThat(Streamable.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
    	 * </pre>
    	 * 
    	
    	 * 
    	 * @param num
    	 *            Number of elemenets to skip
    	 * @return Monad converted to Stream with specified number of elements
    	 *         skipped
    	 */
    	default Streamable<T> skip(long num){
    		return fromStream(reactiveSeq().skip(num));		
    	}
    	/**
    	 * 
    	 * 
    	 * <pre>
    	 * {@code
    	 * assertThat(Streamable.of(4,3,6,7).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
    	 * }
    	 * </pre>
    	 * 
    	 * @param p
    	 *            Predicate to skip while true
    	 * @return Monad converted to Stream with elements skipped while predicate
    	 *         holds
    	 */
    	 default Streamable<T> skipWhile(Predicate<? super T> p){
    		 return fromStream(reactiveSeq().skipWhile(p));		
    	 }

    	/**
    	 * 
    	 * 
    	 * <pre>
    	 * {@code assertThat(Streamable.of(4,3,6,7).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}
    	 * </pre>
    	 * 
    	 * 
    	 * @param p
    	 *            Predicate to skip until true
    	 * @return Monad converted to Stream with elements skipped until predicate
    	 *         holds
    	 */
    	default Streamable<T> skipUntil(Predicate<? super T> p){
    		return fromStream(reactiveSeq().skipUntil(p));		
    	}

    	/**
    	 * 
    	 * 
    	 * <pre>
    	 * {@code assertThat(Streamable.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
    	 * </pre>
    	 * 
    	 * @param num
    	 *            Limit element size to num
    	 * @return Monad converted to Stream with elements up to num
    	 */
    	default Streamable<T> limit(long num){
    		return fromStream(reactiveSeq().limit(num));		
    	}

    	/**
    	 *
    	 * 
    	 * <pre>
    	 * {@code assertThat(Streamable.of(4,3,6,7).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}
    	 * </pre>
    	 * 
    	 * @param p
    	 *            Limit while predicate is true
    	 * @return Monad converted to Stream with limited elements
    	 */
    	default Streamable<T> limitWhile(Predicate<? super T> p){
    		return fromStream(reactiveSeq().limitWhile(p));		
    	}
    	/**
    	 * 
    	 * 
    	 * <pre>
    	 * {@code assertThat(Streamable.of(4,3,6,7).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }
    	 * </pre>
    	 * 
    	 * @param p
    	 *            Limit until predicate is true
    	 * @return Monad converted to Stream with limited elements
    	 */
    	default Streamable<T> limitUntil(Predicate<? super T> p){
    		return fromStream(reactiveSeq().limitUntil(p));		
    	
    	}
    	/**
    	 * True if predicate matches all elements when Monad converted to a Stream
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
    	 * }
    	 * </pre>
    	 * @param c Predicate to check if all match
    	 */
    	default boolean  allMatch(Predicate<? super T> c){
    		return reactiveSeq().allMatch(c);
    	}
    	/**
    	 * True if a single element matches when Monad converted to a Stream
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
    	 * }
    	 * </pre>
    	 * @param c Predicate to check if any match
    	 */
    	default boolean  anyMatch(Predicate<? super T> c){
    		return reactiveSeq().anyMatch(c);
    	}
    	/**
    	 * Check that there are specified number of matches of predicate in the Stream
    	 * 
    	 * <pre>
    	 * {@code 
    	 *  assertTrue(Streamable.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
    	 * }
    	 * </pre>
    	 * 
    	 */
    	default boolean xMatch(int num, Predicate<? super T> c){
    		return reactiveSeq().xMatch(num,c);
    	}
    	/* 
    	 * <pre>
    	 * {@code 
    	 * assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),equalTo(true));
    	 * 
    	 * }
    	 * </pre>
    	 */
    	default boolean  noneMatch(Predicate<? super T> c){
    		return reactiveSeq().noneMatch(c);
    	}
    	/**
    	 * <pre>
    	 * {@code
    	 *  assertEquals("123".length(),Streamable.of(1, 2, 3).join().length());
    	 * }
    	 * </pre>
    	 * 
    	 * @return Stream as concatenated String
    	 */
    	 default String join(){
    		 return reactiveSeq().join();
    	 }
    	/**
    	 * <pre>
    	 * {@code
    	 * assertEquals("1, 2, 3".length(), Streamable.of(1, 2, 3).join(", ").length());
    	 * }
    	 * </pre>
    	 * @return Stream as concatenated String
    	 */
    	default String join(String sep){
    		return reactiveSeq().join(sep);
    	}
    	/**
    	 * <pre>
    	 * {@code 
    	 * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
    	 * }
    	 * </pre> 
    	 *  @return Stream as concatenated String
    	 */
    	default String join(String sep,String start, String end){
    		return reactiveSeq().join(sep,start,end);
    	}
    	
    	
    	/**
    	 * Extract the minimum as determined by supplied function
    	 * 
    	 */
    	default <C extends Comparable<? super C>> Optional<T> minBy(Function<? super T,? extends C> f){
    		return reactiveSeq().minBy(f);
    	}
    	/* (non-Javadoc)
    	 * @see java.util.stream.Stream#min(java.util.Comparator)
    	 */
    	default Optional<T> min(Comparator<? super T> comparator){
    		return reactiveSeq().min(comparator);
    	}
    	/**
    	 * Extract the maximum as determined by the supplied function
    	 * 
    	 */
    	default <C extends Comparable<? super C>> Optional<T> maxBy(Function<? super T,? extends C> f){
    		return reactiveSeq().maxBy(f);
    	}
    		
    	/* (non-Javadoc)
    	 * @see java.util.stream.Stream#max(java.util.Comparator)
    	 */
    	 default Optional<T> max(Comparator<? super T> comparator){
    		 return reactiveSeq().max(comparator);
    	 }	
    	
    	
    	
    	
    	
    	/**
    	 * @return First matching element in sequential order
    	 * <pre>
    	 * {@code
    	 * Streamable.of(1,2,3,4,5).filter(it -> it <3).findFirst().get();
    	 * 
    	 * //3
    	 * }
    	 * </pre>
    	 * (deterministic)
    	 * 
    	 */
    	default Optional<T>  findFirst(){
    		return reactiveSeq().findFirst();
    	}
    	/**
    	 * @return first matching element,  but order is not guaranteed
    	 * <pre>
    	 * {@code
    	 * Streamable.of(1,2,3,4,5).filter(it -> it <3).findAny().get();
    	 * 
    	 * //3
    	 * }
    	 * </pre>
    	 * 
    	 * 
    	 * (non-deterministic) 
    	 */
    	 default Optional<T>  findAny(){
    		 return reactiveSeq().findAny();
    	 }
    	
    	/**
    	 * Attempt to map this Sequence to the same type as the supplied Monoid (Reducer)
    	 * Then use Monoid to reduce values
    	 * <pre>
    	 * {@code 
    	 * Streamable.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
    	 * 
    	 * //4
    	 * }
    	 * </pre>
    	 * 
    	 * @param reducer Monoid to reduce values
    	 * @return Reduce result
    	 */
    	default <R> R mapReduce(Reducer<R> reducer){
    		return reactiveSeq().mapReduce(reducer);
    	}
    	/**
    	 *  Attempt to map this Monad to the same type as the supplied Monoid, using supplied function
    	 *  Then use Monoid to reduce values
    	 *  
    	 *  <pre>
    	 *  {@code
    	 *  Streamable.of("one","two","three","four")
    	 *           .mapReduce(this::toInt,Reducers.toTotalInt());
    	 *  
    	 *  //10
    	 *  
    	 *  int toInt(String s){
    		if("one".equals(s))
    			return 1;
    		if("two".equals(s))
    			return 2;
    		if("three".equals(s))
    			return 3;
    		if("four".equals(s))
    			return 4;
    		return -1;
    	   }
    	 *  }
    	 *  </pre>
    	 *  
    	 * @param mapper Function to map Monad type
    	 * @param reducer Monoid to reduce values
    	 * @return Reduce result
    	 */
    	default <R> R mapReduce(Function<? super T,? extends R> mapper, Monoid<R> reducer){
    		return reactiveSeq().mapReduce(mapper,reducer);
    	}
    	

    	
    	/**
    	 * <pre>
    	 * {@code 
    	 * Streamable.of("hello","2","world","4").reduce(Reducers.toString(","));
    	 * 
    	 * //hello,2,world,4
    	 * }</pre>
    	 * 
    	 * @param reducer Use supplied Monoid to reduce values
    	 * @return reduced values
    	 */
    	default T reduce(Monoid<T> reducer){
    		 return reactiveSeq().reduce(reducer);
    	}
    	/* 
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
    	 * }
    	 * </pre>
    	 * 
    	 */
    	default  Optional<T> reduce(BinaryOperator<T> accumulator){
    		 return reactiveSeq().reduce(accumulator);
    	}
    	 /* (non-Javadoc)
    	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
    	 */
    	default T reduce(T identity, BinaryOperator<T> accumulator){
    		 return reactiveSeq().reduce(identity,accumulator);
    	}
    	 /* (non-Javadoc)
    	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
    	 */
    	default <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner){
    		 return reactiveSeq().reduce(identity,accumulator,combiner);
    	}
    	/**
         * Reduce with multiple reducers in parallel
    	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
    	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
    	 * 
    	 * <pre>
    	 * {@code 
    	 * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
    	   Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
    	   List<Integer> result = Streamable.of(1,2,3,4)
    						.reduce(Arrays.asList(sum,mult).stream() );
    				
    		 
    		assertThat(result,equalTo(Arrays.asList(10,24)));
    	 * 
    	 * }
    	 * </pre>
    	 * 
    	 * 
    	 * @param reducers
    	 * @return
    	 */
    	 default ListX<T> reduce(Stream<? extends Monoid<T>> reducers){
    		 return reactiveSeq().reduce(reducers);
    	 }
    	/**
         * Reduce with multiple reducers in parallel
    	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
    	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
    	 * 
    	 * <pre>
    	 * {@code 
    	 * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
    		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
    		List<Integer> result = Streamable.of(1,2,3,4))
    										.reduce(Arrays.asList(sum,mult) );
    				
    		 
    		assertThat(result,equalTo(Arrays.asList(10,24)));
    	 * 
    	 * }
    	 * 
    	 * @param reducers
    	 * @return
    	 */
    	 default ListX<T> reduce(Iterable<? extends Monoid<T>> reducers){
    		 return reactiveSeq().reduce(reducers);
    	 }
    	
    	
    	/**
    	 * 
    	 * <pre>
    		{@code
    		Streamable.of("a","b","c").foldRight(Reducers.toString(""));
           
            // "cab"
            }
            </pre>
    	 * @param reducer Use supplied Monoid to reduce values starting via foldRight
    	 * @return Reduced result
    	 */
    	 default T foldRight(Monoid<T> reducer){
    		 return reactiveSeq().foldRight(reducer);
    	 }
    	/**
    	 * Immutable reduction from right to left
    	 * <pre>
    	 * {@code 
    	 *  assertTrue(Streamable.of("a","b","c").foldRight("", String::concat).equals("cba"));
    	 * }
    	 * </pre>
    	 * 
    	 * @param identity
    	 * @param accumulator
    	 * @return
    	 */
    	default  T foldRight(T identity,  BinaryOperator<T> accumulator){
    		 return reactiveSeq().foldRight(identity,accumulator);
    	}	
    	/**
    	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
    	 * Then use Monoid to reduce values
    	 * <pre>
    		{@code
    		Streamable.of(1,2,3).foldRightMapToType(Reducers.toString(""));
           
            // "321"
            }
            </pre>
    	 * 
    	 * 
    	 * @param reducer Monoid to reduce values
    	 * @return Reduce result
    	 */
    	default <T> T foldRightMapToType(Reducer<T> reducer){
    		 return reactiveSeq().foldRightMapToType(reducer);
    	}
    	
    	
    	
    	
    	/**
    	 * 
    	 * <pre>
    	 * {@code 
    	 *  assertTrue(Streamable.of(1,2,3,4).startsWith(Arrays.asList(1,2,3)));
    	 * }</pre>
    	 * 
    	 * @param iterable
    	 * @return True if Monad starts with Iterable sequence of data
    	 */
    	default boolean startsWithIterable(Iterable<T> iterable){
    		 return reactiveSeq().startsWithIterable(iterable);
    	}	
    	/**
    	 * 	<pre>{@code assertTrue(Streamable.of(1,2,3,4).startsWith(Arrays.asList(1,2,3).iterator())) }</pre>

    	 * @param iterator
    	 * @return True if Monad starts with Iterators sequence of data
    	 */
    	default boolean startsWith(Iterator<T> iterator){
    		 return reactiveSeq().startsWith(iterator);
    	}
    	
    	/**
    	 * @return this Streamable converted to AnyM format
    	 */
    	default AnyM<T> anyM(){
    		return AnyM.fromStreamable(this);
    	}
    	
    	/**
    	 * Allows flatMap return type to be any Monad type
    	 * <pre>
    	 * {@code 
    	 * 	assertThat(Streamable.of(1,2,3)).flatMapAnyM(i-> anyM(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));

    	 * }</pre>
    	 * 
    	 * 
    	 * @param fn to be applied
    	 * @return new stage in Sequence with flatMap operation to be lazily applied
    	 */
    	default <R> Streamable<R> flatMapAnyM(Function<? super T,AnyM<? extends R>> fn){
  
    		 return fromStream(reactiveSeq().flatMapAnyM(fn));
    	}
    	/**
    	 * FlatMap where the result is a Collection, flattens the resultant collections into the
    	 * host Streamable
    	 * <pre>
    	 * {@code 
    	 * 	Streamable.of(1,2)
    	 * 			.flatMap(i -> asList(i, -i))
    	 *          .toList();
    	 *          
    	 *   //1,-1,2,-2       
    	 * }
    	 * </pre>
    	 * 
    	 * @param fn
    	 * @return
    	 */
    	default <R> Streamable<R> flatMapIterable(Function<? super T,Iterable<? extends R>> fn){
    		 return fromStream(reactiveSeq().flatMapIterable(fn));
    	}
    	/**
    	 * flatMap operation
    	 * 
    	 * <pre>
    	 * {@code 
    	 * 	assertThat(Streamable.of(1,2,3)
    	 *                      .flatMapStream(i->IntStream.of(i))
    	 *                      .toList(),equalTo(Arrays.asList(1,2,3)));

    	 * }
    	 * </pre>
    	 * 
    	 * @param fn to be applied
    	 * @return new stage in Sequence with flatMap operation to be lazily applied
    	*/
    	default <R> Streamable<R> flatMapStream(Function<? super T,BaseStream<? extends R,?>> fn){
    		 return fromStream(reactiveSeq().flatMapStream(fn));
    	}
    	 	
    	/**
    	 * Returns a stream with a given value interspersed between any two values
    	 * of this stream.
    	 * 
    	 * 
    	 * // (1, 0, 2, 0, 3, 0, 4) Streamable.of(1, 2, 3, 4).intersperse(0)
    	 * 
    	 */
    	default  Streamable<T> intersperse(T value){
    		 return fromStream(reactiveSeq().intersperse(value));
    	}
    	/**
    	 * Keep only those elements in a stream that are of a given type.
    	 * 
    	 * 
    	 * // (1, 2, 3) Streamable.of(1, "a", 2, "b",3).ofType(Integer.class)
    	 * 
    	 */
    	@SuppressWarnings("unchecked")
    	default <U> Streamable<U> ofType(Class<U> type){
    		return fromStream(reactiveSeq().ofType(type));
    	}
    	
    	/**
    	 * Cast all elements in a stream to a given type, possibly throwing a
    	 * {@link ClassCastException}.
    	 * 
    	 * 
    	 * // ClassCastException Streamable.of(1, "a", 2, "b", 3).cast(Integer.class)
    	 * 
    	 */
    	default <U> Streamable<U> cast(Class<U> type){
    		return fromStream(reactiveSeq().cast(type));
    	}
    	
    	/**
    	 * Lazily converts this Streamable into a Collection. This does not trigger the Stream. E.g.
    	 * Collection is not thread safe on the first iteration.
    	 * <pre>
    	 * {@code 
    	 * Collection<Integer> col = Streamable.of(1,2,3,4,5)
    											.peek(System.out::println)
    											.toLazyCollection();
    		System.out.println("first!");
    		col.forEach(System.out::println);
    	 * }
    	 * 
    	 * //Will print out "first!" before anything else
    	 * </pre>
    	 * @return
    	 */
    	default CollectionX<T> toLazyCollection(){
    		return reactiveSeq().toLazyCollection();
    	}
    	/**
    	 * Lazily converts this Streamable into a Collection. This does not trigger the Stream. E.g.
    	 * 
    	 * <pre>
    	 * {@code 
    	 * Collection<Integer> col = Streamable.of(1,2,3,4,5)
    											.peek(System.out::println)
    											.toConcurrentLazyCollection();
    		System.out.println("first!");
    		col.forEach(System.out::println);
    	 * }
    	 * 
    	 * //Will print out "first!" before anything else
    	 * </pre>
    	 * @return
    	 */
    	 default CollectionX<T> toConcurrentLazyCollection(){
    		 return reactiveSeq().toConcurrentLazyCollection();
    	 }
    	
    	
    	
    	
    	/* 
    	 * Potentially efficient Sequence reversal. Is efficient if
    	 * 
    	 * - Sequence created via a range
    	 * - Sequence created via a List
    	 * - Sequence created via an Array / var args
    	 * 
    	 * Otherwise Sequence collected into a Collection prior to reversal
    	 * 
    	 * <pre>
    	 * {@code
    	 *  assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
    	 *  }
    	 * </pre>
    	 */
    	default Streamable<T> reverse(){
    		return fromStream(reactiveSeq().reverse());
    	}
    	
    	
    	
    	/* (non-Javadoc)
    	 * @see org.jooq.lambda.Seq#shuffle()
    	 */
    	default Streamable<T> shuffle(){
    		return fromStream(reactiveSeq().shuffle());
    	}
    	/**
    	 * Append Stream to this Streamable
    	 * 
    	 * <pre>
    	 * {@code 
    	 * List<String> result = 	Streamable.of(1,2,3)
    	 *                                  .appendStream(Streamable.of(100,200,300))
    										.map(it ->it+"!!")
    										.collect(Collectors.toList());

    			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
    	 * }
    	 * </pre>
    	 * 
    	 * @param stream to append
    	 * @return Streamable with Stream appended
    	 */
    	default Streamable<T> appendStreamable(Streamable<T> stream){
    		return fromStream(reactiveSeq().appendStream(stream.reactiveSeq()));
    	}
    	/**
    	 * Prepend Stream to this Streamable
    	 * 
    	 * <pre>
    	 * {@code 
    	 * List<String> result = Streamable.of(1,2,3)
    	 * 								  .prependStream(of(100,200,300))
    									  .map(it ->it+"!!")
    									  .collect(Collectors.toList());

    			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
    	 * 
    	 * }
    	 * </pre>
    	 * 
    	 * @param stream to Prepend
    	 * @return Streamable with Stream prepended
    	 */
    	default Streamable<T> prependStreamable(Streamable<T> stream){
    		return fromStream(reactiveSeq().prependStream(stream.reactiveSeq()));
    	}
    	/**
    	 * Append values to the end of this Streamable
    	 * <pre>
    	 * {@code 
    	 * List<String> result = Streamable.of(1,2,3)
    	 * 								   .append(100,200,300)
    										.map(it ->it+"!!")
    										.collect(Collectors.toList());

    			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
    	 * }
    	 * </pre>
    	 * @param values to append
    	 * @return Streamable with appended values
    	 */
    	default Streamable<T> append(T... values){
    		return fromStream(reactiveSeq().append(values));
    	}
    	/**
    	 * Prepend given values to the start of the Stream
    	 * <pre>
    	 * {@code 
    	 * List<String> result = 	Streamable.of(1,2,3)
    	 * 									 .prepend(100,200,300)
    										 .map(it ->it+"!!")
    										 .collect(Collectors.toList());

    			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
    	 * }
    	 * @param values to prepend
    	 * @return Streamable with values prepended
    	 */
    	 default Streamable<T> prepend(T... values){
    		 return fromStream(reactiveSeq().prepend(values));
    	 }
    	/**
    	 * Insert data into a stream at given position
    	 * <pre>
    	 * {@code 
    	 * List<String> result = 	Streamable.of(1,2,3)
    	 * 									 .insertAt(1,100,200,300)
    										 .map(it ->it+"!!")
    										 .collect(Collectors.toList());

    			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
    	 * 
    	 * }
    	 * </pre>
    	 * @param pos to insert data at
    	 * @param values to insert
    	 * @return Stream with new data inserted
    	 */
    	default Streamable<T> insertAt(int pos, T... values){
    		return fromStream(reactiveSeq().insertAt(pos,values));
    	}
    	/**
    	 * Delete elements between given indexes in a Stream
    	 * <pre>
    	 * {@code 
    	 * List<String> result = 	Streamable.of(1,2,3,4,5,6)
    	 * 									 .deleteBetween(2,4)
    										 .map(it ->it+"!!")
    										 .collect(Collectors.toList());

    			assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
    	 * }
    	 * </pre>
    	 * @param start index
    	 * @param end index
    	 * @return Stream with elements removed
    	 */
    	default Streamable<T> deleteBetween(int start,int end){
    		return fromStream(reactiveSeq().deleteBetween(start,end));
    	}
    	/**
    	 * Insert a Stream into the middle of this stream at the specified position
    	 * <pre>
    	 * {@code 
    	 * List<String> result = 	Streamable.of(1,2,3)
    	 * 									 .insertStreamAt(1,of(100,200,300))
    										 .map(it ->it+"!!")
    										 .collect(Collectors.toList());

    			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
    	 * }
    	 * </pre>
    	 * @param pos to insert Stream at
    	 * @param stream to insert
    	 * @return newly conjoined Streamable
    	 */
    	default Streamable<T> insertStreamableAt(int pos, Streamable<T> stream){
    		return fromStream(reactiveSeq().insertStreamAt(pos,stream.reactiveSeq()));
    	}
    	
    	/**
    	 * Access asynchronous terminal operations (each returns a Future)
    	 * 
    	 * @param exec Executor to use for Stream execution
    	 * @return Async Future Terminal Operations
    	 */
    	default FutureOperations<T> futureOperations(Executor exec){
    		return reactiveSeq().futureOperations(exec);
    	}
    	
    	/**
    	 * <pre>
    	 * {@code
    	 *  assertTrue(Streamable.of(1,2,3,4,5,6)
    				.endsWith(Arrays.asList(5,6)));
    	 * 
    	 * }
    	 * 
    	 * @param iterable Values to check 
    	 * @return true if Streamable ends with values in the supplied iterable
    	 */
    	default boolean endsWithIterable(Iterable<T> iterable){
    		return reactiveSeq().endsWithIterable(iterable);
    	}
    	/**
    	 * <pre>
    	 * {@code
    	 * assertTrue(Streamable.of(1,2,3,4,5,6)
    				.endsWith(Stream.of(5,6))); 
    	 * }
    	 * </pre>
    	 * 
    	 * @param stream Values to check 
    	 * @return true if Streamable endswith values in the supplied Stream
    	 */
    	default boolean endsWith(Streamable<T> stream){
    		return reactiveSeq().endsWithIterable(stream);
    	}	
    	/**
    	 * Skip all elements until specified time period has passed
    	 * <pre>
    	 * {@code 
    	 * List<Integer> result = Streamable.of(1,2,3,4,5,6)
    										.peek(i->sleep(i*100))
    										.skip(1000,TimeUnit.MILLISECONDS)
    										.toList();
    		
    		
    		//[4,5,6]
    	 * 
    	 * }
    	 * </pre>
    	 * 
    	 * @param time Length of time
    	 * @param unit Time unit
    	 * @return Streamable that skips all elements until time period has elapsed
    	 */
    	default Streamable<T> skip(long time, final TimeUnit unit){
    		return fromStream(reactiveSeq().skip(time,unit));
    	}
    	/**
    	 * Return all elements until specified time period has elapsed
    	 * <pre>
    	 * {@code 
    	 * List<Integer> result = Streamable.of(1,2,3,4,5,6)
    										.peek(i->sleep(i*100))
    										.limit(1000,TimeUnit.MILLISECONDS)
    										.toList();
    		
    		
    		//[1,2,3,4]
    	 * }
    	 * </pre>
    	 * @param time Length of time
    	 * @param unit Time unit
    	 * @return Streamable that returns all elements until time period has elapsed
    	 */
    	default Streamable<T> limit(long time, final TimeUnit unit){
    		return fromStream(reactiveSeq().limit(time,unit));
    	}
    	/**
    	 * assertThat(Streamable.of(1,2,3,4,5)
    							.skipLast(2)
    							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
    	 * 
    	 * @param num
    	 * @return
    	 */
    	default Streamable<T> skipLast(int num){
    		return fromStream(reactiveSeq().skipLast(num));
    	}
    	/**
    	 * Limit results to the last x elements in a Streamable
    	 * <pre>
    	 * {@code 
    	 * 	assertThat(Streamable.of(1,2,3,4,5)
    							.limitLast(2)
    							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
    	 * 
    	 * }
    	 * 
    	 * @param num of elements to return (last elements)
    	 * @return Streamable limited to last num elements
    	 */
    	default Streamable<T> limitLast(int num){
    		return fromStream(reactiveSeq().limitLast(num));
    	}

    	/**
    	 * Turns this Streamable into a HotStream, a connectable Stream, being executed on a thread on the 
    	 * supplied executor, that is producing data
    	 * <pre>
    	 * {@code 
    	 *  HotStream<Integer> ints = Streamable.range(0,Integer.MAX_VALUE)
    											.hotStream(exec)
    											
    		
    		ints.connect().forEach(System.out::println);									
    	 *  //print out all the ints
    	 *  //multiple consumers are possible, so other Streams can connect on different Threads
    	 *  
    	 * }
    	 * </pre>
    	 * @param e Executor to execute this Streamable on
    	 * @return a Connectable HotStream
    	 */
    	default HotStream<T> hotStream(Executor e){
    		return reactiveSeq().hotStream(e);
    	}
    	
    	/**
    	 * <pre>
    	 * {@code 
    	 * 	assertThat(Streamable.of(1,2,3,4)
    					.map(u->{throw new RuntimeException();})
    					.recover(e->"hello")
    					.firstValue(),equalTo("hello"));
    	 * }
    	 * </pre>
    	 * @return first value in this Stream
    	 */
    	default T firstValue(){
    		return reactiveSeq().firstValue();
    	}	
    	
    	/**
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1).single(),equalTo(1));
    	 * }
    	 * </pre>
    	 * 
    	 * @return a single value or an exception if 0/1 values in this Stream
    	 */
    	default T single(){
    		return reactiveSeq().single();
    		
    	}

    	/**
    	 * Return the elementAt index or Optional.empty
    	 * <pre>
    	 * {@code
    	 * 	assertThat(Streamable.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
    	 * }
    	 * </pre>
    	 * @param index to extract element from
    	 * @return elementAt index
    	 */
    	default Optional<T> get(long index){
    		return reactiveSeq().get(index);
    	}
    	/**
    	 * Gets the element at index, and returns a Tuple containing the element (it must be present)
    	 * and a lazy copy of the Sequence for further processing.
    	 * 
    	 * <pre>
    	 * {@code 
    	 * Streamable.of(1,2,3,4,5).get(2).v1
    	 * //3
    	 * }
    	 * </pre>
    	 * 
    	 * @param index to extract element from
    	 * @return Element and Sequence
    	 */
    	default Tuple2<T,Streamable<T>> elementAt(long index){
    		return reactiveSeq().elementAt(index).map2(s->fromStream(s));
    	}
    	
    	/**
    	 * <pre>
    	 * {@code 
    	 * Streamable.of(1,2,3,4,5)
    				 .elapsed()
    				 .forEach(System.out::println);
    	 * }
    	 * </pre>
    	 * 
    	 * @return Sequence that adds the time between elements in millis to each element
    	 */
    	default Streamable<Tuple2<T,Long>> elapsed(){
    		return fromStream(reactiveSeq().elapsed());
    	}
    	/**
    	 * <pre>
    	 * {@code
    	 *    Streamable.of(1,2,3,4,5)
    				   .timestamp()
    				   .forEach(System.out::println)
    	 * 
    	 * }
    	 * 
    	 * </pre>
    	 * 
    	 * @return Sequence that adds a timestamp to each element
    	 */
    	default Streamable<Tuple2<T,Long>> timestamp(){
    		return fromStream(reactiveSeq().timestamp());
    	}
    	

    	
    	
    	
    	
    	
    	/**
    	 * Construct a Reveresed Sequence from the provided elements
    	 * Can be reversed (again) efficiently
    	 * @param elements To Construct sequence from
    	 * @return
    	 */
    	public static <T> Streamable<T> reversedOf(T... elements){
    		return fromStream(ReactiveSeq.reversedOf(elements));
    		
    	}
    	/**
    	 * Construct a Reveresed Sequence from the provided elements
    	 * Can be reversed (again) efficiently
    	 * @param elements To Construct sequence from
    	 * @return
    	 */
    	public static <T> Streamable<T> reversedListOf(List<T> elements){
    		Objects.requireNonNull(elements);
    		return fromStream(ReactiveSeq.reversedListOf(elements));

    	}
    	/**
    	 * Create an efficiently reversable Sequence that produces the integers between start 
    	 * and end
    	 * @param start Number of range to start from
    	 * @param end Number for range to end at
    	 * @return Range Streamable
    	 */
    	public static Streamable<Integer> range(int start, int end){
    		return fromStream(ReactiveSeq.range(start, end));
    		

    	}
    	/**
    	 * Create an efficiently reversable Sequence that produces the integers between start 
    	 * and end
    	 * @param start Number of range to start from
    	 * @param end Number for range to end at
    	 * @return Range Streamable
    	 */
    	public static Streamable<Long> rangeLong(long start, long end){
    		return fromStream(ReactiveSeq.rangeLong(start, end));
    		

    	}
    	
    	/**
    	 * Construct a Sequence from a Stream
    	 * @param stream Stream to construct Sequence from
    	 * @return
    	 */
    	public static Streamable<Integer> fromIntStream(IntStream stream){
    		Objects.requireNonNull(stream);
    		return fromStream(ReactiveSeq.fromIntStream(stream));
    	}
    	/**
    	 * Construct a Sequence from a Stream
    	 * @param stream Stream to construct Sequence from
    	 * @return
    	 */
    	public static Streamable<Long> fromLongStream(LongStream stream){
    		Objects.requireNonNull(stream);
    		return fromStream(ReactiveSeq.fromLongStream(stream));
    	}
    	/**
    	 * Construct a Sequence from a Stream
    	 * @param stream Stream to construct Sequence from
    	 * @return
    	 */
    	public static Streamable<Double> fromDoubleStream(DoubleStream stream){
    		Objects.requireNonNull(stream);
    		return fromStream(ReactiveSeq.fromDoubleStream(stream));
    	}
    	
    	public static <T> Streamable<T> fromList(List<T> list){
    		Objects.requireNonNull(list);
    		return AsStreamable.fromIterable(list);
    	}
    	
    	
        /**
         * @see Stream#iterate(Object, UnaryOperator)
         */
        static <T> Streamable<T> iterate(final T seed, final UnaryOperator<T> f) {
        	Objects.requireNonNull(f);
            return fromStream(ReactiveSeq.iterate(seed, f));
        }

       
        /**
         * @see Stream#generate(Supplier)
         */
        static <T> Streamable<T> generate(Supplier<T> s) {
        	Objects.requireNonNull(s);
            return fromStream(ReactiveSeq.generate(s));
        }
    	/**
    	 * Unzip a zipped Stream 
    	 * 
    	 * <pre>
    	 * {@code 
    	 *  unzip(Streamable.of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c")))
    	 *  
    	 *  // Streamable[1,2,3], Streamable[a,b,c]
    	 * }
    	 * 
    	 * </pre>
    	 * 
    	 */
    	public static <T,U> Tuple2<Streamable<T>,Streamable<U>> unzip(Streamable<Tuple2<T,U>> sequence){
    		return ReactiveSeq.unzip(sequence.reactiveSeq()).map1(s->fromStream(s)).map2(s->fromStream(s));
    	}
    	/**
    	 * Unzip a zipped Stream into 3
    	 * <pre>
    	 * {@code 
    	 *    unzip3(Streamable.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3,"c", 4l)))
    	 * }
    	 * // Streamable[1,2,3], Streamable[a,b,c], Streamable[2l,3l,4l]
    	 * </pre>
    	 */
    	public static <T1,T2,T3> Tuple3<Streamable<T1>,Streamable<T2>,Streamable<T3>> unzip3(Streamable<Tuple3<T1,T2,T3>> sequence){
    		return ReactiveSeq.unzip3(sequence.reactiveSeq()).map1(s->fromStream(s)).map2(s->fromStream(s)).map3(s->fromStream(s));
    	}
    	/**
    	 * Unzip a zipped Stream into 4
    	 * 
    	 * <pre>
    	 * {@code 
    	 * unzip4(Streamable.of(new Tuple4(1, "a", 2l,'z'), new Tuple4(2, "b", 3l,'y'), new Tuple4(3,
    						"c", 4l,'x')));
    		}
    		// Streamable[1,2,3], Streamable[a,b,c], Streamable[2l,3l,4l], Streamable[z,y,x]
    	 * </pre>
    	 */
    	public static <T1,T2,T3,T4> Tuple4<Streamable<T1>,Streamable<T2>,Streamable<T3>,Streamable<T4>> unzip4(Streamable<Tuple4<T1,T2,T3,T4>> sequence){
    		return ReactiveSeq.unzip4(sequence.reactiveSeq()).map1(s->fromStream(s)).map2(s->fromStream(s)).map3(s->fromStream(s)).map4(s->fromStream(s));
    	}
    	
    	
    	/* (non-Javadoc)
    	 * @see org.jooq.lambda.Seq#crossJoin(java.util.stream.Stream)
    	 */
    	default <U> Streamable<Tuple2<T, U>> crossJoin(Streamable<U> other){
    		return fromStream(reactiveSeq().crossJoin(other.reactiveSeq()));
    	}

    	/* (non-Javadoc)
    	 * @see org.jooq.lambda.Seq#innerJoin(java.util.stream.Stream, java.util.function.BiPredicate)
    	 */
    	default <U> Streamable<Tuple2<T, U>> innerJoin(Streamable<U> other,
    			BiPredicate<T, U> predicate){
    		return fromStream(reactiveSeq().innerJoin(other.reactiveSeq(),predicate));
    	}

    	/* (non-Javadoc)
    	 * @see org.jooq.lambda.Seq#leftOuterJoin(java.util.stream.Stream, java.util.function.BiPredicate)
    	 */
    	default <U> Streamable<Tuple2<T, U>> leftOuterJoin(Streamable<U> other,
    			BiPredicate<T, U> predicate)
    		{
    		return fromStream(reactiveSeq().leftOuterJoin(other.reactiveSeq(),predicate));
    			
    	    
    	}

    	/* (non-Javadoc)
    	 * @see org.jooq.lambda.Seq#rightOuterJoin(java.util.stream.Stream, java.util.function.BiPredicate)
    	 */
    	default <U> Streamable<Tuple2<T, U>> rightOuterJoin(Streamable<U> other,
    			BiPredicate<T, U> predicate){
    		return fromStream(reactiveSeq().rightOuterJoin(other.reactiveSeq(),predicate));
    	}
    	/** If this Streamable is empty replace it with a another Stream
    	 * 
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(4,5,6)
    							.onEmptySwitch(()->Streamable.of(1,2,3))
    							.toList(),
    							equalTo(Arrays.asList(4,5,6)));
    	 * }
    	 * </pre>
    	 * @param switchTo Supplier that will generate the alternative Stream
    	 * @return Streamable that will switch to an alternative Stream if empty
    	 */
    	default Streamable<T> onEmptySwitch(Supplier<Streamable<T>> switchTo){
    		return fromStream(reactiveSeq().onEmptySwitch(()->switchTo.get().reactiveSeq()));
    	}
    	
    	default Streamable<T> onEmpty(T value){
    		return fromStream(reactiveSeq().onEmpty(value));
    	}

    	
    	default Streamable<T> onEmptyGet(Supplier<T> supplier){
    		return fromStream(reactiveSeq().onEmptyGet(supplier));
    	}

    	
    	default <X extends Throwable> Streamable<T> onEmptyThrow(Supplier<X> supplier){
    		return fromStream(reactiveSeq().onEmptyThrow(supplier));
    	}

    	
    	default Streamable<T> concat(Streamable<T> other){
    		return fromStream(reactiveSeq().concat(other.reactiveSeq()));
    	}

    	
    	default Streamable<T> concat(T other){
    		return fromStream(reactiveSeq().concat(other));
    	}

    	
    	
    	default Streamable<T> concat(T... other){
    		return fromStream(reactiveSeq().concat(other));
    	}

    	
    	
    	default <U> Streamable<T> distinct(Function<? super T, ? extends U> keyExtractor){
    		return fromStream(reactiveSeq().distinct(keyExtractor));
    	}

    	
    	

    	
    	default Streamable<T> shuffle(Random random){
    		return fromStream(reactiveSeq().shuffle(random));
        	
    	
    	}

    	
    	
    	default Streamable<T> slice(long from, long to){
    		return fromStream(reactiveSeq().slice(from,to));
    	
    	}

    	
    	
    	default <U extends Comparable<? super U>> Streamable<T> sorted(
    			Function<? super T, ? extends U> function){
    		return fromStream(reactiveSeq().sorted(function));
    	
    	}

    	/**
    	 * emit x elements per time period 
    	 * 
    	 * <pre>
    	 * {@code 
    	 *  SimpleTimer timer = new SimpleTimer();
    		assertThat(Streamable.of(1,2,3,4,5,6)
    		                    .xPer(6,100000000,TimeUnit.NANOSECONDS)
    		                    .collect(Collectors.toList()).size(),is(6));

    	 * }
    	 * </pre>
    	 * @param x number of elements to emit
    	 * @param time period
    	 * @param t Time unit
    	 * @return Streamable that emits x elements per time period
    	 */
    	default Streamable<T> xPer(int x, long time, TimeUnit t){
    		return fromStream(reactiveSeq().xPer(x,time,t));
    	
    	}

    	/**
    	 * emit one element per time period
    	 * <pre>
    	 * {@code 
    	 * Streamable.iterate("", last -> "next")
    				.limit(100)
    				.batchBySize(10)
    				.onePer(1, TimeUnit.MICROSECONDS)
    				.peek(batch -> System.out.println("batched : " + batch))
    				.flatMap(Collection::stream)
    				.peek(individual -> System.out.println("Flattened : "
    						+ individual))
    				.forEach(a->{});
    	 * }
    	 * @param time period
    	 * @param t Time unit
    	 * @return Streamable that emits 1 element per time period
    	 */
    	default Streamable<T> onePer(long time, TimeUnit t){
    		return fromStream(reactiveSeq().onePer(time,t));
    	
    	}

    	/**
    	 * Allow one element through per time period, drop all other 
    	 * elements in that time period
    	 * 
    	 * <pre>
    	 * {@code 
    	 * Streamable.of(1,2,3,4,5,6)
    	 *          .debounce(1000,TimeUnit.SECONDS).toList();
    	 *          
    	 * // 1 
    	 * }</pre>
    	 * 
    	 * @param time
    	 * @param t
    	 * @return
    	 */
    	default Streamable<T> debounce(long time, TimeUnit t){
    		return fromStream(reactiveSeq().debounce(time,t));
    	}

    	/**
    	 * Batch elements by size into a List
    	 * 
    	 * <pre>
    	 * {@code
    	 * Streamable.of(1,2,3,4,5,6)
    				.batchBySizeAndTime(3,10,TimeUnit.SECONDS)
    				.toList();
    			
    	 * //[[1,2,3],[4,5,6]] 
    	 * }
    	 * 
    	 * @param size Max size of a batch
    	 * @param time (Max) time period to build a single batch in
    	 * @param t time unit for batch
    	 * @return Streamable batched by size and time
    	 */
    	default Streamable<ListX<T>> groupedBySizeAndTime(int size, long time, TimeUnit t){
    		return fromStream(reactiveSeq().groupedBySizeAndTime(size,time,t));
    	}
    	/**
    	 *  Batch elements by size into a collection created by the supplied factory 
    	 * <pre>
    	 * {@code 
    	 * List<ArrayList<Integer>> list = of(1,2,3,4,5,6)
    					.batchBySizeAndTime(10,1,TimeUnit.MICROSECONDS,()->new ArrayList<>())
    					.toList();
    	 * }
    	 * </pre>
    	 * @param size Max size of a batch
    	 * @param time (Max) time period to build a single batch in
    	 * @param unit time unit for batch
    	 * @param factory Collection factory
    	 * @return Streamable batched by size and time
    	 */
    	default <C extends Collection<? super T>> Streamable<C> groupedBySizeAndTime(int size,long time, TimeUnit unit, Supplier<C> factory){
    		return fromStream(reactiveSeq().groupedBySizeAndTime(size,time,unit,factory));
    	}
    	/**
    	 * Batch elements in a Stream by time period
    	 * 
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
    	 * assertThat(Streamable.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
    	 * }
    	 * </pre>
    	 * 
    	 * @param time - time period to build a single batch in
    	 * @param t  time unit for batch
    	 * @return Streamable batched into lists by time period
    	 */
    	default Streamable<ListX<T>> groupedByTime(long time, TimeUnit t){
    		return fromStream(reactiveSeq().groupedByTime(time,t));
    	}
    	/**
    	 * Batch elements by time into a collection created by the supplied factory 
    	 * 
    	 * <pre>
    	 * {@code 
    	 *   assertThat(Streamable.of(1,1,1,1,1,1)
    	 *                       .batchByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>())
    	 *                       .toList()
    	 *                       .get(0)
    	 *                       .size(),is(1));
    	 * }
    	 * </pre>
    	 * 
    	 * @param time - time period to build a single batch in
    	 * @param unit time unit for batch
    	 * @param factory Collection factory
    	 * @return Streamable batched into collection types by time period
    	 */
    	default <C extends Collection<? super T>> Streamable<C> groupedByTime(long time, TimeUnit unit, Supplier<C> factory){
    		return fromStream(reactiveSeq().groupedByTime(time,unit,factory));
    	}
    	
    	/**
    	 * Batch elements in a Stream by size into a collection created by the supplied factory 
    	 * <pre>
    	 * {@code
    	 * assertThat(Streamable.of(1,1,1,1,1,1)
    	 * 						.batchBySize(3,()->new TreeSet<>())
    	 * 						.toList()
    	 * 						.get(0)
    	 * 						.size(),is(1));
    	 * }
    	 * 
    	 * @param size batch size
    	 * @param supplier Collection factory
    	 * @return Streamable batched into collection types by size
    	 */
    	default <C extends Collection<? super T>>Streamable<C> grouped(int size, Supplier<C> supplier){
    		return fromStream(reactiveSeq().grouped(size,supplier));
    	}

    	/**
    	 * emit elements after a fixed delay
    	 * <pre>
    	 * {@code 
    	 * 	SimpleTimer timer = new SimpleTimer();
    		assertThat(Streamable.of(1,2,3,4,5,6)
    							.fixedDelay(10000,TimeUnit.NANOSECONDS)
    							.collect(Collectors.toList())
    							.size(),is(6));
    		assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
    	 * }
    	 * </pre>
    	 * @param l time length in nanos of the delay
    	 * @param unit for the delay
    	 * @return Streamable that emits each element after a fixed delay
    	 */
    	default Streamable<T> fixedDelay(long l, TimeUnit unit){
    		return fromStream(reactiveSeq().fixedDelay(l,unit));
    	}

    	/**
    	 * Introduce a random jitter / time delay between the emission of elements
    	 * <pre>
    	 * {@code 
    	 * SimpleTimer timer = new SimpleTimer();
    		assertThat(Streamable.of(1,2,3,4,5,6)
    							.jitter(10000)
    							.collect(Collectors.toList())
    							.size(),is(6));
    		assertThat(timer.getElapsedNanoseconds(),greaterThan(20000l));
    	 * }
    	 * </pre>
    	 * @param maxJitterPeriodInNanos - random number less than this is used for each jitter
    	 * @return Sequence with a random jitter between element emission
    	 */
    	default Streamable<T> jitter(long maxJitterPeriodInNanos){
    		return fromStream(reactiveSeq().jitter(maxJitterPeriodInNanos));
    	}
    	
    	
    	/**
    	 * Create a Streamable batched by List, where each batch is populated until the predicate holds
    	 * <pre>
    	 * {@code 
    	 *  assertThat(Streamable.of(1,2,3,4,5,6)
    				.batchUntil(i->i%3==0)
    				.toList()
    				.size(),equalTo(2));
    	 * }
    	 * </pre>
    	 * @param predicate Batch until predicate holds, then open next batch
    	 * @return Streamable batched into lists determined by the predicate supplied
    	 */
    	
    	default Streamable<ListX<T>> groupedUntil(Predicate<? super T> predicate){
    		return fromStream(reactiveSeq().groupedUntil(predicate));
    	}
    	/**
    	 * Create a Streamable batched by List, where each batch is populated while the predicate holds
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5,6)
    				.batchWhile(i->i%3!=0)
    				.toList().size(),equalTo(2));
    	
    	 * }
    	 * </pre>
    	 * @param predicate Batch while predicate holds, then open next batch
    	 * @return Streamable batched into lists determined by the predicate supplied
    	 */
    	default Streamable<ListX<T>> groupedWhile(Predicate<? super T> predicate){
    		return fromStream(reactiveSeq().groupedWhile(predicate));
    	}
    	/**
    	 * Create a Streamable batched by a Collection, where each batch is populated while the predicate holds
    	 * 
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5,6)
    				.batchWhile(i->i%3!=0)
    				.toList()
    				.size(),equalTo(2));
    	 * }
    	 * </pre>
    	 * @param predicate Batch while predicate holds, then open next batch
    	 * @param factory Collection factory
    	 * @return Streamable batched into collections determined by the predicate supplied
    	 */
    	default <C extends Collection<? super T>>  Streamable<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory){
    		return fromStream(reactiveSeq().groupedWhile(predicate,factory));
    	}
    	/**
    	 * Create a Streamable batched by a Collection, where each batch is populated until the predicate holds
    	 * 
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5,6)
    				.batchUntil(i->i%3!=0)
    				.toList()
    				.size(),equalTo(2));
    	 * }
    	 * </pre>
    	 * 
    	 * 
    	 * @param predicate Batch until predicate holds, then open next batch
    	 * @param factory Collection factory
    	 * @return Streamable batched into collections determined by the predicate supplied
    	 */
    	default <C extends Collection<? super T>>  Streamable<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory){
    		return fromStream(reactiveSeq().groupedUntil(predicate,factory));
    		
    	}

    	/**
    	 * Recover from an exception with an alternative value
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4)
    						   .map(i->i+2)
    						   .map(u->{throw new RuntimeException();})
    						   .recover(e->"hello")
    						   .firstValue(),equalTo("hello"));
    	 * }
    	 * </pre>
    	 * @param fn Function that accepts a Throwable and returns an alternative value
    	 * @return Streamable that can recover from an Exception
    	 */
    	default Streamable<T> recover(final Function<Throwable, ? extends T> fn){
    		return fromStream(reactiveSeq().recover(fn));
    	}
    	/**
    	 * Recover from a particular exception type
    	 * 
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4)
    					.map(i->i+2)
    					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
    					.recover(IOException.class,e->"hello")
    					.firstValue(),equalTo("hello"));
    	 * 
    	 * }
    	 * </pre>
    	 * 
    	 * @param exceptionClass Type to recover from
    	 * @param fn That accepts an error and returns an alternative value
    	 * @return Streamable that can recover from a particular exception
    	 */
    	default <EX extends Throwable> Streamable<T> recover(Class<EX> exceptionClass, final Function<EX, T> fn){
    		return fromStream(reactiveSeq().recover(exceptionClass,fn));
    	
    	}
    	
    	
    	/**
    	 * Retry a transformation if it fails. Default settings are to retry up to 7 times, with an doubling
    	 * backoff period starting @ 2 seconds delay before retry.
    	 * 
    	 * <pre>
    	 * {@code 
    	 * given(serviceMock.apply(anyInt())).willThrow(
    				new RuntimeException(new SocketException("First")),
    				new RuntimeException(new IOException("Second"))).willReturn(
    				"42");

    	
    		String result = Streamable.of( 1,  2, 3)
    				.retry(serviceMock)
    				.firstValue();

    		assertThat(result, is("42"));
    	 * }
    	 * </pre>
    	 * @param fn Function to retry if fails
    	 * 
    	 */
    	default <R> Streamable<R> retry(Function<T,R> fn){
    		return fromStream(reactiveSeq().retry(fn));
    	}
    	
    

    	/**
    	 * True if a streamable contains element t
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5).contains(3),equalTo(true));
    	 * }
    	 * </pre>
    	 * @param t element to check for
    	 */
    	default boolean  contains(T t){
    		return stream().anyMatch(c -> t.equals((c)));
    	}

    
    	/**
    	 * True if a streamable contains element t
    	 * use paralleled stream underneath
    	 * <pre>
    	 * {@code 
    	 * assertThat(Streamable.of(1,2,3,4,5).parallelContains(3),equalTo(true));
    	 * }
    	 * </pre>
    	 * @param t element to check for
    	 */
    	default boolean  parallelContains(T t){
    		return stream().parallel().anyMatch(c -> t.equals((c)));
    	}

		@Override
		default ReactiveSeq<T> stream() {
			return ToStream.super.reactiveSeq();
		}

		@Override
		default Iterator<T> iterator() {
			return stream().iterator();
		}
    	
}
