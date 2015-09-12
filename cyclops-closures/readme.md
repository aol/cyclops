# cyclops-closures


## Getting cyclops-closures

* [![Maven Central : cyclops-closures](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-closures/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-closures)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-closures:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-closures</artifactId>
    <version>x.y.z</version>
</dependency>
```

Utilities for working with Lambda expressions that capture values or variables in the enclosing scope.

* Mutable
* MutableInt
* MutableDouble
* MutableLong
* LazyImmutable


Java lambda expressions can access local variables, but the Java compiler will enforce an 'effectively' final rule. cyclops-closures makes capturing variables in a mutable form a little simpler.

# Mutable, MutableInt, MutableDouble, MutableLong

## Examples

Store a single Object or primitive that can be accessed via get, set via set or mutated via mutate.

```java
MutableInt num = MutableInt.of(20);
		    
Stream.of(1,2,3,4)
      .map(i->i*10)
      .peek(i-> num.mutate(n->n+i))
      .forEach(System.out::println);
		    
assertThat(num.get(),is(120));
```



## Limitations

Not suitable for multi-threaded use, see AtomicReference, AtomicIntger, AtomicDouble & AtomicLong for more appropriate alternatives for sharing data across threads.

# LazyImmutable

A set-once wrapper over an AtomicReference. Unlike the MutableXXX classes LazyImmutable is designed for sharing across threads where the first thread to attempt can write to the reference, and subsequent threads can read only.

## Examples

Create a memoizing (caching) Supplier that can be shared across threads.

```java
public static <T> Supplier<T> memoizeSupplier(Supplier<T> s){
		LazyImmutable<T> lazy = LazyImmutable.def();
		return () -> lazy.computeIfAbsent(s);
	}
```

# Dependencies

None

# Getting cyclops-closures

## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-closures:x.y.z'