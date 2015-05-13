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
	
### Conversion to Streams

Tuples can also be converted to flattened or unflattened Stream of Streams. asStreams will attempt to create a Stream from each element (via Collection::stream for example). BufferedReaders, Files, URLs, Arrays, Collections, CharSequences will all be turned into Streams.
      