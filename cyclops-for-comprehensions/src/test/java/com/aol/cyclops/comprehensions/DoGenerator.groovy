package com.aol.cyclops.comprehensions





import java.util.function.Function
import java.io.BufferedReader;
import java.io.File;
import java.util.Iterator;
import java.util.stream.Stream;

import org.pcollections.PStack;

import com.aol.cyclops.comprehensions.donotation.typed.Do.DoComp;
import com.aol.cyclops.comprehensions.donotation.typed.Do.DoComp1;
import com.aol.cyclops.comprehensions.donotation.typed.Do.DoComp2;
import com.aol.cyclops.comprehensions.donotation.typed.Do.Entry;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.lambda.monads.AnyM;



class DoGenerator {

	public static void main(String[] args){
		println new DoGenerator().build(3)
	}
	def String addMethods(int size,List<String> withType){
		StringBuilder b = new StringBuilder("\n\n")
		withType.each { b.append(addMethod(size,it)).append("\n\n") }
		return b
	}
	def String withMethods(int size,List<String> withType){
		StringBuilder b = new StringBuilder("\n\n")
		withType.each { b.append(withMethod(size,it)).append("\n\n") }
		return b
	}
	def String addMethod(int size, String type){
		"""
		public <T${size+1}> ${classDef(size+1)} add(${type}<T${size+1}> o){
			return new DoComp${size}(assigned.plus(assigned.size(),new Entry("\$\$monad"+assigned.size(),o)));
			
		}
		"""
	}
	def String withMethod(int size, String type){
		"""
		public <T${size+1}> ${classDef(size+1)} with${type}(Function${unclosedTypes(size)},${type}<T${size+1}> o){
			return new DoComp${size}(addToAssigned(f));
			
		}
		"""
	}

	def String yieldMethod(int size){
		"""
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. add 2 to every element in a list
		 * 
		 * <pre>{@code   Do.with(list)
					  .yield((Integer a)-> a +2 );
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> R yield(Function${unclosedTypes(size)},?> f){
			return this.yieldInternal(f);
		}
		"""

	}
	
	def String filterMethod(int size){
		"""
		public  DoComp${size} filter(Function${unclosedTypes(size)},Boolean> f){
			return new DoComp${size}(assigned.plus(assigned.size(),new Entry("\$\$internalGUARD"+assigned.size(),new Guard(f))));
		}
		"""
	}
	
	def String classDef(int size){
		return "DoComp$size${types(size)}"
	}	
	
	def String types(int size){
		if(size==0)
		return "";
		return unclosedTypes(size) + ">"
	}
	def String unclosedTypes(int size){
		if(size==0)
		return "";
		StringBuilder b = new StringBuilder("<")
		String sep  = ""
		size.times{
			b.append(sep)
			b.append("T${it+1}")
			sep = ","
		}
		
		return b
	}
	
	def String build (int size) {
	List<String> withType = ["Iterable","Iterator","Stream","Optional","CompletableFuture","AnyM","TraversibleM","Decomposable","Callable","Supplier"]
	List<String> noType = ["File","URL","BufferedReader"]
	 """
	public class ${classDef(size)} extends DoComp{
		public DoComp$size(PStack<Entry> assigned) {
			super(assigned);
			
		}
		${addMethods(size,withType)}
		${withMethods(size,withType)}
		${yieldMethod(size)}
		${filterMethod(size)}
	}

"""
	}
	
	
}

