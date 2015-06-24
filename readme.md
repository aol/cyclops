# Cyclops

Powerful, modular extensions for Java 8. Take what you need or want.

![cyclops 1](https://cloud.githubusercontent.com/assets/9964792/8341880/19f980be-1ac0-11e5-9904-444405b4b4f1.png)

# Cyclops Modules

* [Extensible For Comprehensions](https://github.com/aol/cyclops/wiki/Extensible-For-Comprehensions-for-Java-8)
* [Pattern Matching](https://github.com/aol/cyclops/wiki/Pattern-matching-:-Pattern-Matching-for-Java-8)
* [Advanced Monadic (Stream, Optional etc) cross-type operations](https://github.com/aol/cyclops/wiki/Monad-&-Stream-utilities)
* [Powerful Tuple implementation](https://github.com/aol/cyclops/wiki/Power-Tuples)
* [Trampoline](https://github.com/aol/cyclops/wiki/Trampoline-:-Stackless-Recursion-for-Java-8)
* [Try](https://github.com/aol/cyclops/wiki/Try-:-functional-exception-handling-for-Java-8)
* [Enable Switch](https://github.com/aol/cyclops/wiki/Enable-and-disable-production-features)
* [Utils for working with Functions](https://github.com/aol/cyclops/wiki/Utilities-for-working-with-Java-8-Functions)


[Cyclops Wiki](https://github.com/aol/cyclops/wiki)



## Cyclops module overview

* Cyclops Base : [![Maven Central : cyclops-base](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-base/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-base)
* Cyclops For Comprehensions : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-for-comprehensions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-for-comprehensions)
* Cyclops Pattern Matching : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching)
* Cyclops Functions : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-functions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-functions)
* Cyclops Core : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-core)
* Cyclops Try : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-try/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-try)
* Cyclops Trampoline : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-trampoline/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-trampoline)
* Cyclops Enable Switch : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-enable-switch/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-enable-switch)
* Cyclops Power Tuples : [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-power-tuples/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-power-tuples)


![cyclops module relationship - class diagram](https://cloud.githubusercontent.com/assets/9964792/7887668/af2f6b4e-062a-11e5-9194-a2a4b6d25c96.png)
   
## Cyclops goals

Cyclops core goal is to raise up Java 8 to a higher level by providing modular, extensible enhancements that will interoperate or aid interoperability with other Java libraries. To do this we make use of Java facilities such as Service Loading for extenisibility and Invoke Dynamic dynamic method calls (when we don't know anything about an external type, but have been asked to handle it). All points of contact with InvokeDynamic code can be replaced by custom extensions if neccessary.

## For Comprehensions

Perform nested operations on Collections or Monads.

![for comprehensions](https://cloud.githubusercontent.com/assets/9964792/7887680/c6ac127c-062a-11e5-9ad7-ad4553761e8d.png)

### Example 

    Stream<Double> s = Do.with(asList(10.00,5.00,100.30))
						.with(asList(2.0))
						.and((Double d)->(Double e)->asList(e*d*10.0))
						.yield((Double i)->(Double j)->(Double k) -> i*(1.0+j)*k);
		
	double total = s.collect(Collectors.summingDouble(t->t));

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

    private <I,T> CheckValues<Object, T> cases(CheckValues<I, T> c) {
		return c.with(1,2,3).then(i->"hello")
				.with(4,5,6).then(i->"goodbye");
	}
	@Test
	public void test(){
		assertThat(As.asMatchable(new MyCase(1,2,3)).match(this::cases),equalTo("hello"));
		
	}

## Extensible Generic Monad Operations

### Example

flatMap (bind) across Stream and Optional types (null entries are removed)

      List<Integer> list = As.<Stream<Integer>,List<Integer>>asMonad(Stream.of(Arrays.asList(1,3),null))
				.bind(Optional::ofNullable)
				.map(i->i.size())
				.peek(System.out::println)
				.toList();
		assertThat(Arrays.asList(2),equalTo(list));

### Example

Lift a File to a Stream


		List<String> result = AsGenericMonad.<Stream<String>,String>asMonad(Stream.of("input.file"))
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.<Stream<String>,String>liftAndbind(File::new)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));

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

The same code using Trampoline works fine.


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

    Try.catchExceptions(FileNotFoundException.class,IOException.class)
				   .init(()->new BufferedReader(new FileReader("file.txt")))
				   .tryWithResources(this::read)
				   .onFail(this::recover)
				   .map(this::continueProcessing)
				
## Production Enable / Disable Switch

* Enable / Disable classes (Pattern Match by type)
* convert to Optional or Stream
* standard Java 8 operators (map, flatMap, peek, filter, forEach) + flatten etc
* isEnabled / isDisabled
* Biased towards enabled (right biased).

### Example

	Switch<Feature> switch = createSwitch(config);
	
    switch.map(this::processData); //if live, data is processed, otherwise nothing happens
    

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
     			return input + timesCalled.mutate(v -> v+1);
     }



## cyclops-converters 

Conversion utilities across Java 8 functional libraries (JAVASLANG, TottalyLazy, FunctionalJava, Guava, jooλ)

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



