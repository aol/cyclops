package com.aol.cyclops.lambda.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.streamable.Streamable;


public class AsAnyMList {

	
	public static <T> List<AnyM<T>> notTypeSafeAnyMList(Collection<Object> anyM){
		return anyM.stream().map(i-> (AnyM<T>)AnyM.ofMonad(i)).collect(Collectors.toList());
	}
	
	public static <T> List<AnyM<T>> streamableToAnyMList(Collection<Streamable<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromStreamable(i)).collect(Collectors.toList());
	}
	
	public static <T> List<AnyM<T>> streamToAnyMList(Collection<Stream<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromStream(i)).collect(Collectors.toList());
	}
	
	public static <T> List<AnyM<T>> optionalToAnyMList(Collection<Optional<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromOptional(i)).collect(Collectors.toList());
	}
	
	public static <T> List<AnyM<T>> completableFutureToAnyMList(Collection<CompletableFuture<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromCompletableFuture(i)).collect(Collectors.toList());
	}
	public static <T> List<AnyM<T>> iterableToAnyMList(Collection<Iterable<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromIterable(i)).collect(Collectors.toList());
	}
	public static <T> List<AnyM<T>> collectionToAnyMList(Collection<Collection<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromCollection(i)).collect(Collectors.toList());
	}
	public static <T> List<AnyM<T>> iteratorToAnyMList(Collection<Iterator<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromIterable(()->i)).collect(Collectors.toList());
	}
	
	
	
}
