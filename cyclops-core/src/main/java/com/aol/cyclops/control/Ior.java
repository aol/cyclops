package com.aol.cyclops.control;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.fluent.FluentFunctions;
import com.aol.cyclops.lambda.monads.BiFunctor;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.value.Value;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 'Right' (or primary type) biased disjunct union.
 *  No 'projections' are provided, swap() and secondaryXXXX alternative methods can be used instead.
 *  
 * 
 * @author johnmcclean
 *
 * @param <ST> Secondary type
 * @param <PT> Primary type
 */
public interface Ior<ST,PT> extends Supplier<PT>,Value<PT>,BiFunctor<ST,PT>{

	public static <ST,PT> Ior<ST,PT> primary(PT primary){
		return new Primary<>(primary);
	}
	public static <ST,PT> Ior<ST,PT> secondary(ST secondary){
		return new Secondary<>(secondary);
	}
	public static <ST,PT> Ior<ST,PT> both(Ior<ST,PT> secondary,Ior<ST,PT> primary){
		return new Both<ST,PT>(secondary,primary);
	}
	default AnyM<PT> anyM(){
		return AnyM.ofMonad(this);
	}
//	Ior<ST,PT> merge(Or<ST,PT> xor);
//	Ior<ListX<ST>,ListX<PT>> append(Or<ListX<ST>,ListX<PT>> xor);
	Xor<ST,PT> toXor(); //drop ST
	Xor<ST,PT> toXorDropPrimary(); //drop ST
	
