# cyclops-functions

[USER GUIDE](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/lambdas.adoc)

## Getting cyclops-functions

* [![Maven Central : cyclops-functions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-functions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-functions)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-functions:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-functions</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Overview

Utilities for working with Lambda expressions that capture values or variables in the enclosing scope.

* Mutable
* MutableInt
* MutableDouble
* MutableLong
* MutableBoolean
* MutableByte
* MutableShort
* MutableChar
* MutableFloat
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
# Features

Light weight module (no dependencies) which adds the following features for Java 8 Functions, Consumers and Suppliers 

* Caching / Memoization
* Currying
* Partial Application
* Type inference

## Caching / Memiozation

Use static Memoize methods on Memoize utility class to create a cachable reference to a function, suppler or method. Provide custom cache interfaces via the Cachable functional interface

## Currying

Cyclops can convert any function (with up to 8 inputs) or method reference into a chain of one method functions (Currying). This technique is a useful (and more safe) alternative to Closures. The Curried function can be created and values explicitly passed in rather than captured by the compiler (where-upon they may change).

#### Currying method references 
```java
	  import static com.aol.cyclops.functions.Curry.*;
	  
	  
      assertThat(curry2(this::mult).apply(3).apply(2),equalTo(6));
      
      public Integer mult(Integer a,Integer b){
		return a*b;
	 }
```	 

#### Currying in place

```java
      		assertThat(Curry.curry2((Integer i, Integer j) -> "" + (i+j) +   "hello").apply(1).apply(2),equalTo("3hello"));
 ```    		

#### Uncurry

```java
      assertThat(Uncurry.uncurry3((Integer a)->(Integer b)->(Integer c)->a+b+c)
								.apply(1,2,3),equalTo(6));
```								

#### Type inferencing help

```java
      import static com.aol.cyclops.functions.Lambda.*;
	 
	 
	   Mutable<Integer> myInt = Mutable.of(0);
		
		Lambda.l2((Integer i)-> (Integer j)-> myInt.set(i*j)).apply(10).apply(20);
		
		assertThat(myInt.get(),
				is(200));
```
#### Curry Consumer

```java
     		CurryConsumer.curry4( (Integer a, Integer b, Integer c,Integer d) -> value = a+b+c+d).apply(2).apply(1).apply(2).accept(3);
     		
		assertThat(value,equalTo(8));
```	
#### Uncurry Consumer 

```java
     UncurryConsumer.uncurry2((Integer a)->(Integer b) -> value = a+b ).accept(2,3);
	 assertThat(value,equalTo(5));
```
# Exception softening examples

## No need to declare CheckedExxceptions

(Or even wrapping them inside RuntimeException)

```java
public Data load(String input) {
         try{
        
        
        }catch(IOException e) {
        
            throw ExceptionSoftener.throwSoftenedException(e);
         }
```   

In the above example IOException can be thrown by load, but it doesn't need to declare it.

## Wrapping calls to methods

### With functional interfaces and lambda's
Where we have existing methods that throw softened Exceptions we can capture a standard Java 8 Functional Interface that makes the call and throws a a softened exception


```java

Function<String,Data> loader = ExceptionSoftener.softenFunction(file->load(file));

public Data load(String file) throws IOException{
     ///load data
}  

```
### Inside a stream

Stream.of("file1","file2","file3")
      .map(ExceptionSoftener.softenFunction(file->load(file)))
      .forEach(this::save)
    
    

### With method references
    
We can simplify further with method references.


```java

Data loaded = ExceptionSoftener.softenFunction(this::load).apply(fileName);
    
Stream.of("file1","file2","file3")
      .map(ExceptionSoftener.softenFunction(this::load))
      .forEach(this::save)      
        
```  

# Mixin functionality

* Mappable
* Printable
* Gettable

Mappable allows any Object to be converted to a map.
Printable adds print(String) to System.out functionality
Gettable allows any Object to be converted to a Supplier

* Coerce / wrap to interface

        asStreamable
        asDecomposable
        asMappable
        asFunctor
        asGenericMonad
        asGenericMonoid
        asSupplier
        



    

# Coerce to Map 

This offers and alternative to adding getters to methods solely for making state available in unit tests.

Rather than break production level encapsulation, in your tests coerce your producition object to a Map and access the fields that way.

```java
    @Test
    public void testMap(){
        Map<String,?> map = AsMappable.asMappable(new MyEntity(10,"hello")).toMap();
        System.out.println(map);
        assertThat(map.get("num"),equalTo(10));
        assertThat(map.get("str"),equalTo("hello"));
    }
    @Value static class MyEntity { int num; String str;}

```
  

            

                        
# Printable interface

Implement Printable to add  the following method

```java
    T print(T object)
```
    
Which can be used inside functions to neatly display the current value, when troubleshooting functional code e.g.

```java
    Function<Integer,Integer> fn = a -> print(a+10);
``` 

# Features

* Cumulative Validation


## Examples

Accumulate 

```java
ValidationResults results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
                                                .isValid(user->user.email!=null, "user email null","email ok")
                                                .accumulate(new User(10,"email@email.com"));
    
        assertThat(results.getResults().size(),equalTo(2));
```

Accumulate until fail

```java
    ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
                                                .add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
                                                .accumulateUntilFail(new User(10,"email@email.com"));
    
        assertThat(results.getResults().size(),equalTo(1));
```
	 
# Trampoline Overview


## Stackless recursion

Trampolines can be used to turn tail recursive calls into a more iterative form, lessening the likelihood of StackOverflow errors 
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

The code above could be further simplified using static imports
```java
    @Test
    public void trampolineTest(){
        
        assertThat(loop(500000,10).result(),equalTo(446198426));
        
    }
    Trampoline<Integer> loop(int times,int sum){
        
        if(times==0)
            return done(sum);
        else
            return more(()->loop(times-1,sum+times));
    }
``` 

## Simulating coroutines and continuations

Trampolines can be used to interleave the execution of different functions on the same thread, in a generic if ugly way

```java
    List results;
    @Test
    public void coroutine(){
        results = new ArrayList();
        Iterator<String> it = Arrays.asList("hello","world","end").iterator();
        Trampoline[] coroutine = new Trampoline[1];
        coroutine[0] = Trampoline.more( ()-> it.hasNext() ? print(it.next(),coroutine[0]) : Trampoline.done(0));
        withCoroutine(coroutine[0]);
        
        assertThat(results,equalTo(Arrays.asList(0,"hello",1,"world",2,"end",3,4)));
    }
    
    private Trampoline<Integer> print(Object next, Trampoline trampoline) {
        System.out.println(next);
        results.add(next);
        return trampoline;
    }
    public void withCoroutine(Trampoline coroutine){
        
        for(int i=0;i<5;i++){
                print(i,coroutine);
                if(!coroutine.complete())
                    coroutine= coroutine.bounce();
                
        }
        
    } 
```


