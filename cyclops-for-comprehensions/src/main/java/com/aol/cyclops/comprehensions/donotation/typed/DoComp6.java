
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
import com.aol.cyclops.sequence.AnyM;
import com.aol.cyclops.sequence.SequenceM;
	public class DoComp6<T1,T2,T3,T4,T5,T6> extends DoComp{
		public DoComp6(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,Character> add(CharSequence seq){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),seq)));
			
		}
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> addValues(T7... values){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),Stream.of(values))));
			
		}

		public  DoComp7<T1,T2,T3,T4,T5,T6,Integer> add(int times){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),times)));
			
		}
		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterable1).add(iterable2).add(iterable3).add(iterable4).add(iterable5).add(iterable6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(Iterable<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterator1).add(iterator2).add(iterator3).add(iterator4).add(iterator5).add(iterator6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(Iterator<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3).add(stream4).add(stream5).add(stream6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> addStream(Supplier<Stream<T7>> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3).add(stream4).add(stream5).add(stream6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> addBaseStream(Supplier<BaseStream<T7,?>> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(optional1).add(optional2).add(optional3).add(optional4).add(optional5).add(optional6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(Optional<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(completablefuture1).add(completablefuture2).add(completablefuture3).add(completablefuture4).add(completablefuture5).add(completablefuture6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(CompletableFuture<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(anym1).add(anym2).add(anym3).add(anym4).add(anym5).add(anym6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(AnyM<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(traversablem1).add(traversablem2).add(traversablem3).add(traversablem4).add(traversablem5).add(traversablem6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(SequenceM<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(callable1).add(callable2).add(callable3).add(callable4).add(callable5).add(callable6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(Callable<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(supplier1).add(supplier2).add(supplier3).add(supplier4).add(supplier5).add(supplier6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(Supplier<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),(Supplier)()->o)));
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(collection1).add(collection2).add(collection3).add(collection4).add(collection5).add(collection6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> add(Collection<T7> o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7 extends String>  DoComp7<T1,T2,T3,T4,T5,T6,T7> add(File o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7 extends String>  DoComp7<T1,T2,T3,T4,T5,T6,T7> add(URL o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7 extends String>  DoComp7<T1,T2,T3,T4,T5,T6,T7> add(BufferedReader o){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterable1).with((Integer i1)->(Integer i2) -> iterable2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterable3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> iterable4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> iterable5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> iterable6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withIterable(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Iterable<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterator1).with((Integer i1)->(Integer i2) -> iterator2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterator3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> iterator4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> iterator5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> iterator6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withIterator(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Iterator<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> stream4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> stream5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> stream6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withStream(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Stream<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> stream4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> stream5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> stream6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withBaseStream(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,BaseStream<T7,?>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> optional1).with((Integer i1)->(Integer i2) -> optional2).with((Integer i1)->(Integer i2)->(Integer i3) -> optional3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> optional4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> optional5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> optional6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withOptional(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Optional<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> completablefuture1).with((Integer i1)->(Integer i2) -> completablefuture2).with((Integer i1)->(Integer i2)->(Integer i3) -> completablefuture3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> completablefuture4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> completablefuture5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> completablefuture6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withCompletableFuture(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,CompletableFuture<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> anym1).with((Integer i1)->(Integer i2) -> anym2).with((Integer i1)->(Integer i2)->(Integer i3) -> anym3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> anym4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> anym5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> anym6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withAnyM(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,AnyM<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> traversablem1).with((Integer i1)->(Integer i2) -> traversablem2).with((Integer i1)->(Integer i2)->(Integer i3) -> traversablem3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> traversablem4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> traversablem5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> traversablem6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withTraversableM(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,SequenceM<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> callable1).with((Integer i1)->(Integer i2) -> callable2).with((Integer i1)->(Integer i2)->(Integer i3) -> callable3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> callable4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> callable5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> callable6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withCallable(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Callable<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> supplier1).with((Integer i1)->(Integer i2) -> supplier2).with((Integer i1)->(Integer i2)->(Integer i3) -> supplier3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> supplier4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> supplier5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> supplier6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withSupplier(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Supplier<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> collection1).with((Integer i1)->(Integer i2) -> collection2).with((Integer i1)->(Integer i2)->(Integer i3) -> collection3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> collection4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> collection5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> collection6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> withCollection(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Collection<T7>>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> list6)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7 extends String> DoComp7<T1,T2,T3,T4,T5,T6,T7> withFile(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,File>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> list6)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7 extends String> DoComp7<T1,T2,T3,T4,T5,T6,T7> withURL(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,URL>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> list6)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T7 extends String> DoComp7<T1,T2,T3,T4,T5,T6,T7> withBufferedReader(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,BufferedReader>>>>>> f){
			return new DoComp7(addToAssigned(f));
			
		}
		


		
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> AnyM<R> yield(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,R>>>>>> f){
			return AsAnyM.notTypeSafeAnyM(this.yieldInternal(f));
		}
		
		
		/**
		 * Filter data
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> i1+i2+i3+i4+i5+i6);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  DoComp6<T1,T2,T3,T4,T5,T6> filter(Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Boolean>>>>>> f){
			return new DoComp6(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		
	}

