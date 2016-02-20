package com.aol.cyclops.control;


import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jooq.lambda.fi.util.function.CheckedBiConsumer;
import org.jooq.lambda.fi.util.function.CheckedBiFunction;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import com.aol.cyclops.control.Matchable.CheckValues;
import com.aol.cyclops.data.MutableInt;
import com.aol.cyclops.internal.invokedynamic.CheckedTriFunction;
import com.aol.cyclops.util.ExceptionSoftener;
import com.aol.cyclops.util.function.Cacheable;
import com.aol.cyclops.util.function.Curry;
import com.aol.cyclops.util.function.CurryVariance;
import com.aol.cyclops.util.function.Memoize;
import com.aol.cyclops.util.function.PartialApplicator;
import com.aol.cyclops.util.function.QuadConsumer;
import com.aol.cyclops.util.function.TriConsumer;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
public class FluentFunctions {

	/**
	 * Construct a FluentSupplier from a checked Supplier
	 * <pre>
	 * {@code
	 * FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(IOException.class, ()->"hello boo!")
						.println()
						.get()
	 * 
	 * }
	 * </pre>
	 * @param supplier that throws CheckedExcpetion
	 * @return FluentSupplier
	 */
	public static <R> FluentFunctions.FluentSupplier<R> ofChecked(CheckedSupplier<R> supplier){
		return FluentFunctions.of(ExceptionSoftener.softenSupplier(supplier));
	}
	/**
	 * Construct a FluentSupplier from a Supplier
	 * 
	 * <pre>
	 * {@code 
	 * Cache<Object, Integer> cache = CacheBuilder.newBuilder()
			       .maximumSize(1000)
			       .expireAfterWrite(10, TimeUnit.MINUTES)
			       .build();

		called=0;
		Supplier<Integer> fn = FluentFunctions.of(this::getOne)
													  .name("myFunction")
													  .memoize((key,f)->cache.get(key,()->f.apply(key)));
		fn.get();
		fn.get();
		fn.get();
		
		//called == 1
	 * 
	 * }
	 * </pre>
	 * @param supplier to make Fluent
	 * @return FluentSupplier
	 */
	public static <R> FluentFunctions.FluentSupplier<R> of(Supplier<R> supplier){
		return new FluentSupplier<>(supplier);
	}
	/**
	 * Construct a FluentFunction from a CheckedFunction
	 * 
	 * <pre>
	 * {@code 
	 * FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(IOException.class, in->in+"boo!")
						.println()
						.apply("hello ")
	 * 
	 * 
	 * }
	 * </pre>
	 * @param fn CheckedFunction
	 * @return FluentFunction
	 */
	public static <T,R> FluentFunctions.FluentFunction<T,R> ofChecked(CheckedFunction<T,R> fn){
		return FluentFunctions.of(ExceptionSoftener.softenFunction(fn));
	}
	/**
	 * Construct a FluentFunction from a Function
	 * 
	 * <pre>
	 * {@code 
	 * FluentFunctions.of(this::addOne)
					   .around(advice->advice.proceed(advice.param+1))
					   .println()
					   .apply(10)
	 * }
	 * </pre>
	 * @param fn Function
	 * @return FluentFunction
	 */
	public static <T,R> FluentFunctions.FluentFunction<T,R> of(Function<T,R> fn){
		return new FluentFunction<>(fn);
	}
	/**
	 * Construct a FluentBiFunction from a CheckedBiFunction
	 * 
	 * <pre>
	 * {@code 
	 * FluentFunctions.ofChecked(this::exceptionalFirstTime)
					   .println()
					   .retry(2,500)
					   .apply("hello","woo!")
	 * }
	 * </pre>
	 * @param fn CheckedBiFunction
	 * @return FluentBiFunction
	 */
	public static <T1,T2,R> FluentFunctions.FluentBiFunction<T1,T2,R> ofChecked(CheckedBiFunction<T1,T2,R> fn){
		return FluentFunctions.of(ExceptionSoftener.softenBiFunction(fn));
	}
	/**
	 * Convert a BiFunction to a FluentBiFunction
	 * <pre>
	 * {@code 
	 * CompletableFuture<Integer> cf = FluentFunctions.of(this::add)
													  .liftAsync(ex)
													  .apply(1,1)
	 * 
	 * }
	 * </pre>
	 * @param fn BiFunction to convert
	 * @return FluentBiFuntion
	 */
	public static <T1,T2,R> FluentFunctions.FluentBiFunction<T1,T2,R> of(BiFunction<T1,T2,R> fn){
		return new FluentBiFunction<>(fn);
	}
	/**
	 * Convert a CheckedTriFunction to a FluentTriFunction
	 * <pre>
	 * {@code
	   FluentFunctions.ofChecked(this::exceptionalFirstTime)
					   .println()
					   .retry(2,500)
					   .apply("hello","woo!","h")
					   
	   } 
	 * @param fn CheckedTriFunction to convert
	 * @return FluentTriFunction
	 */
	public static <T1,T2,T3,R> FluentFunctions.FluentTriFunction<T1,T2,T3,R> ofChecked(CheckedTriFunction<T1,T2,T3,R> fn){
		
		return new FluentTriFunction<>(softenTriFunction(fn));
	}
	/**
	 * Convert a CheckedTriFunction to a FluentTriFunction
	 * <pre>
	 * {@code
	   FluentFunctions.of(this::add)	
					  .matches(-1,c->c.hasValues(3).then(i->3))
					  .apply(1,1,1)		   
	   }  
	 * @param fn TriFunction to convert
	 * @return FluentTriFunction
	 */
	public static <T1,T2,T3,R> FluentFunctions.FluentTriFunction<T1,T2,T3,R> of(TriFunction<T1,T2,T3,R> fn){
		return new FluentTriFunction<>(fn);
	}
	
