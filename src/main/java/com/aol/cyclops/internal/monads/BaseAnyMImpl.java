package com.aol.cyclops.internal.monads;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.util.stream.Streamable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 
 * Wrapper for Any Monad type
 * @see AnyMonads companion class for static helper methods
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@AllArgsConstructor(access=AccessLevel.PROTECTED)
public class BaseAnyMImpl<T> implements AnyM<T>{
	
	private final Monad<Object,T> monad;
	private final Class initialType;
	
	public final <R> R unwrap(){
		return (R)new ComprehenderSelector().selectComprehender(initialType).unwrap(monad.unwrap());
	}
	
	
	
	/**
	 * Collect the contents of the monad wrapped by this AnyM into supplied collector
	 */
	@Override
	public final <R, A> R collect(Collector<? super T, A, R> collector){
		Object o = this.unwrap();
		if(o instanceof Stream){
			return (R)((Stream)o).collect(collector);
		}
		else{
			return this.<T>toSequence().collect(collector);
		}
	}

	public final Monad monad(){
		return (Monad)monad;
	}
	
	public final   AnyM<T>  filter(Predicate<? super T> fn){
		return monad.filter(fn).anyM();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	public final  <R> AnyM<R> map(Function<? super T,? extends R> fn){
		return monad.map(fn).anyM();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	public final   AnyM<T>  peek(Consumer<? super T> c) {
		return monad.peek(c).anyM();
	}
	
	
	/**
	 * Perform a looser typed flatMap / bind operation
	 * The return type can be another type other than the host type
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	*/
	public final <R> AnyM<R> bind(Function<? super T,?> fn){
		return monad.bind(fn).anyM();
	
	} 
	/**
	 * Perform a bind operation (@see #bind) but also lift the return value into a Monad using configured
	 * MonadicConverters
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	public final <R> AnyM<R> liftAndBind(Function<? super T,?> fn){
		return monad.liftAndBind(fn).anyM();
	
	}
	
	
	/**
	 * Perform a flatMap operation where the result will be a flattened stream of Characters
	 * from the CharSequence returned by the supplied function.
	 * 
	 * <pre>
	 * {@code
	 * List<Character> result = anyM("input.file")
								.liftAndBindCharSequence(i->"hello world")
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	 * 
	 * }</pre>
	 * 
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	public final  AnyM<Character> flatMapCharSequence(Function<? super T,CharSequence> fn) {
		try{
			return monad.liftAndBind(fn).anyM().map(this::takeFirst);
		}catch(GotoAsEmpty e){
			return empty();
		}
	}
	private static class GotoAsEmpty extends RuntimeException{

		@Override
		public synchronized Throwable fillInStackTrace() {
			return null;
		}
		
	}
	private <T> T takeFirst(Object o){
		if(o instanceof MaterializedList){
			if(((List)o).size()==0)
				throw new GotoAsEmpty();
			return (T)((List)o).get(0);
		}
		return (T)o;
	}
	
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied files.
	 * 
	 * <pre>
	 * {@code
	 * 		List<String> result = anyM("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.liftAndBindFile(File::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	public final  AnyM<String> flatMapFile(Function<? super T,File> fn) {
		try{
			return monad.liftAndBind(fn).anyM().map(this::takeFirst);
		}catch(GotoAsEmpty e){
			return empty();
		}
	}
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied URLs 
	 * <pre>
	 * {@code 
	 * List<String> result = anyM("input.file")
								.liftAndBindURL(getClass().getClassLoader()::getResource)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	public final  AnyM<String> flatMapURL(Function<? super T, URL> fn) {
		try{
			return monad.liftAndBind(fn).anyM().map(this::takeFirst);
		}catch(GotoAsEmpty e){
			return empty();
		}
	}
	/**
	  *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied BufferedReaders
	 * 
	 * <pre>
	 * {@code
	 * List<String> result = anyM("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								.liftAndBindBufferedReader(BufferedReader::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * 
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	public final  AnyM<String> flatMapBufferedReader(Function<? super T,BufferedReader> fn) {
		try{
			return monad.liftAndBind(fn).anyM().map(this::takeFirst);
		}catch(GotoAsEmpty e){
			return empty();
		}
	}
	
	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * @return Flattened / joined one level
	 */
	public final <T1> AnyM<T1> flatten(){
		return monad.flatten().anyM();
		
	}
	
	/**
	 * Aggregate the contents of this Monad and the supplied Monad 
	 * 
	 * <pre>{@code 
	 * 
	 * List<Integer> result = anyM(Stream.of(1,2,3,4))
	 * 							.aggregate(anyM(Optional.of(5)))
	 * 							.asSequence()
	 * 							.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
		}</pre>
	 * 
	 * @param next Monad to aggregate content with
	 * @return Aggregated Monad
	 */
	public final  AnyM<List<T>> aggregate(AnyM<T> next){
		return unit(Stream.concat(stream(), next.stream()).collect(Collectors.toList()));
		
	}
	
	
	public void forEach(Consumer<? super T> action) {
		asSequence().forEach(action);	
	}
	
	/**
	 * flatMap operation
	 * 
	 * @param fn
	 * @return 
	 */
	public final <R> AnyM<R> flatMap(Function<? super T,? extends AnyM<? extends R>> fn) {
		try{
			return monad.flatMap(in -> fn.apply(in).unwrap()).anyM().map(this::takeFirst);
		}catch(GotoAsEmpty e){
			return empty();
		}
	}
	

	
	
	/**
	 * Sequence the contents of a Monad.  e.g.
	 * Turn an <pre>
	 * 	{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * 
	 * <pre>{@code
	 * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Integer>toSequence(c->c.stream())
											.collect(Collectors.toList());
		
		
		assertThat(list,hasItems(1,2,3,4,5,6));
		
	 * 
	 * }</pre>
	 * 
	 * @return A Sequence that wraps a Stream
	 */
	public final <NT> ReactiveSeq<NT> toSequence(Function<? super T,? extends Stream<? extends NT>> fn){
		return monad.flatMapToStream((Function)fn)
					.sequence();
	}
	/**
	 *  <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * Less type safe equivalent, but may be more accessible than toSequence(fn) i.e. 
	 * <pre>
	 * {@code 
	 *    toSequence(Function<T,Stream<NT>> fn)
	 *   }
	 *   </pre>
	 *  <pre>{@code
	 * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Integer>toSequence()
											.collect(Collectors.toList());
		
		
		
	 * 
	 * }</pre>
	
	 * @return A Sequence that wraps a Stream
	 */
	public final <T> ReactiveSeq<T> toSequence(){
		return monad.streamedMonad().sequence();
	}
	
	
	/**
	 * Wrap this Monad's contents as a Sequence without disaggreating it. .e.
	 *  <pre>{@code Optional<List<Integer>>  into Stream<List<Integer>> }</pre>
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	public final ReactiveSeq<T> asSequence(){
		return monad.sequence();
		
	}
	
	
		
	

	
	public final <R> AnyM<R> applyM(AnyM<Function<? super T,? extends R>> fn){
		return monad.applyM(fn.monad()).anyM();
		
	}
		
	
	//	filterM((a: Int) => List(a > 2, a % 2 == 0), List(1, 2, 3), ListMonad),
	//List(List(3), Nil, List(2, 3), List(2), List(3),
	//	  Nil, List(2, 3), List(2))												
	
	public <T> AnyM<T> unit(T value){
		return AnyM.ofMonad(monad.unit(value));
	}
	public <T> AnyM<T> empty(){
		return (BaseAnyMImpl)unit(null).filter(t->false);
	}
	
	public final AnyM<List<T>> replicateM(int times){
		
		return monad.replicateM(times).anyM();		
	}
	@Override
	public final   AnyMValue<T> reduceMOptional(Monoid<Optional<T>> reducer){
		return new AnyMValueImpl<>(monad.reduceM(reducer).anyM());
	}
	@Override
	public final AnyMValue<T> reduceMMaybe(Monoid<Maybe<T>> reducer){
		return new AnyMValueImpl<>(monad.reduceM(reducer).anyM());
	}
	@Override
	public final   AnyMValue<T> reduceMXor(Monoid<Xor<?,T>> reducer){
		return new AnyMValueImpl<>(monad.reduceM(reducer).anyM());
	}
	
	@Override 
	public final AnyMValue<T> reduceMEval(Monoid<Eval<T>> reducer){
	
		return new AnyMValueImpl<>(monad.reduceM(reducer).anyM());
	}
	
	@Override
	public final   AnyMSeq<T> reduceMStream(Monoid<Stream<T>> reducer){
		return new AnyMSeqImpl<>(monad.reduceM(reducer).anyM());
	}
	@Override
	public final   AnyMSeq<T> reduceMStreamable(Monoid<Streamable<T>> reducer){
		return new AnyMSeqImpl<>(monad.reduceM(reducer).anyM());
	}
	@Override
	public final   AnyMSeq<T> reduceMIterable(Monoid<Iterable<T>> reducer){
		return new AnyMSeqImpl<>(monad.reduceM(reducer).anyM());
	}
	@Override
	public final   AnyMValue<T> reduceMCompletableFuture(Monoid<CompletableFuture<T>> reducer){
		return new AnyMValueImpl<>(monad.reduceM(reducer).anyM());
	}
	
	public final   AnyM<T> reduceM(Monoid<AnyM<T>> reducer){
	//	List(2, 8, 3, 1).foldLeftM(0) {binSmalls} -> Optional(14)
	//	convert to list Optionals
		
		
	
		return monad.reduceM(Monoid.of(reducer.zero().unwrap(), (a,b)-> reducer.combiner().apply(AnyM.ofMonad(a), 
				AnyM.ofMonad(b)))).anyM();		
	}
	
	public ReactiveSeq<T> stream(){
		if(this.monad.unwrap() instanceof Stream){
			return asSequence();
		}
		return this.<T>toSequence();
	}
	
	
	@Override
    public String toString() {
        return String.format("AnyM(%s)", monad );
    }
	
	public List<T> toList() {
		if(this.monad.unwrap() instanceof Stream){
			return asSequence().toList();
		}
		return this.<T>toSequence().toList();
	}
	
	public Set<T> toSet() {
		if(this.monad.unwrap() instanceof Stream){
			return asSequence().toSet();
		}
		return this.<T>toSequence().toSet();
	}
	
	
	
	public <R1, R> AnyM<R> forEach2(Function<? super T, ? extends AnyM<R1>> monad, Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
		if(AnyMForComprehensionFactory.instance==null){
			System.err.println("ERROR : Unable to use AnyM for-comprehensions without cyclops-for-comprehensions on the classpath");
		}
		return AnyMForComprehensionFactory.instance.forEach2(this, monad, yieldingFunction);
	}
	
	public <R1,R> AnyM<R> forEach2(Function<? super T,? extends AnyM<R1>> monad, 
			Function<? super T, Function<? super R1, Boolean>> filterFunction,
					Function<? super T,Function<? super R1,? extends R>> yieldingFunction ) {
		if(AnyMForComprehensionFactory.instance==null){
			System.err.println("ERROR : Unable to use AnyM for-comprehensions without cyclops-for-comprehensions on the classpath");
		}
		return AnyMForComprehensionFactory.instance.forEach2(this, monad, filterFunction,yieldingFunction);
	}
	
	public <R1, R2, R> AnyM<R> forEach3( Function<? super T, ? extends AnyM<R1>> monad1, 	
			Function<? super T,Function<? super R1,? extends AnyM<R2>>> monad2,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction){
		if(AnyMForComprehensionFactory.instance==null){
			System.err.println("ERROR : Unable to use AnyM for-comprehensions without cyclops-for-comprehensions on the classpath");
		}
		return AnyMForComprehensionFactory.instance.forEach3(this, monad1,monad2,yieldingFunction);
	}
	
	public  <R1, R2, R> AnyM<R> forEach3(Function<? super T, ? extends AnyM<R1>> monad1, 	
			Function<? super T,Function<? super R1,? extends AnyM<R2>>> monad2,
	Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction, 
		Function<? super T, Function<? super R1, Function<? super R2,? extends R>>> yieldingFunction){
		if(AnyMForComprehensionFactory.instance==null){
			System.err.println("ERROR : Unable to use AnyM for-comprehensions without cyclops-for-comprehensions on the classpath");
		}
		return AnyMForComprehensionFactory.instance.forEach3(this, monad1,monad2,filterFunction,yieldingFunction);
		
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Unit#emptyUnit()
	 */
	@Override
	public <T> AnyM<T> emptyUnit() {
		return new BaseAnyMImpl(monad.empty(),initialType);
	}



	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.IterableFunctor#unitIterator(java.util.Iterator)
	 */
	
	public <U> AnyM<U> unitIterator(Iterator<U> it) {
		return AnyM.fromIterable(()->it);
	}



	public T get() {
		return monad.get();
	}



	
	
}