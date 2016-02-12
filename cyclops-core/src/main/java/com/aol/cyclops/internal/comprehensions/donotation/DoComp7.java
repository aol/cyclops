
package com.aol.cyclops.internal.comprehensions.donotation;

import com.aol.cyclops.control.Reader;
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
import com.aol.cyclops.control.SequenceM;
	public class DoComp7<T1,T2,T3,T4,T5,T6,T7> extends DoComp{
		public DoComp7(PStack<Entry> assigned,Class orgType) {
			super(assigned,orgType);
			
		}
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,Character> add(CharSequence seq){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),seq)),getOrgType());
			
		}
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(Reader<?,? extends T8> reader){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),seq)),getOrgType());
			
		}
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> addValues(T8... values){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),Stream.of(values))),getOrgType());
			
		}
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,Integer> add(int times){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),times)),getOrgType());
			
		}

		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterable1).add(iterable2).add(iterable3).add(iterable4).add(iterable5).add(iterable6).add(iterable7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(Iterable<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(iterator1).add(iterator2).add(iterator3).add(iterator4).add(iterator5).add(iterator6).add(iterator7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(Iterator<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3).add(stream4).add(stream5).add(stream6).add(stream7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> addStream(Supplier<Stream<T8>> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}

		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream1).add(stream2).add(stream3).add(stream4).add(stream5).add(stream6).add(stream7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> addBaseStream(Supplier<BaseStream<T8,?>> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(optional1).add(optional2).add(optional3).add(optional4).add(optional5).add(optional6).add(optional7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(Optional<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(completablefuture1).add(completablefuture2).add(completablefuture3).add(completablefuture4).add(completablefuture5).add(completablefuture6).add(completablefuture7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(CompletableFuture<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(anym1).add(anym2).add(anym3).add(anym4).add(anym5).add(anym6).add(anym7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(AnyM<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(traversablem1).add(traversablem2).add(traversablem3).add(traversablem4).add(traversablem5).add(traversablem6).add(traversablem7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(SequenceM<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(callable1).add(callable2).add(callable3).add(callable4).add(callable5).add(callable6).add(callable7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(Callable<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(supplier1).add(supplier2).add(supplier3).add(supplier4).add(supplier5).add(supplier6).add(supplier7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(Supplier<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),(Supplier)()->o)),getOrgType());
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(collection1).add(collection2).add(collection3).add(collection4).add(collection5).add(collection6).add(collection7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(Collection<T8> o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6).add(list7)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8 extends String>  DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(File o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6).add(list7)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8 extends String>  DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(URL o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6).add(list7)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8 extends String>  DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> add(BufferedReader o){
			return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		

		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withReader(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Reader<?,? extends T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		
		


		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterable1).with((Integer i1)->(Integer i2) -> iterable2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterable3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> iterable4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> iterable5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> iterable6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> iterable7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withIterable(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Iterable<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> iterator1).with((Integer i1)->(Integer i2) -> iterator2).with((Integer i1)->(Integer i2)->(Integer i3) -> iterator3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> iterator4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> iterator5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> iterator6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> iterator7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withIterator(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Iterator<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> stream4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> stream5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> stream6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> stream7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withStream(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Stream<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> stream1).with((Integer i1)->(Integer i2) -> stream2).with((Integer i1)->(Integer i2)->(Integer i3) -> stream3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> stream4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> stream5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> stream6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> stream7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withBaseStream(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,BaseStream<T8,?>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> optional1).with((Integer i1)->(Integer i2) -> optional2).with((Integer i1)->(Integer i2)->(Integer i3) -> optional3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> optional4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> optional5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> optional6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> optional7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withOptional(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Optional<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> completablefuture1).with((Integer i1)->(Integer i2) -> completablefuture2).with((Integer i1)->(Integer i2)->(Integer i3) -> completablefuture3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> completablefuture4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> completablefuture5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> completablefuture6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> completablefuture7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withCompletableFuture(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,CompletableFuture<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> anym1).with((Integer i1)->(Integer i2) -> anym2).with((Integer i1)->(Integer i2)->(Integer i3) -> anym3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> anym4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> anym5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> anym6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> anym7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withAnyM(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,AnyM<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> traversablem1).with((Integer i1)->(Integer i2) -> traversablem2).with((Integer i1)->(Integer i2)->(Integer i3) -> traversablem3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> traversablem4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> traversablem5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> traversablem6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> traversablem7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withTraversableM(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,SequenceM<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> callable1).with((Integer i1)->(Integer i2) -> callable2).with((Integer i1)->(Integer i2)->(Integer i3) -> callable3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> callable4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> callable5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> callable6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> callable7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withCallable(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Callable<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> supplier1).with((Integer i1)->(Integer i2) -> supplier2).with((Integer i1)->(Integer i2)->(Integer i3) -> supplier3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> supplier4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> supplier5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> supplier6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> supplier7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withSupplier(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Supplier<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.with((Integer i1) -> collection1).with((Integer i1)->(Integer i2) -> collection2).with((Integer i1)->(Integer i2)->(Integer i3) -> collection3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> collection4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> collection5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> collection6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> collection7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withCollection(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Collection<T8>>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> list6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> list7)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8 extends String> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withFile(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,File>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> list6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> list7)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8 extends String> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withURL(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,URL>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do.with((Integer i1) -> list1).with((Integer i1)->(Integer i2) -> list2).with((Integer i1)->(Integer i2)->(Integer i3) -> list3).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4) -> list4).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5) -> list5).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6) -> list6).with((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> list7)
							.filter((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1>5)
							 .yield((String i1)->(String i2)->(String i3)->(String i4)->(String i5)->(String i6)->(String i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T8 extends String> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> withBufferedReader(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,BufferedReader>>>>>>> f){
			return new DoComp8(addToAssigned(f),getOrgType());
			
		}
		


		
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6).add(list7)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> AnyM<R> yield(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,? extends R>>>>>>> f){
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
		 * <pre>{@code   Do.add(list1).add(list2).add(list3).add(list4).add(list5).add(list6).add(list7)
		 				   .filter((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1>5)
					  	   .yield((Integer i1)->(Integer i2)->(Integer i3)->(Integer i4)->(Integer i5)->(Integer i6)->(Integer i7) -> i1+i2+i3+i4+i5+i6+i7);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  DoComp7<T1,T2,T3,T4,T5,T6,T7> filter(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Boolean>>>>>>> f){
			return new DoComp7(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
		}
		
	}

