package com.aol.cyclops.comprehensions





import java.util.function.Function
import java.io.BufferedReader;
import java.io.File;
import java.util.Iterator;
import java.util.stream.Stream;

import org.pcollections.PStack;






class DoGenerator {

	public static void main(String[] args){
		String dir = "/Users/johnmcclean/repos/cyclops/cyclops-for-comprehensions/src/main/java/com/aol/cyclops/comprehensions/donotation/typed"
		int size = 8
		size.times {
			new File(dir,"DoComp"+it+".java").write( new DoGenerator().build(it))
		}
	//	println new DoGenerator().build(3)
	}
	def String addMethods(int size,List<String> withType){
		StringBuilder b = new StringBuilder("\n\n")
		withType.each { b.append(addMethod(size,it)).append("\n\n") }
		return b
	}
	def String addMethodsString(int size,List<String> withType){
		StringBuilder b = new StringBuilder("\n\n")
		withType.each { b.append(addMethodString(size,it)).append("\n\n") }
		return b
	}
	def String withMethodsString(int size,List<String> withType){
		StringBuilder b = new StringBuilder("\n\n")
		withType.each { b.append(withMethodString(size,it)).append("\n\n") }
		return b
	}
	def String withMethods(int size,List<String> withType){
		StringBuilder b = new StringBuilder("\n\n")
		withType.each { b.append(withMethod(size,it)).append("\n\n") }
		return b
	}
	def String addMethod(int size, String type){
		"""
		/**
		 * Add a ${type} as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do${addTimes(type.toLowerCase(),size)}
		 				   .filter(${ofType("Integer",size)} -> i1>5)
					  	   .yield(${ofType("Integer",size)} -> ${sum(size)});
								
			}</pre>
		 * 
		 * 
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T${size+1}> ${classDef(size+1)} add(${type}<T${size+1}> o){
			return new DoComp${size+1}(assigned.plus(assigned.size(),new Entry("\$\$monad"+assigned.size(),o)));
			
		}
		"""
	}
	def String addMethodString(int size,String type){
		"""
		/**
		 * Add a ${type} as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do${addTimes("list",size)}
							.filter(${ofType("String",size)} -> i1>5)
							 .yield(${ofType("String",size)} -> ${sum(size)});
								
			}</pre>
		 *
		 *
		 * @param f Defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T${size+1} extends String>  ${classDef(size+1)} add(${type} o){
			return new DoComp${size+1}(assigned.plus(assigned.size(),new Entry("\$\$monad"+assigned.size(),o)));
			
		}
		"""
	}
	def String withMethod(int size,String type){
		"""
		/**
		 * Add a ${type} as next nested level in the comprehension
		 * 
		 * 
		 * 
		 * <pre>{@code   Do${withTimes(type.toLowerCase(),size)}
		 				   .filter(${ofType("Integer",size)} -> i1>5)
					  	   .yield(${ofType("Integer",size)} -> ${sum(size)});
								
			}</pre>
		 * 
		 * 
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T${size+1}> ${classDef(size+1)} with${type}(Function${curriedTypes(size)},${type}<T${size+1}${curriedClose(size)} f){
			return new DoComp${size+1}(addToAssigned(f));
			
		}
		"""
	}
	def String withMethodString(int size,String type){
		"""
		/**
		 * Add a ${type} as next nested level in the comprehension
		 *
		 *
		 *
		 * <pre>{@code   Do${withTimes("list",size)}
							.filter(${ofType("String",size)} -> i1>5)
							 .yield(${ofType("String",size)} -> ${sum(size)});
								
			}</pre>
		 *
		 *
		 * @param f Gives access to current pointers and defines next level in comprehension
		 * @return Next stage in for comprehension builder
		 */
		public <T${size+1} extends String> ${classDef(size+1)} with${type}(Function${curriedTypes(size)},${type}${curriedClose(size-1)} f){
			return new DoComp${size+1}(addToAssigned(f));
			
		}
		"""
	}
	
	def String addTimes(String type,int size){
		if(size==0)
			return "";
		StringBuilder b = new StringBuilder()
		size.times{
			b.append(".add(${type}${it+1})")
		}
		return  b.toString()
	}

	def String withTimes(String type,int size){
		if(size==0)
			return "";
		StringBuilder b = new StringBuilder()
		size.times{
			b.append(".with(${ofType("Integer",it+1)} -> ${type}${it+1})")
		}
		return  b.toString()
	}

	def String yieldMethod(int size){
		"""
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do${addTimes("list",size)}
					  	   .yield(${ofType("Integer",size)} -> ${sum(size)});
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> R yield(Function${curriedTypes(size)},?${curriedClose(size-1)} f){
			return this.yieldInternal(f);
		}
		"""

	}
	
	def String filterMethod(int size){
		"""
		/**
		 * Filter data
		 * 
		 * 
		 * 
		 * <pre>{@code   Do${addTimes("list",size)}
		 				   .filter(${ofType("Integer",size)} -> i1>5)
					  	   .yield(${ofType("Integer",size)} -> ${sum(size)});
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage with guard / filter applied
		 */
		public  ${classDef(size)} filter(Function${curriedTypes(size)},Boolean${curriedClose(size-1)} f){
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
	def String curriedTypes(int size){
		if(size==0)
		return "";
		StringBuilder b = new StringBuilder("<")
		String sep  = ""
		size.times{
			b.append(sep)
			b.append("T${it+1}")
			sep = ",Function<"
		}
		
		
		return b
	}

	def String curriedClose(int size){
		if(size==0)
		return "";
		StringBuilder b = new StringBuilder("")
		(size+1).times{
			b.append(">")
		}
		return b
	}
	def String ofType(String type,int size){
		if(size==0)
		return "";
		StringBuilder b = new StringBuilder()
		String sep  = ""
		size.times{
			b.append(sep)
			b.append("(${type} i${it+1})")
			sep = "->"
		}
		
		return b
	}
	
	def String sum(int size){
		if(size==0)
		return "";
		StringBuilder b = new StringBuilder()
		String sep  = ""
		size.times{
			b.append(sep)
			b.append("i${it+1}")
			sep = "+"
		}
		
		return b
	}
	
	def String build (int size) {
	List<String> withType = ["Iterable","Iterator","Stream","Optional","CompletableFuture","AnyM","TraversableM","Callable","Supplier","Collection"]
	List<String> noType = ["File","URL","BufferedReader"]
	 """
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
	public class ${classDef(size)} extends DoComp{
		public DoComp$size(PStack<Entry> assigned) {
			super(assigned);
			
		}
		${addMethods(size,withType)}
		${addMethodsString(size,noType)}
		${withMethods(size,withType)}
		${withMethodsString(size,noType)}
		${yieldMethod(size)}
		${filterMethod(size)}
	}

"""
	}
	
	
}

