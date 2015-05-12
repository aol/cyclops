# Features

1. Generic Monad operations
2. Specific and InvokeDynamic based Monadic comprehension (for use in cyclops-for-comprehension and elsewhere)
3. Utils for working with Closures / captured values & variables
4. Currying
5. Coerce to decomposable / map
6. Dynamic tuples


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

### ImmutableClosedValue 

Set values once only inside a Closure.


        ImmutableClosedValue<Integer> value = new ImmutableClosedValue<>();
		Supplier s= () -> value.getOrSet(()->10);
		assertThat(s.get(),is(10));
		assertThat(value.getOrSet(()->20),is(10));
		
### ClosedVar 

Fully mutable variable wrapper manipulatable inside a closure	  
	 
	 import static com.aol.cyclops.comprehensions.functions.Lambda.*;
	 
	 
	   ClosedVar<Integer> myInt = new ClosedVar<>(0);
		
		λ2((Integer i)-> (Integer j)-> myInt.set(i*j)).apply(10).apply(20);
		
		assertThat(myInt.get(),
				is(200));

um.. λ2 ? (Type inferencing helper :) - and without it 

		


        ClosedVar<Integer> myInt = new ClosedVar<>(0);
		
		BiFunction<Integer,Integer,ClosedVar<Integer>> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.get(),
				is(200));
			
## Currying

Cyclops can convert any function (with up to 8 inputs) or method reference into a chain of one method functions (Currying). This technique is a useful (and more safe) alternative to Closures. The Curried function can be created and values explicitly passed in rather than captured by the compiler (where-upon they may change).

#### Currying method references 

	  import static com.aol.cyclops.comprehensions.functions.Curry.*;
	  
	  
      assertThat(curry2(this::mult).apply(3).apply(2),equalTo(6));
      
      public Integer mult(Integer a,Integer b){
		return a*b;
	 }
	 

#### Currying in place

      		assertThat(Curry.curry2((Integer i, Integer j) -> "" + (i+j) +   "hello").apply(1).apply(2),equalTo("3hello"));
      		

#### Uncurry

      assertThat(Uncurry.uncurry3((Integer a)->(Integer b)->(Integer c)->a+b+c)
								.apply(1,2,3),equalTo(6));
								

#### Type inferencing help

      import static com.aol.cyclops.comprehensions.functions.Lambda.*;
	 
	 
	   ClosedVar<Integer> myInt = new ClosedVar<>(0);
		
		λ2((Integer i)-> (Integer j)-> myInt.set(i*j)).apply(10).apply(20);
		
		assertThat(myInt.get(),
				is(200));

#### Curry Consumer

     		CurryConsumer.curry4( (Integer a, Integer b, Integer c,Integer d) -> value = a+b+c+d).apply(2).apply(1).apply(2).accept(3);
     		
		assertThat(value,equalTo(8));
		
#### Uncurry Consumer 

     UncurryConsumer.uncurry2((Integer a)->(Integer b) -> value = a+b ).accept(2,3);
	 assertThat(value,equalTo(5));
     
## Coerce to decomposable / map

#### Coerce to Map 

This offers and alternative to adding getters to methods solely for making state available in unit tests.

Rather than break production level encapsulation, in your tests coerce your producition object to a Map and access the fields that way.

    @Test
	public void testMap(){
		Map<String,?> map = CoerceToMap.toMap(new MyEntity(10,"hello"));
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
		assertThat(CoerceToDecomposable.coerceToDecomposable(new MyCase("key",10))
				.unapply(),equalTo(Arrays.asList("key",10)));
	}
	
	@Value
	static class MyCase { String key; int value;}
	
## Dynamic Tuples

1. Wrap any Tuple type
2. Method call chaining support
3. Asyncrhonous method call chaining support
4. Inheritance relationship between Tuples
5. Lazy and Strict map methods
6. Useful utility methods (asStreamOfStrings, asTwoNumbers etc)

### Wrap any Tuple type

#### Convert from Tuple (or Object)

    @Test
	public void cons(){
		Three three = new Three(1,"hello",new Date());
		assertThat(three.a,equalTo(Tuple3.ofTuple(three).v1()));
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
	
    TwoParams p  = Tuples.tuple(10,"hello").convert().to(TwoParams.class);
	assertThat(p.num,equalTo(10));
	assertThat(p.value,equalTo("hello"));

### Method call chaining

With filtering

    method1().<Tuple1<Integer>>filter(t->t.v1()==0).call(this::method3);
    

Async method chaining

	method1().<Tuple1<Integer>>filter(t->t.v1()==0).callAsync(this::method3).join();
      