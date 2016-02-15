package com.aol.cyclops.functions.collections.extensions;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.stream.HeadAndTail;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

public abstract class AbstractOrderDependentCollectionXTest extends AbstractCollectionXTest {
	
	
	@Test
	public void zip3(){
		List<Tuple3<Integer,Integer,Character>> list =
				of(1,2,3,4,5,6).zip3(of(100,200,300,400).stream(),of('a','b','c').stream())
												.toListX();
		
		System.out.println(list);
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,not(hasItem(400)));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		List<Character> three = list.stream().map(t -> t.v3).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));
		
		
	}
	@Test
	public void zip4(){
		List<Tuple4<Integer,Integer,Character,String>> list =
				of(1,2,3,4,5,6).zip4(of(100,200,300,400).stream(),of('a','b','c').stream(),of("hello","world").stream())
												.toListX();
		System.out.println(list);
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,not(hasItem(300)));
		assertThat(right,not(hasItem(400)));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		List<Character> three = list.stream().map(t -> t.v3).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));
	
		List<String> four = list.stream().map(t -> t.v4).collect(Collectors.toList());
		assertThat(Arrays.asList("hello","world"),hasItem(four.get(0)));
		
		
	}
	
	@Test
	public void testIntersperse() {
		
		assertThat(((Traversable<Integer>)of(1,2,3).intersperse(0)).toListX(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test
	public void patternTestPojo(){
		
		List<String> result = of(new MyCase2(1,2),new MyCase2(3,4))
											  .patternMatch("n/a",
													  c->c.values(i->"one",1,2)
													      .has(i->"two",3,4)
													      .has(i->"three",5,6)
													  )
											  .toListX();
		assertThat(result,equalTo(Arrays.asList("one","two")));
	}
	@AllArgsConstructor
	@EqualsAndHashCode
	static class MyCase implements Decomposable{
		int first;
		int second;
	}
	@AllArgsConstructor
	@EqualsAndHashCode
	static class MyCase2 implements Decomposable{
		int first;
		int second;
	}
	
	@Test
	public void testOfType() {

		

		assertThat((((Traversable<Serializable>)of(1, "a", 2, "b", 3).ofType(Integer.class))).toListX(),containsInAnyOrder(1, 2, 3));

		assertThat((((Traversable<Serializable>)of(1, "a", 2, "b", 3).ofType(Integer.class))).toListX(),not(containsInAnyOrder("a", "b",null)));

		assertThat(((Traversable<Serializable>)of(1, "a", 2, "b", 3)

				.ofType(Serializable.class)).toListX(),containsInAnyOrder(1, "a", 2, "b", 3));

	}
	@Test
	public void patternTestDecomposable(){
		List<String> result = of(new MyCase(1,2),new MyCase(3,4))
											
											  .patternMatch("n/a",
													  c->c.values(i->"one",1,2)
													      .has(i->"two",3,4)
													      .has(i->"three",1,4)
													      .has(i->"four",2,3)
													  
													  
													  )
											  .toListX();
		assertThat(result,equalTo(Arrays.asList("one","two")));
	}
	
	private int addOne(Integer i){
		return i+1;
	}
	private int add(Integer a, Integer b){
		return a+b;
	}
	private String concat(String a, String b, String c){
		return a+b+c;
	}
	private String concat4(String a, String b, String c,String d){
		return a+b+c+d;
	}
	private String concat5(String a, String b, String c,String d,String e){
		return a+b+c+d+e;
	}
	
	@Test
	public void zap1(){
		assertThat(of(1,2,3).ap1(this::addOne)
				  .toListX(),equalTo(Arrays.asList(2,3,4)));
		
	}
	@Test
	public void zap2(){
		assertThat(of(1,2,3).ap2(this::add)
				  .ap(of(3,4,5))
				  .toListX(),equalTo(Arrays.asList(4,6,8)));
		
	}
	@Test
	public void zap3(){
		assertThat(of("a","b","c")
				  .ap3(this::concat)
				  .ap(of("1","2","3"))
				  .ap(of(".","?","!"))
				  .toListX(),equalTo(Arrays.asList("a1.","b2?","c3!")));
	}
	@Test
	public void zap4(){
		assertThat(of("a","b","c")
				  .ap4(this::concat4)
				  .ap(of("1","2","3"))
				  .ap(of(".","?","!"))
				  .ap(of("R","R","R"))
				  .toListX(),equalTo(Arrays.asList("a1.R","b2?R","c3!R")));
	}
	@Test
	public void zap5(){
		assertThat(of("a","b","c")
				  .ap5(this::concat5)
				  .ap(of("1","2","3"))
				  .ap(of(".","?","!"))
				  .ap(of("R","R","R"))
				  .ap(of("Z","Z","Z"))
				  .toListX(),equalTo(Arrays.asList("a1.RZ","b2?RZ","c3!RZ")));
	}
	@Test
	public void patternTest1(){
		List<String> result = of(1,2,3,4)
								         .patternMatch("",
													  	c->c.where(i->"even", (Integer i)->i%2==0 )
													  	    .hasWhere( i->"odd",(Integer i)->i%2!=0)
													  )
											  .toListX();
		assertThat(result,equalTo(Arrays.asList("odd","even","odd","even")));
	}
	@Test
	public void patternTest2(){
		List<String> result = of(1,2,3,4)
										.patternMatch("n/a",c->c.just(i->"one",1))
											 .toListX();
		assertThat(result,equalTo(Arrays.asList("one","n/a","n/a","n/a")));
	}
	@Test
    public void allCombinations3() {
        assertThat(of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

	@Test
	public void emptyAllCombinations() {
		assertThat(of().combinations().map(s -> s.toList()).toList(), equalTo(Arrays.asList(Arrays.asList())));
	}
	
	@Test
    public void emptyPermutations() {
        assertThat(of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(of(of(1, 2, 3),
        		of(1, 3, 2), of(2, 1, 3), of(2, 3, 1), of(3, 1, 2), of(3, 2, 1)).peek(i->System.out.println("peek - " + i)).map(s->s.toList()).toList()));
    }

	@Test
	public void emptyCombinations() {
		assertThat(of().combinations(2).map(s -> s.toList()).toList(), equalTo(Arrays.asList()));
	}
	   
	 @Test
	public void combinations2() {
	        assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
	                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
	    }	
	@Test
	public void whenGreaterThan2(){
		String res=	of(5,2,3).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2hello3"));
	}
	
	@Test
	public void headTailReplay() {

		CollectionX<String> helloWorld = of("hello", "world", "last");
		HeadAndTail<String> headAndTail = helloWorld.headAndTail();
		String head = headAndTail.head();
		assertThat(head, equalTo("hello"));

		ReactiveSeq<String> tail = headAndTail.tail();
		assertThat(tail.headAndTail().head(), equalTo("world"));

	} 
	@Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
    }
	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}
	@Test
	public void testReverse() {
		
		assertThat(of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
	}

	@Test
	public void testFoldRight() {
		Supplier<CollectionX<String>> s = () -> of("a", "b", "c");

		assertTrue(s.get().foldRight("", String::concat).contains("a"));
		assertTrue(s.get().foldRight("", String::concat).contains("b"));
		assertTrue(s.get().foldRight("", String::concat).contains("c"));
		assertEquals(3, (int) s.get().map(str -> str.length()).foldRight(0, (t, u) -> u + t));
	}

	@Test
	public void testFoldLeft() {
		for (int i = 0; i < 100; i++) {
			Supplier<CollectionX<String>> s = () -> of("a", "b", "c");

			assertTrue(s.get().reduce("", String::concat).contains("a"));
			assertTrue(s.get().reduce("", String::concat).contains("b"));
			assertTrue(s.get().reduce("", String::concat).contains("c"));

			assertEquals(3, (int) s.get().map(str -> str.length()).reduce(0, (u, t) -> u + t));

			assertEquals(3, (int) s.get().map(str -> str.length()).foldRight(0, (t, u) -> u + t));
		}
	}

}
