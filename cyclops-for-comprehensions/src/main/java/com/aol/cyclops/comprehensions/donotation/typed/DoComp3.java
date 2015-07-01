
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

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.SequenceM;
	public class DoComp3<T1,T2,T3> extends DoComp{
		public DoComp3(PStack<Entry> assigned) {
			super(assigned);
			
		}
		
		public  DoComp4<T1,T2,T3,Character> add(CharSequence seq){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),seq)));
			
		}
		public <T4> DoComp4<T1,T2,T3,T4> add(T4... values){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),Stream.of(values))));
			
		}
		public  DoComp4<T1,T2,T3,Integer> times(int o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterable1).add(iterable2).add(iterable3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(Iterable<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterator1).add(iterator2).add(iterator3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(Iterator<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> addStream(Supplier<Stream<T4>> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		
		/**
		 * Add a BaseStream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> addBaseStream(Supplier<BaseStream<T4,?>> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(optional1).add(optional2).add(optional3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(Optional<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(completablefuture1).add(completablefuture2).add(completablefuture3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(CompletableFuture<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(anym1).add(anym2).add(anym3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(AnyM<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(traversablem1).add(traversablem2).add(traversablem3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(SequenceM<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(callable1).add(callable2).add(callable3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(Callable<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(supplier1).add(supplier2).add(supplier3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(Supplier<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),(Supplier)()->o)));
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(collection1).add(collection2).add(collection3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> add(Collection<T4> o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3)
							.filter((String i1)->(String i2)->(String i3) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3) -> i1+i2+i3);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4 extends String>  DoComp4<T1,T2,T3,T4> add(File o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3)
							.filter((String i1)->(String i2)->(String i3) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3) -> i1+i2+i3);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4 extends String>  DoComp4<T1,T2,T3,T4> add(URL o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3)
							.filter((String i1)->(String i2)->(String i3) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3) -> i1+i2+i3);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4 extends String>  DoComp4<T1,T2,T3,T4> add(BufferedReader o){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterable1).with((Integer i1)->(Integer i2) -> iterable2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterable3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withIterable(Function<T1,Function<T2,Function<T3,Iterable<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterator1).with((Integer i1)->(Integer i2) -> iterator2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterator3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withIterator(Function<T1,Function<T2,Function<T3,Iterator<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withStream(Function<T1,Function<T2,Function<T3,Stream<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		/**
		 * Add a BaseStream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withBaseStream(Function<T1,Function<T2,Function<T3,BaseStream<T4,?>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> optional1).with((Integer i1)->(Integer i2) -> optional2).with((Integer i1)->(Integer i2)->(Integer i3) -> optional3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withOptional(Function<T1,Function<T2,Function<T3,Optional<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> completablefuture1).with((Integer i1)->(Integer i2) -> completablefuture2).with((Integer i1)->(Integer i2)->(Integer i3) -> completablefuture3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withCompletableFuture(Function<T1,Function<T2,Function<T3,CompletableFuture<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> anym1).with((Integer i1)->(Integer i2) -> anym2).with((Integer i1)->(Integer i2)->(Integer i3) -> anym3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withAnyM(Function<T1,Function<T2,Function<T3,AnyM<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> traversablem1).with((Integer i1)->(Integer i2) -> traversablem2).with((Integer i1)->(Integer i2)->(Integer i3) -> traversablem3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withTraversableM(Function<T1,Function<T2,Function<T3,SequenceM<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> callable1).with((Integer i1)->(Integer i2) -> callable2).with((Integer i1)->(Integer i2)->(Integer i3) -> callable3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withCallable(Function<T1,Function<T2,Function<T3,Callable<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> supplier1).with((Integer i1)->(Integer i2) -> supplier2).with((Integer i1)->(Integer i2)->(Integer i3) -> supplier3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withSupplier(Function<T1,Function<T2,Function<T3,Supplier<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> collection1).with((Integer i1)->(Integer i2) -> collection2).with((Integer i1)->(Integer i2)->(Integer i3) -> collection3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4> DoComp4<T1,T2,T3,T4> withCollection(Function<T1,Function<T2,Function<T3,Collection<T4>>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3)
							.filter((String i1)->(String i2)->(String i3) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3) -> i1+i2+i3);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4 extends String> DoComp4<T1,T2,T3,T4> withFile(Function<T1,Function<T2,Function<T3,File>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3)
							.filter((String i1)->(String i2)->(String i3) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3) -> i1+i2+i3);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4 extends String> DoComp4<T1,T2,T3,T4> withURL(Function<T1,Function<T2,Function<T3,URL>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3)
							.filter((String i1)->(String i2)->(String i3) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3) -> i1+i2+i3);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T4 extends String> DoComp4<T1,T2,T3,T4> withBufferedReader(Function<T1,Function<T2,Function<T3,BufferedReader>>> f){
			return new DoComp4(addToAssigned(f));
			
		}
		


		
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do.add(list1).add(list2).add(list3)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> AnyM<R> yield(Function<T1,Function<T2,Function<T3,R>>> f){
			return AsAnyM.notTypeSafeAnyM(this.yieldInternal(f));
		}
		
		
		/**
		 * Filter data
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(list1).add(list2).add(list3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  DoComp3<T1,T2,T3> filter(Function<T1,Function<T2,Function<T3,Boolean>>> f){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		
	}

