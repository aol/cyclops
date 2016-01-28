package com.aol.cyclops.javaslang;

import java.util.Optional;

import javaslang.algebra.Monad;
import javaslang.collection.List;
import javaslang.collection.Set;
import javaslang.collection.Stream;
import javaslang.control.Either;
import javaslang.control.Either.LeftProjection;
import javaslang.control.Either.RightProjection;
import javaslang.control.Option;
import javaslang.control.Right;
import javaslang.control.Try;
import javaslang.test.Arbitrary;

import com.aol.cyclops.monad.AnyM;

public class Javaslang {
	public static <T> AnyM<T> anyMonad(Monad<T> monadM){
		return AnyM.ofMonad(monadM);
	}
	public static <T> AnyM<T> anyM(Arbitrary<T> arbM){
		return AnyM.ofMonad(arbM);
	}
	public static <T> AnyM<T> anyM(Try<T> tryM){
		return AnyM.ofMonad(tryM);
	}
	public static <T> AnyM<T> anyMFailure(Try<T> tryM){
		if(tryM.isFailure())
			return AnyM.ofMonad(Try.of(()->tryM.toEither().left().get()));
		return AnyM.ofMonad(Optional.empty());
	}
	public static <T> AnyM<T> anyM(Either<?,T> tryM){
		return AnyM.ofMonad(tryM);
	}
	public static <T> AnyM<T> anyM(RightProjection<?,T> tryM){
		if(tryM.toJavaOptional().isPresent())
			return AnyM.ofMonad(new Right(tryM.get()));
		else
			return AnyM.ofMonad(Optional.empty());
	}
	public static <T> AnyM<T> anyM(LeftProjection<T,?> tryM){
		if(tryM.toJavaOptional().isPresent())
			return AnyM.ofMonad(new Right(tryM.get()));
		else
			return AnyM.ofMonad(Optional.empty());
	}
	public static <T> AnyM<T> anyM(Option<T> tryM){
		return AnyM.ofMonad(tryM);
	}
	public static <T> AnyM<T> anyM(Stream<T> tryM){
		return AnyM.ofMonad(tryM);
	}
	public static <T> AnyM<T> anyM(List<T> tryM){
		return AnyM.ofMonad(tryM);
	}
}
