# Cyclops

Powerful, modular extensions for Java 8.

* [Extensible For Comprehensions](https://github.com/aol/cyclops/wiki/Extensible-For-Comprehensions-for-Java-8)
* [Pattern Matching](https://github.com/aol/cyclops/wiki/Pattern-matching-:-Pattern-Matching-for-Java-8)
* [Advanced Monadic (Stream, Optional etc) cross-type operations](https://github.com/aol/cyclops/wiki/Monad-&-Stream-utilities)
* [Powerful Tuple implementation](https://github.com/aol/cyclops/wiki/Power-Tuples)
* [Trampoline](https://github.com/aol/cyclops/wiki/Trampoline-:-Stackless-Recursion-for-Java-8)
* [Try](https://github.com/aol/cyclops/wiki/Try-:-functional-exception-handling-for-Java-8)
* [Enable Switch](https://github.com/aol/cyclops/wiki/Enable-and-disable-production-features)
* [Utils for working with Functions](https://github.com/aol/cyclops/wiki/Utilities-for-working-with-Java-8-Functions)

![cyclops module relationship - class diagram](https://cloud.githubusercontent.com/assets/9964792/7887668/af2f6b4e-062a-11e5-9194-a2a4b6d25c96.png)
   

## For Comprehensions

Perform nested operations on Collections or Monads.

![for comprehensions](https://cloud.githubusercontent.com/assets/9964792/7887680/c6ac127c-062a-11e5-9ad7-ad4553761e8d.png)


## Pattern Matching

Advanced Scala-like pattern matching for Java 8

![whenvalues recursive](https://cloud.githubusercontent.com/assets/9964792/7887716/01eeeeb8-062b-11e5-95e9-3ac10f16acdf.png)

## cyclops-lambda-utils

### LazyImmutable

This is a class that helps work around the limitations of Java 8 lambda expressions as closures. In particular the workings of 'effectively final'.

LazyImmutable allows a capture value to be set exactly once

E.g. from cyclops-pattern-matching the code to make an Extractor memoised ->

    public static final <T,R > Extractor<T,R> memoised( Extractor<T,R> extractor){
		final LazyImmutable<R> value = new LazyImmutable<>();
		return input -> {
			return value.computeIfAbsent(()->extractor.apply(input));
				
		};
		
	}

computeIfAbsent is used to extract the value from the LazyImmutable, and takes a Supplier as an argument. The Supplier is only invoked once (the first time).

### Mutable

Mutable represents a captured variable inside a Java 8 closure. Because of the effectively final rule we can't access variables from within a Closure, but we can mutate the state of captured Objects. Mutable holds a value we would like mutate (if really, really, neccessary)

     Mutable<Integer> timesCalled = Mutable.of(0);
     Function<String,String> fn = input -> {
     			return input + timesCalled.mutable(v -> v+1);
     }



## cyclops-converters 

* Immutable Java classes
* Immutable Java Collections
* Efficient lazy execution
* Streams and sequences
* Actors
* Safe concurrency
* Reactive programming


Integrates 

* Project Lombok : for immutable builders and withers
* Google Guava : for fast non-modifiable collections
* totallylazy : persistent collections, sequences, monads, actors
* javaslang : immutable collections, streams & sequences, monads, tuples, exception handling
* functionalJava : immutable collections, streams & sequences, monads, actors
* lazySeq : lazy Sequence
* jooλ : sequences, tuples,  exception handling
* simple-react : concurrent streaming library

Features

* Convert iterable sequences (i.e. Streams)
* Convert function types
* Convert tuples
* Convert Optional / Either types
* Syntax sugar over monads (e.g. tailRecursion)


