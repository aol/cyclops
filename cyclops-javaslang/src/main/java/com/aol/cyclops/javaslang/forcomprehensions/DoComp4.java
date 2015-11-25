
package com.aol.cyclops.javaslang.forcomprehensions;


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

import javaslang.algebra.Monad;

import org.pcollections.PStack;

import com.aol.cyclops.comprehensions.donotation.typed.DoComp;
import com.aol.cyclops.comprehensions.donotation.typed.Entry;
import com.aol.cyclops.comprehensions.donotation.typed.Guard;
import com.aol.cyclops.lambda.monads.MonadWrapper;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
	public class DoComp4<T1,T2,T3,T4> extends DoComp{
		public DoComp4(PStack<Entry> assigned, Class orgType) {
			super(assigned,orgType);
			
		}
		//${start}
		public <T5> DoComp5<T1,T2,T3,T4,T5> monad(Monad<T5> monad){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),monad)),getOrgType());
		}
		public <T5> DoComp5<T1,T2,T3,T4,Character> add(CharSequence seq){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),seq)),getOrgType());
			
		}
		public <T5> DoComp5<T1,T2,T3,T4,T5> addValues(T5...values){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),Stream.of(values))),getOrgType());
			
		}

		public  DoComp5<T1,T2,T3,T4,Integer> times(int  o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterable1).add(iterable2).add(iterable3).add(iterable4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(Iterable<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterator1).add(iterator2).add(iterator3).add(iterator4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(Iterator<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3).add(stream4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> addBaseStream(Supplier<BaseStream<T5,?>> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3).add(stream4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> addStream(Supplier<Stream<T5>> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(optional1).add(optional2).add(optional3).add(optional4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(Optional<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(completablefuture1).add(completablefuture2).add(completablefuture3).add(completablefuture4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(CompletableFuture<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(anym1).add(anym2).add(anym3).add(anym4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(AnyM<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(traversablem1).add(traversablem2).add(traversablem3).add(traversablem4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(SequenceM<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(callable1).add(callable2).add(callable3).add(callable4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(Callable<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(supplier1).add(supplier2).add(supplier3).add(supplier4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(Supplier<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),(Supplier)()->o)),getOrgType());
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(collection1).add(collection2).add(collection3).add(collection4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> add(Collection<T5> o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4)
							.filter((String i1)->(String i2)->(String i3)->(String i4) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4) -> i1+i2+i3+i4);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5 extends String>  DoComp5<T1,T2,T3,T4,T5> add(File o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4)
							.filter((String i1)->(String i2)->(String i3)->(String i4) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4) -> i1+i2+i3+i4);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5 extends String>  DoComp5<T1,T2,T3,T4,T5> add(URL o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4)
							.filter((String i1)->(String i2)->(String i3)->(String i4) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4) -> i1+i2+i3+i4);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5 extends String>  DoComp5<T1,T2,T3,T4,T5> add(BufferedReader o){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterable1).with((Integer i1)->(Integer i2) -> iterable2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterable3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> iterable4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withIterable(Function<T1,Function<T2,Function<T3,Function<T4,Iterable<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterator1).with((Integer i1)->(Integer i2) -> iterator2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterator3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> iterator4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withIterator(Function<T1,Function<T2,Function<T3,Function<T4,Iterator<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> stream4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withStream(Function<T1,Function<T2,Function<T3,Function<T4,Stream<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		/**
		 * Add a BaseStream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> stream4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withBaseStream(Function<T1,Function<T2,Function<T3,Function<T4,BaseStream<T5,?>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> optional1).with((Integer i1)->(Integer i2) -> optional2).with((Integer i1)->(Integer i2)->(Integer i3) -> optional3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> optional4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withOptional(Function<T1,Function<T2,Function<T3,Function<T4,Optional<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		/**
		 * Add a javaslang Monad as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.withOptional((Integer i1) -> optional1)
		 *                 .withOptional((Integer i1)->(Integer i2) -> optional2)
		 *                 .withOptional((Integer i1)->(Integer i2)->(Integer i3) -> optional3)
		 *                 .withMonad((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> try)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withMonad(Function<T1,Function<T2,Function<T3,Function<T4,Monad<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> completablefuture1).with((Integer i1)->(Integer i2) -> completablefuture2).with((Integer i1)->(Integer i2)->(Integer i3) -> completablefuture3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> completablefuture4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withCompletableFuture(Function<T1,Function<T2,Function<T3,Function<T4,CompletableFuture<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> anym1).with((Integer i1)->(Integer i2) -> anym2).with((Integer i1)->(Integer i2)->(Integer i3) -> anym3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> anym4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withAnyM(Function<T1,Function<T2,Function<T3,Function<T4,AnyM<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> traversablem1).with((Integer i1)->(Integer i2) -> traversablem2).with((Integer i1)->(Integer i2)->(Integer i3) -> traversablem3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> traversablem4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withTraversableM(Function<T1,Function<T2,Function<T3,Function<T4,SequenceM<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> callable1).with((Integer i1)->(Integer i2) -> callable2).with((Integer i1)->(Integer i2)->(Integer i3) -> callable3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> callable4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withCallable(Function<T1,Function<T2,Function<T3,Function<T4,Callable<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> supplier1).with((Integer i1)->(Integer i2) -> supplier2).with((Integer i1)->(Integer i2)->(Integer i3) -> supplier3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> supplier4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withSupplier(Function<T1,Function<T2,Function<T3,Function<T4,Supplier<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> collection1).with((Integer i1)->(Integer i2) -> collection2).with((Integer i1)->(Integer i2)->(Integer i3) -> collection3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> collection4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5> DoComp5<T1,T2,T3,T4,T5> withCollection(Function<T1,Function<T2,Function<T3,Function<T4,Collection<T5>>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4)
							.filter((String i1)->(String i2)->(String i3)->(String i4) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4) -> i1+i2+i3+i4);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5 extends String> DoComp5<T1,T2,T3,T4,T5> withFile(Function<T1,Function<T2,Function<T3,Function<T4,File>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4)
							.filter((String i1)->(String i2)->(String i3)->(String i4) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4) -> i1+i2+i3+i4);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5 extends String> DoComp5<T1,T2,T3,T4,T5> withURL(Function<T1,Function<T2,Function<T3,Function<T4,URL>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4)
							.filter((String i1)->(String i2)->(String i3)->(String i4) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4) -> i1+i2+i3+i4);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T5 extends String> DoComp5<T1,T2,T3,T4,T5> withBufferedReader(Function<T1,Function<T2,Function<T3,Function<T4,BufferedReader>>>> f){
			return new DoComp5(addToAssigned(f),getOrgType());
			
		}
		


		
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> AnyM<R> yield(Function<T1,Function<T2,Function<T3,Function<T4,R>>>> f){
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
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> i1+i2+i3+i4);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  DoComp4<T1,T2,T3,T4> filter(Function<T1,Function<T2,Function<T3,Function<T4,Boolean>>>> f){
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
		}
		
	}

