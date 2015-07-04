package com.aol.cyclops.javaslang;

import java.util.Optional;

import javaslang.collection.List;
import javaslang.collection.Stream;
import javaslang.control.Either;
import javaslang.control.Either.LeftProjection;
import javaslang.control.Either.RightProjection;
import javaslang.control.Failure;
import javaslang.control.Option;
import javaslang.control.Right;
import javaslang.control.Try;
import javaslang.test.Arbitrary;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.monads.AnyM;

public class Javaslang {

	public static <T> AnyM<T> anyM(Arbitrary<T> arbM){
		return AsAnyM.notTypeSafeAnyM(arbM);
	}
	public static <T> AnyM<T> anyM(Try<T> tryM){
		return AsAnyM.notTypeSafeAnyM(tryM);
	}
	public static <T> AnyM<T> anyMFailure(Try<T> tryM){
		if(tryM.isFailure())
			return AsAnyM.notTypeSafeAnyM(Try.of(()->tryM.toEither().left().get()));
		return AsAnyM.notTypeSafeAnyM(Optional.empty());
	}
	public static <T> AnyM<T> anyM(Either<?,T> tryM){
		return AsAnyM.notTypeSafeAnyM(tryM);
	}
	public static <T> AnyM<T> anyM(RightProjection<?,T> tryM){
		if(tryM.toJavaOptional().isPresent())
			return AsAnyM.notTypeSafeAnyM(new Right(tryM.get()));
		else
			return AsAnyM.notTypeSafeAnyM(Optional.empty());
	}
	public static <T> AnyM<T> anyM(LeftProjection<T,?> tryM){
		if(tryM.toJavaOptional().isPresent())
			return AsAnyM.notTypeSafeAnyM(new Right(tryM.get()));
		else
			return AsAnyM.notTypeSafeAnyM(Optional.empty());
	}
	public static <T> AnyM<T> anyM(Option<T> tryM){
		return AsAnyM.notTypeSafeAnyM(tryM);
	}
	public static <T> AnyM<T> anyM(Stream<T> tryM){
		return AsAnyM.notTypeSafeAnyM(tryM);
	}
	public static <T> AnyM<T> anyM(List<T> tryM){
		return AsAnyM.notTypeSafeAnyM(tryM);
	}
}
