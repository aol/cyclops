package com.aol.cyclops.control;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.fluent.FluentFunctions;
import com.aol.cyclops.lambda.applicative.Applicativable;
import com.aol.cyclops.lambda.applicative.Applicative;
import com.aol.cyclops.lambda.monads.Functor;
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
public interface Xor<ST,PT> extends Supplier<PT>,Value<PT>,Functor<PT>, Applicativable<PT>{

	public static <ST,PT> Xor<ST,PT> secondary(ST value){
		return new Secondary<>(value);
	}
	public static <ST,PT> Xor<ST,PT> primary(PT value){
		return new Primary<>(value);
	}
	default AnyM<PT> anyM(){
		return AnyM.ofMonad(this);
	}
	default <R> Xor<ST,R> ap1( Applicative<PT,R, ?> ap){
		return (Xor<ST,R>)Applicativable.super.ap1(ap);
	}
	
	default <T> Xor<?,T> unit(T unit){
		return Xor.primary(unit);
	}
	
	Xor<ST,PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn);
	<R> Xor<R,PT> secondaryMap(Function<? super ST, ? extends R> fn);
	<R> Xor<ST,R> map(Function<? super PT, ? extends R> fn);
	
	
	Xor<ST,PT> secondaryPeek(Consumer<? super ST> action);
	Xor<ST,PT> peek(Consumer<? super PT> action);
	
	Xor<PT,ST> swap();
	
	
	
	PT get();

	Value<ST> secondaryValue();
	ST secondaryGet();
	Optional<ST> secondaryToOptional();
	SequenceM<ST> secondaryToStream();
	
	
	<LT1,RT1> Xor<LT1,RT1> flatMap(Function<? super PT,? extends Xor<LT1,RT1>> mapper);
	<LT1,RT1> Xor<LT1,RT1> secondaryFlatMap(Function<? super ST,? extends Xor<LT1,RT1>> mapper);
	Xor<ST,PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Xor<ST,PT>> fn);
	
	void peek(Consumer<? super ST> stAction,Consumer<? super PT> ptAction);
	
	public boolean isPrimary();
	public boolean isSecondary();
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	static class Primary<ST,PT> implements Xor<ST,PT>{
		private final PT value;

		@Override
		public Xor<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn) {
			return this;
		}

		@Override
		public <R> Xor<R, PT> secondaryMap(Function<? super ST, ? extends R> fn) {
			return (Xor<R,PT>)this;
		}

		@Override
		public <R> Xor<ST, R> map(Function<? super PT, ? extends R> fn) {
			return new Primary<ST,R>(fn.apply(value));
		}

		@Override
		public Xor<ST, PT> secondaryPeek(Consumer<? super ST> action) {
			return this;
		}

		@Override
		public Xor<ST, PT> peek(Consumer<? super PT> action) {
			action.accept(value);
			return this;
		}

		@Override
		public Xor<PT, ST> swap() {
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
		public <LT1, RT1> Xor<LT1, RT1> flatMap(Function<? super PT, ? extends Xor<LT1, RT1>> mapper) {
			return mapper.apply(value);
		}

		@Override
		public <LT1, RT1> Xor<LT1, RT1> secondaryFlatMap(Function<? super ST, ? extends Xor<LT1, RT1>> mapper) {
			return (Xor<LT1, RT1>)this;
		}

		@Override
		public Xor<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Xor<ST, PT>> fn) {
			return (Xor<ST, PT>)this;
		}

		@Override
		public void peek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction) {
			ptAction.accept(value);
		}

		@Override
		public boolean isPrimary() {
			return true;
		}

		@Override
		public boolean isSecondary() {
			return false;
		}
		public Value<ST> secondaryValue(){
			return Value.of(()->null);
		}
		
	}
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	static class Secondary<ST,PT> implements Xor<ST,PT>{
		private final ST value;
		public boolean isSecondary(){
			return true;
		}
		public boolean isPrimary(){
			return false;
		}
		
		
		@Override
		public Xor<ST, PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn) {
			 return new Primary<ST,PT>(fn.apply(value));
		}
		@Override
		public <R> Xor<R, PT> secondaryMap(Function<? super ST, ? extends R> fn) {
			return new Secondary<R,PT>(fn.apply(value));
		}
		@Override
		public <R> Xor<ST, R> map(Function<? super PT, ? extends R> fn) {
			return (Xor<ST,R>)this;
		}
		@Override
		public Xor<ST, PT> secondaryPeek(Consumer<? super ST> action) {
			return secondaryMap((Function)FluentFunctions.expression(action));
		}
		@Override
		public Xor<ST, PT> peek(Consumer<? super PT> action) {
			return this;
		}
		@Override
		public Xor<PT, ST> swap() {
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
		public <LT1, RT1> Xor<LT1, RT1> flatMap(Function<? super PT, ? extends Xor<LT1, RT1>> mapper) {
			return (Xor<LT1, RT1>)this;
		}
		@Override
		public <LT1, RT1> Xor<LT1, RT1> secondaryFlatMap(Function<? super ST, ? extends Xor<LT1, RT1>> mapper) {
			return mapper.apply(value);
		}
		@Override
		public Xor<ST, PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Xor<ST, PT>> fn) {
			return fn.apply(value);
		}
		@Override
		public void peek(Consumer<? super ST> stAction, Consumer<? super PT> ptAction) {
			stAction.accept(value);
			
		}
		public Value<ST> secondaryValue(){
			return Value.of(()->value);
		}
		
	}
}