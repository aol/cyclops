# cyclops-core

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
# Dependencies

cyclops-closures
cyclops-functions
cyclops-monad-api
cyclops-streams
cyclops-monad-functions
cyclops-pattern-matching
cyclops-pattern-matching-collections
cyclops-pattern-matching-recursive
cyclops-trampoline
cyclops-mixins
cyclops-for-comprehensions

