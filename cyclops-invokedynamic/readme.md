# cyclops-invokedynamic

[USER GUIDE](http://gist.asciidoctor.org/?github-aol/cyclops//user-guide/lambdas.adoc)

Utility classes used within cyclops for fast dynamic method calls and variable lookup

## Getting cyclops-invokedynamic

* [![Maven Central : cyclops-invokedynamic](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-invokedynamic/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-invokedynamic)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-invokedynamic:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-invokedynamic</artifactId>
    <version>x.y.z</version>
</dependency>
```


# Dependencies

org.jooq:jool:0.9.7

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