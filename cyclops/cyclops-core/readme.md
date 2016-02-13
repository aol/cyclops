# cyclops-core

* [USER GUIDE : lambdas](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/lambdas.adoc)
* [USER GUIDE : collections ](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/Collections.adoc)
* [USER GUIDE : streams ](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/streams.adoc)


## Getting cyclops-core

* [![Maven Central : cyclops-core](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-core)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-core:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-core</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Overview

Cyclops all contains 2 extremely powerful sets of features

1. CollectionX : a series of fluent & powerful Collection eXtensions
1. FluentFunctions  : a fluent API for working with functions and methods (AOP, caching, logging, exception handling and more).

# Collection eXtensions

Cyclops Collection eXtensions provide a fluent interface with advanced 'stream'-like functionality over standard JDK collections and PCollections persistent collections. Supported extensions are

Standard with mutable and immutable views 

* ListX : JDK List extension
* SetX : JDK Set extension
* SortedSetX : JDK SortedSet extension
* QueueX : JDK Queue extension
* DequeX : JDK Deque extension

Persistent collection eXtensions

* PStackX : persistent analogue to LinkedList
* PVectorX : persistent analogue to ArrayList
* PQueueX : persistent analougue to JDK Queues
* PSetX : persistent analouge to JDK sets
* POrderedSetX : persistent analogue to JDK SortedSets
* PBagX : persisent ordered collection that allows duplicates

== Examples 

With ListX

 ```java
ListX.of(1,2,3)
      .map(i->i+2)
      .plus(5)
      .map(i->"hello" + i)
      .forEach(System.out::println);

//prints 

hello2
hello4
hello5
hello5

```

With PStackX

 ```java
PStackX.of(1,2,3,4,10)
            .map(i->i+2)
            .dropRight(2)
            .plus(5)
            .map(i->"hello" + i)
            .forEach(System.out::println);

//prints 

hello2
hello4
hello5
hello5
```

Creating a SetX from an existing Set

 ```java
 SetX.fromIterable(mySet)
     .limit(10)
     .filter(i->i>100)
     .map(this::load)
     .collect(ListX.toListX());
 ```
 
 Converting a ListX to a SetX and printing out the contents 
 ```java
  SetX<String> set = ListX.of(1,2,3)
                                .flatMap(i->Arrays.asList(i+2,10))
                                 .plus(5)
                                 .map(i->"hello" + i)
                                 .collect(SetX.toSetX());
        
 set.printOut();
 ```
  
# FluentFunctions

## AOP

### Before advice 
```java
    int set;
    public boolean events(Integer i){
        return set==i;
    }
    
    set = 0;
    FluentFunctions.of(this::events)
                   .before(i->set=i)
                    .println()
                    .apply(10);
    
    
    (fluent-function-Parameter[10])
    (fluent-function-Result[true])
```    
### After advice  

```java
setIn= 0;
setOut = true

FluentFunctions.of(this::events)
               .after((in,out)->{setIn=in;setOut=out;} )
               .println()
               .apply(10);
               
(fluent-function-Parameter[10])
(fluent-function-Result[false])

setIn =10
setOut = false               
```
### Around advice
```java
public int addOne(int i ){
        return i+1;
}

FluentFunctions.of(this::addOne)
                       .around(advice->advice.proceed(advice.param+1))
                       .println()
                       .apply(10)
 
(fluent-function-Parameter[10])
(fluent-function-Result[12])
                       
//12 because addOne adds one and so does the around advice
```

## Retry
```java
int times =0;
public String exceptionalFirstTime(String input) throws IOException{
        if(times==0){
            times++;
            throw new IOException();
        }
        return input + " world"; 
}
    
FluentFunctions.ofChecked(this::exceptionalFirstTime)
                       .println()
                       .retry(2,500)
                       .apply("hello");   

(fluent-function-Parameter[hello])
java.io.IOException
    at com.aol.cyclops.functions.fluent.FunctionsTest.exceptionalFirstTime(FunctionsTest.java:95)
   ...
(fluent-function-Parameter[hello])
(fluent-function-Result[hello world])
```
          
## Recover

```java
int times =0;
public String exceptionalFirstTime(String input) throws IOException{
        if(times==0){
            times++;
            throw new IOException();
        }
        return input + " world"; 
}

FluentFunctions.ofChecked(this::exceptionalFirstTime)
                        .recover(IOException.class, in->in+"boo!")
                        .println()
                        .apply("hello ");   
                        
(fluent-function-Parameter[hello ])
(fluent-function-Result[hello boo!])                               
```

## Caching
```java
int called;
public int addOne(int i ){
        called++;
       return i+1;
}

Function<Integer,Integer> fn = FluentFunctions.of(this::addOne)
                                              .name("myFunction")
                                              .memoize();

fn.apply(10);
fn.apply(10);
fn.apply(10);

called is 1
```
### Caching with a Guava cache
```java
Cache<Object, Integer> cache = CacheBuilder.newBuilder()
                   .maximumSize(1000)
                   .expireAfterWrite(10, TimeUnit.MINUTES)
                   .build();

        called=0;
        Function<Integer,Integer> fn = FluentFunctions.of(this::addOne)
                                                      .name("myFunction")
                                                      .memoize((key,f)->cache.get(key,()->f.apply(key)));
        
        fn.apply(10);
        fn.apply(10);
        fn.apply(10);
        
        assertThat(called,equalTo(1));
```        
## Printing function data
```java
public int addOne(int i ){
        return i+1;
}
    
FluentFunctions.of(this::addOne)
               .name("myFunction")
               .println()
               .apply(10)
               
(myFunction-Parameter[10])
(myFunction-Result[11])
```
## Generating a Stream

Load data from a service every second
```java
FluentFunctions.of(this::load)
               .generate("next element")
               .onePer(1, TimeUnit.SECONDS)
               .forEach(System.out::println);
               
public String gen(String input){
        return input+System.currentTimeMillis();
    }
FluentFunctions.of(this::gen)
               .println()
               .generate("next element")
               .onePer(1, TimeUnit.SECONDS)
               .forEach(System.out::println);
(fluent-function-Parameter[next element])
(fluent-function-Result[next element1453819221151])
next element1453819221151
(fluent-function-Parameter[next element])
(fluent-function-Result[next element1453819221151])
next element1453819221151
(fluent-function-Parameter[next element])
(fluent-function-Result[next element1453819222153])
next element1453819222153
(fluent-function-Parameter[next element])
(fluent-function-Result[next element1453819223155])
next element1453819223155
(fluent-function-Parameter[next element])
(fluent-function-Result[next element1453819224158])
```               
## Iterating a Stream
```java
FluentFunctions.of(this::addOne)    
                        .iterate(95281,i->i)
                        .forEach(System.out::println);  
95282
95283
95284
95285
95286
95287
95288
95289
95290
95291
95292
95293
95294     
```
## Pattern Matching
```java
FluentFunctions.of(this::addOne)    
                       .matches(-1,c->c.hasValues(2).then(i->3))
                       .apply(1)    
                       
//returns 3  
```
## Handle nulls

```java
public int addOne(Integer i ){
        return i+1;
}
Integer nullValue = null;
```

Calling addOne directly with nullValue will result in a NullPointerException, but we can use lift.

```java
FluentFunctions.of(this::addOne)    
               .lift()
               .apply(Optional.ofNullable(nullValue)); 
```

## Lift a Function to Any Monad type

Inject functionality into your methods via Java Monads (Stream, List, Optional, Try, CompletableFuture ect)
```java
AnyM<Integer> result = FluentFunctions.of(this::addOne) 
                                              .liftM()
                                              .apply(AnyM.streamOf(1,2,3,4));
        
result.forEach(System.out::println);

2
3
4
5
```
## Handle exceptions

```java
Try<String,IOException> tried = FluentFunctions.ofChecked(this::exceptionalFirstTime)   
                                                       .liftTry(IOException.class)
                                                       .apply("hello");               
        
if(tried.isSuccess())
     fail("expecting failure");
```     
## Asynchronous execution

```java
CompletableFuture<Integer> addOne = FluentFunctions.of(this::addOne)
                                                   .liftAsync(ex)
                                                   .apply(1);
                                                   
FluentFunctions.of(this::addOne)
                        .async(ex)
                        .thenApply(f->f.apply(4)) 
```                        
## Partial application

```java
FluentSupplier<Integer> supplier = FluentFunctions.of(this::addOne)
                                                          .partiallyApply(3)
                                                          .println();
supplier.get(); 
(fluent-supplier-Result[4])    
```

## Convert statements to Expressions

It can be handy to convert Java statements (code that does not return a value), into expressions that do return a value.
```java
FluentFunctions.expression(System.out::println)
                       .apply("hello")  
```

## Features

* Integrate multiple cyclops modules and adds :-
* ValueObject and StreamableValue interfaces
* AsValue & AsStreamableValue
* Duck typing As.. static methods

## Example asValue

### Case classes

With prexisting case classes - coerce to ValueObject
```java
    @AllArgsConstructor(access=AccessLevel.PACKAGE)
	static class Parent{ private final int val; }
	@Value
	static class Child extends Parent{
		int nextVal;
		public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
	}
```
Pattern match once coerced to ValueObject
```java
	AsValue.asValue(new Child(10,20))._match(c-> 
			c.isType( (Child child) -> child.val).with(10,20)

```
10,20 matches against fields


Otherwise implement ValueObject
```java
	@AllArgsConstructor(access=AccessLevel.PACKAGE)
	static class Parent implements ValueObject{ private final int val; }
	@Value static class Child extends Parent{
		int nextVal;
		public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
	}
```
## Duck Typing

com.aol.cyclops.dynamic.As

* asDecomposable
* asFunctor
* asMonad
* asMonoid
* asMappable
* asMatchable
* asStreamable
* asStreamableValue
* asSupplier
* asValue


## Stream Reduction with a Functional Java Monoid
```java
        fj.Monoid m = fj.Monoid.monoid((Integer a) -> (Integer b) -> a+b,0);
		Monoid<Integer> sum = As.asMonoid(m);
		
		assertThat(sum.reduce(Stream.of(1,2,3)),equalTo(6));
```		
## Coercing to Matchable
```java
    @AllArgsConstructor
	static class MyCase2 {
		int a;
		int b;
		int c;
	}
	private <I,T> CheckValues<Object, T> cases(CheckValues<I, T> c) {
		return c.with(1,2,3).then(i->"hello")
				.with(4,5,6).then(i->"goodbye");
	}
	 As.asMatchable(new MyCase2(1,2,3)).match(this::cases)
	 
```
Result is hello!

## Coercing to Streamable
```java
    Stream<Integer> stream = Stream.of(1,2,3,4,5);
	Streamable<Integer> streamable = As.<Integer>asStreamable(stream);
	List<Integer> result1 = streamable.stream().map(i->i+2).collect(Collectors.toList());
	List<Integer> result2 = streamable.stream().map(i->i+2).collect(Collectors.toList());
	List<Integer> result3 = streamable.stream().map(i->i+2).collect(Collectors.toList());
```			
	
## Coercing to Monad


This example mixes JDK 8 Stream and Optional types via the bind method
```java
	List<Integer> list = As.<List<Integer>,Stream>asMonad(Stream.of(Arrays.asList(1,3)))
						   .bind(Optional::of)
						   .<Stream<List<Integer>>>unwrap()
						   .map(i->i.size())
						   .peek(System.out::println)
						   .collect(Collectors.toList());

```
## Coerce to ValueObject

```java
    int result = As.asValue(new Child(10,20))._match(c-> c.isType( (Child child) -> child.val).with(10,20))

```	
Result is 10

## Coerce to StreamableValue


StreamableValue allows Pattern Matching and For Comprehensions on implementing classes.
```java
	@Value
	static class BaseData{
		double salary;
		double pension;
		double socialClub;
	}

    Stream<Double> withBonus = As.<Double>asStreamableValue(new BaseData(10.00,5.00,100.30))
									.doWithThisAnd(d->As.<Double>asStreamableValue(new Bonus(2.0)))
									.yield((Double base)->(Double bonus)-> base*(1.0+bonus));
									
					
```
## Coerce to Mappable

```java
	@Value static class MyEntity { int num; String str;} //implies Constructor (int num, String str)
	
    Map<String,?> map = As.asMappable(new MyEntity(10,"hello")).toMap(); 
 ```   
    
 map is ["num":10,"str":"hello"]
 

## Coerce to Supplier 

```java
     static class Duck{
		
		public String quack(){
			return  "quack";
		}
	}
```
```java
    String result = As.<String>asSupplier(new Duck(),"quack").get()
```

Result is "quack" 

## Coerce to Functor

Provide a common way to access Objects with a map method that accepts a single parameter that accepts one value and returns another. Uses invokeDynamic to call the map method, and dynamic proxies to coerce to appropriate Function type, if not JDK 8 function.

```java
	Functor<Integer> functor = As.<Integer>asFunctor(Stream.of(1,2,3));
	Functor<Integer> times2 = functor.map( i->i*2);
```	

# Goals

*   Offer similar functionality as Scala's Try, but with behaviour more in line with current Java development practices
*   Replace throw / catch / finally exception handling with Try wrapper / Monad
*   Allow specified exceptions to be caught
*   Allow Exception recovery
*   Support Try with Resources 
*   Integrate with JDK Optional and Stream
*   Encapsulate success and failed states
*   Fail fast outside of run blocks
*   Offer functional composition over encapsulated state
    

[Try Examples](https://github.com/aol/cyclops/wiki/Try-examples)    

# Why use Try

Throwing exceptions from methods breaks referential transparency and introduces complex goto like control flow. If a method or function can enter an Exceptional state returning a Try object can allow calling code the cleanly handle the Exception or process the result. E.g.
```java
    private Try<Integer,RuntimeException> exceptionalMethod()

// call method, log exception, add bonus amount if successful

    int total = Try.catchExceptions(RuntimeException.class)
                    .run(()-> exceptionalMethod())
                    .onFail(logger::error)
                    .map(i->i+bonus)
                    .orElse(0);
```
## Try allows only specified Exceptions to be caught
    
With Cyclops Try you can specify which exceptions to catch. This behaviour is similar to JDK Optional, in that you use Try to consciously encapsulate the exceptional state of the method - not to capture unknown exceptional states. 

### Comparison with Optional

With Optional, best practices is to use it when no result is valid result (not automatically on every method - whereby accidental null states - bugs! - are encapsulated in an Optional.

For Try this would mean, if a function is trying to load a file handling FileNotFoundException and IOException is reasonable - handling ClassCastExceptions or NullPointerExceptions may hide bugs. Bugs that you would be better off finding in unit tests early in your development cycle.

## Try with resources
```java
    Try.catchExceptions(FileNotFoundException.class,IOException.class)
                   .init(()->new BufferedReader(new FileReader("file.txt")))
                   .tryWithResources(this::read)
                   
    private String read(BufferedReader br) throws IOException{
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        String everything = sb.toString();
        return everything;
    }
```
### Try with multiple resources

Any iterable can be used in the init method when using Try with resources, in Closeables returned in the Iterable will be closed after the main block has been executed.
```java
    Try.catchExceptions(FileNotFoundException.class,IOException.class)
    .init(()->Tuples.tuple(new BufferedReader(new FileReader("file.txt")),new   FileReader("hello")))
                   .tryWithResources(this::read2)
```
### Differentiated recovery

onFail can recover from any Exception or specified Exceptions
```java
     Try.runWithCatch(this::loadFile,FileNotFoundException.class,IOException.class)
                    .onFail(FileNotFoundException.class,extractFromMemoryCace())
                    .onFail(IOException.class,storeForTryLater())
                    .get()
```
## Try versus Try / Catch

### Try as return type

A JDK 8 readLine method 
```java
    public String readLine() throws IOException
```
Could be rewritten as
```java
    public Try<String,IOException> readLine()
``` 
This forces user code to handle the IOException (unlike Scala's Try monad). Try is less suitable for methods that return multiple different Exception types, although that is possibly a signal that your method is doing more than one thing and should be refactored.

### Checked and Unchecked Exceptions

Try naturally converts Checked Exceptions into Unchecked Exceptions. Consider a method that may throw the Checked IOExeption class. If application Code decides that IOException should NOT be handled, it can simply be thrown without requiring that the rest of the call stack become polluted with throws IOException declarations.

E.g.
```java
    public Try<String,IOException> readLine();
    
    Try<String,IOException> result = readLine();
    result.throwException(); //throws a softened version of IOException
    
    result.map(this::processResult)... 
``` 
### Alternatives and differences

This implementation of Try differs from both the Scala and the Javaslang version. Javaslang Try seems to be very similar in it's implementation to the Scala Try and both will capture all Exceptions thrown at any stage during composition. So if calling Try -> map -> flatMap -> map results in an exception during the map or flatMap state Try will revert to a failure state incorporating the thrown Exception. 

By contrast Cyclops Try only captures Exceptions during specific 'withCatch' phases - which correspond to the initialisation and main execution phases. In the Java world this is equivalent to a Try / Catch block. The further safe processing of the result is not automatically encapsulated in a Try (although developers could do so explicitly if they wished).


#Feature Toggle

Cyclops Feature Toggle makes delivering on CI/CD easy by making it very simple to turn production features on & off!



### Rationale

Concrete type that conveys that a feature may be disabled or may be enabled (switchable).



#### Feature Toggle

* Enable / Disable classes (Pattern Match by type)
* convert to Optional or Stream
* standard Java 8 operators (map, flatMap, peek, filter, forEach) + flatten etc
* isEnabled / isDisabled
* Biased towards enabled (right biased).


### Getting started

The most basic way to use it is (if you are used to programming imperatively)

```java
    if(featureDisabled) 
          return FeatureToggle.disable(data);
    else
        return FeatureToggle.enable(data);

```

Now elsewhere you can check if the switch is enabled or disabled

```java
    FeatureToggle<Data> toggle;
    if(toggle.isEnabled()){
          loadDataToDb(toggle.get());
    }

```

### More advanced usage
 
FeatureToggle  can abstract away entirely the logic for managing whether a feature is enabled or disabled. Users can just code the enabled case and FeatureToggle  will automatically make sure nothing happens when disabled.

The statement above can be rewritten as -
```java

    toggle.map(data -> loadDataToTheDb(data));
```
### Example usage

Creating the FeatureToggle 

```java
    public synchronized FeatureToggle<Supplier<List<DomainExpression>>> readFile() {
        Supplier<List<DomainExpression>> s = ()->serialisedFileReader.readFileFromDisk(rawDomainRuleFileLocation);
        if (rawDomainEnabled) {
            return new Enabled(s);
        }
        return new Disabled(s);

    }
```

Using the Switch 

```java
    FeatureToggle<Supplier<List<DomainExpression>>> domainExpressions; //lazy load data from db
     ...

    domainExpressions.stream().flatMap(s -> s.get().stream()).forEach(domainExpression->{
        
                definitions.put(domainExpression.getDerivedAttributeId(), domainExpression.getExpression());
                timestamps.put(domainExpression.getDerivedAttributeId(), domainExpression.getTimestamp());
            
        });

```

## Power Tuples

1. Wrap any Tuple type / Object (mapping fields to elements and back)
2. Method call chaining support
3. Asyncrhonous method call chaining support
4. Inheritance relationship between Tuples
5. Lazy and Strict map methods
6. Lazy reordering
7. Pattern matching
8. For comprehensions
9. Useful utility methods (asStreamOfStrings, asTwoNumbers etc)
10. Concatonation
11. LazySwap (reverse)
12. Memoization
13. asCollector
14. asReducer

## Entry Point

com.aol.cyclops.lambda.tuple.PowerTuples 

Has static creational methods for PTuples1..8 as well as Tuple Concatenation and Lazily Swapping (reversing) values.

e.g.

     lazySwap(PowerTuples.tuple(1, 2, 3))

### Wrap any Tuple type

#### Convert from Tuple (or Object)

    @Test
    public void cons(){
        Three three = new Three(1,"hello",new Date());
        assertThat(three.a,equalTo(PTuple3.ofTuple(three).v1()));
    }
    @AllArgsConstructor
    static class Three{
        int a;
        String b;
        Date c;
    }

#### Convert to Tuple (or Object)

    @AllArgsConstructor
    static class TwoParams{
        int num;
        String value;
    }
    
    TwoParams p  = PowerTuples.tuple(10,"hello").convert().to(TwoParams.class);
    assertThat(p.num,equalTo(10));
    assertThat(p.value,equalTo("hello"));

### Method call chaining

With filtering

    method1().<PTuple1<Integer>>filter(t->t.v1()==0).call(this::method3);
    

Async method chaining

    method1().<PTuple1<Integer>>filter(t->t.v1()==0).callAsync(this::method3).join();
    
### Conversion to Streams

Tuples can also be converted to flattened or unflattened Stream of Streams. asStreams will attempt to create a Stream from each element (via Collection::stream for example). BufferedReaders, Files, URLs, Arrays, Collections, CharSequences will all be turned into Streams.

## asCollector

A tuple of Collectors can be coerced to a single Collector

e.g. Collecting as a List and Set simultaneously

       PTuple2<Set<Integer>,List<Integer>> res = Stream.of(1, 2, 2)
                       .collect(tuple(Collectors.toSet(),Collectors.toList()).asCollector());

See rich set of Collectors here [java.util.stream.Collectors](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html)

## asReducer

Convert a tuple into a single Monoid (or Reducer) that can perform multiple reduce operations on a single Stream.


       Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
       Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
       val result = tuple(sum,mult).<PTuple2<Integer,Integer>>asReducer()
                                            .mapReduce(Stream.of(1,2,3,4)); 



Or alternatively


      Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
      Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
      val result = tuple(sum,mult).<PTuple2<Integer,Integer>>asReducer()
                                            .mapReduce(Stream.of(1,2,3,4)); 
         
        assertThat(result,equalTo(tuple(10,24)));
        
## Pattern Matching


    String result = PowerTuples.tuple(1,2,3).matchValues(c -> cases(c)   );

    private <I,T> _MembersMatchBuilder<Object, T> cases(_MembersMatchBuilder<I, T> c) {
        return c.with(1,2,3).then(i->"hello")
                .with(4,5,6).then(i->"goodbye");
    }
    
Result is "hello"
      
# Dependencies


cyclops-functions
cyclops-streams
cyclops-pattern-matching


