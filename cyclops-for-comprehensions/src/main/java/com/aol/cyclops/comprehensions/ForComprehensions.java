package com.aol.cyclops.comprehensions;

import java.util.function.Function;

/**
 * Static helper methods for for comprehensions
 * This class aims to make using for comprehenions as succint as possible
 * 
 * @author johnmcclean
 *
 */
@Deprecated //use com.aol.cyclops.comprehensions.donotation.typed.Do instead
public class ForComprehensions {

	
	/**
	 * Create  for comprehension over a single Monad or collection
	 * <pre>
	 * {@code
	 * IntStream res = (IntStream)ForComprehensions.foreach1 (  c-> 
										c.mapAs$1(  IntStream.range(1,3)) 
										 .yield( (Vars1<Integer> v)-> v.$1() + 1));
			List<Integer> expected = Arrays.asList(2,3);
			
			
			
			assertThat(expected, equalTo( res.boxed().collect(Collectors.toList())));
	 * }
	 * </pre>
	 * 
	 * 
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach1(Function<LessTypingForComprehension1.Step1<?,R>,R> fn){
		return (R)new FreeFormForComprehension(LessTypingForComprehension1.Step1.class,LessTypingForComprehension1.getVars()).foreach((Function)fn);
		
	}
	
	/**
	 * Create  for comprehension over two Monads or collections
	 * <pre>
	 * {@code 
	 * 	val one = new  MyCase("hello",20);
		val two  = new MyCase2("France");
		
		Stream<String> result = ForComprehensions.foreach2(c -> c.flatMapAs$1(one)
										 .mapAs$2(v->two)
										 .yield(v-> v.$1().toString() 
												 	+ v.$2().toString()));
		
	
		assertThat(result.collect(Collectors.toList()),equalTo(Arrays.asList("helloFrance","20France")));
		
	
		\@Value static class MyCase implements Decomposable{ String name; int value;}
		\@Value static class MyCase2 implements Decomposable{ String country;}

	 * 
	 * }
	 * </pre>
	 * @param fn  for comprehension
	 * @return Result
	 */
	public static <R> R foreach2(Function<LessTypingForComprehension2.Step1<?,R>,R> fn){
		return (R)new FreeFormForComprehension(LessTypingForComprehension2.Step1.class,LessTypingForComprehension2.getVars()).foreach((Function)fn);
	}
	/**
	 * Create  for comprehension over three Monads or collections
	 * 
	 * <pre>
	 * {@code
	 * val f = CompletableFuture.completedFuture("hello world");
		val f2 = CompletableFuture.completedFuture("2");
		val f3 = CompletableFuture.completedFuture("3");
		CompletableFuture<String> result = ForComprehensions.foreach3(c -> c.flatMapAs$1(f)
										.flatMapAs$2((Vars3<String,String,String> v)->f2)
										.mapAs$3(v->f3)
										.yield(v-> v.$1()+v.$2()+v.$3())
									);
		
		assertThat(result.join(),equalTo("hello world23"));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach3(Function<LessTypingForComprehension3.Step1<?,R>,R> fn){
		return (R)new FreeFormForComprehension(LessTypingForComprehension3.Step1.class,LessTypingForComprehension3.getVars()).foreach((Function)fn);
	}
	
	/**
	 * Create  for comprehension over four Monads or collections
	 * <pre>
	 * {@code 
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		ForComprehensions.foreach4(c -> c.flatMapAs$1(one)
														.flatMapAs$2((Vars4<Integer,Integer,Integer,Integer> v)->empty)
														.flatMapAs$3(v->Optional.empty())
														.mapAs$4(v->Optional.empty())
														.run(v->{ result= f2.apply(v.$1(), v.$2());}));
		
		assertThat(result,equalTo(null));
	 * }
	 * </pre>
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach4(Function<LessTypingForComprehension4.Step1<?,R>,R> fn){
		return (R)new FreeFormForComprehension(LessTypingForComprehension4.Step1.class,LessTypingForComprehension4.getVars()).foreach((Function)fn);
	}
	
	/**
	 * Create a custom for comprehension virtually unlimited in nesting depths
	 * 
	 * <pre>
	 * {@code 
	 * List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = foreachX(c -> c.$("hello",list)
										.filter(()->c.<Integer>$("hello")<10)
										.yield(()-> c.<Integer>$("hello")+2));
		
		assertThat(Arrays.asList(3,4,5),equalTo(stream.collect(Collectors.toList())));
	 * }
	 * </pre>
	 * 
	 * @param fn For comprehension
	 * @return Result
	 */
	public static <R> R foreachX(Function<ComprehensionData<?,R,? extends Initialisable>,R> fn){
		return (R)new FreeFormForComprehension().foreach((Function)fn);
	}
	
	/**
	 * Create a for comprehension using a custom interface 
	 * 
	 * <pre>
	 * {@code
	 * 	List<Integer> list= Arrays.asList(1,2,3);
	 	Stream<Integer> stream = foreachX(Custom.class,  
									c-> c.myVar(list)
										.yield(()->c.myVar()+3)
									);
		
		assertThat(Arrays.asList(4,5,6),equalTo(stream.collect(Collectors.toList())));
		
		static interface Custom extends CustomForComprehension<Stream<Integer>,Custom>{
			Integer myVar();
			Custom myVar(List<Integer> value);
		 }
	 * }</pre>
	 * 
	 * 
	 * @param c Interface that defines for comprehension - should extend CustomForComprehension
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <X,R> R foreachX(Class<X> c,Function<X,R> fn){
		return (R)new FreeFormForComprehension(c,(Class)null).foreach(fn);
	}
	/**
	 * Step builder for Creating a for comprehension using a custom interface
	 * 
	 * <pre>
	 * {@code
	 *  MyComprehension<Custom2,Custom2> comp2 = ForComprehensions.custom(Custom2.class);
	 *   comp.foreach(c -> c.i(Arrays.asList(20,30))
								.j(Arrays.asList(1,2,3))
								.yield(() -> c.i() +c.j()));
	 * }
	 * 
	 * </pre>
	 * 
	 * @param c Interface that defines for comprehension - should extend CustomForComprehension
	 * @return next stage in the step builder
	 */
	public static <X,V> MyComprehension<X,V> custom(Class<X> c){
		
	 return   new MyComprehension<>(c,null);
	}
	
	
	
	
}
