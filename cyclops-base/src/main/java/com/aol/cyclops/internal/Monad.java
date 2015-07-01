package com.aol.cyclops.internal;

import static com.aol.cyclops.internal.AsGenericMonad.asMonad;
import static com.aol.cyclops.internal.AsGenericMonad.monad;










import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.ComprehenderSelector;
import com.aol.cyclops.lambda.monads.Filterable;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.SequenceM;
import com.aol.cyclops.lambda.monads.StreamBasedFunctions;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.streams.Pair;
import com.nurkiewicz.lazyseq.LazySeq;



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
public interface Monad<MONAD,T> extends MonadFunctions<MONAD,T>,StreamBasedFunctions<MONAD,T>,Functor<T>, Filterable<T>{
	
	
	public <MONAD,T> Monad<MONAD,T> withMonad(Object invoke);
	//public Object unwrap();
	default Monad<Stream<T>,T> cycle(int times){
		return StreamBasedFunctions.super.cycle(times);
	}
	default <T> Monad<MONAD,T> withFunctor(T functor){
		return withMonad(functor);
	}
	default Object getFunctor(){
		return unwrap();
	}
	default Stream<T> stream(){
		return StreamBasedFunctions.super.stream();
	}
	@Override
	default Filterable<T> withFilterable(T filter){
		return withMonad(filter);
	}
	default Object getFilterable(){
		return unwrap();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.Predicate)
	 */
	default   Monad<MONAD,T>  filter(Predicate<? super T> fn){
		return (Monad)Filterable.super.filter(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	default  <R> Monad<MONAD,R> map(Function<? super T,? extends R> fn){
		return (Monad)Functor.super.map(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	default   Monad<MONAD,T>  peek(Consumer<? super T> c) {
		return (Monad)Functor.super.peek(c);
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
		return (Monad)this.flatMap( t->   (MONAD)t );
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
		return monad(opt.flatMap(i->fn.apply(unwrap())));
	}
	
	default <R> Monad<Stream<R>,R> flatMapToStream(Function<? super MONAD,Stream<? extends R>> fn){
		//Stream stream = Stream.of(1);
	//	List<Stream>
	//	Stream<List<Integer>>
	//	System.out.println(unwrap());
	//	return monad(stream.flatMap(i->fn.apply(unwrap())));
		
		Stream stream = Stream.of(1);
		 Monad r = this.<Stream,T>withMonad((Stream)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap()));
		 return r.flatMap(e->e);
		 /**
		Stream stream = Stream.of(1);
		 Monad r = this.<Stream,T>withMonad((Stream)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> { return bind((Function)fn)
														.peek(System.out::println).unwrap();}));
		 return r.flatMap(e->e);**/
	}
	
	default <R> Monad<CompletableFuture<R>,R> flatMapToCompletableFuture(Function<? super MONAD,CompletableFuture<? extends R>> fn){
		CompletableFuture future = CompletableFuture.completedFuture(1);
		return monad(future.thenCompose(i->fn.apply(unwrap())));
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
		Stream concat = StreamUtils.concat(stream(),next.stream() );
		
		return (Monad)withMonad(new ComprehenderSelector().selectComprehender(
				unwrap()).of(monad(concat)
						.flatMap(Function.identity())
						.toList()))
						.bind(Function.identity() );
	}
	default <MONAD2,NT>  Monad<MONAD2,NT> monadMap(Function<? super MONAD,? extends NT> fn) {
		return asMonad(fn.apply(unwrap()));
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
	public <X> AnyM<X> anyM();
	public <X> SequenceM<X>  sequence();
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
		return AsGenericMonad.asMonad(o);
	}

}
