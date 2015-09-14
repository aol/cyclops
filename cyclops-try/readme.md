# Cyclops Try

Cyclops Try offers an alternative way to manage exception handling.

## Getting cyclops-try

* [![Maven Central : cyclops-try](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-try/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-try)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-try:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-try</artifactId>
    <version>x.y.z</version>
</dependency>
```


#Goals

* 	Offer similar functionality as Scala's Try, but with behaviour more in line with current Java development practices
*	Replace throw / catch / finally exception handling with Try wrapper / Monad
*	Allow specified exceptions to be caught
*	Allow Exception recovery
*	Support Try with Resources 
*	Integrate with JDK Optional and Stream
* 	Encapsulate success and failed states
* 	Fail fast outside of run blocks
*	Offer functional composition over encapsulated state
	
	

# Why use Try

Throwing exceptions from methods breaks referential transparency and introduces complex goto like control flow. If a method or function can enter an Exceptional state returning a Try object can allow calling code the cleanly handle the Exception or process the result. E.g.

	private Try<Integer,RuntimeException> exceptionalMethod()

// call method, log exception, add bonus amount if successful

	int total = Try.catchExceptions(RuntimeException.class)
					.run(()-> exceptionalMethod())
					.onFail(logger::error)
					.map(i->i+bonus)
					.orElse(0);

## Try allows only specified Exceptions to be caught
	
With Cyclops Try you can specify which exceptions to catch. This behaviour is similar to JDK Optional, in that you use Try to consciously encapsulate the exceptional state of the method - not to capture unknown exceptional states. 

### Comparison with Optional

With Optional, best practices is to use it when no result is valid result (not automatically on every method - whereby accidental null states - bugs! - are encapsulated in an Optional.

For Try this would mean, if a function is trying to load a file handling FileNotFoundException and IOException is reasonable - handling ClassCastExceptions or NullPointerExceptions may hide bugs. Bugs that you would be better off finding in unit tests early in your development cycle.

## Try with resources

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

### Try with multiple resources

Any iterable can be used in the init method when using Try with resources, in Closeables returned in the Iterable will be closed after the main block has been executed.

    Try.catchExceptions(FileNotFoundException.class,IOException.class)
	.init(()->Tuples.tuple(new BufferedReader(new FileReader("file.txt")),new   FileReader("hello")))
				   .tryWithResources(this::read2)

### Differentiated recovery

onFail can recover from any Exception or specified Exceptions

     Try.runWithCatch(this::loadFile,FileNotFoundException.class,IOException.class)
					.onFail(FileNotFoundException.class,extractFromMemoryCace())
					.onFail(IOException.class,storeForTryLater())
					.get()

## Try versus Try / Catch

### Try as return type

A JDK 8 readLine method 

	public String readLine() throws IOException

Could be rewritten as

	public Try<String,IOException> readLine()
	
This forces user code to handle the IOException (unlike Scala's Try monad). Try is less suitable for methods that return multiple different Exception types, although that is possibly a signal that your method is doing more than one thing and should be refactored.

### Checked and Unchecked Exceptions

Try naturally converts Checked Exceptions into Unchecked Exceptions. Consider a method that may throw the Checked IOExeption class. If application Code decides that IOException should NOT be handled, it can simply be thrown without requiring that the rest of the call stack become polluted with throws IOException declarations.

E.g.

	public Try<String,IOException> readLine();
	
	Try<String,IOException> result = readLine();
	result.throwException(); //throws a softened version of IOException
	
	result.map(this::processResult)... 
	
### Alternatives and differences

This implementation of Try differs from both the Scala and the Javaslang version. Javaslang Try seems to be very similar in it's implementation to the Scala Try and both will capture all Exceptions thrown at any stage during composition. So if calling Try -> map -> flatMap -> map results in an exception during the map or flatMap state Try will revert to a failure state incorporating the thrown Exception.	

By contrast Cyclops Try only captures Exceptions during specific 'withCatch' phases - which correspond to the initialisation and main execution phases. In the Java world this is equivalent to a Try / Catch block. The further safe processing of the result is not automatically encapsulated in a Try (although developers could do so explicitly if they wished).