package com.aol.cyclops.functions.collections.extensions;

import cyclops.Semigroups;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.stream.HeadAndTail;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public  abstract class AbstractAnyMSeqOrderedDependentTest<W extends WitnessType<W>> extends AbstractAnyMSeqTest<W> {
    
    @Test
    public void groupedT(){
       
       assertThat(of(1,2,3,4).groupedT(2)
                    .toListOfLists(),equalTo(ListX.of(ListX.of(1,2),ListX.of(3,4))));
                    
    }
    @Test
    public void sortedComparator() {
        assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(Collectors.toList()),is(Arrays.asList(5,4,3,2,1)));
    }
    @Test
    public void testOnEmptyOrdered() throws X {
        assertEquals(asList(1), of().onEmpty(1).toListX());
        assertEquals(asList(1), of().onEmptyGet(() -> 1).toListX());

        assertEquals(asList(2), of(2).onEmpty(1).toListX());
        assertEquals(asList(2), of(2).onEmptyGet(() -> 1).toListX());
        assertEquals(asList(2), of(2).onEmptyThrow(() -> new X()).toListX());

        assertEquals(asList(2, 3), of(2, 3).onEmpty(1).toListX());
        assertEquals(asList(2, 3), of(2, 3).onEmptyGet(() -> 1).toListX());
        assertEquals(asList(2, 3), of(2, 3).onEmptyThrow(() -> new X()).toListX());
    }
    @SuppressWarnings("serial")
    public static class X extends Exception {
    }
    @Test
    public void testCycle() {
        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle(3).toListX());
        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).cycle(2).toListX());
    }
    @Test
    public void testCycleTimes() {
        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle(3).toListX());
       
    }
    int count =0;
    @Test
    public void testCycleWhile() {
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleWhile(next->count++<6).toListX());
       
    }
    @Test
    public void testCycleUntil() {
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toListX());
       
    }
    @Test
    public void sliding() {
        
       
        
        List<List<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());

      
        
        assertThat(list.get(0), hasItems(1, 2));
        assertThat(list.get(1), hasItems(2, 3));
    }

    @Test
    public void slidingIncrement() {
        List<List<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

        System.out.println(list);
        assertThat(list.get(0), hasItems(1, 2, 3));
        assertThat(list.get(1), hasItems(3, 4, 5));
    }
    @Test
    public void slidingT() {
        
       
        
        ArrayList<List<Integer>> list = of(1, 2, 3, 4, 5, 6).slidingT(2).collect(()->new ArrayList(),Collectors.toList());

       
        
        assertThat(list.get(0), hasItems(1, 2));
        assertThat(list.get(1), hasItems(2, 3));
    }

    @Test
    public void slidingIncrementT() {
        ArrayList<List<Integer>> list = of(1, 2, 3, 4, 5, 6).slidingT(3, 2).collect(()->new ArrayList(),Collectors.toList());

        System.out.println(list);
        assertThat(list.get(0), hasItems(1, 2, 3));
        assertThat(list.get(1), hasItems(3, 4, 5));
    }

    @Test
    public void combine(){
        assertThat(of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX(),equalTo(ListX.of(4,3))); 
                   
    }
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
		
		assertThat((of(1,2,3).intersperse(0)).toListX(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}


	@AllArgsConstructor
	@EqualsAndHashCode
	static class MyCase{
		int first;
		int second;
	}
	@AllArgsConstructor
	@EqualsAndHashCode
	static class MyCase2{
		int first;
		int second;
	}
	
	@Test
	public void testOfType() {

		

		assertThat(((of(1, "a", 2, "b", 3).ofType(Integer.class))).toListX(),containsInAnyOrder(1, 2, 3));

		assertThat(((of(1, "a", 2, "b", 3).ofType(Integer.class))).toListX(),not(containsInAnyOrder("a", "b",null)));

		assertThat((of(1, "a", 2, "b", 3)

				.ofType(Serializable.class)).toListX(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test
    public void groupedFunction(){
        assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b").count(),equalTo((2L)));
        assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b").filter(t->t.v1.equals("a"))
                        .map(t->t.v2).map(s->s.toList()).single(),
                            equalTo((Arrays.asList(1,2))));
    }
	@Test
    public void groupedFunctionCollector(){
        assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b",Collectors.toList()).count(),equalTo((2L)));
        assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b",Collectors.toList()).filter(t->t.v1.equals("a"))
                .map(t->t.v2).single(),
                    equalTo((Arrays.asList(1,2))));
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
    public void whenGreaterThan2() {
        String res = of(5, 2, 3).visit((x, xs) -> xs.join(x > 2 ? "hello" : "world"), () -> "boo!");

        assertThat(res, equalTo("2hello3"));
    }

	@Test
	public void headTailReplay() {

		AnyMSeq<W,String> helloWorld = of("hello", "world", "last");
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
		Supplier<AnyMSeq<W,String>> s = () -> of("a", "b", "c");

		assertTrue(s.get().foldRight("", String::concat).contains("a"));
		assertTrue(s.get().foldRight("", String::concat).contains("b"));
		assertTrue(s.get().foldRight("", String::concat).contains("c"));
		assertEquals(3, (int) s.get().map(str -> str.length()).foldRight(0, (t, u) -> u + t));
	}

	@Test
	public void testFoldLeft() {
		for (int i = 0; i < 100; i++) {
			Supplier<AnyMSeq<W,String>> s = () -> of("a", "b", "c");

			assertTrue(s.get().reduce("", String::concat).contains("a"));
			assertTrue(s.get().reduce("", String::concat).contains("b"));
			assertTrue(s.get().reduce("", String::concat).contains("c"));

			assertEquals(3, (int) s.get().map(str -> str.length()).reduce(0, (u, t) -> u + t));

			assertEquals(3, (int) s.get().map(str -> str.length()).foldRight(0, (t, u) -> u + t));
		}
	}

}
