
package com.aol.cyclops.internal.comprehensions.donotation;


import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import org.pcollections.PStack;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Assignment;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.SequenceM;

public class DoComp0 extends DoComp{
		public DoComp0(PStack<Entry> assigned) {
			super(assigned,null);
			
		}
		
		public <T1> DoComp1<T1> addValues(T1... values){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),Stream.of(values))),getOrgType());

		}

		public  DoComp1<Character> add(CharSequence seq){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),seq)),getOrgType());

		}
		public  DoComp1<Integer> times(int times){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),times)),getOrgType());
			
		}
		/**
		 * Add a Iterable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Iterable<T1> o){
			Class orgType =null;
			if(o instanceof List)
				orgType = List.class;
			else if(o instanceof Set)
				orgType = Set.class;
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),orgType);
			
		}
		


		/**
		 * Add a Iterator as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Iterator<T1> o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.add(stream)
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> addStream(Stream<T1> o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		
		


		/**
		 * Add a Optional as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Optional<T1> o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a CompletableFuture as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(CompletableFuture<T1> o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a AnyM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(AnyM<T1> o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a TraversableM as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(SequenceM<T1> o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * Will behave as a CompletableFuture i.e. executed asynchronously 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Callable<T1> o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * Will behave as a CompletableFuture i.e. executed asynchronously 
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Supplier<T1> o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),(Supplier)()->o)),getOrgType());
			
		}
		


		/**
		 * Add a Collection as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Collection<T1> o){
			Class orgType =null;
			if(o instanceof List)
				orgType = List.class;
			else if(o instanceof Set)
				orgType = Set.class;
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),orgType);
			
		}
		


		


		/**
		 * Add a File as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do
							.filter( -> i1>5)
							 .yield( -> );
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1 extends String>  DoComp1<T1> add(File o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a URL as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do
							.filter( -> i1>5)
							 .yield( -> );
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1 extends String>  DoComp1<T1> add(URL o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}
		


		/**
		 * Add a BufferedReader as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do
							.filter( -> i1>5)
							 .yield( -> );
								
			}</pre>
		 *
		 *
		 * @param o Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1 extends String>  DoComp1<T1> add(BufferedReader o){
			return new DoComp1(getAssigned().plus(getAssigned().size(),new Entry("$$monad"+getAssigned().size(),o)),getOrgType());
			
		}


		


		


		
	}