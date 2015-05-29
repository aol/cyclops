# Custom Extensions

Cyclops performs generic Monadic operations by virtue of the following interfaces

* Comprehender
* MonadicConverter

Comprehender is used by Cyclops to 'understand' how apply operations on a Monad. 

MonadicConverter is used by Cyclops to 'lift' types which are not Monads into a Monadic form.

## Creating a Comprehender

The methods to implement are :-

* map  
* filter
* flatMap
* of(value)
* empty()
* getTargetClass

Optionally implement 

* handleReturnForCrossTypeFlatMap

### Implementing Map

Map in Comprehender accepts a JDK 8 Function. Your implementation should convert to whatever function type the Monad being wrapped uses and call the underlying map method.

### Implementing FlatMap

Map in Comprehender accepts a JDK 8 Function. Your implementation should convert to whatever function type the Monad being wrapped uses and call the underlying map method.

### Implementing Filter
 
Filter in Comprehender accepts a JDK 8 Predicate. Your implementation should convert to whatever function type the Monad being wrapped uses and call the underlying filter method.

### Implementing of(value) and empty()

These are creational methods used when cross Monadic types are returned from flatMap execution. 

E.g. if we have a Stream of data and apply a function that may return Optional. Applying that function via 'map' will result in a Stream of Optionals. Using the 'bind' (flatMap equivalent, in this case with type safety loosened to allow different types to be returned) function in the Monad interface we will instead have a Stream of non-Optional data. All Optional.empty results will have been removed.  The of(value) and empty() methods on the Stream Comprehender will have been invoked for Optional.present and Optional.empty values respectively.

### Implementing getTargetClass

Return the identifying class type for the targeted Monad (e.g. Stream.class, Optional.class etc).


### Implementing handleReturnForCrossTypeFlatMap

Implement this if you want to be able to return your type in For Comprehensions or bind operations for other Monads and behave in a special way. For example - we could unwrap optionals and remove empties with a Stream

e.g.

     List<Integer> list = MonadWrapper.<Stream<Integer>,List<Integer>>of(Stream.of(Arrays.asList(1,3),null))
				.bind(Optional::ofNullable)
				.map(i->i.size())
				.peek(System.out::println)
				.toList();
				
The way this funky functionality is implemented is to ask the current Comprehender to create a new instance of it's Monad type that contains the unwrapped Optional if present, otherwise it asks the current Comprehender if it can create an 'empty' instance of it's Monad.
		
		Optional result..
		if (result.isPresent())
			return comprehender.of(((Optional) apply).get());
		return comprehender.empty();
					

### Registering a Comprehender

Create a file in META-INF/services/com.aol.cyclops.lambda.api.Comprehender

And add the fully qualified name of your implementing class/es to it. E.g com.aol.cyclops.comprehensions.comprehenders.StreamComprehender.

Cyclops will then pick up your Comprehender automatically.

## Monadic Converters

Monadic Converters are used to Lift non-Monadic types into Monads in for-comphrensions and via the liftAndBind method on the Cyclops Monad wrapper. Examples of Converters included with cyclops include  lifting of Collections to Streams, of Null values to Optionals, Strings (and all CharSequences) to Streams, Files and URLs to Streams of their Content, Suppliers to async CompletableFuture Operations and more.

The MonadicConverter interface

### accept(Object)

Accept is used to determine if this converter should / can apply to provided instance

## convertToMonadicForm(Object)

Converts a given Object (which has already passed the accept test) into a new Monadic Form.

## priority 

Is used to determine priority level - default is 5. All Cyclops Converters have a mutable public static field Priority that you can use (judiciously) to mitigate clashes. We recommend that any Converters you create also follow this pattern.



### Registering a MonadicConverter

Create a file in com.aol.cyclops.lambda.api.MonadicConverter

And add the fully qualified name of your implementing class/es to it. E.g com.aol.cyclops.comprehensions.converters.BufferedReaderToStreamConverter.

Cyclops will then pick up your Converter automatically.




