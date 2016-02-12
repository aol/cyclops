
package com.aol.cyclops.control;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.matcher2.CheckValues;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.ToStream;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.Applicativable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * Light weight Try Monad
 * 
 * Fail fast behaviour with more explicit declararions required compared to
 *  with scala.util.Try and javaslang.monad.Try. This is probably closer
 *  to how most Java devs currently handle exceptions.
 * 
 * Goals / features
 * 
 * Support for init block / try block  / finally block
 * Try with resources
 * Try with multiple resources
 * Does not automatically catch exceptions in combinators
 * Can target specific exception types
 * Exception types to be caught can be specified in xxxWithCatch methods
 * Handle exceptions conciously, not coding bugs
 * Fluent step builders
 * Fail fast
 * 
 * @author johnmcclean
 *
 * @param <T> Return type (success)
 * @param <X> Base Error type
 */
public interface Try<T,X extends Throwable> extends Supplier<T>,Value<T>, ToStream<T>,Applicativable<T> {

	/**
	 * Construct a Failure instance from a throwable
	 * 
	 * @param error for Failure
	 * @return new Failure with error
	 */
	public static <T,X extends Throwable> Failure<T,X> failure(X error){
		return new Failure<>(error);
	}
	/**
	 * @param value Successful value
	 * @return new Success with value
	 */
	
	public static <T,X extends Throwable> Success<T,X> success(T value){
		return new Success<>(value,new Class[0]);
	}
	default Xor<X,T> toXor(){
		if(isSuccess())
			return Xor.primary(get());
		else
			return Xor.<X,T>secondary(this.toFailedOptional().get());
	}
	
