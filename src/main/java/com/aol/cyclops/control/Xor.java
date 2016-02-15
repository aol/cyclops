package com.aol.cyclops.control;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.util.stream.StreamUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

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
public interface Xor<ST,PT> extends Supplier<PT>,Value<PT>,Functor<PT>, Filterable<PT>,Applicativable<PT>{

	public static <ST,PT> Xor<ST,PT> secondary(ST value){
		return new Secondary<>(value);
	}
	public static <ST,PT> Xor<ST,PT> primary(PT value){
		return new Primary<>(value);
	}
	default AnyM<PT> anyM(){
		return AnyM.ofMonad(this);
	}
	
	default <T> Xor<?,T> unit(T unit){
		return Xor.primary(unit);
	}
	
	
	Xor<ST,PT> filter(Predicate<? super PT> test);
	
	Xor<ST,PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn);
	<R> Xor<R,PT> secondaryMap(Function<? super ST, ? extends R> fn);
	<R> Xor<ST,R> map(Function<? super PT, ? extends R> fn);
	
	
	Xor<ST,PT> secondaryPeek(Consumer<? super ST> action);
	Xor<ST,PT> peek(Consumer<? super PT> action);
	
	Xor<PT,ST> swap();
	
	
	public static <ST,PT> Xor<ListX<PT>,ListX<ST>> sequenceSecondary(CollectionX<Xor<ST,PT>> xors){
		return AnyM.sequence(AnyM.listFromXor(xors.map(Xor::swap))).unwrap();
	}
	
	public static <ST,PT,R> Xor<?,R> accumulateSecondary(CollectionX<Xor<ST,PT>> xors,Reducer<R> reducer){
		return sequenceSecondary(xors).map(s->s.mapReduce(reducer));
	}
	public static <ST,PT,R> Xor<?,R> accumulateSecondary(CollectionX<Xor<ST,PT>> xors,Function<? super ST, R> mapper,Semigroup<R> reducer){
		return sequenceSecondary(xors).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}
	public static <ST,PT> Xor<ListX<ST>,ListX<PT>> sequencePrimary(CollectionX<Xor<ST,PT>> xors){
		return AnyM.sequence(AnyM.<ST,PT>listFromXor(xors)).unwrap();
	}
	
	public static <ST,PT,R> Xor<?,R> accumulatePrimary(CollectionX<Xor<ST,PT>> xors,Reducer<R> reducer){
		return sequencePrimary(xors).map(s->s.mapReduce(reducer));
	}
	public static <ST,PT,R> Xor<?,R> accumulatePrimary(CollectionX<Xor<ST,PT>> xors,Function<? super PT, R> mapper,Semigroup<R> reducer){
		return sequencePrimary(xors).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}
	
	/**
	 * 
	 * <pre>
	 * {@code 
	 * Xor.accumulateSecondary(ListX.of(Xor.secondary("failed1"),
													Xor.secondary("failed2"),
													Xor.primary("success")),
													Semigroups.stringConcat)
													
													
	 * //Xors.Primary[failed1failed2]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param xors
	 * @param reducer
	 * @return
	 */
	public static <ST,PT> Xor<?,ST> accumulateSecondary(CollectionX<Xor<ST,PT>> xors,Semigroup<ST> reducer){
			return sequenceSecondary(xors).map(s->s.reduce(reducer.reducer()).get());
	}
	
	default <R1,R2> Xor<R1,R2> visit(Function<? super ST,? extends R1> secondary, 
			Function<? super PT,? extends R2> primary){
		if(isSecondary())
			return (Xor<R1,R2>)swap().map(secondary);
		return (Xor<R1,R2>)map(primary);
	}
	@Override
	default <R> Xor<ST,R> patternMatch(R defaultValue,
			Function<CheckValues<PT, R>, CheckValues<PT, R>> case1) {
		
		return (Xor<ST,R>)Applicativable.super.patternMatch(defaultValue, case1);
	}
	
	
	PT get();

	Value<ST> secondaryValue();
	ST secondaryGet();
	Optional<ST> secondaryToOptional();
	ReactiveSeq<ST> secondaryToStream();
	
	
	<LT1,RT1> Xor<LT1,RT1> flatMap(Function<? super PT,? extends Xor<LT1,RT1>> mapper);
	<LT1,RT1> Xor<LT1,RT1> secondaryFlatMap(Function<? super ST,? extends Xor<LT1,RT1>> mapper);
	Xor<ST,PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Xor<ST,PT>> fn);
	
	void peek(Consumer<? super ST> stAction,Consumer<? super PT> ptAction);
	
	public boolean isPrimary();
	public boolean isSecondary();
	
	
	
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#ofType(java.lang.Class)
	 */
	@Override
	default <U> Xor<ST,U> ofType(Class<U> type) {
		
		return (Xor<ST,U>)Filterable.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
	 */
	@Override
	default Xor<ST,PT> filterNot(Predicate<? super PT> fn) {
		
		return (Xor<ST,PT>)Filterable.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	@Override
	default Xor<ST,PT> notNull() {
		
		return (Xor<ST,PT>)Filterable.super.notNull();
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> Xor<ST,U> cast(Class<U> type) {
		
		return (Xor<ST,U>)Applicativable.super.cast(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> Xor<ST,R> trampoline(Function<? super PT, ? extends Trampoline<? extends R>> mapper) {
		
		return (Xor<ST,R>)Applicativable.super.trampoline(mapper);
	}
	

	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	@EqualsAndHashCode(of={"value"})
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

		public Xor<ST,PT> filter(Predicate<? super PT> test){
			if(test.test(value))
				return this;
			return Xor.secondary(null);
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
		public ReactiveSeq<ST> secondaryToStream() {
			return ReactiveSeq.empty();
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
		public String toString(){
			return "Xor.primary["+value+"]";
		}
		
	}
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	@EqualsAndHashCode(of={"value"})
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
		public Xor<ST,PT> filter(Predicate<? super PT> test){
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
		public ReactiveSeq<ST> secondaryToStream() {
			return ReactiveSeq.fromStream(StreamUtils.optionalToStream(secondaryToOptional()));
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
		
		public Maybe<PT> toMaybe(){
			return Maybe.none();
		}
		public Optional<PT> toOptional(){
			return Optional.empty();
		}
		public Value<ST> secondaryValue(){
			return Value.of(()->value);
		}
		
		public String toString(){
			return "Xor.secondary["+value+"]";
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.value.Value#unapply()
		 */
		@Override
		public ListX<ST> unapply() {
			return	ListX.of(value);
		}
		
		
		
	}
}