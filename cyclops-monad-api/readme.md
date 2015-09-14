# cyclops-monad-api

cyclops-monad-api is normally used by other cyclops modules, but library writers may like to make use of it. Instructions to import it in standalone format are at the bottom of this page.

An alternative to higher-kinded types for providing a common interface over classes that define map / flatMap / filter (etc) methods or their equivalent.

Works by either using a registered 'Comprehender' that handles the actual method invocations, or by taking advantage of InvokeDynamic facility introduced in Java 7 that can make the performance of a dynamic method call almost equivalent to a static call.

## Docs

[Introducing the cyclops monad api](https://medium.com/@johnmcclean/introducing-the-cyclops-monad-api-a7a6b7967f4d)


<img width="873" alt="screen shot 2015-07-22 at 10 19 24 pm" src="https://cloud.githubusercontent.com/assets/9964792/8837752/e478f5bc-30bf-11e5-972d-e6ac54e80b7a.png">

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


# Examples



# Dependencies

cyclops-invokedynamic
cyclops-sequence-api

## Recommended in conjunction with

cyclops-streams



# Monoids

Fit the Stream.reduce signature. Can be used to wrap any Monoid type (e.g. functional java).

```java
	Monoid.of("",(a,b)->a+b).reduce(Stream.of("a","b","c"));
```
	
Produces "abc"
```java 
	fj.Monoid m = fj.Monoid.monoid((Integer a) -> (Integer b) -> a+b,0);
	Monoid<Integer> sum = As.asMonoid(m);
		
	assertThat(sum.reduce(Stream.of(1,2,3)),equalTo(6));
```
	
Use in conjunction with Power Tuples or StreamUtils for Multiple simultanous reduction on a Stream.


### Coerce to Decomposable

The Decomposable interface specifies an unapply method (with a default implementation) that decomposes an Object into it's elemental parts. It used used in both Cyclops Pattern Matching (for recursively matching against Case classes) and Cyclops for comprehensions (where Decomposables can be lifted to Streams automatically on input - if desired).

```java
     @Test
	public void test() {
		assertThat(AsDecomposable.asDecomposable(new MyCase("key",10))
				.unapply(),equalTo(Arrays.asList("key",10)));
	}
	
	@Value
	static class MyCase { String key; int value;}
```
	
## Getting cyclops-monad-api

* [![Maven Central : cyclops-monad-api](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-monad-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-monad-api)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-monad-api:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-monad-api</artifactId>
    <version>x.y.z</version>
</dependency>
```
	
