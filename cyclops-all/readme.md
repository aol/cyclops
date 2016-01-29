# cyclops-all

cyclops-all adds all non-integration modules to your classpath and additional provides Collection eXtension and FluentFunctions functionality (since 7.3.0).

* [USER GUIDE : lambdas](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/lambdas.adoc)
* [USER GUIDE : collections ](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/Collections.adoc)
* [USER GUIDE : streams ](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/streams.adoc)

## Getting cyclops-all

* [![Maven Central : cyclops-all](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-all/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-all)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-all:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-all</artifactId>
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
