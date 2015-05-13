todos :
	test against enable switch / try etc
	test adding more monadic converters
	test adding more comprehenders
	update documentation

# for comprehensions explained

For comprehensions are useful for iterating over nested structures (e.g. collections, Streams, Optionals, CompletableFutures or other Monads).
    
Given a list of Strings 

     List<String> list = Arrays.asList("hello","world","3");
     
We can iterate over them using Java 5 'foreach' syntax
     
     for(String element : list){
     	System.out.println(element);
     }
     

The equivalent for comprehension would be 

     ForComprehensions.foreach1(c -> c.mapAs$1(list)
                                	  .run( (Vars1<String> v) -> System.out.println(v.$1())
                                      
If we nest our looping
	
	  List<Integer> numbers = Arrays.asList(1,2,3,4);

	  for(String element : list){
	     for(Integer num : numbers){
     		System.out.println(element + num);
     	  }
      }                              

Things start to become a little unwieldy, but a little less so with for comprehensions
      
     ForComprehensions.foreach2(c -> c.flatMapAs$1(list)
                                      .mapAs$2((Vars2<String,Integer> v)->numbers)                                                    
                                      .run(v -> System.out.println(v.$1()+v.$2())


Let's add a third level of nesting

    List<Date> dates = Arrays.asList(new Date(),new Date(0));

    for(String element : list){
	     for(Integer num : numbers){
	    	 for(Date date : dates){
     			System.out.println(element + num + ":" + date);
     	 	 }
     		
     	  }
      }
    
 And the for comprehension looks like 
   
     ForComprehensions.foreach3(c -> c.flatMapAs$1(list)
                                      .flatMapAs$2((Vars<String,Integer,Date> v) -> numbers)
                                      .mapAs$2(v -> dates)                                                    
                                      .run( v-> System.out.println(v.$1()+v.$2()+v.$3())
 
      
     list.stream()
         .map(element -> element.toUpperCase())
         .collect(Collectors.toList());
         
         
Can be written as

	  ForComprehensions.foreach1(c -> c.mapAs$1(list))
	                                   .yield( (Vars1<String> v) -> c.$1().toUpperCase())
	                    .collect(Collectors.toList());
     
 ## Mixing types
 
 Running a for comprehension over a list (stream) and an Optional
 
     val strs = Arrays.asList("hello","world");  //using Lombok val
	 val opt = Optional.of("cool");
		
		
	  Seq<String> results = ForComprehensions.foreach2( c-> c.flatMapAs$1(strs)
										 .mapAs$2((Vars2<String,String> v) -> opt)
										 .yield( v -> v.$1() + v.$2()));
										 
Outputs : [hellocool, worldcool]


Or the other way around 


        val strs = Arrays.asList("hello","world");
		val opt = Optional.of("cool");
		
		
		Optional<List<String>> results = ForComprehensions.foreach2( c-> c.flatMapAs$1(opt)
										 .mapAs$2( (Vars2<String,String> v) -> strs)
										 .yield( v -> v.<String>$1() + v.$2()));
		
		assertThat(results.get(),hasItem("coolhello"));
		assertThat(results.get(),hasItem("coolworld"));
		
Outputs : [[coolhello],[coolworld]]

## Filtering

## Convert any Object to a Monad

### Stream conversions

Collection to Stream
Iterable to Stream
Iterator to Stream
Array to Stream
Int to IntStream.range(int)
File to Stream
URL to Stream
BufferedReader to Stream
InputStream to Stream
ResultSet to Stream
Enum to Stream
String to Stream

ObjectToStreamConverter

### Optional conversions

NullToOptionalConverter
Optional<Primitive> to Optional

### CompletableFuture Conversionss

Callable to CompletableFuture
Supplier to CompletableFuture

## Dynamic Proxy Caching

To support Monads that use non standard Functional interfaces, Cyclops will create / cache and reuse dynamic proxies that wrap JDK8 Functional interfaces in suitable wrappers.


# Do Notation

	List<Integer> list= Arrays.asList(1,2,3);
	Stream<Integer> stream = Do.withVars(letters)
								.assign(a, list)
								.filter(λ1((Integer a) -> a>2))
								.yield(λ1((Integer a)-> a +2) );
				
										
		
		assertThat(Arrays.asList(5),equalTo(stream.collect(Collectors.toList())));


Yield and Filter take curried functions

		Stream<Integer> stream = Do.withVars(Do.letters)
								   .assign(Do.Letters.a, Arrays.asList(20,30))
								   .assign(Do.Letters.b, Arrays.asList(1,2,3))
								   .yield(λ2((Integer a)-> (Integer b) -> a + b+2) );

