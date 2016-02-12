
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

import javaslang.Value;

import org.pcollections.PStack;

import com.aol.cyclops.comprehensions.donotation.typed.DoComp;
import com.aol.cyclops.comprehensions.donotation.typed.Entry;
import com.aol.cyclops.comprehensions.donotation.typed.Guard;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.lambda.monads.MonadWrapper;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.SequenceM;
	public class DoComp5<T1,T2,T3,T4,T5> extends DoComp{
		public DoComp5(PStack<Entry> assigned, Class orgType) {
			super(assigned,orgType);
			
		}
		//${start}
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> monad(Value<T6> monad){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),monad)),getOrgType());
		}
		public <T6> DoComp6<T1,T2,T3,T4,T5,Character> add(CharSequence seq){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),seq)),getOrgType());
			
		}
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> addValues(T6... values){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),Stream.of(values))),getOrgType());
			
		}
		public  DoComp6<T1,T2,T3,T4,T5,Integer> times(int times){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),times)),getOrgType());
			
		}
		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterable1).add(iterable2).add(iterable3).add(iterable4).add(iterable5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(Iterable<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterator1).add(iterator2).add(iterator3).add(iterator4).add(iterator5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(Iterator<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3).add(stream4).add(stream5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> addStream(Supplier<Stream<T6>> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3).add(stream4).add(stream5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> addBaseStream(Supplier<BaseStream<T6,?>> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(optional1).add(optional2).add(optional3).add(optional4).add(optional5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(Optional<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(completablefuture1).add(completablefuture2).add(completablefuture3).add(completablefuture4).add(completablefuture5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(CompletableFuture<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(anym1).add(anym2).add(anym3).add(anym4).add(anym5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(AnyM<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(traversablem1).add(traversablem2).add(traversablem3).add(traversablem4).add(traversablem5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(SequenceM<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(callable1).add(callable2).add(callable3).add(callable4).add(callable5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(Callable<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(supplier1).add(supplier2).add(supplier3).add(supplier4).add(supplier5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(Supplier<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),(Supplier)()->o)),getOrgType());
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(collection1).add(collection2).add(collection3).add(collection4).add(collection5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> add(Collection<T6> o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6 extends String>  DoComp6<T1,T2,T3,T4,T5,T6> add(File o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6 extends String>  DoComp6<T1,T2,T3,T4,T5,T6> add(URL o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6 extends String>  DoComp6<T1,T2,T3,T4,T5,T6> add(BufferedReader o){
			return new DoComp6(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterable1).with((Integer i1)->(Integer i2) -> iterable2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterable3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> iterable4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> iterable5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withIterable(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Iterable<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterator1).with((Integer i1)->(Integer i2) -> iterator2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterator3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> iterator4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> iterator5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withIterator(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Iterator<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> stream4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> stream5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withStream(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Stream<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		/**
		 * Add a BaseStream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> stream4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> stream5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withBaseStream(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,BaseStream<T6,?>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> optional1).with((Integer i1)->(Integer i2) -> optional2).with((Integer i1)->(Integer i2)->(Integer i3) -> optional3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> optional4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> optional5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withOptional(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Optional<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		/**
		 * Add a javaslang Monad as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.withOptional((Integer i1) -> optional1)
		 *                 .withOptional((Integer i1)->(Integer i2) -> optional2)
		 *                 .withOptional((Integer i1)->(Integer i2)->(Integer i3) -> optional3)
		 *                 .withOptional((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> optional4)
		 *                 .withMonad((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> future)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withMonad(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Value<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> completablefuture1).with((Integer i1)->(Integer i2) -> completablefuture2).with((Integer i1)->(Integer i2)->(Integer i3) -> completablefuture3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> completablefuture4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> completablefuture5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withCompletableFuture(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,CompletableFuture<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> anym1).with((Integer i1)->(Integer i2) -> anym2).with((Integer i1)->(Integer i2)->(Integer i3) -> anym3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> anym4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> anym5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withAnyM(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,AnyM<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> traversablem1).with((Integer i1)->(Integer i2) -> traversablem2).with((Integer i1)->(Integer i2)->(Integer i3) -> traversablem3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> traversablem4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> traversablem5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withTraversableM(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,SequenceM<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> callable1).with((Integer i1)->(Integer i2) -> callable2).with((Integer i1)->(Integer i2)->(Integer i3) -> callable3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> callable4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> callable5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withCallable(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Callable<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> supplier1).with((Integer i1)->(Integer i2) -> supplier2).with((Integer i1)->(Integer i2)->(Integer i3) -> supplier3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> supplier4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> supplier5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withSupplier(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Supplier<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> collection1).with((Integer i1)->(Integer i2) -> collection2).with((Integer i1)->(Integer i2)->(Integer i3) -> collection3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> collection4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> collection5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> withCollection(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Collection<T6>>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6 extends String> DoComp6<T1,T2,T3,T4,T5,T6> withFile(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,File>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6 extends String> DoComp6<T1,T2,T3,T4,T5,T6> withURL(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,URL>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T6 extends String> DoComp6<T1,T2,T3,T4,T5,T6> withBufferedReader(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,BufferedReader>>>>> f){
			return new DoComp6(addToAssigned(f),getOrgType());
			
		}
		


		
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> AnyM<R> yield(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,R>>>>> f){
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
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> i1+i2+i3+i4+i5);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  DoComp5<T1,T2,T3,T4,T5> filter(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Boolean>>>>> f){
			return new DoComp5(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
		}
		
	}

