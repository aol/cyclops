## Power Tuples

1. Wrap any Tuple type / Object (mapping fields to elements and back)
2. Method call chaining support
3. Asyncrhonous method call chaining support
4. Inheritance relationship between Tuples
5. Lazy and Strict map methods
6. Lazy reordering
7. Pattern matching
8. For comprehensions
9. Useful utility methods (asStreamOfStrings, asTwoNumbers etc)
10. Concatonation
11. LazySwap (reverse)
12. Memoization
13. asCollector
14. asReducer

### Wrap any Tuple type

#### Convert from Tuple (or Object)

    @Test
	public void cons(){
		Three three = new Three(1,"hello",new Date());
		assertThat(three.a,equalTo(PTuple3.ofTuple(three).v1()));
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
	
    TwoParams p  = PowerTuples.tuple(10,"hello").convert().to(TwoParams.class);
	assertThat(p.num,equalTo(10));
	assertThat(p.value,equalTo("hello"));

### Method call chaining

With filtering

    method1().<PTuple1<Integer>>filter(t->t.v1()==0).call(this::method3);
    

Async method chaining

	method1().<PTuple1<Integer>>filter(t->t.v1()==0).callAsync(this::method3).join();
	
### Conversion to Streams

Tuples can also be converted to flattened or unflattened Stream of Streams. asStreams will attempt to create a Stream from each element (via Collection::stream for example). BufferedReaders, Files, URLs, Arrays, Collections, CharSequences will all be turned into Streams.

## asCollector

A tuple of Collectors can be coerced to a single Collector

e.g. Collecting as a List and Set simultaneously

       PTuple2<Set<Integer>,List<Integer>> res = Stream.of(1, 2, 2)
                       .collect(tuple(Collectors.toSet(),Collectors.toList()).asCollector());

See rich set of Collectors here [java.util.stream.Collectors](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html)

## asReducer

Convert a tuple into a single Monoid (or Reducer) that can perform multiple reduce operations on a single Stream.


       Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
	   Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
	   val result = tuple(sum,mult).<PTuple2<Integer,Integer>>asReducer()
											.mapReduce(Stream.of(1,2,3,4)); 



Or alternatively


      Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
	  Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
	  val result = tuple(sum,mult).<PTuple2<Integer,Integer>>asReducer()
											.mapReduce(Stream.of(1,2,3,4)); 
		 
		assertThat(result,equalTo(tuple(10,24)));
      