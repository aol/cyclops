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

* asDecomposable
* asFunctor
* asGenericMonad
* asMappable
* asMatchable
* asStreamable
* asSupplier
* asValue
