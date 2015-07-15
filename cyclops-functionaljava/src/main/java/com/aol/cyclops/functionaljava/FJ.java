package com.aol.cyclops.functionaljava;

import java.util.Optional;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.monads.AnyM;

import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import fj.data.Stream;

public class FJ {
	
	
	public static <T> AnyM<T> anyM(Either<?,T> tryM){
		return AsAnyM.notTypeSafeAnyM(tryM);
	}
	public static <T> AnyM<T> anyM(Either<?,T>.RightProjection<?,T> rM){
		if(rM.toOption().isSome())
			return AsAnyM.notTypeSafeAnyM(Either.right(rM.value()).right());
		else
			return AsAnyM.notTypeSafeAnyM(Optional.empty());
	}
	public static <T> AnyM<T> anyM(Either<T,?>.LeftProjection<T,?> lM){
		if(lM.toOption().isNone())
			return AsAnyM.notTypeSafeAnyM(Either.right(lM.value()).right());
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
