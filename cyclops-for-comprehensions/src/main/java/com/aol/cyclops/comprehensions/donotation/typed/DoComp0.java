
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

import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.TraversableM;
	public class DoComp0 extends DoComp{
		public DoComp0(PStack<Entry> assigned) {
			super(assigned);
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Iterable<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Iterator<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Stream as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Stream<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Optional<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(CompletableFuture<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(AnyM<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o.unwrap())));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(TraversableM<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Callable as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Callable<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		/**
		 * Add a Supplier as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do
		 				   .filter( -> i1>5)
					  	   .yield( -> );
								
			}</pre>
		 * 
		 * 
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Supplier<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1> DoComp1<T1> add(Collection<T1> o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1 extends String>  DoComp1<T1> add(File o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1 extends String>  DoComp1<T1> add(URL o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
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
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T1 extends String>  DoComp1<T1> add(BufferedReader o){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		


		


		


		
	}