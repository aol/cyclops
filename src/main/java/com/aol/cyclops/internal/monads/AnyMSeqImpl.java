package com.aol.cyclops.internal.monads;

import com.aol.cyclops.control.AnyM;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.IterableFunctor;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.util.stream.Streamable;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
@AllArgsConstructor
public class AnyMSeqImpl<T> implements AnyMSeq<T> {
	@Wither
	AnyM anyM;
	

	
	static <T> AnyMSeqImpl<T> with(AnyM<T> anyM){
		return new  AnyMSeqImpl<T>(anyM);
	}
	public static <T> AnyMSeq<T> from(AnyM<T> anyM){
		if(anyM instanceof AnyMSeq)
			return (AnyMSeq)anyM;
		return new AnyMSeqImpl(anyM);
			
	}
	
	private BaseAnyMImpl<T> baseImpl(){
		return (BaseAnyMImpl)anyM;
		
	}
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
	 */
	@Override
	public <U> IterableFunctor<U> unitIterator(Iterator<U> it) {
		return AnyM.fromIterable(()->it);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#emptyUnit()
	 */
	@Override
	public <T> AnyMSeq<T> emptyUnit() {
		return with((AnyMSeq<T>)anyM.emptyUnit());
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceMOptional(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyMValue<T> reduceMOptional(Monoid<Optional<T>> reducer) {
		return anyM.reduceMOptional(reducer);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceMEval(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyMValue<T> reduceMEval(Monoid<Eval<T>> reducer) {
		return anyM.reduceMEval(reducer);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceMMaybe(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyMValue<T> reduceMMaybe(Monoid<Maybe<T>> reducer) {
		return anyM.reduceMMaybe(reducer);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceMXor(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyMValue<T> reduceMXor(Monoid<Xor<?, T>> reducer) {
		return anyM.reduceMXor(reducer);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceMStream(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyMSeq<T> reduceMStream(Monoid<Stream<T>> reducer) {
		return anyM.reduceMStream(reducer);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceMStreamable(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyMSeq<T> reduceMStreamable(Monoid<Streamable<T>> reducer) {
		return anyM.reduceMStreamable(reducer);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceMIterable(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyMSeq<T> reduceMIterable(Monoid<Iterable<T>> reducer) {
		return anyM.reduceMIterable(reducer);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceMCompletableFuture(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyMValue<T> reduceMCompletableFuture(Monoid<CompletableFuture<T>> reducer) {
		return anyM.reduceMCompletableFuture(reducer);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#stream()
	 */
	@Override
	public ReactiveSeq<T> stream() {
	  return asSequence();     
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#unwrap()
	 */
	@Override
	public <R> R unwrap() {
		return (R)anyM.unwrap();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#monad()
	 */
	@Override
	public <X> X monad() {
		return (X)anyM.monad();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#filter(java.util.function.Predicate)
	 */
	@Override
	public AnyMSeq<T> filter(Predicate<? super T> p) {
		return withAnyM(anyM.filter(p));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#map(java.util.function.Function)
	 */
	@Override
	public <R> AnyMSeq<R> map(Function<? super T, ? extends R> fn) {
		return with(anyM.map(fn));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#bind(java.util.function.Function)
	 */
	@Override
	public <R> AnyMSeq<R> bind(Function<? super T, ?> fn) {
		return with(anyM.bind(fn));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#liftAndBind(java.util.function.Function)
	 */
	@Override
	public <R> AnyMSeq<R> liftAndBind(Function<? super T, ?> fn) {
		return with(anyM.liftAndBind(fn));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#flatten()
	 */
	@Override
	public <T1> AnyM<T1> flatten() {
		return with(anyM.flatten());
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#aggregate(com.aol.cyclops.control.AnyM)
	 */
	@Override
	public AnyMSeq<List<T>> aggregate(AnyM<T> next) {
		return with(anyM.aggregate(next));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#forEach2(java.util.function.Function, java.util.function.Function)
	 */
	@Override
	public <R1, R> AnyMSeq<R> forEach2(Function<? super T, ? extends AnyM<R1>> monad,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
		return with(this.baseImpl().forEach2(monad, yieldingFunction));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#forEach2(java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	public <R1, R> AnyMSeq<R> forEach2(Function<? super T, ? extends AnyM<R1>> monad,
			Function<? super T, Function<? super R1, Boolean>> filterFunction,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
		return with(this.baseImpl().forEach2(monad, filterFunction, yieldingFunction));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#forEach3(java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	public <R1, R2, R> AnyMSeq<R> forEach3(Function<? super T, ? extends AnyM<R1>> monad1,
			Function<? super T, Function<? super R1, ? extends AnyM<R2>>> monad2,
			Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
		return with(this.baseImpl().forEach3(monad1, monad2, filterFunction, yieldingFunction));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#forEach3(java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	public <R1, R2, R> AnyMSeq<R> forEach3(Function<? super T, ? extends AnyM<R1>> monad1,
			Function<? super T, Function<? super R1, ? extends AnyM<R2>>> monad2,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
		return with(this.baseImpl().forEach3(monad1, monad2, yieldingFunction));

	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#flatMap(java.util.function.Function)
	 */
	@Override
	public <R> AnyMSeq<R> flatMap(Function<? super T, ? extends AnyM<? extends R>> fn) {
		return with(this.baseImpl().flatMap(fn));

	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#applyM(com.aol.cyclops.control.AnyM)
	 */
	@Override
	public <R> AnyMSeq<R> applyM(AnyM<Function<? super T, ? extends R>> fn) {
		return with(this.baseImpl().applyM(fn));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#toSequence(java.util.function.Function)
	 */
	@Override
	public <NT> ReactiveSeq<NT> toSequence(Function<? super T, ? extends Stream<? extends NT>> fn) {
		return anyM.toSequence(fn);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#toSequence()
	 */
	@Override
	public <T> ReactiveSeq<T> toSequence() {
		return anyM.toSequence();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#asSequence()
	 */
	@Override
	public ReactiveSeq<T> asSequence() {
		return anyM.asSequence();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#unit(java.lang.Object)
	 */
	@Override
	public <T> AnyMSeq<T> unit(T value) {
		return with(anyM.unit(value));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return with(anyM.empty());
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#replicateM(int)
	 */
	@Override
	public AnyMSeq<List<T>> replicateM(int times) {
		return with(anyM.replicateM(times));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.anyM.AnyMSeq#reduceM(com.aol.cyclops.Monoid)
	 */
	@Override
	public AnyM<T> reduceM(Monoid<AnyM<T>> reducer) {
		return  anyM.reduceM(reducer);
	}
	@Override
	public <R, A> R collect(Collector<? super T, A, R> collector) {
		return (R)anyM.collect(collector);
	}
	
}
