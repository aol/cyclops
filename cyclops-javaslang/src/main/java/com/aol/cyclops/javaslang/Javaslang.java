package com.aol.cyclops.javaslang;

import java.util.Optional;

import com.aol.cyclops.monad.AnyM;

import javaslang.Value;
import javaslang.collection.LazyStream;
import javaslang.collection.List;
import javaslang.control.Either;
import javaslang.control.Either.LeftProjection;
import javaslang.control.Either.RightProjection;
import javaslang.control.Option;
import javaslang.control.Try;

public class Javaslang {
	public static <T> AnyM<T> anyMonad(Value<T> monadM){
		return AnyM.ofMonad(monadM);
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
			return AnyM.ofMonad(Either.right(tryM.get()));
		else
			return AnyM.ofMonad(Optional.empty());
	}
	public static <T> AnyM<T> anyM(LeftProjection<T,?> tryM){
		if(tryM.toJavaOptional().isPresent())
			return AnyM.ofMonad(Either.right(tryM.get()));
		else
			return AnyM.ofMonad(Optional.empty());
	}
	public static <T> AnyM<T> anyM(Option<T> tryM){
		return AnyM.ofMonad(tryM);
	}
	public static <T> AnyM<T> anyM(LazyStream<T> tryM){
		return AnyM.ofMonad(tryM);
	}
	public static <T> AnyM<T> anyM(List<T> tryM){
		return AnyM.ofMonad(tryM);
	}
}
