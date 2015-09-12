# cyclops-mixins

## Getting cyclops-mixins

* [![Maven Central : cyclops-mixins](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-mixins/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-mixins)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-mixins:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-mixins</artifactId>
    <version>x.y.z</version>
</dependency>
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


# Dependencies

cyclops-invokedynamic

	
	