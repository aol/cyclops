Powerful, lightweight & modular extensions for JDK 8. Import only the modules you need.

[![Join the chat at https://gitter.im/aol/cyclops](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/aol/cyclops?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

<img alt="cyclops" src="https://cloud.githubusercontent.com/assets/9964792/8509305/4b0518d6-2294-11e5-83f5-10945539f73d.png">


# Cyclops Modules & Features Including


* [Powerful extensions to the Stream interface with SequenceM](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/SequenceM.html)
* [Static Stream functions with StreamUtils](http://static.javadoc.io/com.aol.cyclops/cyclops-streams/6.0.1/com/aol/cyclops/streams/StreamUtils.html)
* [Memoization](http://static.javadoc.io/com.aol.cyclops/cyclops-functions/6.0.1/com/aol/cyclops/functions/caching/Memoize.html)
* [Extensible For Comprehensions](https://github.com/aol/cyclops/wiki/Extensible-For-Comprehensions-for-Java-8)
* [Pattern Matching](https://github.com/aol/cyclops/wiki/Pattern-matching-:-Pattern-Matching-for-Java-8)
* [Advanced Monadic (Stream, Optional etc) cross-type operations](https://github.com/aol/cyclops/wiki/Monad-&-Stream-utilities)
* [Powerful Tuple implementation](https://github.com/aol/cyclops/wiki/Power-Tuples)
* [Trampoline](https://github.com/aol/cyclops/wiki/Trampoline-:-Stackless-Recursion-for-Java-8)
* [Try](https://github.com/aol/cyclops/wiki/Try-:-functional-exception-handling-for-Java-8)
* [Enable Switch](https://github.com/aol/cyclops/wiki/Enable-and-disable-production-features)
* [Utils for working with Functions](https://github.com/aol/cyclops/wiki/Utilities-for-working-with-Java-8-Functions)




[Cyclops Wiki](https://github.com/aol/cyclops/wiki)


# Cyclops articles across the web

* [Introducting the Cyclops Monad API](https://medium.com/@johnmcclean/introducing-the-cyclops-monad-api-a7a6b7967f4d)
* [Easier Try with Cyclops](http://rdafbn.blogspot.com/2015/06/java-8-easier-with-cyclops-try.html)
* [4 flavors of Java 8 Functions](https://medium.com/@johnmcclean/4-flavours-of-java-8-functions-6cafbcf5bb4f)
* [Memoise Functions in Java 8](http://rdafbn.blogspot.com/2015/06/memoize-functions-in-java-8.html)
* [Strategy Pattern in Java 8 ](http://rdafbn.blogspot.com/2015/06/startegy-pattern-in-java-8.html)
* [Pattern Matching in Java 8](https://medium.com/@johnmcclean/pattern-matching-in-cyclops-for-java-8-21a4912bfe4d)
* [Functional Feature Toggling](https://medium.com/@johnmcclean/feature-toggling-with-cyclops-a29d1eead62c)
* [Dependency injection using the Reader Monad in Java8](https://medium.com/@johnmcclean/dependency-injection-using-the-reader-monad-in-java8-9056d9501c75)
* [Deterministic and Non-Deterministic Finite State Machines with Cyclops](http://sebastian-millies.blogspot.de/2015/11/deterministic-and-non-deterministic.html)

![cyclops - duke2](https://cloud.githubusercontent.com/assets/9964792/8359084/28d79944-1b5c-11e5-9c56-2c44d33f3aed.png)


## Cyclops modules on Maven Central!

To import all modules use cyclops-all. For individual modules, see bottom of this page!

* Cyclops All : [![Maven Central : cyclops-all](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-all/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-all)

Import integration modules individually as needed.

* Cyclops Javaslang : [![Maven Central : cyclops-javaslang](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-javaslang/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-javaslang)
* Cyclops Functional Java : [![Maven Central : cyclops-javaslang](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-functionaljava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-functionaljava)
* Cyclops Guava : [![Maven Central : cyclops-guava](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-guava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-guava)


**NB** Cyclops All includes all Cyclops module except the integration modules (currently cyclops-javaslang).



![cyclops module relationship - class diagram](https://cloud.githubusercontent.com/assets/9964792/7887668/af2f6b4e-062a-11e5-9194-a2a4b6d25c96.png)
   
## Cyclops goals

Cyclops core goal is to raise up Java 8 to a higher level by providing modular, extensible enhancements that will interoperate or aid interoperability with other Java libraries. To do this we make use of Java facilities such as Service Loading for extenisibility and Invoke Dynamic dynamic method calls (when we don't know anything about an external type, but have been asked to handle it). All points of contact with InvokeDynamic code can be replaced by custom extensions if neccessary.

## For Comprehensions

Perform nested operations on Collections or Monads.

![for comprehensions](https://cloud.githubusercontent.com/assets/9964792/7887680/c6ac127c-062a-11e5-9ad7-ad4553761e8d.png)

### Example 
```java
    Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
						.add(asList(2.0))
						.with( d -> e ->asList(e*d*10.0))
						.yield(i -> j -> k  -> i*(1.0+j)*k).unwrap();
		
	double total = s.collect(Collectors.summingDouble(t->t));
```
## Pattern Matching

Advanced Scala-like pattern matching for Java 8. Match recursively against most Objects / datastructures.

![whenvalues recursive](https://cloud.githubusercontent.com/assets/9964792/7887716/01eeeeb8-062b-11e5-95e9-3ac10f16acdf.png)

Features include

* Sequential, Parallel and Async execution
* Match by type, value, predicate or Hamcrest Matcher
* Recursively decompose and match against Case classes
* Fluent step builders for common cases
* Fluent, functionally compositional monad-like core Case and Cases classes
* Support for chain of responsibility pattern within a Stream
* Support hamcrest matchers
* Java 8 predicates for matching.
* Match on first (return Optional)
* Match many (return Stream)
* Strict and lose typing
* Pre & post value extraction per case
* Match using multiple in case expressions via tuples or iterables of predicates / matchers
* Match against streams of data
* Usable within a Stream (strategy pattern)
* Fluent step builders

### Example 
```java
    private <I,T> CheckValues<Object, T> cases(CheckValues<I, T> c) {
		return c.with(1,2,3).then(i->"hello")
				.with(4,5,6).then(i->"goodbye");
	}
	@Test
	public void test(){
		assertThat(As.asMatchable(new MyCase(1,2,3)).match(this::cases),equalTo("hello"));
		
	}
```
## Extensible Generic Monad Operations with AnyM and SequenceM

### Example

flatMap (bind) across Stream and Optional types (null entries are removed)
```java
      List<Integer> list = anyM(Stream.of(Arrays.asList(1,3),null))
									.flatMapOptional(d-> Optional.ofNullable(d))
									.map(i->i.size())
									.peek(System.out::println)
									.asSequence()
									.toList();
									
		assertThat(Arrays.asList(2),equalTo(list));
```
### Example

Lift a File to a Stream

With a file "input.file" that contains two lines 

* hello
* world

We can stream the contents like so...
```java
		List<String> result = anyM("./input.file")
								.liftAndBindFile(File::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
```		
For multiple files...
```java
		List<String> result = anyM("./input.file","./input2.file")
								.liftAndBindFile(File::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world","hello2","world2")));
```
## Power Tuples 

Features include

* Wrap any Tuple type / Object (mapping fields to elements and back)
* Method call chaining support
* Asyncrhonous method call chaining support
* Inheritance relationship between Tuples
* Lazy and Strict map methods
* Lazy reordering
* Pattern matching
* For comprehensions
* Useful utility methods (asStreamOfStrings, asTwoNumbers etc)
* Concatonation
* LazySwap (reverse)
vMemoization
* asCollector
* asReducer

[See Power Tuples wiki](https://github.com/aol/cyclops/wiki/Power-Tuples)

## Stackless Recursion with Trampoline

Utilise the heap rather than the Stack for (tail) recursive algorithms in Java.
   
 The Java code below will result in a Stackoverflow error  
 ```java  
    @Test @Ignore
	public void trampolineTest1(){
		
		assertThat(loop1(500000,10),equalTo(446198426));
		
	}
	Integer loop1(int times,int sum){
		
		if(times==0)
			return sum;
		else
			return loop1(times-1,sum+times);
	}  
```
The same code using Trampoline works fine.

```java
    @Test
    public void trampolineTest(){

        assertThat(loop(500000,10).result(),equalTo(446198426));

     }

     Trampoline<Integer> loop(int times,int sum){
       
       if(times==0)
          return Trampoline.done(sum);
       else
          return Trampoline.more(()->loop(times-1,sum+times));
     }
```    
## Try : functional exception handling

Cyclops Try is similar to, but functionally different from the Scala (and JAVASLANG) Try monads. 

Features 

* Try with Resources
* Success and Failure states
* Step builders to guide you through use
* Catch specified (expected) exceptions
* Doesn't operate as a 'catch-all' that may hide bugs
* Recover from different exceptions independently
* Functional composition over both success and failed states


### Example : Try with resources
```java
    Try.catchExceptions(FileNotFoundException.class,IOException.class)
				   .init(()->new BufferedReader(new FileReader("file.txt")))
				   .tryWithResources(this::read)
				   .onFail(this::recover)
				   .map(this::continueProcessing)
```				
## Production Enable / Disable Switch

* Enable / Disable classes (Pattern Match by type)
* convert to Optional or Stream
* standard Java 8 operators (map, flatMap, peek, filter, forEach) + flatten etc
* isEnabled / isDisabled
* Biased towards enabled (right biased).

### Example
```java
	Switch<Feature> switch = createSwitch(config);
	
    switch.map(this::processData); //if live, data is processed, otherwise nothing happens
 ```   

## Traits

* Decomposable : decompose an Object to a Iterable over it's values
* Matchable : add pattern matching capabilities to an Object
* Doable : add for comprehension capabilities to an Object
* Streamable : add repeatable Streaming capabilities
* Mappable : add the ability to coerce an Object to a map
* Printable : ability to println as an expression
* ValueObject : Matchable and Decomposable object
* StreamableValue : Streamable and Doable ValueObject

com.aol.cyclops.dynamic.As offers duck typing / coercion to many different types (including the above traits) and

* com.aol.cyclops.lambda.monads.Monad
* com.aol.cyclops.lambda.monads.Functor
* com.aol.cyclops.lambda.monads.Monoid
* Supplier

## Function utilities

* Currying : com.aol.cyclops.functions.Curry
* Currying for Consumers : com.aol.cyclops.functions.CurryConsumer
* Uncurrying : com.aol.cyclops.functions.Uncurry
* Uncurrying for Consumers : com.aol.cyclops.functions.UncurryConsumer
* Type Inferencing help : com.aol.cyclops.lambda.utils.Lambda
* Memoisation : com.aol.cyclops.functions.Memoise

## cyclops-base

### LazyImmutable

This is a class that helps work around the limitations of Java 8 lambda expressions as closures. In particular the workings of 'effectively final'.

LazyImmutable allows a capture value to be set exactly once

E.g. from cyclops-pattern-matching the code to make an Extractor memoised ->
```java
    public static final <T,R > Extractor<T,R> memoised( Extractor<T,R> extractor){
		final LazyImmutable<R> value = new LazyImmutable<>();
		return input -> {
			return value.computeIfAbsent(()->extractor.apply(input));
				
		};
		
	}
```
computeIfAbsent is used to extract the value from the LazyImmutable, and takes a Supplier as an argument. The Supplier is only invoked once (the first time).

### Mutable

Mutable represents a captured variable inside a Java 8 closure. Because of the effectively final rule we can't access variables from within a Closure, but we can mutate the state of captured Objects. Mutable holds a value we would like mutate (if really, really, neccessary)
```java
     Mutable<Integer> timesCalled = Mutable.of(0);
     Function<String,String> fn = input -> {
     			return input + timesCalled.mutate(v -> v+1);
     }

```




## Attribution

* Trampoline pic by Mikefifield Licenced under  [Creative Commons Attribution 3.0 Unported](https://creativecommons.org/licenses/by/3.0/deed.en) : https://commons.wikimedia.org/wiki/File:Rebounder01.jpg
* Feature Toggle pic by Jason Zack Licenced under  [Creative Commons Attribution-Share Alike 2.5 Generic](https://creativecommons.org/licenses/by-sa/2.5/deed.en) : https://commons.wikimedia.org/wiki/File:On-Off_Switch.jpg 


## Maven for individual modules

* Cyclops Base : [![Maven Central : cyclops-base](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-base/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-base)
* Cyclops For Comprehensions : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-for-comprehensions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-for-comprehensions)
* Cyclops Pattern Matching : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching)
* Cyclops Functions : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-functions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-functions)
* Cyclops Core : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-core)
* Cyclops Try : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-try/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-try)
* Cyclops Trampoline : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-trampoline/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-trampoline)
* Cyclops Enable Switch : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-enable-switch/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-enable-switch)
* Cyclops Power Tuples : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-power-tuples/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-power-tuples)
