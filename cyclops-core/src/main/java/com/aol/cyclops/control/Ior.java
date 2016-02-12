package com.aol.cyclops.control;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.matcher2.Case;
import com.aol.cyclops.internal.matcher2.CheckValues;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.BiFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.types.applicative.Applicative;
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
public interface Ior<ST,PT> extends Supplier<PT>,
									Value<PT>,
									BiFunctor<ST,PT>,
									Functor<PT>,
									Filterable<PT>,
									Applicativable<PT>{

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
	

	default <T> Ior<?,T> unit(T unit){
		return Ior.primary(unit);
	}
	Ior<ST,PT> filter(Predicate<? super PT> test);
	Xor<ST,PT> toXor(); //drop ST
	Xor<ST,PT> toXorDropPrimary(); //drop ST
	
	Ior<ST,PT> secondaryToPrimayMap(Function<? super ST, ? extends PT> fn);
	<R> Ior<R,PT> secondaryMap(Function<? super ST, ? extends R> fn);
	<R> Ior<ST,R> map(Function<? super PT, ? extends R> fn);
	
	
	Ior<ST,PT> secondaryPeek(Consumer<? super ST> action);
	Ior<ST,PT> peek(Consumer<? super PT> action);
	
	Ior<PT,ST> swap();
	Optional<Tuple2<ST,PT>> both();
	default Value<Optional<Tuple2<ST,PT>>> bothValue(){
		return ()->both();
	}
	@Override
	default <R> Xor<ST,R> patternMatch(R defaultValue,
			Function<CheckValues<? super PT, R>, CheckValues<? super PT, R>> case1) {
		
		return (Xor<ST,R>)Applicativable.super.patternMatch(defaultValue, case1);
	}
	default <R1,R2> Ior<R1,R2>  bimap(Function<? super ST,? extends R1> fn1,Function<? super PT,? extends R2> fn2){
		Eval<Ior<R1,R2>> ptMap = (Eval)Eval.later(()->this.map(fn2)); //force unused secondary to required
		Eval<Ior<R1,R2>> stMap = (Eval)Eval.later(()->this.secondaryMap(fn1)); //force unused primary to required
		if(isPrimary())
			return Ior.<R1,R2>primary(ptMap.get().get());
		if(isSecondary())
			return Ior.<R1,R2>secondary(stMap.get().swap().get());
		
		return Ior.both(stMap.get(),ptMap.get());
	}
	default <R1,R2> Ior<R1,R2> visit(Function<? super ST,? extends R1> secondary, 
			Function<? super PT,? extends R2> primary){
		if(isSecondary())
			return (Ior<R1,R2>)swap().map(secondary);
		if(isPrimary())
			return (Ior<R1,R2>)map(primary);
		return bimap(secondary,primary);
	}
	PT get();

	Value<ST> secondaryValue();
	ST secondaryGet();
	Optional<ST> secondaryToOptional();
	ReactiveSeq<ST> secondaryToStream();
	
	
	<LT1,RT1> Ior<LT1,RT1> flatMap(Function<? super PT,? extends Ior<LT1,RT1>> mapper);
	<LT1,RT1> Ior<LT1,RT1> secondaryFlatMap(Function<? super ST,? extends Ior<LT1,RT1>> mapper);
	Ior<ST,PT> secondaryToPrimayFlatMap(Function<? super ST, ? extends Ior<ST,PT>> fn);
	
	
	
	public boolean isPrimary();
	public boolean isSecondary();
	public boolean isBoth();
	
	public static <ST,PT> Ior<ListX<PT>,ListX<ST>> sequenceSecondary(CollectionX<Ior<ST,PT>> iors){
		return AnyM.sequence(AnyM.listFromIor(iors.map(Ior::swap))).unwrap();
	}
	
	public static <ST,PT,R> Ior<?,R> accumulateSecondary(CollectionX<Ior<ST,PT>> iors,Reducer<R> reducer){
		return sequenceSecondary(iors).map(s->s.mapReduce(reducer));
	}
	public static <ST,PT,R> Ior<?,R> accumulateSecondary(CollectionX<Ior<ST,PT>> iors,Function<? super ST, R> mapper,Semigroup<R> reducer){
		return sequenceSecondary(iors).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}
	public static <ST,PT> Ior<ListX<ST>,ListX<PT>> sequencePrimary(CollectionX<Ior<ST,PT>> iors){
		return AnyM.sequence(AnyM.<ST,PT>listFromIor(iors)).unwrap();
	}
	
	public static <ST,PT,R> Ior<?,R> accumulatePrimary(CollectionX<Ior<ST,PT>> iors,Reducer<R> reducer){
		return sequencePrimary(iors).map(s->s.mapReduce(reducer));
	}
	public static <ST,PT,R> Ior<?,R> accumulatePrimary(CollectionX<Ior<ST,PT>> iors,Function<? super PT, R> mapper,Semigroup<R> reducer){
		return sequencePrimary(iors).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#ofType(java.lang.Class)
	 */
	@Override
	default <U> Ior<ST,U> ofType(Class<U> type) {
		
		return (Ior<ST,U>)Filterable.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
	 */
	@Override
	default Ior<ST,PT> filterNot(Predicate<? super PT> fn) {
		
		return (Ior<ST,PT>)Filterable.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	@Override
	default Ior<ST,PT> notNull() {
		
		return (Ior<ST,PT>)Filterable.super.notNull();
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> Ior<ST,U> cast(Class<U> type) {
		
		return (Ior<ST,U>)Applicativable.super.cast(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> Ior<ST,R> trampoline(Function<? super PT, ? extends Trampoline<? extends R>> mapper) {
		
		return (Ior<ST,R>)Applicativable.super.trampoline(mapper);
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bipeek(java.util.function.Consumer, java.util.function.Consumer)
	 */
	@Override
	default Ior<ST, PT> bipeek(Consumer<? super ST> c1, Consumer<? super PT> c2) {
		
		return (Ior<ST,PT>)BiFunctor.super.bipeek(c1, c2);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bicast(java.lang.Class, java.lang.Class)
	 */
	@Override
	default <U1, U2> Ior<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {
		
		return (Ior<U1, U2>)BiFunctor.super.bicast(type1, type2);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bitrampoline(java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R1, R2> Ior<R1, R2> bitrampoline(Function<? super ST, ? extends Trampoline<? extends R1>> mapper1,
			Function<? super PT, ? extends Trampoline<? extends R2>> mapper2) {
		
		return (Ior<R1, R2>)BiFunctor.super.bitrampoline(mapper1, mapper2);
	}
	


	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	@EqualsAndHashCode(of={"value"})
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
		public Ior<ST,PT> filter(Predicate<? super PT> test){
			if(test.test(value))
				return this;
			return Ior.secondary(null);
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
		public ReactiveSeq<ST> secondaryToStream() {
			return ReactiveSeq.empty();
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
		public String toString(){
			return "Ior.primary["+value+"]";
		}
		
		
	}
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	@EqualsAndHashCode(of={"value"})
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
		public Ior<ST,PT> filter(Predicate<? super PT> test){
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
		public ReactiveSeq<ST> secondaryToStream() {
			return ReactiveSeq.fromStream(StreamUtils.optionalToStream(secondaryToOptional()));
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
		public Maybe<PT> toMaybe(){
			return Maybe.none();
		}
		public Optional<PT> toOptional(){
			return Optional.empty();
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.value.Value#unapply()
		 */
		@Override
		public ListX<ST> unapply() {
			return ListX.of(value);
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.objects.Decomposable#unwrap()
		 */
		@Override
		public Object unwrap() {
			return value;
		}
		public String toString(){
			return "Ior.secondary["+value+"]";
		}
	}
	@AllArgsConstructor(access=AccessLevel.PACKAGE)
	@EqualsAndHashCode(of={"secondary","primary"})
	public static class Both<ST,PT> implements Ior<ST,PT>{
		private final Ior<ST,PT> secondary;
		private final Ior<ST,PT> primary;
		
		
		@Override
		public ReactiveSeq<PT> stream() {
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
		public Ior<ST,PT> filter(Predicate<? super PT> test){
			return primary.filter(test);
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
		public ReactiveSeq<ST> secondaryToStream() {
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
		public String toString(){
			return "Ior.both["+primary.toString() + ":" + secondary.toString()+"]";
		}
	}
}