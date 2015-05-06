
package com.aol.cyclops.trycatch;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.val;

/**
 * Light weight Try Monad
 * 
 * Behaviour modelled closer on how try / catch is used in Java
 * currently (compared with scala.util.Try and javaslang.monad.Try).
 * 
 * Goals / features
 * 
 * Support for init block / try block  / finally block (with resources)
 * Does not automatically catch exceptions in combinators
 * Can target specific exception types
 * Exception types to be caught can be specified in xxxWithCatch methods
 * Handle exceptions conciously, not coding bugs
 * Fluent step builders
 * Fail fast
 * 
 * @author johnmcclean
 *
 * @param <T>
 * @param <X>
 */
public interface Try<T,X extends Throwable> {

	public T get();
	public T orElse(T value);
	public T orElseGet(Supplier<T> value);
	public <R> Try<R,X> map(Function<T,R> fn);
	public <R> Try<R,X> flatMap(Function<T,Try<R,X>> fn);
	public Try<T,X> filter(Predicate<T> p);
	public Try<T,X> onFail(Consumer<X> consumer);
	public Try<T,X> onFail(Class<? extends X> t,Consumer<X> consumer);
	public Try<T,X> recover(Function<X,T> fn);
	public Try<T,X> recoverWith(Function<X,Try<T,X>> fn);
	public Try<T,X> recoverFor(Class<? extends X> t,Function<X, T> fn);
	public Try<T,X> recoverWithFor(Class<? extends X> t,Function<X, Try<T,X>> fn);
	public Try<T,X> flatten();
	public Optional<T> toOptional();
	public Stream<T> toStream();
	public boolean isSuccess();
	public boolean isFailure();
	public void foreach(Consumer<T> consumer);
	
	
	@SafeVarargs
	public static <T,X extends Throwable> Try<T,X> withCatch(CheckedSupplier<T,X> cf,Class<? extends X>...classes){
		try{
			return Success.of(cf.get());
		}catch(Throwable t){
			if(classes.length==0)
				return Failure.of((X)t);
			val error = Stream.of(classes).filter(c -> t.getClass().isAssignableFrom(c)).findFirst();
			if(error.isPresent())
				return Failure.of((X)t);
			else
				throw new RuntimeException(t);
		}
		
	}
	@SafeVarargs
	public static <X extends Throwable> Init<X> catchExceptions(Class<? extends X>...classes){
		return new MyInit<X>((Class[])classes);
	}
	
	@SafeVarargs
	public static <X extends Throwable>  Try<Void,X> runWithCatch(CheckedRunnable cf,Class<? extends X>...classes){
		try{
			cf.run();
			return Success.of(null);
		}catch(Throwable t){
			if(classes.length==0)
				return Failure.of((X)t);
			val error = Stream.of(classes).filter(c -> c.isAssignableFrom(t.getClass())).findFirst();
			if(error.isPresent())
				return Failure.of((X)t);
			else
				throw new RuntimeException(t);
		}
		
	}
	@AllArgsConstructor
	static class MyInit<X extends Throwable> implements Init<X>{
		private final Class<X>[] classes;
		@Override
		public <V> TryCatch<V, X> init(CheckedSupplier<V, X> input) {
			return new MyTryCatch(classes,input);
		}
		@Override
		public Try<Void, X> run(CheckedRunnable<X> input) {
			return runWithCatch(input,classes);
		}
		@Override
		public <V> Try<V, X> tryThis(CheckedSupplier<V,X> input) {
			return withCatch(input,classes);
		}
		
	}
	@AllArgsConstructor
	static class MyTryCatch<V,X extends Throwable> implements TryCatch<V,X>{
		private final Class<X>[] classes;
		private final CheckedSupplier<V,X> inputSupplier;

		@Override
		public <T> AndFinally<T,V,X> tryThis(CheckedFunction<V, T, X> catchBlock) {
			return new MyFinallyBlock(classes,inputSupplier,catchBlock);
		}
		
	}
	@AllArgsConstructor
	public static class MyFinallyBlock<T,V,X extends Throwable> implements AndFinally<T,V,X>{
		private final Class<X>[] classes;
		private final CheckedSupplier<V,X> inputSupplier;
		private final CheckedFunction<V, T, X> catchBlock;
		@Override
		public Try<T, X> andFinally(CheckedConsumer<V, X> finallyBlock) {
			try{
				val input = inputSupplier.get();
				try{
					return withCatch(()->catchBlock.apply(input),classes);
				
				}finally{
					try{
						finallyBlock.accept(input);
					}catch(Throwable t){
						if(classes.length==0)
							return Failure.of((X)t);
						val error = Stream.of(classes).filter(c -> t.getClass().isAssignableFrom(c)).findFirst();
						if(error.isPresent())
							return Failure.of((X)t);
						else
							throw new RuntimeException(t);
					}
				}
			}catch(Throwable t){
				if(classes.length==0)
					return Failure.of((X)t);
				val error = Stream.of(classes).filter(c -> t.getClass().isAssignableFrom(c)).findFirst();
				if(error.isPresent())
					return Failure.of((X)t);
				else
					throw new RuntimeException(t);
			}
		}
		
	}
	
	public static interface Init<X extends Throwable>{
		<V> TryCatch<V,X> init(CheckedSupplier<V,X> input);
		Try<Void,X> run(CheckedRunnable<X> input);
		<V> Try<V,X> tryThis(CheckedSupplier<V,X> input);
	}
	public static interface TryCatch<V,X extends Throwable> {
		<T> AndFinally<T,V,X> tryThis(CheckedFunction<V,T,X> catchBlock);
	}
	public static interface AndFinally<T,V,X extends Throwable> {
		 Try<T,X> andFinally(CheckedConsumer<V,X> finallyBlock);
	}
	
	
	public static interface CheckedFunction<T,R,X extends Throwable>{
		public R apply(T t) throws X;
	}
	public static interface CheckedSupplier<T,X extends Throwable>{
		public T get() throws X;
	}
	public static interface CheckedConsumer<T,X extends Throwable>{
		public void accept(T t) throws X;
	}
	public static interface CheckedRunnable<X extends Throwable>{
		public void run() throws X;
	}
	
}
