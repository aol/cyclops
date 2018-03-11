# Cyclops Reactive Collections : Fast, lazy and asynchronous operations on mutable and persistent collections


## Get cyclops-reactive-collections


* [![Maven Central : cyclops-reactive-collections](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-reactive-collections/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-reactive-collections)
* [Javadoc for cyclops-reactive-collections](http://www.javadoc.io/doc/com.oath.cyclops/cyclops-reactive-collections)


## Enhanced performance via laziness

Chaining combinator operations on traditional functional datastructures such as the core persistent data structures provided in cyclops.data (or in Vavr or the Scala SDK) results in traversing the datastructure multiple times, once per combinator operation.

E.g. to transform and filter a Cyclops Vector we could write code like the following
```java

Vector.of(1,2,3)
      .map(i->i*100)
      .filter(i->i<200);

```
Each of the chained operations will traverse the Vector, once for map and once for filter.

With Cyclops Reactive Collections VectorX the code remains very similar, the underlying datastructure remains identical, but the performance is much improved

```java

VectorX.of(1,2,3)
       .map(i->i*100)
       .filter(i->i<200);

```
VectorX in contrast to a raw Vector traverses the underlying data structure just once.

### Performance improvement for Vavr's Vector when used with Cyclops VectorX

Lower score is better

* Raw Vavr Vector is on the Right.
* Vavr Vector with Cyclops VectorX is on the left.
<img width="553" alt="screen shot 2018-03-01 at 15 14 43" src="https://user-images.githubusercontent.com/9964792/36852335-4e02c0a2-1d63-11e8-9a81-ec600ee39c30.png">


# Reactive Collections!

In the example below we asynchronously populate an Xtended list using an asynchronously executing ReactiveSeq. Additional reactive operations can be performed on the List asynchronously.
The ListX only blocks on first access to the data.

```java

AtomicBoolean complete = new AtomicBoolean(false);


ReactiveSeq<Integer> async =  Spouts.async(ReactiveSeq.of(1,2,3),Executors.newFixedThreadPool(1));

ListX<Integer> asyncList = ListX.listX(async)
                                .map(i->i+1);

System.out.println("Blocked? " + complete.get());

System.out.println("First value is "  + asyncList.get(0));

System.out.println("Completed? " + complete.get());
```
Which will print

```
Blocked? false
First value is 101
Completed? true
```

## Available data structures

## cyclops.collections (mutable / immutable)

| type | description | characteristics |
|------|-------------|-----------------|
| ListX     | Functional extensions for working with Lists            | Optionally Reactive or Coreactive, Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| DequeX     | Functional extensions for working with Deques            | Optionally Reactive or Coreactive, Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| QueueX     | Functional extensions for working with Queues            | Optionally Reactive or Coreactive, Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| SetX     | Functional extensions for working with Sets            | Optionally Reactive or Coreactive, Lazy , mutable, immutable, 3rd party support                |
| SortedSetX     | Functional extensions for working with SortedSets            | Optionally Reactive or Coreactive, Lazy, mutable, immutable                 |
| MapX     | Functional extensions for working with Maps            | Eager, mutable, immutable                 |

## cyclops.collections.persistent

| type | description | characteristics |
|------|-------------|-----------------|
| LinkedListX     | Functional extensions for working with persistent Lists            | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support, Higher kinded                 |
| VectorX     | Functional extensions for working with persistent Vectors            | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support, Higher kinded                 |
| PersistentSetX     | Functional extensions for working with persistent Sets            | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support                 |
| OrderedSetX     | Functional extensions for working with persistent Ordered Sets            | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support                 |
| PersistentQueueX     | Functional extensions for working with persistent Queues           | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support, Higher kinded                 |
| BagX     | Functional extensions for working with persistent Bags (set like collections that allow duplicates)          | Optionally Reactive or Coreactive,Lazy, persistent, 3rd party support                 |



# Examples

Lazy execution using extended Collections
```java
Eval<Integer> lazyResult = ListX.of(1,2,3,4)
                                 .map(i->i*10)
                                 .foldLazy(s->s
                                 .reduce( 50,(acc,next) -> acc+next));
```

# Articles

* [JDK Collection eXtensions](https://medium.com/@johnmcclean/extending-jdk-8-collections-8ae8d43dd75e#.tn7ctbaks)
* [Faster Purely Data Structures For Java](https://medium.com/@johnmcclean/the-rise-and-rise-of-java-functional-data-structures-63782436f93b)
