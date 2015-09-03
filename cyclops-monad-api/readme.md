# cyclops-monad-api

An alternative to higher-kinded types for providing a common interface over classes that define map / flatMap / filter (etc) methods or their equivalent.

Works by either using a registered 'Comprehender' that handles the actual method invocations, or by taking advantage of InvokeDynamic facility introduced in Java 7 that can make the performance of a dynamic method call almost equivalent to a static call.

## Docs

[Introducing the cyclops monad api](https://medium.com/@johnmcclean/introducing-the-cyclops-monad-api-a7a6b7967f4d)


# AnyM

## Operators

* aggregate
* applyM
* asSequence
* bind / flatMap - collection, completableFuture, optional, sequenceM, stream, streamable
* empty
* filter
* map
* reduceM
* simpleFilter


## Examples


Wrap a Stream inside an AnyM, and a Optional inside another, and aggregate their values

```java
List<Integer> result = anyM(Stream.of(1,2,3,4))
								.aggregate(anyM(Optional.of(5)))
								.asSequence()
								.toList();
		
assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
```
# AsAnyM / AsAnyMList: factory methods

# AnyMonadFunctions

* liftM  : Lift a Function to a function that accepts and returns any monad type (via AnyM).
* liftM2 : Lift a BiFunction to a function that accepts and returns any monad type (via AnyM).
* sequence : Convert a collection of monads, to a single monad with a collection
* traverse : Convert a collection of Monads to a single Monad with a wrapped Collection applying the supplied function in the process
* applyM
* reduceM
* simpleFilter
* cycle
* replicateM

# Examples



# Dependencies

cyclops-invokedynamic
cyclops-sequence-api

## Recommended in conjection with

cyclops-streams

# Getting cyclops-monad-api

## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-monad-api:x.y.z'