	/**
	 * Convert a statement (e.g. a method or Consumer with no return value) to an Expression. The input is returned as output
	 * <pre>
	 * {@code 
	 * FluentFunctions.expression(System.out::println)
					   .apply("hello");
		
		//hello
	 * }
	 * </pre>
	 * @param action Consumer
	 * @return FluentFunction
	 */
	public static <T> FluentFunctions.FluentFunction<T,T> expression(Consumer<T> action){
		return FluentFunctions.of(t->{
			action.accept(t);
			return t;
		});
	}
	/**
	 * Convert a checked statement (e.g. a method or Consumer with no return value that throws a Checked Exception) to a 
	 * fluent expression (FluentFunction).  The input is returned as output
	 * <pre>
	 * {@code 
	 * public void print(String input) throws IOException{
		  System.out.println(input);
	    }
	    FluentFunctions.checkedExpression(this::print)
				   .apply("hello")
	 * }
	 * </pre>
	 * @param action
	 * @return FluentFunction
	 */
	public static <T> FluentFunctions.FluentFunction<T,T> checkedExpression(CheckedConsumer<T> action){
		final Consumer<T> toUse = ExceptionSoftener.softenConsumer(action);
		return FluentFunctions.of(t->{
			toUse.accept(t);
			return t;
		});
	}
	/**
	 * Convert a BiConsumer into a FluentBiFunction that returns it's input in a Tuple
	 * <pre>
	 * {@code 
	 * public void withTwo(Integer a,Integer b){
		 System.out.println(a+b);
		}
		FluentFunctions.expression(this::withTwo)
					   .apply(1,2)
					   
		//returns Tuple2[1,2]
	 * 
	 * }
	 * </pre>
	 * @param action BiConsumer
	 * @return FluentBiFunction
	 */
	public static <T1,T2> FluentFunctions.FluentBiFunction<T1,T2,Tuple2<T1,T2>> expression(BiConsumer<T1,T2> action){
		return FluentFunctions.of((t1,t2)->{
			action.accept(t1,t2);
			return Tuple.tuple(t1,t2);
		});
	}
	/**
	 * Convert a CheckedBiConsumer into a FluentBiConsumer that returns it's input in a tuple
	 * 
	 * <pre>
	 * {@code 
	 * public void printTwo(String input1,String input2) throws IOException{
		System.out.println(input1);
		System.out.println(input2);
	   }
	 * 
	 * FluentFunctions.checkedExpression(this::printTwo)
					   .apply("hello","world");
					   
		//returns Tuple2["hello","world"]		   
	 * }
	 * </pre>
	 * @param action
	 * @return
	 */
	public static <T1,T2> FluentFunctions.FluentBiFunction<T1,T2,Tuple2<T1,T2>> checkedExpression(CheckedBiConsumer<T1,T2> action){
		final BiConsumer<T1,T2> toUse = ExceptionSoftener.softenBiConsumer(action);
		return FluentFunctions.of((t1,t2)->{
			toUse.accept(t1,t2);
			return Tuple.tuple(t1,t2);
		});
	}
	private static <T1, T2,T3, R> TriFunction<T1, T2, T3,R> softenTriFunction(CheckedTriFunction<T1, T2,T3, R> fn) {
		return (t1, t2,t3) -> {
			try {
				return fn.apply(t1, t2,t3);
			} catch (Throwable e) {
				throw ExceptionSoftener.throwSoftenedException(e);
			}
		};
	}
	
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class FluentSupplier<R> implements Supplier<R>{
		private final Supplier<R> fn;
		private final String name;
		
		public FluentSupplier(Supplier<R> fn){
			this.name = null;
			this.fn = fn;
		}
		
		@Override
		public R get(){
			return fn.get();
		}
		
		
		public FluentSupplier<R> before(Runnable r){
			return withFn(()->{
				r.run();
				return fn.get();
				}
			  );
		}
		public FluentSupplier<R> after(Consumer<R> action){
			return withFn(()->{
				final R result = fn.get();
				action.accept(result);
				return result;
				}
			  );
		}
		
		public FluentSupplier<R> around(Function<Advice0<R>,R> around){
			return withFn(()->around.apply(new Advice0<R>(fn)));
		}
		
		public FluentSupplier<R> memoize(){
			return withFn(Memoize.memoizeSupplier(fn));
		}
		public FluentSupplier<R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeSupplier(fn,cache));
		}
		
		public FluentSupplier<R> name(String name){
			return this.withName(name);
		}
		private String handleNameStart(){
			return name==null ? "(fluent-supplier-" : "("+name+"-";
				
		}
		private String handleNameEnd(){
			return ")";
				
		}
		public FluentSupplier<R> println(){
			return log(s->System.out.println(s),t->t.printStackTrace());
			
		}	
		public FluentSupplier<R> log(Consumer<String> logger,Consumer<Throwable> error){
			return FluentFunctions.of(()->{
				try{
					R result = fn.get();
					logger.accept(handleNameStart()+"Result["+result+"]"+handleNameEnd());
					return result;
				}catch(Throwable t){
					error.accept(t);
					throw ExceptionSoftener.throwSoftenedException(t);
				}
			});
		}	

		public <X extends Throwable> FluentSupplier<R> recover(Class<X> type,Supplier<R> onError){
			return FluentFunctions.of(()->{
				try{
					return fn.get();
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return onError.get();	
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
			});
			
		}
		public FluentSupplier<R> retry(int times,int backoffStartTime){
			return FluentFunctions.of(() -> {
				int count = times;
				MutableInt sleep =MutableInt.of(backoffStartTime);
				Throwable exception=null;
				while(count-->0){
					try{
						return fn.get();
					}catch(Throwable e){
						exception = e;
					}
					ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep.get()));
					
					sleep.mutate(s->s*2);
				}
				throw ExceptionSoftener.throwSoftenedException(exception);
				
			});
			
		}
		
		public <R1 >FluentSupplier<R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1,Supplier<? extends R1> otherwise){
			return FluentFunctions.of(()->Matchable.of(fn.get()).mayMatch(case1).orElseGet(otherwise));
		}
		
		
		public ReactiveSeq<R> generate(){
			return ReactiveSeq.generate(fn);
		}
		
		public FluentSupplier<Optional<R>> lift(){
			return new FluentSupplier<>(() -> Optional.ofNullable(fn.get()));
		}
		public <X extends Throwable> FluentSupplier<Try<R,X>> liftTry(Class<X>... classes){
			return FluentFunctions.of(() -> Try.withCatch(()->fn.get(),classes));
		}
		
		public FluentSupplier<AnyM<R>> liftM(){
			return new FluentSupplier<>(()-> AnyM.ofNullable(fn.get()));
		}
		
		public FluentSupplier<CompletableFuture<R>> liftAsync(Executor ex){
			return FluentFunctions.of(()->CompletableFuture.supplyAsync(fn,ex));
		}
		public CompletableFuture<FluentSupplier<R>> async(Executor ex){
			return CompletableFuture.supplyAsync(()->FluentFunctions.of(fn),ex);
		}
		
		
	}
	
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class FluentFunction<T,R> implements Function<T,R>, Reader<T,R>{
		private final Function<T,R> fn;
		private final String name;
		
		public FluentFunction(Function<T,R> fn){
			this.name = null;
			this.fn = fn;
		}
		@Override
		public R apply(T t) {
			return fn.apply(t);
		}
		
		public <R1> FluentFunction<T, R1> map(Function<? super R, ? extends R1> f2) {
			return FluentFunctions.of(fn.andThen( f2));
		}

		public <R1> FluentFunction<T, R1> flatMap(Function<? super R, ? extends Reader<T, R1>> f) {
			return FluentFunctions.of(a -> f.apply(fn.apply(a)).apply(a));
		}
		public FluentFunction<T,R> before(Consumer<T> action){
			return withFn(t->{
				action.accept(t);
				return fn.apply(t);
				}
			  );
		}

		public FluentFunction<T,R> after(BiConsumer<T,R> action){
			return withFn(t->{
				
				final R result = fn.apply(t);
				action.accept(t,result);
				 return result;
				}
			  );
		}
		
		public FluentFunction<T,R> around(Function<Advice1<T,R>,R> around){
			return withFn(t->around.apply(new Advice1<T,R>(t,fn)));
		}
		
		public FluentSupplier<R> partiallyApply(T param){
			return new FluentSupplier<>(PartialApplicator.partial(param,fn));
		}
		public FluentFunction<T,R> memoize(){
			return withFn(Memoize.memoizeFunction(fn));
		}
		public FluentFunction<T,R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeFunction(fn));
		}
		public FluentFunction<T,R> name(String name){
			return this.withName(name);
		}
		private String handleNameStart(){
			return name==null ? "(fluent-function-" : "("+name+"-";
				
		}
		private String handleNameEnd(){
			return ")";
				
		}
		public FluentFunction<T,R> log(Consumer<String> logger,Consumer<Throwable> error){
			return FluentFunctions.of(t1->{
				
				try{
					logger.accept(handleNameStart()+"Parameter["+t1+"]"+handleNameEnd());
					R result = fn.apply(t1);
					logger.accept(handleNameStart()+"Result["+result+"]"+handleNameEnd());
					return result;
				}catch(Throwable t){
					error.accept(t);
					throw ExceptionSoftener.throwSoftenedException(t);
				}
				
			});
		}	
		public FluentFunction<T,R> println(){
			return log(s->System.out.println(s),t->t.printStackTrace());
		}	
		public <X extends Throwable> FluentFunction<T,R> recover(Class<X> type,Function<T,R> onError){
			return FluentFunctions.of(t1->{
				try{
					return fn.apply(t1);
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return onError.apply(t1);
						
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
			});
		}
		public FluentFunction<T,R> retry(int times,int backoffStartTime){
			return FluentFunctions.of(t -> {
				int count = times;
				MutableInt sleep =MutableInt.of(backoffStartTime);
				Throwable exception=null;
				while(count-->0){
					try{
						return fn.apply(t);
					}catch(Throwable e){
						exception = e;
					}
					ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep.get()));
					
					sleep.mutate(s->s*2);
				}
				throw ExceptionSoftener.throwSoftenedException(exception);
				
			});
			
		}
		public <R1> FluentFunction<T,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1, Supplier<? extends R1> otherwise){
		
			return FluentFunctions.of(t->Matchable.of(fn.apply(t)).mayMatch(case1).orElseGet(otherwise));
		}
		
		/**
		 * Generate an infinite Stream that iterates from the specified seed using the currently wrapped function
		 * 
		 * e.g.
		 * 
		 * <pre>
		 * {@code 
		 * FluentFunctions.of(this::addOne)	
						.iterate(95281,i->i)
						.forEach(System.out::println);
		 * 
		 * 
		 * //95282
		   //95283
		   //95284
		   //95285
		   //95286
		 * //etc
		 * }
		 * </pre>
		 * 
		 * 
		 * @param seed initial value
		 * @param mapToType Convert from supplied functions return type to Stream input type
		 * @return Infinite Stream
		 */
		public ReactiveSeq<R> iterate(T seed,Function<R,T> mapToType){
			return ReactiveSeq.iterate(fn.apply(seed),t->fn.compose(mapToType).apply(t));
		}
		public ReactiveSeq<R> generate(T input){
			return ReactiveSeq.generate(()->fn.apply(input));
		}
		public FluentFunction<Optional<T>,Optional<R>> lift(){
			return new FluentFunction<>(opt -> opt.map(t->fn.apply(t)));
		}
		public <X extends Throwable> FluentFunction<T,Try<R,X>> liftTry(Class<X>... classes){
			return FluentFunctions.of((t1) -> Try.withCatch(()->fn.apply(t1),classes));
		}
		public FluentFunction<AnyM<T>,AnyM<R>> liftM(){
			return FluentFunctions.of(AnyM.liftM(fn));
		}
		public FluentFunction<T,CompletableFuture<R>> liftAsync(Executor ex){
			return FluentFunctions.of(t->CompletableFuture.supplyAsync(()->fn.apply(t),ex));
		}
		public CompletableFuture<FluentFunction<T,R>> async(Executor ex){
			return CompletableFuture.supplyAsync(()->FluentFunctions.of(fn),ex);
		}
		
	}
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class FluentBiFunction<T1,T2,R> implements BiFunction<T1,T2,R>{
		BiFunction<T1,T2,R> fn;
		private final String name;
		
		public FluentBiFunction(BiFunction<T1,T2,R> fn){
			this.name = null;
			this.fn = fn;
		}
		@Override
		public R apply(T1 t1,T2 t2) {
			return fn.apply(t1,t2);
		}
		public FluentBiFunction<T1,T2,R> before(BiConsumer<T1,T2> action){
			return withFn((t1,t2)->{
				action.accept(t1,t2);
				return fn.apply(t1,t2);
				}
			  );
		}

		public FluentBiFunction<T1,T2,R> after(TriConsumer<T1,T2,R> action){
			return withFn((t1,t2)->{
				
				final R result = fn.apply(t1,t2);
				action.accept(t1,t2,result);
				 return result;
				}
			  );
		}
		
		public FluentBiFunction<T1,T2,R> around(Function<Advice2<T1,T2,R>,R> around){
			return withFn((t1,t2)->around.apply(new Advice2<>(t1,t2,fn)));
		}
		
		public FluentFunction<T2,R> partiallyApply(T1 param){
			return new FluentFunction<>(PartialApplicator.partial2(param,fn));
		}
		public FluentSupplier<R> partiallyApply(T1 param1,T2 param2){
			return new FluentSupplier<>(PartialApplicator.partial2(param1,param2,fn));
		}
		public FluentFunction<T1,Function<T2,R>> curry(){
			return new FluentFunction(CurryVariance.curry2(fn));
		}
		public FluentBiFunction<T1,T2,R> memoize(){
			return withFn(Memoize.memoizeBiFunction(fn));
		}
		public FluentBiFunction<T1,T2,R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeBiFunction(fn));
		}
		public FluentBiFunction<T1,T2,R> name(String name){
			return this.withName(name);
		}
		private String handleNameStart(){
			return name==null ? "(fluent-function-" : "("+name+"-";
				
		}
		private String handleNameEnd(){
			return ")";
				
		}
		public FluentBiFunction<T1,T2,R> log(Consumer<String> logger,Consumer<Throwable> error){
			return FluentFunctions.of((t1,t2)->{
				try{
					logger.accept(handleNameStart()+"Parameters["+t1+","+t2+"]"+handleNameEnd());
					R result = fn.apply(t1,t2);
					logger.accept(handleNameStart()+"Result["+result+"]"+handleNameEnd());
					return result;
				}catch(Throwable t){
					error.accept(t);
					throw ExceptionSoftener.throwSoftenedException(t);
				}
			});
		}	
		public FluentBiFunction<T1,T2,R> println(){
			return log(s->System.out.println(s),t->t.printStackTrace());
		}	
		
		public <X extends Throwable> FluentBiFunction<T1,T2,R> recover(Class<X> type,BiFunction<T1,T2,R> onError){
			return FluentFunctions.of((t1,t2)->{
				try{
					return fn.apply(t1,t2);
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return onError.apply(t1,t2);
						
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
			});
			
		}
		public FluentBiFunction<T1,T2,R> retry(int times,int backoffStartTime){
			return FluentFunctions.of((t1,t2) -> {
				int count = times;
				MutableInt sleep =MutableInt.of(backoffStartTime);
				Throwable exception=null;
				while(count-->0){
					try{
						return fn.apply(t1,t2);
					}catch(Throwable e){
						exception = e;
					}
					ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep.get()));
					
					sleep.mutate(s->s*2);
				}
				throw ExceptionSoftener.throwSoftenedException(exception);
				
			});
			
		}
		public <R1> FluentBiFunction<T1,T2,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1, Supplier<? extends R1> otherwise){
			return FluentFunctions.of((t1,t2)->Matchable.of(fn.apply(t1,t2)).mayMatch(case1).orElseGet(otherwise));
		}
		
		
		public ReactiveSeq<R> iterate(T1 seed1,T2 seed2,Function<R,Tuple2<T1,T2>> mapToTypeAndSplit){
			return ReactiveSeq.iterate(fn.apply(seed1,seed2),t->{ 
				Tuple2<T1,T2> tuple =mapToTypeAndSplit.apply(t);
				return fn.apply(tuple.v1,tuple.v2);
			});
		}
		public ReactiveSeq<R> generate(T1 input1,T2 input2){
			return ReactiveSeq.generate(()->fn.apply(input1,input2));
		}
		
		public FluentBiFunction<Optional<T1>,Optional<T2>,Optional<R>> lift(){
			return new FluentBiFunction<>((opt1,opt2) -> opt1.flatMap(t1-> opt2.map(t2->fn.apply(t1,t2))));
		}
		public <X extends Throwable> FluentBiFunction<T1,T2,Try<R,X>> liftTry(Class<X>... classes){
			return FluentFunctions.of((t1,t2) -> Try.withCatch(()->fn.apply(t1,t2),classes));
		}
		
		public FluentBiFunction<AnyM<T1>,AnyM<T2>,AnyM<R>> liftM(){
			return FluentFunctions.of(AnyM.liftM2(fn));
		}
		public FluentBiFunction<T1,T2,CompletableFuture<R>> liftAsync(Executor ex){
			return FluentFunctions.of((t1,t2)->CompletableFuture.supplyAsync(()->fn.apply(t1,t2),ex));
		}
		public CompletableFuture<FluentBiFunction<T1,T2,R>> async(Executor ex){
			return CompletableFuture.supplyAsync(()->FluentFunctions.of(fn),ex);
		}
		
	}
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class FluentTriFunction<T1,T2,T3,R>{
		private  final TriFunction<T1,T2,T3,R> fn;
		private final String name;
		
		public FluentTriFunction(TriFunction<T1,T2,T3,R> fn){
			this.name = null;
			this.fn = fn;
		}
		
		
		public R apply(T1 t1,T2 t2,T3 t3) {
			return fn.apply(t1,t2,t3);
		}
		public FluentTriFunction<T1,T2,T3,R> before(TriConsumer<T1,T2,T3> action){
			return withFn((t1,t2,t3)->{
				action.accept(t1,t2,t3);
				return fn.apply(t1,t2,t3);
				}
			  );
		}

		public FluentTriFunction<T1,T2,T3,R> after(QuadConsumer<T1,T2,T3,R> action){
			return withFn((t1,t2,t3)->{
				
				final R result = fn.apply(t1,t2,t3);
				action.accept(t1,t2,t3,result);
				 return result;
				}
			  );
		}
		
		public FluentTriFunction<T1,T2,T3,R> around(Function<Advice3<T1,T2,T3,R>,R> around){
			return withFn((t1,t2,t3)->around.apply(new Advice3<>(t1,t2,t3,fn)));
		}
		public FluentBiFunction<T2,T3,R> partiallyApply(T1 param){
			return new FluentBiFunction<>(PartialApplicator.partial3(param,fn));
		}
		public FluentFunction<T3,R> partiallyApply(T1 param1,T2 param2){
			return new FluentFunction<>(PartialApplicator.partial3(param1,param2,fn));
		}
		public FluentSupplier<R> partiallyApply(T1 param1,T2 param2,T3 param3){
			return new FluentSupplier<>(PartialApplicator.partial3(param1,param2,param3,fn));
		}
		public FluentFunction<T1,Function<T2,Function<T3,R>>> curry(){
			return new FluentFunction<>(Curry.curry3(fn));
		}
		public FluentTriFunction<T1,T2,T3,R> memoize(){
			return withFn(Memoize.memoizeTriFunction(fn));
		}
		public FluentTriFunction<T1,T2,T3,R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeTriFunction(fn));
		}
		public FluentTriFunction<T1,T2,T3,R> name(String name){
			return this.withName(name);
		}
		private String handleNameStart(){
			return name==null ? "(fluent-function-" : "("+name+"-";
				
		}
		private String handleNameEnd(){
			return ")";
				
		}
		public FluentTriFunction<T1,T2,T3,R> log(Consumer<String> logger,Consumer<Throwable> error){
			return FluentFunctions.of((t1,t2,t3)->{
				try{
					logger.accept(handleNameStart()+"Parameters["+t1+","+t2+","+t3+"]"+handleNameEnd());
					R result = fn.apply(t1,t2,t3);
					logger.accept(handleNameStart()+"Result["+result+"]"+handleNameEnd());
					return result;
				}catch(Throwable t){
					error.accept(t);
					throw ExceptionSoftener.throwSoftenedException(t);
				}
			});
		}	
		public FluentTriFunction<T1,T2,T3,R> println(){
			return log(s->System.out.println(s),t->t.printStackTrace());
		}	
		
		public <X extends Throwable> FluentTriFunction<T1,T2,T3,R> recover(Class<X> type,TriFunction<T1,T2,T3,R> onError){
			return FluentFunctions.of((t1,t2,t3)->{
				try{
					return fn.apply(t1,t2,t3);
				}catch(Throwable t){
					if(type.isAssignableFrom(t.getClass())){
						return onError.apply(t1,t2,t3);
						
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
			});
			
		}
		public FluentTriFunction<T1,T2,T3,R> retry(int times,int backoffStartTime){
			return FluentFunctions.of((t1,t2,t3) -> {
				int count = times;
				MutableInt sleep =MutableInt.of(backoffStartTime);
				Throwable exception=null;
				while(count-->0){
					try{
						return fn.apply(t1,t2,t3);
					}catch(Throwable e){
						exception = e;
					}
					ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep.get()));
					
					sleep.mutate(s->s*2);
				}
				throw ExceptionSoftener.throwSoftenedException(exception);
				
			});
			
		}
		public <R1> FluentTriFunction<T1,T2,T3,R1> matches(Function<CheckValues<R,R1>,CheckValues<R,R1>> case1, Supplier<? extends R1> otherwise){
			return FluentFunctions.of((t1,t2,t3)->Matchable.of(fn.apply(t1,t2,t3)).mayMatch(case1).orElseGet(otherwise));
		}
		
		
		public ReactiveSeq<R> iterate(T1 seed1,T2 seed2,T3 seed3,Function<R,Tuple3<T1,T2,T3>> mapToType){
			return ReactiveSeq.iterate(fn.apply(seed1,seed2,seed3),t->{ 
				Tuple3<T1,T2,T3> tuple =mapToType.apply(t);
				return fn.apply(tuple.v1,tuple.v2,tuple.v3);
			});
		}
		public ReactiveSeq<R> generate(T1 input1,T2 input2,T3 input3){
			return ReactiveSeq.generate(()->fn.apply(input1,input2,input3));
		}
		
		public FluentTriFunction<Optional<T1>,Optional<T2>,Optional<T3>,Optional<R>> lift(){
			return new FluentTriFunction<>((opt1,opt2,opt3) -> opt1.flatMap(t1-> opt2.flatMap(t2-> opt3.map(t3->fn.apply(t1,t2,t3)))));
		}
		public <X extends Throwable> FluentTriFunction<T1,T2,T3,Try<R,X>> liftTry(Class<X>... classes){
			return FluentFunctions.of((t1,t2,t3) -> Try.withCatch(()->fn.apply(t1,t2,t3),classes));
		}
		
		public FluentTriFunction<AnyM<T1>,AnyM<T2>,AnyM<T3>,AnyM<R>> liftM(){
			return FluentFunctions.of(AnyM.liftM3Cyclops(fn));
		}
		public FluentTriFunction<T1,T2,T3,CompletableFuture<R>> liftAsync(Executor ex){
			return FluentFunctions.of((t1,t2,t3)->CompletableFuture.supplyAsync(()->fn.apply(t1,t2,t3),ex));
		}
		public CompletableFuture<FluentTriFunction<T1,T2,T3,R>> async(Executor ex){
			return CompletableFuture.supplyAsync(()->FluentFunctions.of(fn),ex);
		}
		
	   public <R2> FluentTriFunction<T1,T2,T3,R2> andThen(Function<? super R, ? extends R2> after) {
	        Objects.requireNonNull(after);
	        return FluentFunctions.of((T1 t1,T2 t2, T3 t3) -> after.apply(apply(t1,t2,t3)));
	    }
	}
	
	@AllArgsConstructor
	public static class Advice0<R>{
		
		private final Supplier<R> fn;
		
		public R proceed(){
			return fn.get();
		}
		
	}
	
	@AllArgsConstructor
	public static class Advice1<T,R>{
		public final T param;
		private final Function<T,R> fn;
		
		public R proceed(){
			return fn.apply(param);
		}
		public R proceed(T param){
			return fn.apply(param);
		}
	}
	@AllArgsConstructor
	public static class Advice2<T1,T2,R>{
		public final T1 param1;
		public final T2 param2;
		private final BiFunction<T1,T2,R> fn;
		
		public R proceed(){
			return fn.apply(param1,param2);
		}
		public R proceed(T1 param1, T2 param2){
			return fn.apply(param1,param2);
		}
		public R proceed1(T1 param){
			return fn.apply(param,param2);
		}
		public R proceed2(T2 param){
			return fn.apply(param1,param);
		}
	}
	@AllArgsConstructor
	public static class Advice3<T1,T2,T3,R>{
		public final T1 param1;
		public final T2 param2;
		public final T3 param3;
		private final TriFunction<T1,T2,T3,R> fn;
		
		public R proceed(){
			return fn.apply(param1,param2,param3);
		}
		public R proceed(T1 param1, T2 param2,T3 param3){
			return fn.apply(param1,param2,param3);
		}
		public R proceed1(T1 param){
			return fn.apply(param,param2,param3);
		}
		public R proceed2(T2 param){
			return fn.apply(param1,param,param3);
		}
		public R proceed3(T3 param){
			return fn.apply(param1,param2,param);
		}
	}
}
