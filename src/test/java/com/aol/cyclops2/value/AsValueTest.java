package com.aol.cyclops2.value;

public class AsValueTest {
/**
	@Test
	public void testAsValueUnapply() {
		List list = new ArrayList();
		AsValue.asValue(new Child(10,20)).unapply().forEach(i->list.add(i));
		assertThat(list,equalTo(Arrays.asList(10,20)));
	}
	@Test
	public void testAsValueMatch() {
		List list = new ArrayList();
		
		assertThat(AsValue.asValue(new Child(10,20)).matches(c-> 
			c.isType((Child child) -> child.val ).anyValues())
		,equalTo(10));
	}
	@Test
	public void testAsValue_Match() {
		List list = new ArrayList();
		
		
		
		assertThat(AsValue.asValue(new Child(10,20)).matches(c-> 
			c.isType( (Child child) -> child.val).hasValues(10,20))
		,equalTo(10));
	}
	@Test
	public void testAsValue_MatchDefault() {
		List list = new ArrayList();
		
		assertThat(AsValue.asValue(new Child(10,20)).mayMatch(c-> 
			c.isType( (Child child) -> child.val).hasValues(20,20)).orElse(50)
		,equalTo(50));
	}

	@AllArgsConstructor(access=AccessLevel.PACKAGE)
	static class Parent{
		int val;
	}
	@Value
	static class Child extends Parent{
		int nextVal;
		public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
	}
<<<<<<< HEAD
**/

}
