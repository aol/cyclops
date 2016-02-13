package com.aol.cyclops;

import java.math.BigInteger;
import java.util.Optional;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.control.ReactiveSeq;

public interface Semigroups {

	static <T, C extends FluentCollectionX<T>> Semigroup<C> combineFluentCollection() {
		return () -> (a, b) -> (C) a.plusAll(b);
	}

	static <T> Semigroup<ReactiveSeq<T>> combineSequenceM() {
		return () -> (a, b) -> a.appendStream(b);
	}

	static <T> Semigroup<Maybe<T>> firstPresentMaybe() {
		return () -> (a, b) -> a.isPresent() ? a : b;
	}

	static <T> Semigroup<Optional<T>> firstPresentOptional() {
		return () -> (a, b) -> a.isPresent() ? a : b;
	}

	static <T> Semigroup<Maybe<T>> lastPresentMaybe() {
		return () -> (a, b) -> b.isPresent() ? b : a;
	}

	static <T> Semigroup<Optional<T>> lastPresentOptional() {
		return () -> (a, b) -> b.isPresent() ? b : a;
	}
	
	static Semigroup<String> stringJoin(String joiner) {
		return () -> (a, b) -> a + joiner + b;
	}

	static Semigroup<StringBuilder> stringBuilderJoin(String joiner) {
		return () -> (a, b) -> a.append(joiner).append(b);
	}

	static Semigroup<StringBuffer> stringBufferJoin(String joiner) {
		return () -> (a, b) -> a.append(joiner).append(b);
	}
	static <T, T2 extends Comparable<T>> Semigroup<T2> minComparable(){
		return ()->(a,b) ->a.compareTo((T)b) > 0 ? b:a;
	}
	static <T, T2 extends Comparable<T>> Semigroup<T2> maxComparable(){
		return ()->(a,b) ->a.compareTo((T)b) > 0 ? a:b;
	}

	static Semigroup<BigInteger> bigIntCount = () -> (a, b) -> a.add(BigInteger.ONE));
	static Semigroup<Integer> intCount = () -> (a, b) -> a + 1;
	static Semigroup<Long> longCount = () -> (a, b) -> a + 1;
	static Semigroup<Double> doubleCount = () -> (a, b) -> a + 1;
	static Semigroup<Integer> intSum = () -> (a, b) -> a + b;
	static Semigroup<Long> longSum = () -> (a, b) -> a + b;
	static Semigroup<Double> doubleSum = () -> (a, b) -> a + b;
	static Semigroup<BigInteger> bigIntSum = () -> (a, b) -> a.add(b);
	static Semigroup<Integer> intMult = () -> (a, b) -> a * b;
	static Semigroup<Long> longMult = () -> (a, b) -> a * b;
	static Semigroup<Double> doubleMult = () -> (a, b) -> a * b;
	static Semigroup<BigInteger> bigIntMult = () -> (a, b) -> a.multiply(b);
	static Semigroup<Integer> intMax = () -> (a, b) -> b > a ? b : a;
	static Semigroup<Long> longMax = () -> (a, b) -> b > a ? b : a;
	static Semigroup<Double> doubleMax = () -> (a, b) -> b > a ? b : a;
	static Semigroup<BigInteger> bigIntMax = () -> (a, b) -> a.max(b);
	static Semigroup<Integer> intMin = () -> (a, b) -> a < b ? a : b;
	static Semigroup<Long> longMin = () -> (a, b) -> a < b ? a : b;
	static Semigroup<Double> doubleMin = () -> (a, b) -> a < b ? a : b;
	static Semigroup<BigInteger> bigIntMin = () -> (a, b) -> a.min(b);
	static Semigroup<String> stringConcat = () -> (a, b) -> a + b;
	static Semigroup<StringBuffer> stringBufferConcat = () -> (a, b) -> a.append(b);
	static Semigroup<StringBuilder> stringBuilderConcat = () -> (a, b) -> a.append(b);
	static Semigroup<Boolean> booleanDisjunction = () -> (a, b) -> a || b;
	static Semigroup<Boolean> booleanXDisjunction = () -> (a, b) -> a && !b || b && !a;
	static Semigroup<Boolean> booleanConjunction = () -> (a, b) -> a && b;

}
