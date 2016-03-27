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
import com.aol.cyclops.internal.matcher2.MatchableCase;
import com.aol.cyclops.internal.matcher2.MatchingInstance;
import com.aol.cyclops.internal.matcher2.PatternMatcher;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.util.stream.StreamUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * eXclusive Or (Xor)
 * 
 * 'Right' (or primary type) biased disjunct union.
 *  No 'projections' are provided, swap() and secondaryXXXX alternative methods can be used instead.
 *  
 *  <pre>
 *  {@code 
 *      Xor.primary("hello").map(v->v+" world") 
 *      //Xor.primary["hello world"]
 *  }
 *  </pre>
 *  
 *   Values can be accumulated via 
 *  <pre>
 *  {@code 
 *  Xor.accumulateSecondary(ListX.of(Xor.secondary("failed1"),
                                                    Xor.secondary("failed2"),
                                                    Xor.primary("success")),
                                                    Semigroups.stringConcat)
 *  
 *  //failed1failed2
 *  
 *   Xor<String,String> fail1 = Xor.secondary("failed1");
     fail1.swap().ap((a,b)->a+b)
                 .ap(Xor.secondary("failed2").swap())
                 .ap(Xor.<String,String>primary("success").swap())
 *  
 *  //failed1failed2
 *  }
 *  </pre>
 * 
 * @author johnmcclean
 *
 * @param <ST> Secondary type
 * @param <PT> Primary type
 */
public interface Xor<ST,PT> extends Supplier<PT>,Value<PT>,Functor<PT>, Filterable<PT>,Applicativable<PT>{

	/**
	 * Create an instance of the secondary type. Most methods are biased to the primary type,
	 * so you will need to use swap() or secondaryXXXX to manipulate the wrapped value
	 * 
	 * <pre>
	 * {@code 
	 *   Xor.<Integer,Integer>secondary(10).map(i->i+1);
	 *   //Xor.secondary[10]
	 *    
	 *    Xor.<Integer,Integer>secondary(10).swap().map(i->i+1);
	 *    //Xor.primary[11]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param value to wrap
	 * @return Secondary instance of Xor
	 */
	public static <ST,PT> Xor<ST,PT> secondary(ST value){
		return new Secondary<>(value);
	}
	public static <ST,PT> Xor<ST,PT> primary(PT value){
		return new Primary<>(value);
	}
	default AnyMValue<PT> anyM(){
		return AnyM.ofValue(this);
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
	@Override
    Ior<ST,PT> toIor();
	
	@Override
    default Xor<ST,PT> toXor(){
        return this;
    }
   
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
	public static <ST,PT> Xor<?,PT> accumulatePrimary(CollectionX<Xor<ST,PT>> xors,Semigroup<PT> reducer){
        return sequencePrimary(xors).map(s->s.reduce(reducer.reducer()).get());
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
	<R> R visit(Function<? super ST,? extends R> secondary, 
            Function<? super PT,? extends R> primary);
	
	default <R1,R2> Xor<R1,R2> visitXor(Function<? super ST,? extends R1> secondary, 
			Function<? super PT,? extends R2> primary){
		if(isSecondary())
			return (Xor<R1,R2>)swap().map(secondary).swap();
		return (Xor<R1,R2>)map(primary);
	}
	@Override
	default <R> Xor<ST,R> patternMatch(
			Function<CheckValues<PT, R>, CheckValues<PT, R>> case1,Supplier<? extends R> otherwise) {
		
		return (Xor<ST,R>)Applicativable.super.patternMatch(case1,otherwise);
	}
	<R> Eval<R>  matches(Function<CheckValue1<ST,R>,CheckValue1<ST,R>> fn1,Function<CheckValue1<PT,R>,CheckValue1<PT,R>> fn2,Supplier<? extends R> otherwise);
        
	
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
			return mkString();
		}
		public String mkString(){
            return "Xor.primary["+value+"]";
        }

        @Override
        public Ior<ST, PT> toIor() {
           return Ior.primary(value);
        }
        @Override
        public <R> R visit(Function<? super ST,? extends R> secondary, 
                Function<? super PT,? extends R> primary){
            return primary.apply(value);
        }
        @Override
        public <R> Eval<R> matches(
                Function<com.aol.cyclops.control.Matchable.CheckValue1<ST, R>, com.aol.cyclops.control.Matchable.CheckValue1<ST, R>> secondary,
                Function<com.aol.cyclops.control.Matchable.CheckValue1<PT, R>, com.aol.cyclops.control.Matchable.CheckValue1<PT, R>> fn1,
                Supplier<? extends R> s) {
            return  Eval.later(()->(R)new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue1)
                    new MatchableCase(new PatternMatcher()).withType1(getMatchable().getClass())).getPatternMatcher()))
                        .match(getMatchable()).orElseGet(s));
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
        public <R> Eval<R> matches(
                Function<com.aol.cyclops.control.Matchable.CheckValue1<ST, R>, com.aol.cyclops.control.Matchable.CheckValue1<ST, R>> fn1,
                Function<com.aol.cyclops.control.Matchable.CheckValue1<PT, R>, com.aol.cyclops.control.Matchable.CheckValue1<PT, R>> primary,
                Supplier<? extends R> s) {
            return  Eval.later(()->(R)new MatchingInstance(new MatchableCase( fn1.apply( (CheckValue1)
                    new MatchableCase(new PatternMatcher()).withType1(value.getClass())).getPatternMatcher()))
                        .match(value).orElseGet(s));
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
		@Override
        public <R> R visit(Function<? super ST,? extends R> secondary, 
                Function<? super PT,? extends R> primary){
            return secondary.apply(value);
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
            return mkString();
        }
		public String mkString(){
			return "Xor.secondary["+value+"]";
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.value.Value#unapply()
		 */
		@Override
		public ListX<ST> unapply() {
			return	ListX.of(value);
		}
		
        @Override
        public Ior<ST, PT> toIor() {
            return Ior.secondary(value);
        }

        
		
	}
}