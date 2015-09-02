
package com.aol.cyclops.comprehensions.donotation.typed;


import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import org.pcollections.PStack;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
	public class DoComp1<T1> extends DoComp{
		public DoComp1(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public  DoComp2<T1,Character> add(CharSequence seq){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),seq)));
			
		}
		public <T2> DoComp2<T1,T2> addValues(T2... values){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),Stream.of(values))));
			
		}
		public DoComp2<T1,Integer> times(int times){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),times)));
			
		}
		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterable1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(Iterable<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterator1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(Iterator<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1)
		 * 					.addStream(()->stream2)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> addStream(Supplier<Stream<T2>> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		/**
		 * Add a BaseStream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>>{@code   Do.add(stream1)
		 * 					.addStream(()->intStream)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> addBaseStream(Supplier<BaseStream<T2,?>> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(optional1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(Optional<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(completablefuture1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(CompletableFuture<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(anym1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(AnyM<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(traversablem1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(SequenceM<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(callable1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(Callable<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(supplier1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(Supplier<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),(Supplier)()->o)));
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(collection1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> add(Collection<T2> o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1)
							.filter((String i1) -> i1>5)
							 .yield((String i1) -> i1);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2 extends String>  DoComp2<T1,T2> add(File o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1)
							.filter((String i1) -> i1>5)
							 .yield((String i1) -> i1);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2 extends String>  DoComp2<T1,T2> add(URL o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1)
							.filter((String i1) -> i1>5)
							 .yield((String i1) -> i1);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2 extends String>  DoComp2<T1,T2> add(BufferedReader o){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterable1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withIterable(Function<T1,Iterable<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterator1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withIterator(Function<T1,Iterator<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream)
		 * 					.withStream((Integer i1) -> stream1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withStream(Function<T1,Stream<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		
		/**
		 * Add a BaseStream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream)
		 * 				    .withBaseStream((Integer i1) -> stream1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withBaseStream(Function<T1,BaseStream<T2,?>> f){
			return new DoComp2(addToAssigned(f));
			
		}


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> optional1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withOptional(Function<T1,Optional<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> completablefuture1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withCompletableFuture(Function<T1,CompletableFuture<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> anym1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withAnyM(Function<T1,AnyM<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> traversablem1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withTraversableM(Function<T1,SequenceM<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> callable1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withCallable(Function<T1,Callable<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> supplier1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withSupplier(Function<T1,Supplier<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> collection1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2> DoComp2<T1,T2> withCollection(Function<T1,Collection<T2>> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1)
							.filter((String i1) -> i1>5)
							 .yield((String i1) -> i1);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2 extends String> DoComp2<T1,T2> withFile(Function<T1,File> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1)
							.filter((String i1) -> i1>5)
							 .yield((String i1) -> i1);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2 extends String> DoComp2<T1,T2> withURL(Function<T1,URL> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1)
							.filter((String i1) -> i1>5)
							 .yield((String i1) -> i1);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T2 extends String> DoComp2<T1,T2> withBufferedReader(Function<T1,BufferedReader> f){
			return new DoComp2(addToAssigned(f));
			
		}
		


		
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do.add(list1)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> AnyM<R> yield(Function<T1,R> f){
			return AsAnyM.notTypeSafeAnyM(this.yieldInternal(f));
		}
		
		
		/**
		 * Filter data
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(list1)
		 				   .filter((Integer i1) -> i1>5)
					  	   .yield((Integer i1) -> i1);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  DoComp1<T1> filter(Function<T1,Boolean> f){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		
	}

