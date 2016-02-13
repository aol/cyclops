package com.aol.cyclops.internal;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.internal.stream.SeqUtils;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.mixins.WrappingFilterable;
import com.aol.cyclops.types.mixins.WrappingFunctor;
import com.aol.cyclops.util.stream.Streamable;



/**
 * An interoperability Trait that encapsulates java Monad implementations.
 * 
 * A generalised view into Any Monad (that implements flatMap or bind and accepts any function definition
 * with an arity of 1). Operates as a  Monad Monad (yes two Monads in a row! - or a Monad that encapsulates and operates on Monads).
 * 
 * NB the intended use case is to wrap already existant Monad-like objects from diverse sources, to improve
 * interoperability - it's not intended for use as an interface to be implemented on a Monad class.
 * 
 * @author johnmcclean
 *
 * @param <T>
 * @param <MONAD>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public interface Monad<MONAD,T> extends MonadFunctions<MONAD,T>,WrappingFunctor<T>, WrappingFilterable<T>{
	
	
	public <MONAD,T> Monad<MONAD,T> withMonad(Object invoke);
	
	default <T> Monad<MONAD,T> withFunctor(T functor){
		return withMonad(functor);
	}
	default Object getFunctor(){
		return unwrap();
	}
	/**
	 * Transform the contents of a Monad into a Monad wrapping a Stream e.g.
	 * Turn an <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * 
	 * <pre>{@code
	 * List<List<Integer>> list = monad(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Stream<Integer>,Integer>streamedMonad()
											.grouped(3)
											.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	 * 
	 * }</pre>
	 * 
	 * 
	 * @return A Monad that wraps a Stream
	 */
	default <R,NT> Monad<R,NT> streamedMonad(){
		Stream stream = Stream.of(1);
		 Monad r = this.<Stream,T>withMonad((Stream)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap()));
		 return r.flatMap(e->e);
	}
	/**
	 * Unwrap this Monad into a Stream.
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	default Stream<T> stream(){
		if(unwrap() instanceof Stream)
			return (Stream)unwrap();
		if(unwrap() instanceof Iterable)
			return StreamSupport.stream(((Iterable)unwrap()).spliterator(), false);
		Stream stream = Stream.of(1);
		return (Stream)withMonad((Stream)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap()))
				.unwrap();
		
	}
	
	/**
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * @param times Times values should be repeated within a Stream
	 * @return Stream with values repeated
	 */
	default Monad<Stream<T>,T> cycle(int times){
		
		return fromStream(SeqUtils.cycle(times,Streamable.fromStream(stream())));
		
	}
	@Override
	default WrappingFilterable<T> withFilterable(T filter){
		return withMonad(filter);
	}
	default Object getFilterable(){
		return unwrap();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.Predicate)
	 */
	default   Monad<MONAD,T>  filter(Predicate<? super T> fn){
		return (Monad)WrappingFilterable.super.filter(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	default  <R> Monad<MONAD,R> map(Function<? super T,? extends R> fn){
		return (Monad)WrappingFunctor.super.map(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	default   Monad<MONAD,T>  peek(Consumer<? super T> c) {
		return (Monad)WrappingFunctor.super.peek(c);
	}

	/**
	 * Perform a looser typed flatMap / bind operation
	 * The return type can be another type other than the host type
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	default <R> Monad<MONAD,T> bind(Function<? super T,? extends R> fn){
		return withMonad((MONAD)new ComprehenderSelector().selectComprehender(
				unwrap())
				.executeflatMap(unwrap(), fn));
	
	}
	/**
	 * Perform a bind operation (@see #bind) but also lift the return value into a Monad using configured
	 * MonadicConverters
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	default <MONAD1,R> Monad<MONAD1,R> liftAndBind(Function<? super T,?> fn){
		return withMonad((MONAD)new ComprehenderSelector().selectComprehender(
				unwrap())
				.liftAndFlatMap(unwrap(), fn));
	
	}
	
	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * @return Flattened / joined one level
	 */
	default <T1> Monad<T,T1> flatten(){
		return (Monad)this.flatMap( t->  t instanceof AnyM ?   (MONAD)((AnyM)t).unwrap() : (MONAD)t );
	}
	
	/**
	 * @return This monad coverted to an Optional
	 * 
	 * Streams will be converted into <pre>{@code Optional<List<T>> }</pre>
	 * 
	 */
	default <T> Optional<T> toOptional(){
		Optional stream = Optional.of(1);
		return this.<Optional,T>withMonad((Optional)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap())).unwrap();
		
	}
	
	default <R> Monad<Optional<R>,R> flatMapToOptional(Function<? super MONAD,Optional<? extends R>> fn){
		Optional opt = Optional.of(1);
		return new MonadWrapper(opt.flatMap(i->fn.apply(unwrap())));
	}
	
	default <R> Monad<Stream<R>,R> flatMapToStream(Function<? super MONAD,Stream<? extends R>> fn){
		
		
		Stream stream = Stream.of(1);
		 Monad r = this.<Stream,T>withMonad((Stream)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap()));
		 return r.flatMap(e->e);
		 
	}
	
	default <R> Monad<CompletableFuture<R>,R> flatMapToCompletableFuture(Function<? super MONAD,CompletableFuture<? extends R>> fn){
		CompletableFuture future = CompletableFuture.completedFuture(1);
		return new MonadWrapper<>(future.thenCompose(i->fn.apply(unwrap())));
	}
	

	

	/**
	 * Generate a new instance of the underlying monad with given value
	 * 
	 * @param value  to construct new instance with
	 * @return new instance of underlying Monad
	 */
	default <MONAD,T> MONAD unit(T value) {
		return (MONAD)new ComprehenderSelector().selectComprehender(unwrap()).of(value);
	}
	
	default  T get(){
		Mutable<T> captured = Mutable.of(null);
		new ComprehenderSelector().selectComprehender(unwrap()).resolveForCrossTypeFlatMap(new Comprehender(){

			/* (non-Javadoc)
			 * @see com.aol.cyclops.lambda.api.Comprehender#of(java.lang.Object)
			 */
			@Override
			public Object of(Object o) {
				return captured.set((T)o);
			}

			/* (non-Javadoc)
			 * @see com.aol.cyclops.lambda.api.Comprehender#empty()
			 */
			@Override
			public Object empty() {
				throw new NoSuchElementException();
			}

			@Override
			public Object map(Object t, Function fn) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object flatMap(Object t, Function fn) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Class getTargetClass() {
				// TODO Auto-generated method stub
				return null;
			}
			
		}, unwrap());
		return captured.get();
	}
	
	
	/**
	 * Aggregate the contents of this Monad and the supplied Monad 
	 * 
	 * <pre>{@code 
	 * 
	 * List<Integer> result = monad(Stream.of(1,2,3,4)).<Integer>aggregate(monad(Optional.of(5))).toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
		}</pre>
	 * 
	 * @param next Monad to aggregate content with
	 * @return Aggregated Monad
	 */
	default <R> Monad<MONAD,R> aggregate(Monad<?,?> next){
		Stream concat = Stream.concat(stream(),next.stream() );
		
		return (Monad)withMonad(new ComprehenderSelector().selectComprehender(
				unwrap()).of(fromStream(concat)
						.flatMap(Function.identity())
						.sequence().collect(Collectors.toList())))
						.bind(Function.identity() );
	}
	default <MONAD2,NT>  Monad<MONAD2,NT> monadMap(Function<? super MONAD,? extends NT> fn) {
		return new MonadWrapper<>(fn.apply(unwrap()));
	}
	default Optional<MONAD> monadFilter(Predicate<? super MONAD> p) {
		return p.test(unwrap()) ? Optional.of(unwrap()) : Optional.empty();
	}
	
	default <MONAD2,NT,R extends Monad<MONAD2,NT>> R monadFlatMap(Function<? super MONAD,? extends R> fn) {
		return fn.apply(unwrap());
	}
	/**
	 * flatMap operation
	 * 
	 * @param fn
	 * @return
	 */
	default <R extends MONAD,NT> Monad<R,NT> flatMap(Function<? super T,? extends R> fn) {
		return (Monad)bind(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#unwrap()
	 */
	default   MONAD unwrap(){
		return (MONAD)unwrap();
	}
	
	
	
	
	
	/**
	 * @return AnyM view on a wrapped Monad, with a single typed parameter - which is the datatype
	 * ultimately being handled by the Monad.
	 * 
	 * E.g.
	 * <pre>{@code 
	 * 		Monad<Stream<String>,String> becomes Simplex<String>
	 * }</pre>
	 * To get back to <pre>{@code Stream<String> }</pre> use
	 * 
	 * <pre>{@code
	 *   
	 * 	simplex.<Stream<String>>.monad();  
	 * }</pre>
	 * 
	 */
	public <T> AnyM<T> anyM();
	public <T> AnyMValue<T> anyMValue();
	public <T> AnyMSeq<T> anyMSeq();
	public <T> ReactiveSeq<T>  sequence();
	/**
	 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param o  to wrap
	 * @return Duck typed Monad
	 */
	public static <MONAD,T> Monad<MONAD,T> of(Object o){
		return new MonadWrapper(o);
	}

	default Monad<MONAD,T> empty(){
		return (Monad)new ComprehenderSelector().selectComprehender(
				unwrap()).empty();
	}
	static <T> Monad<Stream<T>,T> fromStream(Stream<T> monad){
		return new MonadWrapper<>(monad);
	}

	

}
