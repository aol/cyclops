# Integration layer

* ValueObject and StreamableValue interfaces
* AsValue & AsStreamableValue
* Duck typing As.. static methods

## Example asValue

### Case classes

With prexisting case classes - coerce to ValueObject

    @AllArgsConstructor(access=AccessLevel.PACKAGE)
	static class Parent{ private final int val; }
	@Value
	static class Child extends Parent{
		int nextVal;
		public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
	}
	
Pattern match once coerced to ValueObject

	AsValue.asValue(new Child(10,20))._match(c-> 
			c.isType( (Child child) -> child.val).with(10,20)


10,20 matches against fields


Otherwise implement ValueObject

	@AllArgsConstructor(access=AccessLevel.PACKAGE)
	static class Parent implements ValueObject{ private final int val; }
	@Value static class Child extends Parent{
		int nextVal;
		public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
	}

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

        fj.Monoid m = fj.Monoid.monoid((Integer a) -> (Integer b) -> a+b,0);
		Monoid<Integer> sum = As.asMonoid(m);
		
		assertThat(sum.reduce(Stream.of(1,2,3)),equalTo(6));
		
## Coercing to Matchable

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
	 

Result is hello!

## Coercing to Streamable

    Stream<Integer> stream = Stream.of(1,2,3,4,5);
	Streamable<Integer> streamable = As.<Integer>asStreamable(stream);
	List<Integer> result1 = streamable.stream().map(i->i+2).collect(Collectors.toList());
	List<Integer> result2 = streamable.stream().map(i->i+2).collect(Collectors.toList());
	List<Integer> result3 = streamable.stream().map(i->i+2).collect(Collectors.toList());
			
	
## Coercing to Monad


This example mixes JDK 8 Stream and Optional types via the bind method

	List<Integer> list = As.<List<Integer>,Stream>asMonad(Stream.of(Arrays.asList(1,3)))
						   .bind(Optional::of)
						   .<Stream<List<Integer>>>unwrap()
						   .map(i->i.size())
						   .peek(System.out::println)
						   .collect(Collectors.toList());


## Coerce to ValueObject

    int result = As.asValue(new Child(10,20))._match(c-> c.isType( (Child child) -> child.val).with(10,20))
	
Result is 10

## Coerce to StreamableValue


StreamableValue allows Pattern Matching and For Comprehensions on implementing classes.

	@Value
	static class BaseData{
		double salary;
		double pension;
		double socialClub;
	}

    Stream<Double> withBonus = As.<Double>asStreamableValue(new BaseData(10.00,5.00,100.30))
									.doWithThisAnd(d->As.<Double>asStreamableValue(new Bonus(2.0)))
									.yield((Double base)->(Double bonus)-> base*(1.0+bonus));
									
					

## Coerce to Mappable

	@Value static class MyEntity { int num; String str;} //implies Constructor (int num, String str)
	
    Map<String,?> map = As.asMappable(new MyEntity(10,"hello")).toMap(); 
    
    
 map is ["num":10,"str":"hello"]
 

## Coerce to Supplier 

     static class Duck{
		
		public String quack(){
			return  "quack";
		}
	}

    String result = As.<String>asSupplier(new Duck(),"quack").get()

Result is "quack" 

## Coerce to Functor

Provide a common way to access Objects with a map method that accepts a single parameter that accepts one value and returns another. Uses invokeDynamic to call the map method, and dynamic proxies to coerce to appropriate Function type, if not JDK 8 function.

	Functor<Integer> functor = As.<Integer>asFunctor(Stream.of(1,2,3));
	Functor<Integer> times2 = functor.map( i->i*2);