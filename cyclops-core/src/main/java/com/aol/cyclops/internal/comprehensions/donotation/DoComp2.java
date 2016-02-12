package com.aol.cyclops.internal.comprehensions.donotation;

import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Assignment;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import org.pcollections.PStack;

import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Reader;
public class DoComp2<T1, T2> extends DoComp {
	
	public DoComp2(PStack<Entry> assigned, Class orgType) {
		super(assigned,orgType);

	}

	public DoComp3<T1, T2, Character> add(CharSequence seq) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), seq)),getOrgType());

	}
	public DoComp3<T1, T2, T3> add(Reader<?,T3> seq) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), seq)),getOrgType());

	}

	public <T3> DoComp3<T1, T2, T3> addValues(T3... values) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), Stream.of(values))),getOrgType());

	}

	public DoComp3<T1, T2, Integer> times(int o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a Iterable as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(iterable1).add(iterable2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(Iterable<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a Iterator as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(iterator1).add(iterator2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(Iterator<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a Stream as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(stream1).addStream(stream2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> addStream(Supplier<Stream<T3>> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a Stream as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(stream1).add(stream2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> addBaseStream(Supplier<BaseStream<T3, ?>> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a Optional as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(optional1).add(optional2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(Optional<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a CompletableFuture as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(completablefuture1).add(completablefuture2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(CompletableFuture<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a AnyM as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(anym1).add(anym2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(AnyM<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a TraversableM as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(traversablem1).add(traversablem2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(ReactiveSeq<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a Callable as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(callable1).add(callable2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(Callable<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a Supplier as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(supplier1).add(supplier2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(Supplier<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), (Supplier) () -> o)),getOrgType());

	}

	/**
	 * Add a Collection as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(collection1).add(collection2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> add(Collection<T3> o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a File as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>
	 * {@code   Do.add(list1).add(list2)
	 * 							.filter((String i1)->(String i2) -> i1>5)
	 * 							 .yield((String i1)->(String i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 *
	 *
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3 extends String> DoComp3<T1, T2, T3> add(File o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a URL as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>
	 * {@code   Do.add(list1).add(list2)
	 * 							.filter((String i1)->(String i2) -> i1>5)
	 * 							 .yield((String i1)->(String i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 *
	 *
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3 extends String> DoComp3<T1, T2, T3> add(URL o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	/**
	 * Add a BufferedReader as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>
	 * {@code   Do.add(list1).add(list2)
	 * 							.filter((String i1)->(String i2) -> i1>5)
	 * 							 .yield((String i1)->(String i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 *
	 *
	 * @param o
	 *            Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3 extends String> DoComp3<T1, T2, T3> add(BufferedReader o) {
		return new DoComp3(getAssigned().plus(getAssigned().size(), new Entry("$$monad" + getAssigned().size(), o)),getOrgType());

	}

	public <T3> DoComp3<T1, T2, T3> withReader(Function<? super T1, Function<? super T2, Reader<?, ? extends T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}
	/**
	 * Add a Iterable as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> iterable1).with((Integer i1)->(Integer i2) -> iterable2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withIterable(Function<? super T1, Function<? super T2, Iterable<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a Iterator as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> iterator1).with((Integer i1)->(Integer i2) -> iterator2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withIterator(Function<? super T1, Function<? super T2, Iterator<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a Stream as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withStream(Function<? super T1, Function<? super T2, Stream<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a BaseStream as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withBaseStream(Function<? super T1, Function<? super T2, BaseStream<T3, ?>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a Optional as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> optional1).with((Integer i1)->(Integer i2) -> optional2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withOptional(Function<? super T1, Function<? super T2, Optional<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a CompletableFuture as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> completablefuture1).with((Integer i1)->(Integer i2) -> completablefuture2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withCompletableFuture(Function<? super T1, Function<? super T2, CompletableFuture<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a AnyM as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> anym1).with((Integer i1)->(Integer i2) -> anym2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withAnyM(Function<? super T1, Function<? super T2, AnyM<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a TraversableM as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> traversablem1).with((Integer i1)->(Integer i2) -> traversablem2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withTraversableM(Function<? super T1, Function<? super T2, ReactiveSeq<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a Callable as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> callable1).with((Integer i1)->(Integer i2) -> callable2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withCallable(Function<? super T1, Function<? super T2, Callable<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a Supplier as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> supplier1).with((Integer i1)->(Integer i2) -> supplier2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withSupplier(Function<? super T1, Function<? super T2, Supplier<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a Collection as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.with((Integer i1) -> collection1).with((Integer i1)->(Integer i2) -> collection2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3> DoComp3<T1, T2, T3> withCollection(Function<? super T1, Function<? super T2, Collection<T3>>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a File as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>
	 * {@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2)
	 * 							.filter((String i1)->(String i2) -> i1>5)
	 * 							 .yield((String i1)->(String i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 *
	 *
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3 extends String> DoComp3<T1, T2, T3> withFile(Function<? super T1, Function<? super T2, File>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a URL as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>
	 * {@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2)
	 * 							.filter((String i1)->(String i2) -> i1>5)
	 * 							 .yield((String i1)->(String i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 *
	 *
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3 extends String> DoComp3<T1, T2, T3> withURL(Function<? super T1, Function<? super T2, URL>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Add a BufferedReader as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>
	 * {@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2)
	 * 							.filter((String i1)->(String i2) -> i1>5)
	 * 							 .yield((String i1)->(String i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 *
	 *
	 * @param f
	 *            Gives access to current pointers and defines next level in
	 *            comprehension
	 * @return Next stage in for comprehension builder
	 */
	public <T3 extends String> DoComp3<T1, T2, T3> withBufferedReader(Function<? super T1, Function<? super T2, BufferedReader>> f) {
		return new DoComp3(addToAssigned(f),getOrgType());

	}

	/**
	 * Execute and Yield a result from this for comprehension using the supplied
	 * function
	 * 
	 * e.g. sum every element across nested structures
	 * 
	 * <pre>
	 * {@code   Do.add(list1).add(list2)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            To be applied to every element in the for comprehension
	 * @return For comprehension result
	 */
	public <R> AnyM<R> yield(Function<? super T1, Function<? super T2, ? extends R>> f) {
		if(getOrgType()!=null)
			return new MonadWrapper(this.yieldInternal(f),this.getOrgType()).anyM();
		else
			return AnyM.ofMonad(this.yieldInternal(f));
	}

	/**
	 * Filter data
	 * 
	 * 
	 * 
	 * <pre>
	 * {@code   Do.add(list1).add(list2)
	 * 		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
	 * 					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
	 * 								
	 * 			}
	 * </pre>
	 * 
	 * 
	 * @param f
	 *            To be applied to every element in the for comprehension
	 * @return Current stage with guard / filter applied
	 */
	public DoComp2<T1, T2> filter(Function<? super T1, Function<? super T2, Boolean>> f) {
		return new DoComp2(getAssigned().plus(getAssigned().size(), new Entry("$$internalGUARD" + getAssigned().size(), new Guard(f))),getOrgType());
	}

}
