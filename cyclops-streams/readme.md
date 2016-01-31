# cyclops-streams

[USER GUIDE](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/streams.adoc)

cyclops-streams provides a sequential reactive-streams implmentation ( [SequenceM](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.2.1/com/aol/cyclops/sequence/SequenceM.html)) that forms the basis of the multi-threaded reactive-streams implementations in [simple-react](https://github.com/aol/simple-react), as such it provides all the advanced streaming functionality you would expect for an FRP library - windowing / batching (by size, time, state), failure handing, zipping, stream manipulation (insertAt, deleteBetween), advanced skip and limit operations (time based, conditional), asynchronous streaming operations (Future based operations and hotstreams).

cyclops-streams is java.util.Streams on steriods! An implementation of the cyclops-sequence-api [SequenceM](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/SequenceM.html) for more advanced sequential Streams. Extends [java.util.stream.Stream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html) and [jool.Seq](http://www.jooq.org/products/jOO%CE%BB/javadoc/0.9.7/org/jooq/lambda/Seq.html) to add even more functionality. 
Easy to use Reactive Streams support available if simple-react (v0.99.3 and above) is added to the classpath.

Features include

* Scheduling (cron, fixed rate, fixed delay)
* Failure handling (recover / retry)
* windowing / batching (by time, size, state, predicate)
* zipping
* HotStreams
* reactive-streams : subscriber / publisher/ forEachX, forEachWithErrors, forEachEvent and more
* Asynchronous execution
* Stream manipulation - insert/At, deleteAt
* Frequency management (xPer, onePer, jitter, debounce)
* Efficient reversal
* StreamUtils - static methods for java.util.stream.Streams
* Streamables - efficient / lazy replayable Streams as java.util.stream.Stream or SequenceM

* [See Streaming Examples wiki page for more details & examples](https://github.com/aol/cyclops/wiki/cyclops-streams-:-Streaming-examples)
* [Asynchronous operations](https://github.com/aol/cyclops/wiki/cyclops-streams-:-Asynchronous-Terminal-Operations)


## Getting cyclops-streams

* [![Maven Central : cyclops-streams](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-streams/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-streams)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-streams:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-streams</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Overview

* Fast sequential Streams that can run asyncrhonously
* Reactive Stream Support
* Efficient Stream reversal and right based operations (foldRight / scanRight etc)
* static Stream Utilities
* SequenceM implementation
* Terminal operations that return a Future to be populated asynchronously
* HotStream support




# Operators

Large number of Stream operators available on SequenceM or as static methods for java.util.Stream (SequenceM extends java.util.Stream)

Javadoc 
 
* [SequenceM](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/7.0.0/com/aol/cyclops/sequence/SequenceM.html)
* [Streamable](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/7.0.0/com/aol/cyclops/sequence/streamable/Streamable.html)
* [Async FutureOperations](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/future/FutureOperations.html)
* [Async IntOperators](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/7.0.0/com/aol/cyclops/sequence/future/IntOperators.html)
* [Async DoubleOperators](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/7.0.0/com/aol/cyclops/sequence/future/DoubleOperators.html)
* [Async LongOperators](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/7.0.0/com/aol/cyclops/sequence/future/LongOperators.html)

# Examples

## Creating Streams

SequenceM streams can be created via creational Operators such as

* of
* fromStream
* fromIterable
* fromList
* fromIntStream
* fromLongStream
* fromDoubleStream

### With efficient reversability


range, of(List), of(..values) all result in Sequences that can be efficiently reversed (and used in scanRight, foldRight etc)

```java
SequenceM.range(0,Integer.MAX_VALUE);

List<Intger> list;
SequenceM.fromList(list);

SequenceM.of(1,2,3)
        .reverse()
        .forEach(System.out::println);
```

## recover / Retry

Recover and retry operators allow different strategies for error recovery.

Recover allows a default value to be provided when an exception occurs

```java
SequenceM.of(1,2,3,4)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue()
//hello
```

Recovery can be linked to specific exception types.

```java
SequenceM.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{throw ExceptionSoftener.throwSoftenedException( new IOException());})
					.recover(IOException.class,e->"hello")
					.firstValue()
//hello
```

With retry, a function will be called with an increasing back-off up to 7 times. 

```java
SequenceM.of( 1,  2, 3)
				.retry(this::loadData)
				.firstValue()
```
## FutureOperations

FutureOperations allow a Stream to be executed Asynchronously with the result of a terminal operation captured in a Future.

```java
Executor exec = Executors.newfixedThreadPool(1);
CompletableFuture<List<Integer>> list = SequenceM.of(1,2,3,4,5)
                                                 .map(this:expensiveOperation)
         										 .futureOperations(exec)
        										 .collect(Collectors.toList());
        										 
 // list populates Asynchronously
 
 list.join()
     .forEach(System.out::println);
```



## HotStream 

HotStreams are executed immediately and asynchronously (on the provided Executor - they will make use of a single thread only). They are also connectable (by potentially multiple Streams), the connected Stream receive those elements emitted *after* they connect.

```java
HotStream<Integer> range = SequenceM.range(0,Integer.MAX_VALUE)
									.peek(System.out::println)
									.hotStream(Executors.fixedThreadPool(1));

// will start printing out each value in range									
									
range.connect()
	.limit(100)
	.futureOperations(ForkJoinPool.commonPool())
	.forEach(System.out::println);
	
//will print out the first 100 values it recieves (after joining) on a separate thread	
```

## Reactive Streams

In conjunction with simple-react v0.99.3 and above SequenceM can be turned into a reactive-stream publisher or subscriber.
```java
CyclopsSubscriber<Integer> sub = SequenceM.subscriber();
		SequenceM.of(1,2,3).subscribe(sub);
		sub.sequenceM().toList();
		
//[1,2,3]
```
# Overview

* Advanced & powerful Streaming api
* Reactive Streams support
* Asynchronous single threaded Streaming
* Terminal operations that return a Future to be populated asynchronously
* Reversable Spliterators for efficient Stream reversal and right based operations
* Retry / onFail
* HotStream support

This primarily defines the interfaces to be used for cyclops Streaming, for an implementation see cyclops-stream.

# The Decomposable Interface  / Trait

The Decomposable Interface defines an unapply method that is used to convert the implementing Object into an iterable. This can be used to control how Cyclops performs recursive decomposition.

```java
    public <I extends Iterable<?>> I unapply();
```
    
### Interfaces that extend Decomposable

* ValueObject
* StreamableValue
* CachedValues, PTuple1-8

## Coercing any Object to a Decomposable

```java
    As.asDecomposable(myObject).unapply().forEach(System.out::println);
```

com.aol.cyclops.dynamic.As provides a range of methods to dynamically convert types
# cyclops-monad-api

* AnyM : Abstraction over any Java monad
* OptionalT : Optional Monad Transformer
* StreamT : Stream Monad Transformer
* StreamableT : Streamable Monad Transformer
* ListT : List Monad Transformer
* CompletableFutureT : CompletableFutute Monad Transformer

cyclops-monad-api is normally imported via other cyclops modules, as full functioning depends on cyclops-streams. Instructions to import it in standalone format are at the bottom of this page.

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

# Features

Simplify deeply nested looping (over Collections, even  Streams, Optionals and more!).

![for comprehensions](https://cloud.githubusercontent.com/assets/9964792/7887680/c6ac127c-062a-11e5-9ad7-ad4553761e8d.png)

# Docs

* [Javadoc for Cyclops For Comprehensions](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-for-comprehensions/4.0.2)
* [Wiki for Extensible For Comprehensions](https://github.com/aol/cyclops/wiki/Extensible-For-Comprehensions-for-Java-8)
* [For comprehension examples](https://github.com/aol/cyclops/wiki/For-Comprehension-Examples)
* 
# Overview of Cyclops For Comprehensions

Two supported formats

1. Type Do Notation via Do.add / with
2. Untpyed Do Notation via UntypedDo.add  / with



# Do Notation
```java
    List<Integer> list= Arrays.asList(1,2,3);
    
    List<Integer> list = Do.add(list)
                                .yield((Integer i)-> i +2)
                                .unwrap();
                
                                        
        
    assertThat(Arrays.asList(3,4,5),equalTo(list));
```

Yield, Filter and 'and' take curried functions

(That is a chain of single input parameter functions)

```java
        Stream<Integer> stream = Do.add(asList(20,30))
                                   .with( i->asList(1,2,3))
                                   .yield(i-> j -> i + j+2)
                                   .asSequence();
```

Parameters are stack based, the parameter to the first function is an index into the first Collection or Monad, the parameter to the second function is an index into the second Collection or Monad and so on.

The above code could be rewritten as 

```java
        Stream<Integer> stream = Do.add(asList(20,30))
                                   .with(any->asList(1,2,3))
                                   .yield(x-> y -> x + y+2)
                                   .asSequence();
```
And it would work in exactly the same way

```java
        List<Integer> list= Arrays.asList(1,2,3);
        Stream<Integer> stream = Do.add(list)
                                .filter(a -> a>2)
                                .yield(a-> a +2)
                                .asSequence();
                
                                        
        
        assertThat(Arrays.asList(5),equalTo(stream.collect(Collectors.toList())));
```

# for comprehensions explained

For comprehensions are useful for iterating over nested structures (e.g. collections, Streams, Optionals, CompletableFutures or other Monads).
    
Given a list of Strings 
```java
     List<String> list = Arrays.asList("hello","world","3");
 ```
We can iterate over them using Java 5 'foreach' syntax
     
```java
     for(String element : list){
        System.out.println(element);
     }
```   

The equivalent for comprehension would be 

 ```java    
    Do.add(list)
      .yield( element ->  element )
      .forEach(System.out::println);  
 ```
     
 We have simply converted the list to a Stream and are using Stream forEach to iterate over it.                           
                                      
But.. if we nest our looping

```java 
      List<Integer> numbers = Arrays.asList(1,2,3,4);

      for(String element : list){
         for(Integer num : numbers){
            System.out.println(element + num);
          }
      }                              
```

Things start to become a little unwieldy, but a little less so with for comprehensions
      
 ```java    
    Do.add(list)
      .with(element -> numbers)
      .yield(  element -> num  -> element + num )
      .unwrap()
      .forEach(System.out::println);
 ```     
                                  
Let's add a third level of nesting

```java
    List<Date> dates = Arrays.asList(new Date(),new Date(0));

    for(String element : list){
         for(Integer num : numbers){
             for(Date date : dates){
                System.out.println(element + num + ":" + date);
             }
            
          }
      }
 ```
    
 And the for comprehension looks like 
   
```java  
    Do.add(list)
      .add(numbers)
      .add(dates)
      .yield( element ->  num ->  date -> element + num+":"+date )
      .unwrap()
      .forEach(System.out::println);
 ```
 
 Stream map
  
```java    
     list.stream()
         .map(element -> element.toUpperCase())
         .collect(Collectors.toList());
 ```        
         
Can be written as

```java
      ForComprehensions.foreach1(c -> c.mapAs$1(list))
                                       .yield( (Vars1<String> v) -> c.$1().toUpperCase())
                        .collect(Collectors.toList());
```    
 ## Mixing types
 
 Running a for comprehension over a list (stream) and an Optional
 
  ```java       
        List<String> strs = Arrays.asList("hello","world");
        Optional<String> opt = Optional.of("cool");
        
        
        Do.add(strs)
          .add(opt)
          .yield(v1->v2 -> v1 + v2)
          .unwrap()
          .forEach(System.out::println);
```
                                         
Outputs : [hellocool, worldcool]


Or the other way around 

```java
        List<String> strs strs = Arrays.asList("hello","world");
        Optional<String> opt = Optional.of("cool");
        
        Do.add(opt)
          .add(strs)
          .yield(v1->v2 -> v1+ v2)
          .<String>toSequence()
          .forEach(System.out::println);
        
        assertThat(results.get(),hasItem("coolhello"));
        assertThat(results.get(),hasItem("coolworld"));
```
        
Outputs : [[coolhello],[coolworld]]

**The first type used controls the interaction!**

## Visualisation of CompletableFuture / Stream mixed combinations

**CompletableFuture defined first**
![do - completablefuture and stream](https://cloud.githubusercontent.com/assets/9964792/7887748/42efba28-062b-11e5-911a-5067e9095928.png)


**Stream defined first**
![do - stream and completablefuture](https://cloud.githubusercontent.com/assets/9964792/7887756/53519b2a-062b-11e5-9249-217d6c904a5e.png)

## Filtering

Guards (filter commands) can be placed at any stage of a for comprehension. E.g.
```java
                 Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
                        .and((Double d)->Arrays.asList(2.0))
                        .filter((Double d)-> (Double e) -> e*d>10.00)
                        .yield((Double base)->(Double bonus)-> base*(1.0+bonus))
                        .asSequence();
        
        double total = s.collect(Collectors.summingDouble(t->t));
        assertThat(total,equalTo(330.9));
```
## Convert any Object to a Monad

### Stream conversions

* Collection to Stream
* Iterable to Stream
* Iterator to Stream
* Array to Stream
* Int to IntStream.range(int)
* File to Stream
* URL to Stream
* BufferedReader to Stream
* InputStream to Stream
* ResultSet to Stream
* Enum to Stream
* String to Stream

* ObjectToStreamConverter

### Optional conversions

* NullToOptionalConverter
* Optional<Primitive> to Optional

### CompletableFuture Conversionss

* Callable to CompletableFuture
* Supplier to CompletableFuture

## Cyclops Monadic For Comprehensions

Cyclops for comphrensions allow deeply nested iterations or monadic operations to be expressed as a simple foreach expression. The implementation is inspired by the rather excellent Groovy implementation by Mark Perry (Functional Groovy)  see [Groovy Null Handling Using Bind, Comprehensions and Lift](https://mperry.github.io/2013/07/28/groovy-null-handling.html). They will work with *any* Monad type (JDK, Functional Java, Javaslang, TotallyLazy, Cyclops etc).

The Cyclops implementation is pure Java however, and although it will revert to dynamic execution when it needs to, reflection can be avoided entirely.

### Custom interfaces

 
* Support for custom interface definition with virtually unlimited nesting
```java
    Stream<Integer> stream = foreachX(Custom.class,  
                                    c-> c.myVar(list)
                                        .yield(()->c.myVar()+3)
                                    );

    Optional<Integer> one = Optional.of(1);
    Optional<Integer> empty = Optional.empty();
    BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
```     
    




### For more info

* [Scala Sequence Comprehensions](http://docs.scala-lang.org/tutorials/tour/sequence-comprehensions.html)
* [Scala yield] (http://docs.scala-lang.org/tutorials/FAQ/yield.html)

# Dependencies

* cyclops-invokedynamic
* cyclops-monad-api
* cyclops-sequence-api

## Recommended in conjunction with

simple-react