	Ior<ST,PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn);
	<R> Ior<R,PT> secondaryMap(Function<? super ST, ? extends R> fn);
	<R> Ior<ST,R> map(Function<? super PT, ? extends R> fn);
	
	
	Ior<ST,PT> secondaryPeek(Consumer<? super ST> action);
	Ior<ST,PT> peek(Consumer<? super PT> action);
	
	Ior<PT,ST> swap();
	Optional<Tuple2<ST,PT>> both();
	
	default <R1,R2> Ior<R1,R2>  bimap(Function<? super ST,? extends R1> fn1,Function<? super PT,? extends R2> fn2){
		Eval<Ior<R1,R2>> ptMap = (Eval)Eval.later(()->this.map(fn2)); //force unused secondary to required
		Eval<Ior<R1,R2>> stMap = (Eval)Eval.later(()->this.secondaryMap(fn1)); //force unused primary to required
		if(isPrimary())
			return Ior.<R1,R2>primary(ptMap.get().get());
		if(isSecondary())
			return Ior.<R1,R2>secondary(stMap.get().swap().get());
		
		return Ior.both(stMap.get(),ptMap.get());
	}
	PT get();

	Value<ST> secondaryValue();
	ST secondaryGet();
	Optional<ST> secondaryToOptional();
	SequenceM<ST> secondaryToStream();
	
	
	<LT1,RT1> Ior<LT1,RT1> flatMap(Function<? super PT,? extends Ior<LT1,RT1>> mapper);
	<LT1,RT1> Ior<LT1,RT1> secondaryFlatMap(Function<? super ST,? extends Ior<LT1,RT1>> mapper);
	Ior<ST,PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Ior<ST,PT>> fn);
	
	
	
	public boolean isPrimary();
	public boolean isSecondary();
	public boolean isBoth();
	
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	public static class Primary<ST,PT> implements Ior<ST,PT>{
		private final PT value;
		public Xor<ST,PT> toXor(){
			return Xor.primary(value);
		}
		public Xor<ST,PT> toXorDropPrimary(){
			return Xor.primary(value);
		}
		@Override
		public Ior<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn) {
			return this;
		}

		@Override
		public <R> Ior<R, PT> secondaryMap(Function<? super ST, ? extends R> fn) {
			return (Ior<R,PT>)this;
		}

		@Override
		public <R> Ior<ST, R> map(Function<? super PT, ? extends R> fn) {
			return new Primary<ST,R>(fn.apply(value));
		}

		@Override
		public Ior<ST, PT> secondaryPeek(Consumer<? super ST> action) {
			return this;
		}

		@Override
		public Ior<ST, PT> peek(Consumer<? super PT> action) {
			action.accept(value);
			return this;
		}
		@Override
		public<R1,R2> Ior<R1,R2>  bimap(Function<? super ST,? extends R1> fn1,Function<? super PT,? extends R2> fn2){
			return Ior.<R1,R2>primary(fn2.apply(value));	
		}

		@Override
		public Ior<PT, ST> swap() {
			return new Secondary<PT,ST>(value);
		}

		@Override
		public PT get() {
			return value;
		}
		

		@Override
		public ST secondaryGet() {
			return null;
		}

		@Override
		public Optional<ST> secondaryToOptional() {
			return Optional.empty();
		}

		@Override
		public SequenceM<ST> secondaryToStream() {
			return SequenceM.empty();
		}

		@Override
		public <LT1, RT1> Ior<LT1, RT1> flatMap(Function<? super PT, ? extends Ior<LT1, RT1>> mapper) {
			return mapper.apply(value);
		}

		@Override
		public <LT1, RT1> Ior<LT1, RT1> secondaryFlatMap(Function<? super ST, ? extends Ior<LT1, RT1>> mapper) {
			return (Ior<LT1, RT1>)this;
		}

		@Override
		public Ior<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Ior<ST, PT>> fn) {
			return (Ior<ST, PT>)this;
		}

		@Override
		public Ior<ST,PT> bipeek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction) {
			ptAction.accept(value);
			return this;
		}
		@Override
		public Optional<Tuple2<ST,PT>> both(){
			return Optional.empty();
		}

		@Override
		public boolean isPrimary() {
			return true;
		}

		@Override
		public boolean isSecondary() {
			return false;
		}
		@Override
		public boolean isBoth() {
			return false;
		}
		public Value<ST> secondaryValue(){
			return Value.of(()->null);
		}
		
	}
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	public static class Secondary<ST,PT> implements Ior<ST,PT>{
		private final ST value;
		public boolean isSecondary(){
			return true;
		}
		public boolean isPrimary(){
			return false;
		}
		public Xor<ST,PT> toXor(){
			return Xor.secondary(value);
		}
		public Xor<ST,PT> toXorDropPrimary(){
			return Xor.secondary(value);
		}
		
		@Override
		public Ior<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn) {
			 return new Primary<ST,PT>(fn.apply(value));
		}
		@Override
		public <R> Ior<R, PT> secondaryMap(Function<? super ST, ? extends R> fn) {
			return new Secondary<R,PT>(fn.apply(value));
		}
		@Override
		public <R> Ior<ST, R> map(Function<? super PT, ? extends R> fn) {
			return (Ior<ST,R>)this;
		}
		@Override
		public Ior<ST, PT> secondaryPeek(Consumer<? super ST> action) {
			return secondaryMap((Function)FluentFunctions.expression(action));
		}
		@Override
		public Optional<Tuple2<ST,PT>> both(){
			return Optional.empty();
		}
		@Override
		public Ior<ST, PT> peek(Consumer<? super PT> action) {
			return this;
		}
		@Override
		public <R1,R2> Ior<R1,R2>  bimap(Function<? super ST,? extends R1> fn1,Function<? super PT,? extends R2> fn2){
			return Ior.<R1,R2>secondary(fn1.apply(value));		
		}
		@Override
		public Ior<PT, ST> swap() {
			return new Primary<PT,ST>(value);
		}
		@Override
		public PT get() {
			throw new NoSuchElementException();
		}
		
		@Override
		public ST secondaryGet() {
			return value;
		}
		@Override
		public Optional<ST> secondaryToOptional() {
			return Optional.ofNullable(value);
		}
		@Override
		public SequenceM<ST> secondaryToStream() {
			return SequenceM.fromStream(StreamUtils.optionalToStream(secondaryToOptional()));
		}
		@Override
		public <LT1, RT1> Ior<LT1, RT1> flatMap(Function<? super PT, ? extends Ior<LT1, RT1>> mapper) {
			return (Ior<LT1, RT1>)this;
		}
		@Override
		public <LT1, RT1> Ior<LT1, RT1> secondaryFlatMap(Function<? super ST, ? extends Ior<LT1, RT1>> mapper) {
			return mapper.apply(value);
		}
		@Override
		public Ior<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Ior<ST, PT>> fn) {
			return fn.apply(value);
		}
		@Override
		public Ior<ST,PT> bipeek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction) {
			stAction.accept(value);
			return this;
		}
		public Value<ST> secondaryValue(){
			return Value.of(()->value);
		}
		
		@Override
		public boolean isBoth() {
			return false;
		}
	}
	@AllArgsConstructor(access=AccessLevel.PACKAGE)
	public static class Both<ST,PT> implements Ior<ST,PT>{
		private final Ior<ST,PT> secondary;
		private final Ior<ST,PT> primary;
		
		
		@Override
		public SequenceM<PT> stream() {
			return primary.stream();
		}
		@Override
		public Iterator<PT> iterator() {
			return primary.iterator();
		}
		
		@Override
		public Xor<ST, PT> toXor() {
			return primary.toXor();
		}
		@Override
		public Xor<ST, PT> toXorDropPrimary() {
			return secondary.toXor();
		}
		@Override
		public Ior<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn) {
			return this;
		}
		@Override
		public <R> Ior<R, PT> secondaryMap(Function<? super ST, ? extends R> fn) {
			return Ior.both(secondary.secondaryMap(fn),primary.secondaryMap(fn));
		}
		@Override
		public <R> Ior<ST, R> map(Function<? super PT, ? extends R> fn) {
			return Ior.<ST,R>both(secondary.map(fn),primary.map(fn));
		}
		@Override
		public Ior<ST, PT> secondaryPeek(Consumer<? super ST> action) {
			secondary.secondaryPeek(action);
			return this;
		}
		@Override
		public Ior<ST, PT> peek(Consumer<? super PT> action) {
			primary.peek(action);
			return this;
		}
		@Override
		public Ior<PT, ST> swap() {
			return Ior.both(primary.swap(), secondary.swap());
			
		}
		@Override
		public Optional<Tuple2<ST, PT>> both() {
			return Optional.of(Tuple.tuple(secondary.secondaryGet(),primary.get()));
		}
		@Override
		public PT get() {
			return primary.get();
		}
		@Override
		public Value<ST> secondaryValue() {
			return secondary.secondaryValue();
		}
		@Override
		public ST secondaryGet() {
			return secondary.secondaryGet();
		}
		@Override
		public Optional<ST> secondaryToOptional() {
			return secondary.secondaryToOptional();
		}
		@Override
		public SequenceM<ST> secondaryToStream() {
			return secondary.secondaryToStream();
		}
		@Override
		public <LT1, RT1> Ior<LT1, RT1> flatMap(Function<? super PT, ? extends Ior<LT1, RT1>> mapper) {
			return Ior.both(secondary.flatMap(mapper), primary.flatMap(mapper));
		}
		@Override
		public <LT1, RT1> Ior<LT1, RT1> secondaryFlatMap(Function<? super ST, ? extends Ior<LT1, RT1>> mapper) {
			return Ior.both(secondary.secondaryFlatMap(mapper), primary.secondaryFlatMap(mapper));
		}
		@Override
		public Ior<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Ior<ST, PT>> fn) {
			return Ior.both(secondary.secondaryToPrimayFlatMap(fn), primary.secondaryToPrimayFlatMap(fn));
		}
		@Override
		public Ior<ST,PT> bipeek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction) {
			secondary.secondaryPeek(stAction);
			primary.peek(ptAction);
			return this;
		}
		@Override
		public <R1,R2> Ior<R1,R2>  bimap(Function<? super ST,? extends R1> fn1,Function<? super PT,? extends R2> fn2){
			return Ior.both((Ior)secondary.secondaryMap(fn1),(Ior)primary.map(fn2));
		}
		@Override
		public boolean isPrimary() {
			
			return false;
		}
		@Override
		public boolean isSecondary() {
			
			return false;
		}
		@Override
		public boolean isBoth() {
			return true;
		}
	}
}