package com.aol.cyclops.internal.monads;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.util.Streamable;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
@AllArgsConstructor
public class AnyMValueImpl<T> implements AnyMValue<T> {
	
	@Wither
	AnyM anyM;
	
	public static <T> AnyMValue<T> from(AnyM<T> anyM){
		if(anyM instanceof AnyMSeq)
			return (AnyMValue)anyM;
		return new AnyMValueImpl(anyM);
			
	}
	
	static <T> AnyMValueImpl<T> with(AnyM<T> anyM){
		return new  AnyMValueImpl<T>(anyM);
	}
	
	private BaseAnyMImpl<T> baseImpl(){
		return (BaseAnyMImpl)anyM;
		
	}

	@Override
	public SequenceM<T> sequenceM() {
		return stream();
	}

	@Override
	public T get() {
		return baseImpl().get();
	}

	
	@Override
	public <T> AnyMValue<T> emptyUnit() {
		return with((BaseAnyMImpl)anyM.emptyUnit());
	}

	@Override
	public AnyMValue<T> reduceMOptional(Monoid<Optional<T>> reducer) {
		return with(anyM.reduceMOptional(reducer));
	}

	@Override
	public AnyMValue<T> reduceMEval(Monoid<Eval<T>> reducer) {
		return anyM.reduceMEval(reducer);
	}

	@Override
	public AnyMValue<T> reduceMMaybe(Monoid<Maybe<T>> reducer) {
		return anyM.reduceMMaybe(reducer);
	}

	@Override
	public AnyMValue<T> reduceMXor(Monoid<Xor<?, T>> reducer) {
		return anyM.reduceMXor(reducer);
	}

	@Override
	public AnyMSeq<T> reduceMStream(Monoid<Stream<T>> reducer) {
		return anyM.reduceMStream(reducer);
	}

	@Override
	public AnyMSeq<T> reduceMStreamable(Monoid<Streamable<T>> reducer) {
		return anyM.reduceMStreamable(reducer);
	}

	@Override
	public AnyMSeq<T> reduceMIterable(Monoid<Iterable<T>> reducer) {
		return anyM.reduceMIterable(reducer);
	}

	@Override
	public AnyMValue<T> reduceMCompletableFuture(Monoid<CompletableFuture<T>> reducer) {
		return anyM.reduceMCompletableFuture(reducer);
	}

	@Override
	public AnyMValue<T> filter(Predicate<? super T> p) {
		return withAnyM(anyM.filter(p));
	}

	@Override
	public AnyMValue<T> peek(Consumer<? super T> c) {
		return withAnyM(anyM.peek(c));
	}

	@Override
	public AnyMValue<List<T>> aggregate(AnyM<T> next) {
		return with(anyM.aggregate(next));
	}

	@Override
	public <T> AnyMValue<T> unit(T value) {
		return with(anyM.unit(value));
	}

	@Override
	public <T> AnyMValue<T> empty() {
		return with(anyM.empty());
	}

	@Override
	public AnyMValue<List<T>> replicateM(int times) {
		return with(anyM.replicateM(times));
	}

	@Override
	public AnyMValue<T> reduceM(Monoid<AnyM<T>> reducer) {
		return with(anyM.reduceM(reducer));
	}

	@Override
	public <NT> SequenceM<NT> toSequence(Function<? super T, ? extends Stream<? extends NT>> fn) {
		return anyM.toSequence(fn);
	}

	@Override
	public <T> SequenceM<T> toSequence() {
		return anyM.toSequence();
	}

	@Override
	public Iterator<T> iterator() {
		return anyM.iterator();
	}

	@Override
	public T getMatchable() {
		return get();
	}

	@Override
	public SequenceM<T> stream() {
		return anyM.stream();
	}

	@Override
	public <X> X monad() {
		return (X)anyM.monad();
	}

	@Override
	public <R> AnyMValue<R> map(Function<? super T, ? extends R> fn) {
		return with(anyM.map(fn));
	}

	@Override
	public <R> AnyMValue<R> bind(Function<? super T, ?> fn) {
		return with(anyM.bind(fn));
	}

	@Override
	public <R> AnyMValue<R> liftAndBind(Function<? super T, ?> fn) {
		return with(anyM.liftAndBind(fn));
	}

	@Override
	public <T1> AnyMValue<T1> flatten() {
		return with(anyM.flatten());
	}

	@Override
	public <R1, R> AnyMValue<R> forEach2(Function<? super T, ? extends AnyMValue<R1>> monad,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
		return with(baseImpl().forEach2(monad, yieldingFunction));
	}

	@Override
	public <R1, R> AnyMValue<R> forEach2(Function<? super T, ? extends AnyMValue<R1>> monad,
			Function<? super T, Function<? super R1, Boolean>> filterFunction,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
		return with(baseImpl().forEach2(monad, filterFunction,yieldingFunction));
	}

	@Override
	public <R1, R2, R> AnyMValue<R> forEach3(Function<? super T, ? extends AnyMValue<R1>> monad1,
			Function<? super T, Function<? super R1, ? extends AnyMValue<R2>>> monad2,
			Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
		return with(baseImpl().forEach3((Function)monad1, (Function)monad2,filterFunction, yieldingFunction));
	}

	@Override
	public <R1, R2, R> AnyMValue<R> forEach3(Function<? super T, ? extends AnyMValue<R1>> monad1,
			Function<? super T, Function<? super R1, ? extends AnyMValue<R2>>> monad2,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
		return with(baseImpl().forEach3((Function)monad1, (Function)monad2, yieldingFunction));
	}

	@Override
	public <R> AnyMValue<R> flatMap(Function<? super T, ? extends AnyMValue<? extends R>> fn) {
		return with(baseImpl().flatMap(fn));
	}

	@Override
	public SequenceM<T> asSequence() {
		return anyM.asSequence();
	}

	@Override
	public <R> AnyMValue<R> applyM(AnyMValue<Function<? super T, ? extends R>> fn) {
		return with(baseImpl().applyM(fn));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyM#collect(java.util.stream.Collector)
	 */
	@Override
	public <R, A> R collect(Collector<? super T, A, R> collector) {
		return baseImpl().collect(collector);
	}

}
