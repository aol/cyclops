
package com.aol.cyclops.internal.comprehensions.donotation;

import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Assignment;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
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

import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Reader;
public class DoComp3<T1,T2,T3> extends DoComp{
		public DoComp3(PStack<Entry> assigned, Class orgType) {
			super(assigned,orgType);
			
		}
		
		public  DoComp4<T1,T2,T3,Character> add(CharSequence seq){
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),seq)),getOrgType());
			
		}
		public  <T4> DoComp4<T1,T2,T3,T4> add(Reader<?,T4> seq){
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),seq)),getOrgType());
			
		}
		
		public <T4> DoComp4<T1,T2,T3,T4> addValues(T4... values){
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),Stream.of(values))),getOrgType());
			
		}
		public  DoComp4<T1,T2,T3,Integer> times(int o){
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> add(ReactiveSeq<T4> o){
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),(Supplier)()->o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
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
			return new DoComp4(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(), o)),getOrgType());
			
		}
		


		
		public <T4> DoComp4<T1,T2,T3,T4> withReader(Function<? super T1,Function<? super T2,Function<? super T3,Reader<?,? extends T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withIterable(Function<? super T1,Function<? super T2,Function<? super T3,Iterable<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withIterator(Function<? super T1,Function<? super T2,Function<? super T3,Iterator<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withStream(Function<? super T1,Function<? super T2,Function<? super T3,Stream<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withBaseStream(Function<? super T1,Function<? super T2,Function<? super T3,BaseStream<T4,?>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withOptional(Function<? super T1,Function<? super T2,Function<? super T3,Optional<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withCompletableFuture(Function<? super T1,Function<? super T2,Function<? super T3,CompletableFuture<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withAnyM(Function<? super T1,Function<? super T2,Function<? super T3,AnyM<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withTraversableM(Function<? super T1,Function<? super T2,Function<? super T3,ReactiveSeq<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withCallable(Function<? super T1,Function<? super T2,Function<? super T3,Callable<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withSupplier(Function<? super T1,Function<? super T2,Function<? super T3,Supplier<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4> DoComp4<T1,T2,T3,T4> withCollection(Function<? super T1,Function<? super T2,Function<? super T3,Collection<T4>>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4 extends String> DoComp4<T1,T2,T3,T4> withFile(Function<? super T1,Function<? super T2,Function<? super T3,File>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4 extends String> DoComp4<T1,T2,T3,T4> withURL(Function<? super T1,Function<? super T2,Function<? super T3,URL>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <T4 extends String> DoComp4<T1,T2,T3,T4> withBufferedReader(Function<? super T1,Function<? super T2,Function<? super T3,BufferedReader>>> f){
			return new DoComp4(addToAssigned(f),getOrgType());
			
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
		public <R> AnyM<R> yield(Function<? super T1,Function<? super T2,Function<? super T3,? extends R>>> f){
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
		 * <pre>{@code   Do.add(list1).add(list2).add(list3)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3) -> i1+i2+i3);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  DoComp3<T1,T2,T3> filter(Function<? super T1,Function<? super T2,Function<? super T3,Boolean>>> f){
			return new DoComp3(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
		}
		
	}

