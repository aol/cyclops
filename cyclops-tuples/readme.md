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
      