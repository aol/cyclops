
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
import java.util.stream.Stream;

import org.pcollections.PStack;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.TraversableM;
	public class DoComp2<T1,T2> extends DoComp{
		public DoComp2(PStack<Entry> assigned) {
			super(assigned);
			
		}
		


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterable1).add(iterable2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(Iterable<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterator1).add(iterator2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(Iterator<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> addStream(Supplier<Stream<T3>> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(optional1).add(optional2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(Optional<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(completablefuture1).add(completablefuture2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(CompletableFuture<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(anym1).add(anym2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(AnyM<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(traversablem1).add(traversablem2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(TraversableM<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(callable1).add(callable2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(Callable<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(supplier1).add(supplier2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(Supplier<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(collection1).add(collection2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> add(Collection<T3> o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2)
							.filter((String i1)->(String i2) -> i1>5)
							 .yield((String i1)->(String i2) -> i1+i2);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3 extends String>  DoComp3<T1,T2,T3> add(File o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2)
							.filter((String i1)->(String i2) -> i1>5)
							 .yield((String i1)->(String i2) -> i1+i2);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3 extends String>  DoComp3<T1,T2,T3> add(URL o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2)
							.filter((String i1)->(String i2) -> i1>5)
							 .yield((String i1)->(String i2) -> i1+i2);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3 extends String>  DoComp3<T1,T2,T3> add(BufferedReader o){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterable1).with((Integer i1)->(Integer i2) -> iterable2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withIterable(Function<T1,Function<T2,Iterable<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterator1).with((Integer i1)->(Integer i2) -> iterator2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withIterator(Function<T1,Function<T2,Iterator<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withStream(Function<T1,Function<T2,Stream<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> optional1).with((Integer i1)->(Integer i2) -> optional2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withOptional(Function<T1,Function<T2,Optional<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> completablefuture1).with((Integer i1)->(Integer i2) -> completablefuture2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withCompletableFuture(Function<T1,Function<T2,CompletableFuture<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> anym1).with((Integer i1)->(Integer i2) -> anym2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withAnyM(Function<T1,Function<T2,AnyM<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> traversablem1).with((Integer i1)->(Integer i2) -> traversablem2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withTraversableM(Function<T1,Function<T2,TraversableM<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> callable1).with((Integer i1)->(Integer i2) -> callable2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withCallable(Function<T1,Function<T2,Callable<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> supplier1).with((Integer i1)->(Integer i2) -> supplier2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withSupplier(Function<T1,Function<T2,Supplier<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> collection1).with((Integer i1)->(Integer i2) -> collection2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3> DoComp3<T1,T2,T3> withCollection(Function<T1,Function<T2,Collection<T3>>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2)
							.filter((String i1)->(String i2) -> i1>5)
							 .yield((String i1)->(String i2) -> i1+i2);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3 extends String> DoComp3<T1,T2,T3> withFile(Function<T1,Function<T2,File>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2)
							.filter((String i1)->(String i2) -> i1>5)
							 .yield((String i1)->(String i2) -> i1+i2);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3 extends String> DoComp3<T1,T2,T3> withURL(Function<T1,Function<T2,URL>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2)
							.filter((String i1)->(String i2) -> i1>5)
							 .yield((String i1)->(String i2) -> i1+i2);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T3 extends String> DoComp3<T1,T2,T3> withBufferedReader(Function<T1,Function<T2,BufferedReader>> f){
			return new DoComp3(addToAssigned(f));
			
		}
		


		
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do.add(list1).add(list2)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> AnyM<R> yield(Function<T1,Function<T2,R>> f){
			return AsAnyM.notTypeSafeAnyM(this.yieldInternal(f));
		}
		
		
		/**
		 * Filter data
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(list1).add(list2)
		 				   .filter((Integer i1)->(Integer i2) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2) -> i1+i2);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  DoComp2<T1,T2> filter(Function<T1,Function<T2,Boolean>> f){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		
	}

