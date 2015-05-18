# Features

*  Generic Monad operations
*  Specific and InvokeDynamic based Monadic comprehension (for use in cyclops-for-comprehension and elsewhere)
*  Mutable / LazyImmutable Utils for working with Closures / captured values & variables
* Interfaces
	
		Streamable  : repeatable stream()
		Decomposable (for Value objects) : unapply()
		Mappable (convert fields to Map)  : toMap()
		Functor (Generic functor interface) : map(Function)
		Monad (Generic Monad interface) : flatMap(Function)
		Gettable : get()

* Coerce / wrap to interface

		asStreamable
		asDecomposable
		asMappable
		asFunctor
		asMonad
		



## Generic Monad Operations

Wrap and nest any Monadic type :

      val list = MonadWrapper.<List<Integer>,Stream>of(Stream.of(Arrays.asList(1,3)))
				.bind(Optional::of).<Stream<List<Integer>>>unwrap()
				.map(i->i.size())
				.peek(System.out::println)
				.collect(Collectors.toList());
		assertThat(Arrays.asList(2),equalTo(list));
		
bind :-> flatMap

Not possible to flatMap an Optional inside a Stream in JDK, but you can with the MonadWrapper (or any other type of Monad)
		
## Closure utils

### LazyImmutable

Set values once only inside a Closure.


        LazyImmutable<Integer> value = new LazyImmutable<>();
		Supplier s= () -> value.getOrSet(()->10);
		assertThat(s.get(),is(10));
		assertThat(value.getOrSet(()->20),is(10));
		
### Mutable

Fully mutable variable wrapper manipulatable inside a closure	  
	 
	 import static com.aol.cyclops.lambda.utils.Lambda.*;
	 
	 
	   Mutable<Integer> myInt = new Mutable<>(0);
		
		λ2((Integer i)-> (Integer j)-> myInt.set(i*j)).apply(10).apply(20);
		
		assertThat(myInt.get(),
				is(200));

um.. λ2 ? (Type inferencing helper :) - and without it 

		


        Mutable<Integer> myInt = Mutable.of(0);
		
		BiFunction<Integer,Integer,ClosedVar<Integer>> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.get(),
				is(200));
			
  
## Coerce to decomposable / mappable / streamable / functor / monad

#### Coerce to Map 

This offers and alternative to adding getters to methods solely for making state available in unit tests.

Rather than break production level encapsulation, in your tests coerce your producition object to a Map and access the fields that way.

    @Test
	public void testMap(){
		Map<String,?> map = AsMappable.asMappable(new MyEntity(10,"hello")).toMap();
		System.out.println(map);
		assertThat(map.get("num"),equalTo(10));
		assertThat(map.get("str"),equalTo("hello"));
	}
	@Value static class MyEntity { int num; String str;}
}
  
### Coerce to Decomposable

The Decomposable interface specifies an unapply method (with a default implementation) that decomposes an Object into it's elemental parts. It used used in both Cyclops Pattern Matching (for recursively matching against Case classes) and Cyclops for comprehensions (where Decomposables can be lifted to Streams automatically on input - if desired).

     @Test
	public void test() {
		assertThat(AsDecomposable.asDecomposable(new MyCase("key",10))
				.unapply(),equalTo(Arrays.asList("key",10)));
	}
	
	@Value
	static class MyCase { String key; int value;}
	
	
## Type inferencing help

    import static com.aol.cyclops.functions.Lambda.*;
 
 
    Mutable<Integer> myInt = Mutable.of(0);
    
    λ2((Integer i)-> (Integer j)-> myInt.set(i*j)).apply(10).apply(20);
    
    assertThat(myInt.get(),
            is(200));
	