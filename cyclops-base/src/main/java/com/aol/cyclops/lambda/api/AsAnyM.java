package com.aol.cyclops.lambda.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.aol.cyclops.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.lambda.monads.MonadWrapper;
import com.aol.cyclops.lambda.monads.AnyM;


public class AsAnyM extends AsGenericMonad{

	/**
	 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * }</pre>
	 * 
	 * we can write
	 * <pre>{@code
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> asAnyM(Object anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	/**
	 * Create a Monad wrapper from a Streamable
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Streamable<T> anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	/**
	 * Create a Monad wrapper from a Stream
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  </pre>
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Stream<T> anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	/**
	 * Create a Monad wrapper from an Optional
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Optional<Integer>,Integer> opt;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> opt;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Optional<T> anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	/**
	 * Create a Monad wrapper from a CompletableFuture
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<CompletableFuture<Integer>,Integer> future;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> future;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)  -- thenApply/Async
	 * 
	 * flatMap(F<x,MONAD> fm) -- thenCompose/Async
	 * 
	 * and optionally 
	 * 
	 * filter(P p)  -- not present for CompletableFutures
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(CompletableFuture<T> anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	/**
	 * Create a Monad wrapper from a Collection
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Collection<T> anyM){
		return convertToAnyM(anyM);
	}
	/**
	 * Create a Monad wrapper from an Iterable
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Iterable<T> anyM){
		return convertToAnyM(anyM);
	}
	/**
	 * Create a Monad wrapper from an Iterator
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Iterator<T> anyM){
		return convertToAnyM(anyM);
	}
	/**
	 * Create a Monad wrapper from an array of values
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(T... values){
		return anyM(Stream.of(values));
	}
	/**
	 * Create a Monad wrapper from an Object
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> toAnyM(Object anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	/**
	 * Create a Monad wrapper from an Object that will be converted to Monadic form if neccessary by the registered
	 * MonadicConverters. You can register your own MonadicConverter instances and / or change the priorities of currently registered converters.
	 * 
	* Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }</pre>
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> convertToAnyM(Object anyM){
		return new MonadWrapper<>(new MonadicConverters().convertToMonadicForm(anyM)).anyM();
	}
}