	default <T> Try<T,?> unit(T value){
		return success(value);
	}
	@Override
	default <R> Try<R,X> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (Try<R,X>)Applicativable.super.patternMatch(defaultValue, case1);
	}
	
	public <R> R visit(Function<? super T, ? extends R> success, Function<? super X, ? extends R> failure);
	/**
	 * @return This monad, wrapped as AnyM of Success
	 */
	public AnyM<T> anyM();
	/**
	 * @return This monad, wrapped as AnyM of Failure
	 */
	public AnyM<X> anyMFailure();
	/**
	 * @return This monad, wrapped as AnyM of Success
	 */
	public AnyM<T> anyMSuccess();
	/**
	 * @return Successful value or will throw Throwable (X) if Failire
	 */
	public T get();
	/**
	 * Throw exception if Failure, do nothing if success
	 */
	public void throwException();
	/**
	 * @param value Return value supplied if Failure, otherwise return Success value
	 * @return Success value or supplied value
	 */
	public T orElse(T value);
	
	/**
	 * 
	 * @param value from supplied Supplier if Failure otherwise return Success value
	 * @return Success value
	 */
	public T orElseGet(Supplier<? extends T> value);
	
	/**
	 * @param fn Map success value from T to R. Do nothing if Failure (return this)
	 * @return New Try with mapped value (Success) or this (Failure)
	 */
	
	public <R> Try<R,X> map(Function<? super T,? extends R> fn);
	
	/**
	 * @param fn FlatMap success value or Do nothing if Failure (return this)
	 * @return Try returned from FlatMap fn
	 */
	public <R> Try<R,X> flatMap(Function<? super T,? extends Try<R,X>> fn);
	
	
	/**
	 * @param p Convert a Success to a Failure (with a null value for Exception) if predicate does not hold.
	 *          Do nothing to a Failure
	 * @return this if Success and Predicate holds, or if Failure. New Failure if Success and Predicate fails
	 */
	public Optional<T> filter(Predicate<? super T> p);
	
	/**
	 * @param consumer Accept Exception if present (Failure)
	 * @return this
	 */
	public Try<T,X> onFail(Consumer<? super X> consumer);
	/**
	 * @param t Class type of match Exception against
	 * @param consumer Accept Exception if present (Failure) and if class types match
	 * @return this
	 */
	public Try<T,X> onFail(Class<? extends X> t,Consumer<X> consumer);
	
	/**
	 * @param fn Recovery function - map from a failure to a Success.
	 * @return new Success
	 */
	public Success<T,X> recover(Function<? super X,? extends T> fn);
	/**
	 * flatMap recovery
	 * 
	 * @param fn Recovery FlatMap function. Map from a failure to a Success
	 * @return Success from recovery function
	 */
	public Success<T,X> recoverWith(Function<? super X,? extends Success<T,X>> fn);
	
	/**
	 * Recover if exception is of specified type
	 * @param t Type of exception to match against
	 * @param fn Recovery function
	 * @return New Success if failure and types match / otherwise this
	 */
	public Try<T,X> recoverFor(Class<? extends X> t,Function<? super X, ? extends T> fn);
	
	/**
	 * 
	 * FlatMap recovery function if exception is of specified type
	 * 
	 * @param t Type of exception to match against
	 * @param fn Recovery FlatMap function. Map from a failure to a Success
	 * @return Success from recovery function or this  and types match or if already Success
	 */
	public Try<T,X> recoverWithFor(Class<? extends X> t,Function<? super X, ? extends Success<T,X>> fn);
	/**
	 * Flatten a nested Try Structure
	 * @return Lowest nested Try
	 */
	public Try<T,X> flatten();
	
	/**
	 * @return Optional present if Success, Optional empty if failure
	 */
	public Optional<T> toOptional();
	/**
	 * @return Stream with value if Sucess, Empty Stream if failure
	 */
	public SequenceM<T> stream();
	/**
	 * @return Optional present if Failure (with Exception), Optional empty if Success
	 */
	public Optional<X> toFailedOptional();
	/**
	 * @return Stream with error if Failure, Empty Stream if success
	 */
	public Stream<X> toFailedStream();
	
	/**
	 * @return true if Success / false if Failure
	 */
	public boolean isSuccess();
	
	/**
	 * @return True if Failure / false if Success
	 */
	public boolean isFailure();
	
	/**
	 * @param consumer Accept value if Success / not called on Failure
	 */
	public void forEach(Consumer<? super T> consumer);
	/**
	 * @param consumer Accept value if Failure / not called on Failure
	 */
	public void forEachFailed(Consumer<? super X> consumer);
	
	/**
	 * @param consumer Accept value if Success
	 * @return this
	 */
	default Try<T,X> peek(Consumer<? super T> consumer){
		forEach(consumer);
		return this;
	}
	/**
	 * @param consumer Accept Exception if Failure
	 * @return this
	 */
	default Try<T,X> peekFailed(Consumer<? super X> consumer){
		forEachFailed(consumer);
		return this;
	}
	
	@Override
	default Iterator<T> iterator() {
		
		return Applicativable.super.iterator();
	}
	/**
	 * Return a Try that will catch specified exceptions when map / flatMap called
	 * For use with liftM / liftM2 and For Comprehensions (when Try is at the top level)
	 * 
	 * @param value Initial value
	 * @param classes Exceptions to catch during map / flatMap
	 * @return Try instance
	 */
	@SafeVarargs
	public static <T,X extends Throwable> Try<T,X> of(T value,Class<? extends Throwable>... classes){
		return new Success<>(value,classes);
	}
	
	/**
	 * Try to execute supplied Supplier and will Catch specified Excpetions or java.lang.Exception
	 * if none specified.
	 * 
	 * @param cf CheckedSupplier to attempt to execute
	 * @param classes  Exception types to catch (or java.lang.Exception if none specified)
	 * @return New Try
	 */
	@SafeVarargs
	public static <T,X extends Throwable> Try<T,X> withCatch(CheckedSupplier<T,X> cf,
						Class<? extends X>...classes){
		Objects.requireNonNull(cf);
		try{
			return Success.of(cf.get());
		}catch(Throwable t){
			if(classes.length==0)
				return Failure.of((X)t);
			val error = Stream.of(classes).filter(c -> c.isAssignableFrom(t.getClass())).findFirst();
			if(error.isPresent())
				return Failure.of((X)t);
			else
				throw ExceptionSoftener.throwSoftenedException(t);
		}
		
	}
	/**
	 * Try to execute supplied Runnable and will Catch specified Excpetions or java.lang.Exception
	 * if none specified.
	 * 
	 * @param cf CheckedRunnable to attempt to execute
	 * @param classes  Exception types to catch (or java.lang.Exception if none specified)
	 * @return New Try
	 */
	@SafeVarargs
	public static <X extends Throwable>  Try<Void,X> runWithCatch(CheckedRunnable<X> cf,Class<? extends X>...classes){
		Objects.requireNonNull(cf);
		try{
			cf.run();
			return Success.of(null);
		}catch(Throwable t){
			t.printStackTrace();
			if(classes.length==0)
				return Failure.of((X)t);
			val error = Stream.of(classes).filter(c -> c.isAssignableFrom(t.getClass())).findFirst();
			if(error.isPresent())
				return Failure.of((X)t);
			else
				throw ExceptionSoftener.throwSoftenedException(t);
		}
		
	}
	/**
	 * Fluent step builder for Try / Catch / Finally and Try with resources equivalents.
	 * Start with Exception types to catch.
	 * 
	 * @param classes Exception types to catch
	 * @return Next step in the fluent Step Builder
	 */
	@SafeVarargs
	public static <X extends Throwable> Init<X> catchExceptions(Class<? extends X>...classes){
		return new MyInit<X>((Class[])classes);
	}
	
	
	
	@AllArgsConstructor
	static class MyInit<X extends Throwable> implements Init<X>{
		private final Class<X>[] classes;
		/* 
		 *	@param input
		 *	@return
		 * @see com.aol.cyclops.trycatch.Try.Init#init(com.aol.cyclops.trycatch.Try.CheckedSupplier)
		 */
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
			return new MyFinallyBlock<>(classes,inputSupplier,catchBlock);
		}
		@Override
		public <T> Try<T,X> tryWithResources(CheckedFunction<V, T, X> catchBlock) {
			return new MyFinallyBlock<>(classes,inputSupplier,catchBlock).close();
		}
		
	}
	@AllArgsConstructor
	public static class MyFinallyBlock<T,V,X extends Throwable> implements AndFinally<T,V,X>{
		private final Class<X>[] classes;
		private final CheckedSupplier<V,X> inputSupplier;
		private final CheckedFunction<V, T, X> catchBlock;
		
		private void invokeClose(Object in) {
			if(in instanceof Closeable)
				invokeCloseableClose((Closeable)in);
			else if( in instanceof AutoCloseable)
				invokeAutocloseableClose((AutoCloseable)in);
			else if(in instanceof Iterable)
				invokeClose((Iterable)in);
			else
				_invokeClose(in);
		}
		private void invokeClose(Iterable in){
			for(Object next : in)
				invokeClose(next);
			
		
	}
		private void invokeCloseableClose(Closeable in){
			
			Try.runWithCatch(()->in.close());
		
	}
		private void invokeAutocloseableClose(AutoCloseable in){
			
			Try.runWithCatch(()->in.close());
		
	}
		private void _invokeClose(Object in){
			
				Try.withCatch(()->in.getClass().getMethod("close")).filter(m->m!=null)
						.flatMap(m->Try.withCatch(()->m.invoke(in))
											.filter(o->o!=null));
			
		}
		public Try<T,X> close(){
			
			return andFinally(in -> {invokeClose(in);} );
		}
		
		@Override
		public Try<T, X> andFinally(final CheckedConsumer<V, X> finallyBlock) {
			
				
							
				Try<V,X> input =  Try.withCatch(() ->inputSupplier.get(),classes);
				Try<T,X> result = null;
				try{
					result  =input.flatMap(in -> withCatch(()->catchBlock.apply(in),classes) );
				
				}finally{
					Try finalResult  = result.flatMap(i-> Try.runWithCatch(() ->finallyBlock.accept(inputSupplier.get()),classes));
					if(finalResult instanceof Failure)
						return finalResult;
				
				}
			return result;
		}
		
	}
	
	public static interface Init<X extends Throwable>{
		/**
		 * Initialise a try / catch / finally block
		 * Define the variables to be used within the block.
		 * A Tuple or Iterable can be returned to defined multiple values.
		 * Closeables (either individually or within an iterable) will be closed
		 * via tryWithResources.
		 * 
		 * <pre>
		 * 
		 * Try.catchExceptions(FileNotFoundException.class,IOException.class)
		 *		   .init(()-&gt;new BufferedReader(new FileReader(&quot;file.txt&quot;)))
	     *		   .tryWithResources(this::read);
		 * 
		 * </pre>
		 * 
		 * or
		 * 
		 * <pre>
		 * 
		 * Try t2 = Try.catchExceptions(FileNotFoundException.class,IOException.class)
		 *		   .init(()-&gt;Tuple.tuple(new BufferedReader(new FileReader(&quot;file.txt&quot;)),new FileReader(&quot;hello&quot;)))
		 *		   .tryWithResources(this::read2);
	     * 
	     * private String read2(Tuple2&lt;BufferedReader,FileReader&gt; res) throws IOException{
		 * String line = res.v1.readLine();
		 * 
		 * </pre>
		 * 
		 * @param input Supplier that provides input values to be used in the Try / Catch
		 * @return
		 */
		<V> TryCatch<V,X> init(CheckedSupplier<V,X> input);
		/**
		 * Run the supplied CheckedRunnable and trap any Exceptions
		 * Return type is Void
		 * 
		 * @param input CheckedRunnable
		 * @return Try that traps any errors (no return type)
		 */
		Try<Void,X> run(CheckedRunnable<X> input);
		
		/**
		 * Run the supplied CheckedSupplier and trap the return value or an Exception
		 * inside a Try
		 * 
		 * @param input CheckedSupplier to run
		 * @return new Try
		 */
		<V> Try<V,X> tryThis(CheckedSupplier<V,X> input);
	}
	public static interface TryCatch<V,X extends Throwable> {
		
		/**
		 * Will execute and run the CheckedFunction supplied and will automatically
		 * safely close any Closeables supplied during init (either individually or inside an iterable)
		 * 
		 * @param catchBlock CheckedFunction to Try
		 * @return New Try capturing return data or Exception
		 */
		<T> Try<T,X> tryWithResources(CheckedFunction<V, T, X> catchBlock);
		
		/**
		 * Build another stage in try / catch / finally block
		 * This defines the CheckedFunction that will be run in the main body of the catch block
		 * Next step can define the finally block
		 * 
		 * @param catchBlock To Try
		 * @return Next stage in the fluent step builder (finally block)
		 */
		<T> AndFinally<T,V,X> tryThis(CheckedFunction<V,T,X> catchBlock);
		
	}
	public static interface AndFinally<T,V,X extends Throwable> {
		 
		/**
		 * Define the finally block and execute the Try
		 * 
		 * @param finallyBlock to execute
		 * @return New Try capturing return data or Exception
		 */
		Try<T,X> andFinally(CheckedConsumer<V,X> finallyBlock);
		 /**
		  * Create a finally block that auto-closes any Closeables specified during init
		  *  including those inside an Iterable
		  * 
		 * @return New Try capturing return data or Exception
		 */
		Try<T,X> close();
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
	
	/**
	 * Class that represents a Successful Try
	 * 
	 * @author johnmcclean
	 *
	 * @param <T> Success data type
	 * @param <X> Error data type
	 */
	@RequiredArgsConstructor @ToString @EqualsAndHashCode
	public static class Success<T, X extends Throwable> implements Try<T,X>{
		
		private final T value;
		private final Class<? extends Throwable>[] classes;
		
		@Override
		public ListX<T> unapply() {
			return ListX.of(value);
		}

		
		
		/* 
		 *	@return Current value
		 * @see com.aol.cyclops.trycatch.Try#get()
		 */
		public T get(){
			return value;
		}
		/**
		 * @return This monad wrapped as AnyM
		 */
		public AnyM<T> anyM(){
			return AnyM.fromIterable(this);
		}
		/**
		 * @return This monad, wrapped as AnyM of Failure
		 */
		public AnyM<X> anyMFailure(){
			return AnyM.fromOptional(Optional.empty());
		}
		/**
		 * @return This monad, wrapped as AnyM of Success
		 */
		public AnyM<T> anyMSuccess(){
			return anyM();
		}
		/**
		 * @param value Successful value
		 * @return new Success with value
		 */
		@Deprecated //use Try.success instead
		public static <T,X extends Throwable> Success<T,X> of(T value){
			return new Success<>(value,new Class[0]);
		}
		/**
		 * @param value Successful value
		 * @return new Success with value
		 */
		public static <T,X extends Throwable> AnyM<T> anyMOf(T value,Class<? extends Throwable>[] classes){
			return new Success<>(value,classes).anyM();
		}
		/**
		 * @param value Successful value
		 * @return new Success with value
		 */
		public static <T,X extends Throwable> AnyM<T> anyMOf(T value){
			return new Success<>(value,new Class[0]).anyM();
		}
		/**
		 * @param value Successful value
		 * @return new Success with value
		 */
		public static <T,X extends Throwable> Success<T,X> of(T value,Class<? extends Throwable>[] classes){
			return new Success<>(value,classes);
		}
		
		/* 
		 * @param fn Map success value from T to R.
		 * @return New Try with mapped value
		 * @see com.aol.cyclops.trycatch.Try#map(java.util.function.Function)
		 */
		@Override
		public <R> Try<R,X> map(Function<? super T,? extends R> fn) {
			return safeApply( ()->of(fn.apply(get())));
		}
		
		private <R> R safeApply(Supplier<R> s){
			try{
				return s.get();
			}catch(Throwable t){
				return (R)Failure.of(orThrow(Stream.of(classes).filter(c->c.isAssignableFrom(t.getClass())).map(c->t).findFirst(),t));
				
			}
		}

		private Throwable orThrow(Optional<Throwable> findFirst, Throwable t) {
			if(findFirst.isPresent())
				return findFirst.get();
			ExceptionSoftener.throwSoftenedException(t);
			return null;
		}



		/* 
		 * @param fn FlatMap success value or Do nothing if Failure (return this)
		 * @return Try returned from FlatMap fn
		 * @see com.aol.cyclops.trycatch.Try#flatMap(java.util.function.Function)
		 */
		@Override
		public <R> Try<R,X> flatMap(Function<? super T,? extends Try<R,X>> fn) {
			return safeApply(()-> fn.apply(get()));
			
		}

		/* 
		 * @param p Convert a Success to a Failure (with a null value for Exception) if predicate does not hold.
		 *         
		 * @return this if  Predicate holds, new Failure if not
		 * @see com.aol.cyclops.trycatch.Try#filter(java.util.function.Predicate)
		 */
		@Override
		public Optional<T> filter(Predicate<? super T> p) {
			if(p.test(value))
				return Optional.of(get());
			else
				return Optional.empty();
		}

		/* 
		 * Does nothing (no error to recover from)
		 * @see com.aol.cyclops.trycatch.Try#recover(java.util.function.Function)
		 */
		@Override
		public Success<T,X> recover(Function<? super X,? extends T> fn) {
			return this;
		}

		/* 
		 * Does nothing (no error to recover from)
		 * @see com.aol.cyclops.trycatch.Try#recoverWith(java.util.function.Function)
		 */
		@Override
		public Success<T,X> recoverWith(Function<? super X,? extends Success<T,X>> fn) {
			return this;
		}

		/* 
		 * Does nothing (no error to recover from)
		 * @see com.aol.cyclops.trycatch.Try#recoverFor(java.lang.Class, java.util.function.Function)
		 */
		@Override
		public Success<T,X> recoverFor(Class<? extends X> t, Function<? super X, ? extends T> fn) {
			return this;
		}

		/* 
		 * Does nothing (no error to recover from)
		 * @see com.aol.cyclops.trycatch.Try#recoverWithFor(java.lang.Class, java.util.function.Function)
		 */
		@Override
		public Success<T,X> recoverWithFor(Class<? extends X> t,
				Function<? super X,? extends Success<T,X>> fn) {
			return this;
		}

		/* 
		 * Flatten a nested Try Structure
		 * @return Lowest nested Try
		 * @see com.aol.cyclops.trycatch.Try#flatten()
		 */
		@Override
		public Try<T,X> flatten() {
			if(value instanceof Try)
				return ((Try)value).flatten();
			return this;
		}

		/* 
		 *	
		 *	@return Returns current value (ignores supplied value)
		 * @see com.aol.cyclops.trycatch.Try#orElse(java.lang.Object)
		 */
		@Override
		public T orElse(T value) {
			return get();
		}

		/* 
		 *	@param value (ignored)
		 *	@return Returns current value (ignores Supplier)
		 * @see com.aol.cyclops.trycatch.Try#orElseGet(java.util.function.Supplier)
		 */
		@Override
		public T orElseGet(Supplier<? extends T> value) {
			return get();
		}

		/* 
		 *	@return Optional of current value
		 * @see com.aol.cyclops.trycatch.Try#toOptional()
		 */
		@Override
		public Optional<T> toOptional() {
			return Optional.of(value);
		}

		/* 
		 *	@return Stream of current value
		 * @see com.aol.cyclops.trycatch.Try#toStream()
		 */
		@Override
		public SequenceM<T> stream() {
			return SequenceM.<T>of();
		}

		/* 
		 *	@return true
		 * @see com.aol.cyclops.trycatch.Try#isSuccess()
		 */
		@Override
		public boolean isSuccess() {
			return true;
		}

		/* 
		 *	@return false
		 * @see com.aol.cyclops.trycatch.Try#isFailure()
		 */
		@Override
		public boolean isFailure() {
			return false;
		}

		/* 
		 *	@param consumer to recieve current value
		 * @see com.aol.cyclops.trycatch.Try#foreach(java.util.function.Consumer)
		 */
		@Override
		public void forEach(Consumer<? super T> consumer) {
			consumer.accept(value);
			
		}

		/* 
		 *  does nothing no failure
		 * @see com.aol.cyclops.trycatch.Try#onFail(java.util.function.Consumer)
		 */
		@Override
		public Try<T,X> onFail(Consumer<? super X> consumer) {
			return this;
		}

		/* 
		 *  does nothing no failure
		 * @see com.aol.cyclops.trycatch.Try#onFail(java.lang.Class, java.util.function.Consumer)
		 */
		@Override
		public Try<T, X> onFail(Class<? extends X> t, Consumer<X> consumer) {
			return this;
		}

		/* 
		 *  does nothing no failure
		 * @see com.aol.cyclops.trycatch.Try#throwException()
		 */
		@Override
		public void throwException() {
			
			
		}

		/* 
		 *  @return java.util.Optional#empty()
		 * @see com.aol.cyclops.trycatch.Try#toFailedOptional()
		 */
		@Override
		public Optional<X> toFailedOptional() {
			return Optional.empty();
		}

		/* 
		 *	@return empty Stream
		 * @see com.aol.cyclops.trycatch.Try#toFailedStream()
		 */
		@Override
		public Stream<X> toFailedStream() {
			return Stream.of();
		}

		/* 
		 *	does nothing - no failure
		 * @see com.aol.cyclops.trycatch.Try#foreachFailed(java.util.function.Consumer)
		 */
		@Override
		public void forEachFailed(Consumer<? super X> consumer) {
			
			
		}



		/* (non-Javadoc)
		 * @see com.aol.cyclops.trycatch.Try#when(java.util.function.Function, java.util.function.Function)
		 */
		@Override
		public <R> R visit(Function<? super T, ? extends R> success, Function<? super X, ? extends R> failure) {
			return success.apply(get());
		}
		
	}
	/**
	 * Class that represents the Failure of a Try
	 * 
	 * @author johnmcclean
	 *
	 * @param <T> Value type
	 * @param <X> Error type
	 */
	@RequiredArgsConstructor @ToString @EqualsAndHashCode
	public static class Failure<T,X extends Throwable> implements Try<T,X> {

		@Override
		public ListX<X> unapply() {
			return ListX.of(error);
		}
		private final X error;
		
		/**
		 * @return This monad, wrapped as AnyM of Success
		 */
		public AnyM<T> anyM(){
			return this.anyMSuccess();
		}
		/**
		 * @return This monad, wrapped as AnyM of Failure
		 */
		public AnyM<X> anyMFailure(){
			return AnyM.ofMonad(this);
		}
		/**
		 * @return This monad, wrapped as AnyM of Success
		 */
		public AnyM<T> anyMSuccess(){
			return AnyM.fromOptional(Optional.empty());
		}
		/**
		 * Construct a Failure instance from a throwable
		 * 
		 * @param error for Failure
		 * @return new Failure with error
		 */
		@Deprecated //use Try.failuer instead
		public static <T,X extends Throwable> Failure<T,X> of(X error){
			return new Failure<>(error);
		}
		/**
		 * Construct a Failure instance from a throwable
		 * 
		 * @param error for Failure
		 * @return new Failure with error
		 */
		public static <T,X extends Throwable> AnyM<X> anyMOf(X error){
			return new Failure<>(error).anyMFailure();
		}
		/* 
		 *	@return throws an Exception
		 * @see com.aol.cyclops.trycatch.Try#get()
		 */
		public T get(){
			ExceptionSoftener.throwSoftenedException((Throwable)error);
			return null;
		}

		/* 
		 *	@return this
		 * @see com.aol.cyclops.trycatch.Try#map(java.util.function.Function)
		 */
		@Override
		public <R> Try<R,X> map(Function<? super T,? extends R> fn) {
			return (Failure)this;
		}

		/* 
		 *	@return this
		 * @see com.aol.cyclops.trycatch.Try#flatMap(java.util.function.Function)
		 */
		@Override
		public <R> Try<R,X> flatMap(Function<? super T, ? extends Try<R,X>> fn) {
			return (Try)this;
		}
		
		/* 
		 *	@return Empty optional
		 * @see com.aol.cyclops.trycatch.Try#filter(java.util.function.Predicate)
		 */
		@Override
		public Optional<T> filter(Predicate<? super T> p) {
			return Optional.empty();
		}
		
		/* 
		 * FlatMap recovery function if exception is of specified type
		 * 
		 * @param t Type of exception to match against
		 * @param fn Recovery FlatMap function. Map from a failure to a Success
		 * @return Success from recovery function
		 * @see com.aol.cyclops.trycatch.Try#recoverWithFor(java.lang.Class, java.util.function.Function)
		 */
		@Override
		public Try<T,X> recoverWithFor(Class<? extends X> t,Function<? super X,? extends Success<T,X>> fn){
			if(t.isAssignableFrom(error.getClass()))
				return recoverWith(fn);
			return this;
		}
		
		
		/* 
		 * Recover if exception is of specified type
		 * @param t Type of exception to match against
		 * @param fn Recovery function
		 * @return New Success
		 * @see com.aol.cyclops.trycatch.Try#recoverFor(java.lang.Class, java.util.function.Function)
		 */
		@Override
		public Try<T,X> recoverFor(Class<? extends X> t,Function<? super X, ? extends T> fn){
			if(t.isAssignableFrom(error.getClass()))
				return recover(fn);
			return this;
		}
		
		/* 
		 * @param fn Recovery function - map from a failure to a Success.
		 * @return new Success
		 * @see com.aol.cyclops.trycatch.Try#recover(java.util.function.Function)
		 */
		@Override
		public Success<T,X> recover(Function<? super X,? extends T> fn) {
			return Success.of(fn.apply(error));
		}
		
		/* 
		 * flatMap recovery
		 * 
		 * @param fn Recovery FlatMap function. Map from a failure to a Success
		 * @return Success from recovery function
		 * @see com.aol.cyclops.trycatch.Try#recoverWith(java.util.function.Function)
		 */
		@Override
		public  Success<T,X> recoverWith(Function<? super X,? extends Success<T,X>> fn){
			return fn.apply(error);
		}
		/* 
		 * Flatten a nested Try Structure
		 * @return Lowest nested Try
		 * @see com.aol.cyclops.trycatch.Try#flatten()
		 */
		@Override
		public Try<T,X> flatten() {
			return this;
		}
		/* 
		 *  @param value Return value supplied 
		 * @return  supplied value
		 * @see com.aol.cyclops.trycatch.Try#orElse(java.lang.Object)
		 */
		@Override
		public T orElse(T value) {
			return value;
		}
		/* 
		 * @param value from supplied Supplier 
		 * @return value from supplier
		 * @see com.aol.cyclops.trycatch.Try#orElseGet(java.util.function.Supplier)
		 */
		@Override
		public T orElseGet(Supplier<? extends T> value) {
			return value.get();
		}
		/* 
		 *	@return Optional.empty()
		 * @see com.aol.cyclops.trycatch.Try#toOptional()
		 */
		@Override
		public Optional<T> toOptional() {
			return Optional.empty();
		}
		/* 
		 *	@return empty Stream
		 * @see com.aol.cyclops.trycatch.Try#toStream()
		 */
		@Override
		public SequenceM<T> stream() {
			return SequenceM.<T>of();
		}
		/* 
		 *	@return false
		 * @see com.aol.cyclops.trycatch.Try#isSuccess()
		 */
		@Override
		public boolean isSuccess() {
			return false;
		}
		/*  
		 *	@return true
		 * @see com.aol.cyclops.trycatch.Try#isFailure()
		 */
		@Override
		public boolean isFailure() {
			return true;
		}
		/* 
		 *	does nothing
		 * @see com.aol.cyclops.trycatch.Try#foreach(java.util.function.Consumer)
		 */
		@Override
		public void forEach(Consumer<? super T> consumer) {
			
			
		}
		/* 
		 *	@param consumer is passed error
		 *	@return this
		 * @see com.aol.cyclops.trycatch.Try#onFail(java.util.function.Consumer)
		 */
		@Override
		public Try<T,X> onFail(Consumer<? super X> consumer) {
			consumer.accept(error);
			return this;
		}
		/* 
		 * @param t Class type of match Exception against
		 * @param consumer Accept Exception if present
		 * @return this
		 * @see com.aol.cyclops.trycatch.Try#onFail(java.lang.Class, java.util.function.Consumer)
		 */
		@Override
		public Try<T, X> onFail(Class<? extends X> t, Consumer<X> consumer) {
			if(t.isAssignableFrom(error.getClass()))
				consumer.accept(error);
			return this;
		}
		/* 
		 *	
		 * @see com.aol.cyclops.trycatch.Try#throwException()
		 */
		@Override
		public void throwException() {
			ExceptionSoftener.throwSoftenedException(error);
			
		}
		/* 
		 * @return Optional containing error
		 * @see com.aol.cyclops.trycatch.Try#toFailedOptional()
		 */
		@Override
		public Optional<X> toFailedOptional() {
			
			return Optional.of(error);
		}
		/* 
		 *	@return Stream containing error
		 * @see com.aol.cyclops.trycatch.Try#toFailedStream()
		 */
		@Override
		public Stream<X> toFailedStream() {
			return Stream.of(error);
		}
		/* 
		 * @param consumer that will accept error
		 * @see com.aol.cyclops.trycatch.Try#foreachFailed(java.util.function.Consumer)
		 */
		@Override
		public void forEachFailed(Consumer<? super X> consumer) {
			consumer.accept(error);
			
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.trycatch.Try#when(java.util.function.Function, java.util.function.Function)
		 */
		@Override
		public <R> R visit(Function<? super T, ? extends R> success, Function<? super X, ? extends R> failure) {
			return failure.apply(error);
		}
	}

	
	
}
